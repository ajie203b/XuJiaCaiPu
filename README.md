# 徐家菜谱 Android 应用

一个基于 Kotlin + Jetpack Compose 开发的菜谱应用，帮助你查找和学习各种美食的制作方法。

## 功能特点

- 浏览 357 道菜品的详细做法
- 按分类筛选（素菜、荤菜、早餐、汤、其他）
- 支持按菜名或食材搜索
- 显示菜品难度星级
- 查看完整的原料、计算、操作步骤和附加内容
- 收藏喜欢的菜品

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **架构**: MVVM
- **数据库**: Room（收藏功能）
- **图片加载**: Coil
- **导航**: Navigation Compose
- **JSON 解析**: Gson

## 项目结构

```
app/src/main/
├── assets/data/          # JSON 数据文件
│   ├── dishes.json       # 菜品数据
│   └── tips.json         # 烹饪技巧数据
├── java/com/xujia/cookbook/
│   ├── data/             # 数据层（模型、仓库、数据库）
│   ├── ui/               # UI 层
│   │   ├── components/   # 可复用组件
│   │   ├── screens/      # 页面
│   │   └── theme/        # 主题配置
│   └── MainActivity.kt
└── res/                  # 资源文件
```

## 构建运行

### 环境要求

- Android Studio Hedgehog 或更高版本
- JDK 17+
- Android SDK 34

### 步骤

1. 克隆仓库
   ```bash
   git clone https://github.com/ajie203b/HowToCook.git
   cd HowToCook
   ```

2. 用 Android Studio 打开项目

3. 同步 Gradle 并运行

## 数据来源

菜品数据基于 [HowToCook](https://github.com/Anduin2017/HowToCook) 开源项目。

## 许可证

MIT License
