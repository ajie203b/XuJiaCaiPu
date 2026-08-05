import json
import os
import time
import requests
from pathlib import Path
from bs4 import BeautifulSoup
import re

BASE_DIR = Path(r"D:\AAA-resource\app\HowToCook-1.6.0\android_project")
ASSETS_DIR = BASE_DIR / "app" / "src" / "main" / "assets"
IMAGES_DIR = ASSETS_DIR / "images"
DISHES_JSON = ASSETS_DIR / "data" / "dishes.json"

with open(DISHES_JSON, "r", encoding="utf-8") as f:
    data = json.load(f)
dishes = data["dishes"]

print(f"Total dishes: {len(dishes)}")

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}

def download_image(url, save_path, timeout=15):
    """下载图片到指定路径"""
    try:
        resp = requests.get(url, headers=HEADERS, timeout=timeout, stream=True)
        resp.raise_for_status()
        content = resp.content
        if len(content) < 1000:
            return False
        with open(save_path, "wb") as f:
            f.write(content)
        return True
    except Exception as e:
        return False

def search_dish_image(dish_name):
    """使用 Bing 搜索菜品图片"""
    query = f"{dish_name} 菜 美食"
    url = f"https://www.bing.com/images/search?q={query}&first=1&count=5"
    try:
        resp = requests.get(url, headers=HEADERS, timeout=15)
        resp.raise_for_status()
        soup = BeautifulSoup(resp.text, 'html.parser')
        # 找图片链接
        imgs = soup.find_all('img', class_='mimg')
        for img in imgs:
            src = img.get('src', '')
            if src and src.startswith('http') and 'mm.bing.net' in src:
                # 尝试获取更大尺寸的图
                src = re.sub(r'w=\d+', 'w=400', src)
                src = re.sub(r'h=\d+', 'h=400', src)
                return src
    except Exception as e:
        pass
    return None

success = 0
failed = 0
skipped = 0

for i, dish in enumerate(dishes):
    name = dish["name"]
    category = dish["category"]

    # 跳过已有图片的
    if dish.get("images") and len(dish["images"]) > 0:
        skipped += 1
        continue

    print(f"[{i+1}/{len(dishes)}] {name}...", end=" ", flush=True)
    image_url = search_dish_image(name)

    if not image_url:
        print("no image found")
        failed += 1
        continue

    # 创建保存路径
    cat_dir = IMAGES_DIR / category / name
    cat_dir.mkdir(parents=True, exist_ok=True)
    save_path = cat_dir / "main.jpg"

    if download_image(image_url, save_path):
        rel_path = f"images/{category}/{name}/main.jpg"
        dish["images"] = [rel_path]
        dish["main_image"] = rel_path
        print("OK")
        success += 1
    else:
        print("download failed")
        failed += 1

    # 每处理10个保存一次
    if (i + 1) % 10 == 0:
        with open(DISHES_JSON, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        print(f"  --- saved (ok:{success}, fail:{failed}, skip:{skipped}) ---")

    # 控制请求频率
    time.sleep(1.0)

# 最终保存
with open(DISHES_JSON, "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"\n{'='*50}")
print(f"Done! ok:{success}, fail:{failed}, skip:{skipped}")
print(f"{'='*50}")
