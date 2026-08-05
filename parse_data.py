import os, json, shutil
from pathlib import Path

BASE_DIR = Path(r"D:/AAA-resource/app/HowToCook-1.6.0")
DISHES_DIR = BASE_DIR / "dishes"
TIPS_DIR = BASE_DIR / "tips"
OUTPUT_DIR = BASE_DIR / "android_project/app/src/main/assets"
DATA_OUTPUT = OUTPUT_DIR / "data"

CATEGORY_MAP = {
    "vegetable_dish": "素菜", "meat_dish": "荤菜",
    "breakfast": "早餐", "soup": "汤",
    "aquatic": "其他", "staple": "其他",
    "dessert": "其他", "drink": "其他",
    "condiment": "其他", "semi-finished": "其他",
}
VALID_CATEGORIES = ["素菜", "荤菜", "早餐", "汤", "其他"]


def parse_difficulty(text):
    key = "预估烹饪难度"
    idx = text.find(key)
    if idx == -1: return 0
    star_start = idx + len(key) + 1
    count = 0
    i = star_start
    while i < len(text) and text[i] == "★":
        count += 1
        i += 1
    return count

def parse_section(text, section_name):
    marker = "## " + section_name
    start = text.find(marker)
    if start == -1: return ""
    start = text.find(chr(10), start) + 1
    end = text.find(chr(10) + "## ", start)
    if end == -1: end = len(text)
    return text[start:end].strip()

def find_images(dd, dn):
    imgs = []
    for ext in [".jpg",".jpeg",".png",".webp"]:
        f = dd / (dn + ext)
        if f.exists() and f not in imgs: imgs.append(f)
    sd = dd / dn
    if sd.exists() and sd.is_dir():
        for ext in [".jpg",".jpeg",".png",".webp"]:
            for f in sd.glob("**/*" + ext):
                if f not in imgs: imgs.append(f)
    for ext in [".jpg",".jpeg",".png",".webp"]:
        for f in dd.glob("[0-9]*" + ext):
            if f not in imgs and f.stem.isdigit(): imgs.append(f)
    for ext in [".jpg",".jpeg",".png",".webp"]:
        for nm in ["成品", "摆盘", "改刀"]:
            f = dd / (nm + ext)
            if f.exists() and f not in imgs: imgs.append(f)
    return sorted(list(set(imgs)))


def parse_dish(fp, cat):
    try:
        with open(fp, "r", encoding="utf-8") as fh: content = fh.read()
    except: return None
    tm = content.find("# ")
    if tm == -1: name = fp.stem
    else:
        end_line = content.find(chr(10), tm)
        name = content[tm+2:end_line].strip()
        if name.endswith("的做法"): name = name[:-3]
    diff = parse_difficulty(content)
    ing = parse_section(content, "必备原料和工具")
    calc = parse_section(content, "计算")
    ops = parse_section(content, "操作")
    add = parse_section(content, "附加内容")
    dd = fp.parent
    imgs = find_images(dd, name)
    ip = ["images/" + cat + "/" + name + "/" + i.name for i in imgs]
    il = []
    if ing:
        for line in ing.split(chr(10)):
            line = line.strip().lstrip("*- ").strip()
            if line and not line.startswith("使用") and len(line) > 1:
                il.append(line)
    return {"id": cat + "_" + name, "name": name, "category": cat,
        "difficulty": diff, "ingredients_raw": ing,
        "ingredients_list": il[:15], "calculation": calc,
        "operations": ops, "additional": add,
        "images": ip, "main_image": ip[0] if ip else ""}


def parse_tips():
    tips = []
    ld = TIPS_DIR / "learn"
    if ld.exists():
        for mf in sorted(ld.glob("*.md")):
            try:
                with open(mf, "r", encoding="utf-8") as fh: c = fh.read()
                tm = c.find(chr(10))
                t = c[1:tm].strip() if c.startswith("# ") else mf.stem
                tips.append({"id": "tip_" + mf.stem, "title": t, "file": mf.name, "content": c, "category": "技巧"})
            except: pass
    for md in ["厨房准备.md", "食材相克与禁忌.md"]:
        fp = TIPS_DIR / md
        if fp.exists():
            try:
                with open(fp, "r", encoding="utf-8") as fh: c = fh.read()
                tm = c.find(chr(10))
                t = c[1:tm].strip() if c.startswith("# ") else fp.stem
                tips.append({"id": "tip_" + fp.stem, "title": t, "file": fp.name, "content": c, "category": "常识"})
            except: pass
    return tips

def copy_imgs(data):
    copied = 0
    for d in data:
        dn = d["name"]
        for ir in d["images"]:
            img = ir.split("/")[-1]
            dp = OUTPUT_DIR / ir
            dp.parent.mkdir(parents=True, exist_ok=True)
            found = False
            for sd in DISHES_DIR.iterdir():
                if not sd.is_dir(): continue
                c = sd / img
                if c.exists(): shutil.copy2(c, dp); copied += 1; found = True; break
                c = sd / dn / img
                if c.exists(): shutil.copy2(c, dp); copied += 1; found = True; break
                for c in sd.glob("**/{}".format(img)):
                    shutil.copy2(c, dp); copied += 1; found = True; break
                if found: break
    return copied


def main():
    print("=" * 50)
    print("HowToCook Data Preprocessor")
    print("=" * 50)
    DATA_OUTPUT.mkdir(parents=True, exist_ok=True)
    ad = []
    cc = {}
    for dn, dsn in CATEGORY_MAP.items():
        dp = DISHES_DIR / dn
        if not dp.exists(): continue
        ct = 0
        for mf in sorted(dp.glob("*.md")):
            dish = parse_dish(mf, dsn)
            if dish: ad.append(dish); ct += 1
        for sd in sorted(dp.iterdir()):
            if sd.is_dir():
                for mf in sorted(sd.glob("*.md")):
                    dish = parse_dish(mf, dsn)
                    if dish: ad.append(dish); ct += 1
        cc[dsn] = cc.get(dsn, 0) + ct
        if ct > 0: print(f"  [{dsn}] {dn}: {ct}")
    print(f"Total: {len(ad)} dishes")
    tips = parse_tips()
    print(f"Tips: {len(tips)} articles")
    print("Copying images...")
    ic = copy_imgs(ad)
    print(f"Copied {ic} images")
    print("Saving JSON...")
    dj = {"version": "1.0", "total": len(ad), "categories": VALID_CATEGORIES, "dishes": ad}
    with open(DATA_OUTPUT / "dishes.json", "w", encoding="utf-8") as fh: json.dump(dj, fh, ensure_ascii=False, indent=2)
    tj = {"version": "1.0", "total": len(tips), "tips": tips}
    with open(DATA_OUTPUT / "tips.json", "w", encoding="utf-8") as fh: json.dump(tj, fh, ensure_ascii=False, indent=2)
    for cat in VALID_CATEGORIES:
        cd = [d for d in ad if d["category"] == cat]
        cl = [{"id": d["id"], "name": d["name"], "difficulty": d["difficulty"], "main_image": d["main_image"], "ingredients_list": d["ingredients_list"][:5]} for d in cd]
        with open(DATA_OUTPUT / f"category_{cat}.json", "w", encoding="utf-8") as fh: json.dump(cl, fh, ensure_ascii=False, indent=2)
        print(f"  category_{cat}.json ({len(cl)} items)")
    print("DONE!")

if __name__ == "__main__":
    main()
