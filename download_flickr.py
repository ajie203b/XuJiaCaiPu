import json
import os
import time
import requests
import re
import urllib.parse
from pathlib import Path

BASE_DIR = Path(r"D:\AAA-resource\app\HowToCook-1.6.0\android_project")
ASSETS_DIR = BASE_DIR / "app" / "src" / "main" / "assets"
IMAGES_DIR = ASSETS_DIR / "images"
DISHES_JSON = ASSETS_DIR / "data" / "dishes.json"

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
}

with open(DISHES_JSON, 'r', encoding='utf-8') as f:
    data = json.load(f)
dishes = data['dishes']

def translate_to_en(text):
    """Google 翻译"""
    url = "https://translate.googleapis.com/translate_a/single"
    params = {"client": "gtx", "sl": "zh-CN", "tl": "en", "dt": "t", "q": text}
    try:
        resp = requests.get(url, params=params, timeout=10)
        data = resp.json()
        return data[0][0][0]
    except:
        return None

def flickr_search(query):
    """Flickr 图片搜索"""
    url = f"https://www.flickr.com/search/?text={urllib.parse.quote(query)}"
    try:
        resp = requests.get(url, headers=HEADERS, timeout=15)
        imgs = re.findall(r'(https?://live\.staticflickr\.com/[^"\s]+)', resp.text)
        return imgs[:5]
    except:
        return []

def mealdb_search(query):
    """TheMealDB 搜索"""
    url = f"https://www.themealdb.com/api/json/v1/1/search.php?s={urllib.parse.quote(query)}"
    try:
        resp = requests.get(url, timeout=10)
        data = resp.json()
        meals = data.get('meals', [])
        if meals:
            return meals[0]['strMealThumb']
    except:
        pass
    return None

def download_image(url, save_path, timeout=15):
    try:
        resp = requests.get(url, headers=HEADERS, timeout=timeout, stream=True)
        resp.raise_for_status()
        content = resp.content
        if len(content) < 2000:
            return False
        with open(save_path, 'wb') as f:
            f.write(content)
        return True
    except:
        return False

# 找出没有图片的菜品
no_image_dishes = []
for dish in dishes:
    if not dish.get('main_image'):
        no_image_dishes.append(dish)

print(f"需要下载图片的菜品: {len(no_image_dishes)}")

success = 0
failed = 0
translations = {}

for i, dish in enumerate(no_image_dishes):
    name = dish['name']
    category = dish['category']

    print(f"[{i+1}/{len(no_image_dishes)}] {name}...", end=" ", flush=True)

    # 翻译
    en_name = translate_to_en(name)
    if not en_name:
        print("translate failed")
        failed += 1
        continue

    translations[name] = en_name
    search_term = en_name + " food dish"

    # 先试 TheMealDB
    img_url = mealdb_search(en_name)

    # 如果 TheMealDB 没有，用 Flickr
    if not img_url:
        flickr_imgs = flickr_search(search_term)
        if flickr_imgs:
            img_url = flickr_imgs[0]

    if not img_url:
        print(f"no image (en: {en_name})")
        failed += 1
        continue

    # 下载图片
    cat_dir = IMAGES_DIR / category / name
    cat_dir.mkdir(parents=True, exist_ok=True)
    save_path = cat_dir / "main.jpg"

    if download_image(img_url, save_path):
        rel_path = f"images/{category}/{name}/main.jpg"
        dish['images'] = [rel_path]
        dish['main_image'] = rel_path
        print(f"OK (en: {en_name})")
        success += 1
    else:
        # 尝试 Flickr 其他结果
        downloaded = False
        if not img_url.startswith('https://www.themealdb'):
            for flick_url in flickr_imgs[1:]:
                if download_image(flick_url, save_path):
                    rel_path = f"images/{category}/{name}/main.jpg"
                    dish['images'] = [rel_path]
                    dish['main_image'] = rel_path
                    downloaded = True
                    success += 1
                    print(f"OK (flickr)")
                    break
        if not downloaded:
            print(f"download failed (en: {en_name})")
            failed += 1

    if (i + 1) % 10 == 0:
        with open(DISHES_JSON, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        print(f"  --- saved (ok:{success}, fail:{failed}) ---")

    time.sleep(0.5)

# 最终保存
with open(DISHES_JSON, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

# 保存翻译映射
with open(BASE_DIR / 'translations.json', 'w', encoding='utf-8') as f:
    json.dump(translations, f, ensure_ascii=False, indent=2)

print(f"\nDone! ok:{success}, fail:{failed}")
