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

TARGET_RATIO = 4 / 3  # 目标比例 4:3 = 1.333

with open(DISHES_JSON, 'r', encoding='utf-8') as f:
    data = json.load(f)
dishes = data['dishes']

# 先清除所有旧图片
print("清除旧图片...")
if IMAGES_DIR.exists():
    shutil.rmtree(IMAGES_DIR)
IMAGES_DIR.mkdir(parents=True, exist_ok=True)

for dish in dishes:
    dish['images'] = []
    dish['main_image'] = ''

print(f"开始下载 {len(dishes)} 个菜品的图片...")

def baidu_img_search(query):
    """百度图片搜索 - 返回带尺寸的图片列表"""
    url = f"https://image.baidu.com/search/acjson?tn=resultjson_com&logid=11576251023323905949&ipn=rj&ct=201326592&is=&fp=result&queryWord={urllib.parse.quote(query)}&cl=2&lm=-1&ie=utf-8&oe=utf-8&word={urllib.parse.quote(query)}&z=&ic=&hd=&latest=&copyright=&s=&se=&tab=&width=&height=&face=&istype=&qc=&nc=&fr=&expermode=&force=&pn=0&rn=15"
    try:
        resp = requests.get(url, headers=HEADERS, timeout=15)
        data = resp.json()
        results = []
        for item in data.get('data', []):
            if item.get('thumbURL'):
                w = item.get('width', 0)
                h = item.get('height', 0)
                if w > 100 and h > 100:  # 过滤太小的图
                    results.append({
                        'url': item['thumbURL'],
                        'width': w,
                        'height': h,
                        'ratio': w / h if h > 0 else 1
                    })
        return results
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

def select_best_image(results, target_ratio=TARGET_RATIO):
    """选择最接近目标比例的图片"""
    if not results:
        return None
    # 按与目标比例的差距排序
    results.sort(key=lambda x: abs(x['ratio'] - target_ratio))
    return results[0]

success = 0
failed = 0

for i, dish in enumerate(dishes):
    name = dish['name']
    category = dish['category']

    print(f"[{i+1}/{len(dishes)}] {name}...", end=" ", flush=True)

    results = baidu_img_search(name)
    best = select_best_image(results)

    if not best:
        print("no image")
        failed += 1
        time.sleep(0.3)
        continue

    cat_dir = IMAGES_DIR / category / name
    cat_dir.mkdir(parents=True, exist_ok=True)
    save_path = cat_dir / "main.jpg"

    if download_image(best['url'], save_path):
        rel_path = f"images/{category}/{name}/main.jpg"
        dish['images'] = [rel_path]
        dish['main_image'] = rel_path
        print(f"OK ({best['width']}x{best['height']}, ratio={best['ratio']:.2f})")
        success += 1
    else:
        # 尝试其他结果
        downloaded = False
        for r in results[1:3]:
            if download_image(r['url'], save_path):
                rel_path = f"images/{category}/{name}/main.jpg"
                dish['images'] = [rel_path]
                dish['main_image'] = rel_path
                downloaded = True
                print(f"OK ({r['width']}x{r['height']})")
                success += 1
                break
        if not downloaded:
            print("download failed")
            failed += 1

    if (i + 1) % 20 == 0:
        with open(DISHES_JSON, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        print(f"  --- saved (ok:{success}, fail:{failed}) ---")

    time.sleep(0.5)

with open(DISHES_JSON, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"\nDone! ok:{success}, fail:{failed}")
