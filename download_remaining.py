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
    "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
}

with open(DISHES_JSON, 'r', encoding='utf-8') as f:
    data = json.load(f)
dishes = data['dishes']

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

def bing_image_search(dish_name):
    """Bing 中文站图片搜索"""
    query = f"{dish_name} 菜 做法 美食"
    url = f"https://cn.bing.com/images/search?q={urllib.parse.quote(query)}&first=1&count=15"
    try:
        resp = requests.get(url, headers=HEADERS, timeout=15)
        # 从 src 属性提取 tse.mm.bing.net 图片
        urls = re.findall(r'src="(https://tse\d+-mm\.cn\.bing\.net/th/id/[^"]+)"', resp.text)
        # 解码 &amp; 为 &
        urls = [u.replace('&amp;', '&') for u in urls]
        # 去重
        seen = set()
        unique_urls = []
        for u in urls:
            if u not in seen:
                seen.add(u)
                unique_urls.append(u)
        return unique_urls[:8]
    except:
        return []

# 找出没有图片的 dishes
no_image_dishes = []
for dish in dishes:
    if not dish.get('main_image'):
        no_image_dishes.append(dish)

print(f"需要下载图片的菜品: {len(no_image_dishes)}")

success = 0
failed = 0

for i, dish in enumerate(no_image_dishes):
    name = dish['name']
    category = dish['category']

    print(f"[{i+1}/{len(no_image_dishes)}] {name}...", end=" ", flush=True)

    urls = bing_image_search(name)
    if not urls:
        print("no result")
        failed += 1
        continue

    downloaded = False
    for img_url in urls:
        cat_dir = IMAGES_DIR / category / name
        cat_dir.mkdir(parents=True, exist_ok=True)
        save_path = cat_dir / "main.jpg"
        if download_image(img_url, save_path):
            rel_path = f"images/{category}/{name}/main.jpg"
            dish['images'] = [rel_path]
            dish['main_image'] = rel_path
            print("OK")
            success += 1
            downloaded = True
            break

    if not downloaded:
        print("download failed")
        failed += 1

    if (i + 1) % 20 == 0:
        with open(DISHES_JSON, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        print(f"  --- saved (ok:{success}, fail:{failed}) ---")

    time.sleep(0.8)

with open(DISHES_JSON, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"\nDone! ok:{success}, fail:{failed}")
