import json
import os
import time
import requests
import re
import urllib.parse
import shutil
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

# 先清除所有旧图片
print("清除旧图片...")
if IMAGES_DIR.exists():
    shutil.rmtree(IMAGES_DIR)
IMAGES_DIR.mkdir(parents=True, exist_ok=True)

# 重置所有菜品的图片信息
for dish in dishes:
    dish['images'] = []
    dish['main_image'] = ''

print(f"开始下载 {len(dishes)} 个菜品的图片（百度图片）...")

def baidu_img_search(query):
    """百度图片搜索"""
    url = f"https://image.baidu.com/search/acjson?tn=resultjson_com&logid=11576251023323905949&ipn=rj&ct=201326592&is=&fp=result&queryWord={urllib.parse.quote(query)}&cl=2&lm=-1&ie=utf-8&oe=utf-8&word={urllib.parse.quote(query)}&z=&ic=&hd=&latest=&copyright=&s=&se=&tab=&width=&height=&face=&istype=&qc=&nc=&fr=&expermode=&force=&pn=0&rn=5"
    try:
        resp = requests.get(url, headers=HEADERS, timeout=15)
        data = resp.json()
        imgs = []
        for item in data.get('data', []):
            if item.get('thumbURL'):
                imgs.append(item['thumbURL'])
            elif item.get('middleURL'):
                imgs.append(item['middleURL'])
        return imgs[:5]
    except:
        return []

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

success = 0
failed = 0

for i, dish in enumerate(dishes):
    name = dish['name']
    category = dish['category']

    print(f"[{i+1}/{len(dishes)}] {name}...", end=" ", flush=True)

    imgs = baidu_img_search(name)

    if not imgs:
        print("no image")
        failed += 1
        time.sleep(0.3)
        continue

    cat_dir = IMAGES_DIR / category / name
    cat_dir.mkdir(parents=True, exist_ok=True)
    save_path = cat_dir / "main.jpg"

    downloaded = False
    for img_url in imgs:
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

    time.sleep(0.5)

# 最终保存
with open(DISHES_JSON, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"\nDone! ok:{success}, fail:{failed}")
