import json
import os
import time
import random
import requests
import urllib.parse
import shutil
from pathlib import Path

BASE_DIR = Path(r"D:\AAA-resource\app\HowToCook-1.6.0\android_project")
ASSETS_DIR = BASE_DIR / "app" / "src" / "main" / "assets"
IMAGES_DIR = ASSETS_DIR / "images"
DISHES_JSON = ASSETS_DIR / "data" / "dishes.json"

# 更完整的请求头，模拟真实浏览器
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
    "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
    "Accept-Encoding": "gzip, deflate, br",
    "Referer": "https://image.baidu.com/",
    "Connection": "keep-alive",
}

TARGET_RATIO = 1.0  # 目标比例 1:1

with open(DISHES_JSON, 'r', encoding='utf-8') as f:
    data = json.load(f)
dishes = data['dishes']

# 清除旧图片
print("清除旧图片...")
if IMAGES_DIR.exists():
    shutil.rmtree(IMAGES_DIR)
IMAGES_DIR.mkdir(parents=True, exist_ok=True)

for dish in dishes:
    dish['images'] = []
    dish['main_image'] = ''

print(f"开始下载 {len(dishes)} 个菜品的 1:1 正方形图片...")


def baidu_img_search(query):
    """百度图片搜索 - 返回带尺寸的图片列表"""
    # 使用更精确的搜索关键词
    search_term = f"{query} 美食 菜品"
    encoded = urllib.parse.quote(search_term)
    url = (
        f"https://image.baidu.com/search/acjson?"
        f"tn=resultjson_com&logid=11576251023323905949&ipn=rj&ct=201326592"
        f"&is=&fp=result&queryWord={encoded}&cl=2&lm=-1&ie=utf-8&oe=utf-8"
        f"&word={encoded}&z=0&ic=0&hd=0&latest=0&copyright=0&s=0&se=0&tab=0"
        f"&width=0&height=0&face=0&istype=2&qc=0&nc=1&fr=&expermode=0&force=0"
        f"&pn=0&rn=30"
    )
    try:
        resp = requests.get(url, headers=HEADERS, timeout=20)
        resp.raise_for_status()
        result_data = resp.json()
        results = []
        for item in result_data.get('data', []):
            thumb_url = item.get('thumbURL', '')
            if thumb_url and thumb_url.startswith('http'):
                w = item.get('width', 0)
                h = item.get('height', 0)
                if w > 200 and h > 200:
                    results.append({
                        'url': thumb_url,
                        'width': w,
                        'height': h,
                        'ratio': w / h if h > 0 else 1
                    })
        return results
    except Exception as e:
        return []


def download_image(url, save_path, timeout=20):
    """下载图片"""
    try:
        resp = requests.get(url, headers=HEADERS, timeout=timeout, stream=True)
        resp.raise_for_status()
        content = resp.content
        if len(content) < 3000:  # 过滤太小的图
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
    results.sort(key=lambda x: abs(x['ratio'] - target_ratio))
    return results[0]


success = 0
failed = 0
no_image_list = []

for i, dish in enumerate(dishes):
    name = dish['name']
    category = dish['category']

    print(f"[{i+1}/{len(dishes)}] {name}...", end=" ", flush=True)

    results = baidu_img_search(name)
    best = select_best_image(results)

    if not best:
        print("no image")
        failed += 1
        no_image_list.append(name)
        time.sleep(random.uniform(0.5, 1.0))
        continue

    cat_dir = IMAGES_DIR / category / name
    cat_dir.mkdir(parents=True, exist_ok=True)
    save_path = cat_dir / "main.jpg"

    if download_image(best['url'], save_path):
        rel_path = f"images/{category}/{name}/main.jpg"
        dish['images'] = [rel_path]
        dish['main_image'] = rel_path
        print(f"OK ({best['width']}x{best['height']}, r={best['ratio']:.2f})")
        success += 1
    else:
        # 尝试其他结果
        downloaded = False
        for r in results[1:5]:
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
            no_image_list.append(name)

    if (i + 1) % 20 == 0:
        with open(DISHES_JSON, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        print(f"  --- saved (ok:{success}, fail:{failed}) ---")

    time.sleep(random.uniform(0.5, 1.2))

# 最终保存
with open(DISHES_JSON, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"\n===== Done! ok:{success}, fail:{failed} =====")
if no_image_list:
    print(f"\n未下载到图片的菜品 ({len(no_image_list)} 个):")
    for name in no_image_list[:20]:
        print(f"  - {name}")
    if len(no_image_list) > 20:
        print(f"  ... 还有 {len(no_image_list) - 20} 个")
