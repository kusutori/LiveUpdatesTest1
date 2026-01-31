# Android 16 Live Updates 测试应用

这是一个用于测试 Android 16（API 36 / BAKLAVA）新引入的 **实时更新通知（Live Updates）** 功能的示例应用。

## 功能特性

本应用包含 10 种不同类型的实时通知示例：

| 通知类型 | 样式 | 特点 |
|---------|------|------|
| 📥 下载进度 | ProgressStyle | 简单进度条 0-100% |
| 🚗 外卖配送 | ProgressStyle | 4阶段分段进度（下单→准备→取餐→配送） |
| ✈️ 航班追踪 | ProgressStyle | 单一进度 + 起点/终点/追踪器图标 |
| 🚄 火车追踪 | ProgressStyle | 多站点分段 + 里程碑点 |
| 🎫 取餐码 | BigTextStyle | 无进度条 + 操作按钮 + 大图标 |
| ⏱️ 倒计时 | BigTextStyle + Chronometer | 自动倒计时显示 |
| 📞 通话 | CallStyle | 来电/通话中状态 + 接听/挂断按钮 |
| 🏃 运动健康 | ProgressStyle + Chronometer | 计时器 + 进度显示 |
| 🚕 打车行程 | ProgressStyle | 4阶段分段 + ETA显示 |
| ⚽ 比赛实况 | ProgressStyle | 上半场/中场/下半场 + 比分显示 |

## 环境要求

- **Android Studio**：Ladybug (2024.2.1) 或更高版本
- **Android SDK**：API 36 (Android 16 / BAKLAVA)
- **Kotlin**：2.0.0+
- **Gradle**：9.1.0+
- **测试设备**：运行 Android 16 的真机或模拟器

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd LiveUpdatesTest1
```

### 2. 打开项目

使用 Android Studio 打开项目文件夹。

### 3. 同步 Gradle

等待 Android Studio 自动同步 Gradle 依赖，或手动点击 "Sync Project with Gradle Files"。

### 4. 运行应用

连接运行 Android 16 的设备或模拟器，然后点击 Run 或执行：

```bash
./gradlew installDebug
```

### 5. 授予权限

首次运行时，点击「请求通知权限」按钮授予通知权限。

## 项目结构

```
app/src/main/
├── AndroidManifest.xml          # 权限声明
├── java/.../
│   ├── MainActivity.kt          # UI 界面（Jetpack Compose）
│   └── LiveUpdateNotifier.kt    # 通知发送逻辑
└── res/drawable/                # 图标资源
    ├── ic_flight.xml
    ├── ic_train.xml
    ├── ic_car.xml
    ├── ic_running.xml
    └── ...

docs/
├── live-updates.md              # API 文档和开发笔记
└── TODO-scenarios.md            # 场景分析和开发计划
```

## 关键权限

```xml
<!-- 通知权限（运行时请求） -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- 实时更新权限（无需运行时请求） -->
<uses-permission android:name="android.permission.POST_PROMOTED_NOTIFICATIONS" />

<!-- CallStyle 通知需要 -->
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
```

## 核心 API

### 请求实时更新提升

```kotlin
val extras = Bundle()
extras.putBoolean("android.requestPromotedOngoing", true)
builder.addExtras(extras)
```

### ProgressStyle 示例

```kotlin
val style = Notification.ProgressStyle()
    .setStyledByProgress(true)
    .setProgress(50)
    .setProgressTrackerIcon(Icon.createWithResource(context, R.drawable.ic_train))
    .addProgressSegment(Notification.ProgressStyle.Segment(100).setColor(Color.GREEN))
    .addProgressPoint(Notification.ProgressStyle.Point(50).setColor(Color.WHITE))
```

## 注意事项

1. **仅在 Android 16+ 有效**：实时更新功能是 Android 16 新增的，低版本会回退到普通通知。

2. **CallStyle 特殊要求**：必须使用 `fullScreenIntent`、前台服务或用户发起的任务。

3. **不支持自定义 RemoteViews**：实时通知只支持标准样式、BigTextStyle、CallStyle 和 ProgressStyle。

4. **用户可禁用**：用户可以在系统设置中禁用应用的实时更新功能。

## 参考链接

- [创建实时更新通知](https://developer.android.com/develop/ui/views/notifications/live-update)
- [以进度为中心的通知](https://developer.android.com/about/versions/16/features/progress-centric-notifications)
- [Notification.ProgressStyle](https://developer.android.com/reference/android/app/Notification.ProgressStyle)

## License

MIT License
