# Android 16 实时更新（Live Updates）开发笔记

## 需要的权限

1. 运行时通知权限（Android 13+）
   - `android.permission.POST_NOTIFICATIONS`
   - 需要在运行时请求。

2. 实时更新（推广通知）权限（非运行时）
   - `android.permission.POST_PROMOTED_NOTIFICATIONS`
   - 仅在清单中声明即可。

> 参考：官方文档 "创建实时更新通知（Live Update）"

## 实时更新（推广通知）的必要条件

要让通知具备实时更新/推广展示资格，通知必须满足：

- **样式**：必须是标准样式、`BigTextStyle`、`CallStyle` 或 `Notification.ProgressStyle`。
- **必须是 ongoing**：`setOngoing(true)`。
- **必须设置标题**：`setContentTitle(...)`。
- **不能使用自定义 RemoteViews**。
- **不能是 group summary**。
- **不能 colorized**：`setColorized(false)`。
- **渠道重要性**：通知渠道不能是 `IMPORTANCE_MIN`。
- **申请提升**：使用 extras `android.requestPromotedOngoing = true`。

## 关键 API

### 核心类
- `Notification.ProgressStyle`：Android 16 新增进度式样
- `Notification.ProgressStyle.Segment`：进度条分段
- `Notification.ProgressStyle.Point`：里程碑点

### 检查方法
- `NotificationManager.canPostPromotedNotifications()`：检查是否允许发布推广通知
- `Settings.ACTION_MANAGE_APP_PROMOTED_NOTIFICATIONS`：跳转设置页以开启/关闭实时更新

## ProgressStyle 详细 API

### 基础设置
```kotlin
Notification.ProgressStyle()
    .setProgress(progress)              // 当前进度值
    .setStyledByProgress(true)          // 是否根据进度自动着色
    .setProgressIndeterminate(false)    // 是否为不确定进度
```

### 图标设置
```kotlin
.setProgressStartIcon(Icon)      // 起点图标（左侧）
.setProgressEndIcon(Icon)        // 终点图标（右侧）
.setProgressTrackerIcon(Icon)    // 追踪器图标（在进度条上移动）
```

### 分段设置（Segment）
```kotlin
// 添加分段，用于表示不同阶段或状态
.addProgressSegment(Notification.ProgressStyle.Segment(length).setColor(color))

// 或批量设置
.setProgressSegments(listOf(
    Notification.ProgressStyle.Segment(100).setColor(Color.GREEN),
    Notification.ProgressStyle.Segment(100).setColor(Color.BLUE)
))
```
- `length`：分段长度（无单位，相对值）
- 总进度 = 所有分段长度之和
- 默认无分段时总长度为 100

### 里程碑点设置（Point）
```kotlin
// 添加里程碑点，用于标记关键位置
.addProgressPoint(Notification.ProgressStyle.Point(position).setColor(color))

// 或批量设置
.setProgressPoints(listOf(
    Notification.ProgressStyle.Point(100).setColor(Color.WHITE),
    Notification.ProgressStyle.Point(200).setColor(Color.WHITE)
))
```
- `position`：点在进度条上的位置

## 开发模式

### 1. 简单进度通知（下载类）
适用于：下载、上传、同步等简单进度任务

```kotlin
val style = Notification.ProgressStyle()
    .setStyledByProgress(true)
    .setProgress(progress)  // 0-100
```

### 2. 分段进度通知（外卖/打车类）
适用于：有明确阶段的流程，如外卖配送、打车行程

```kotlin
val style = Notification.ProgressStyle()
    .setStyledByProgress(true)
    .setProgress(totalProgress)
    // 4个阶段，每段100
    .addProgressSegment(Segment(100).setColor(Color.GREEN))   // 下单
    .addProgressSegment(Segment(100).setColor(Color.ORANGE))  // 准备
    .addProgressSegment(Segment(100).setColor(Color.BLUE))    // 取餐
    .addProgressSegment(Segment(100).setColor(Color.PURPLE))  // 配送
    // 里程碑点
    .addProgressPoint(Point(100).setColor(Color.WHITE))
    .addProgressPoint(Point(200).setColor(Color.WHITE))
    .addProgressPoint(Point(300).setColor(Color.WHITE))
```

### 3. 出行通知 - 飞机（单一进度 + 图标）
适用于：航班追踪、长途直达交通

```kotlin
val style = Notification.ProgressStyle()
    .setStyledByProgress(true)
    .setProgress(progress)
    .setProgressStartIcon(Icon.createWithResource(context, R.drawable.ic_location))  // 起点
    .setProgressEndIcon(Icon.createWithResource(context, R.drawable.ic_location))    // 终点
    .setProgressTrackerIcon(Icon.createWithResource(context, R.drawable.ic_flight))  // 飞机图标
    .addProgressSegment(Segment(100).setColor(Color.parseColor("#03A9F4")))
```

### 4. 出行通知 - 火车（多站点 + 分段）
适用于：火车、地铁、公交等多站点交通

```kotlin
val stations = listOf("北京南", "济南西", "南京南", "上海虹桥")
val segmentLength = 100
val totalSegments = stations.size - 1

val style = Notification.ProgressStyle()
    .setStyledByProgress(true)
    .setProgress(currentStation * segmentLength + stationProgress)
    .setProgressStartIcon(startIcon)
    .setProgressEndIcon(endIcon)
    .setProgressTrackerIcon(trainIcon)

// 每个区间一个分段
for (i in 0 until totalSegments) {
    style.addProgressSegment(Segment(segmentLength).setColor(colors[i]))
}

// 中间站点标记
for (i in 1 until stations.size - 1) {
    style.addProgressPoint(Point(i * segmentLength).setColor(Color.WHITE))
}
```

## 适用场景

✅ 适合使用实时更新的场景：
- 导航、通话、网约车、外卖
- 运动记录（跑步、骑行）
- 航班/火车追踪
- 有明确开始/结束的正在进行活动

❌ 不适合的场景：
- 广告、宣传内容
- 聊天消息
- 提醒、日历事件
- 应用功能快捷方式

---

## 无进度条的实时通知

### 5. 取餐码/登机牌通知（BigTextStyle + 操作按钮）
适用于：取餐码、登机牌、入场二维码、快递取件码

实时通知**不仅限于 ProgressStyle**，也支持 `BigTextStyle` 和 `CallStyle`！

```kotlin
val builder = Notification.Builder(context, CHANNEL_ID)
    .setSmallIcon(R.mipmap.ic_launcher)
    .setContentTitle("星巴克咖啡 - 取餐码 A086")
    .setContentText("您的饮品已制作完成")
    .setOngoing(true)
    .setOnlyAlertOnce(true)
    // 大图标（店铺Logo或二维码）
    .setLargeIcon(Icon.createWithResource(context, R.drawable.ic_qrcode))
    // 操作按钮
    .addAction(
        Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_check),
            "已取餐",
            pickupPendingIntent
        ).build()
    )

// 使用 BigTextStyle
val style = Notification.BigTextStyle()
    .bigText("取餐码: A086\n您的饮品已制作完成\n\n请在柜台出示此码取餐")
    .setBigContentTitle("星巴克咖啡")
    .setSummaryText("等待取餐")

builder.setStyle(style)

// 请求提升
val extras = Bundle()
extras.putBoolean("android.requestPromotedOngoing", true)
builder.addExtras(extras)

// 状态chip显示取餐码
builder.setShortCriticalText("A086")
```

### 6. 倒计时通知（Chronometer）
适用于：演唱会开场倒计时、限时优惠、预约时间

```kotlin
val targetTime = System.currentTimeMillis() + 5 * 60 * 1000  // 5分钟后

val builder = Notification.Builder(context, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_timer)
    .setContentTitle("演唱会即将开始")
    .setContentText("请提前入场")
    .setOngoing(true)
    .setOnlyAlertOnce(true)
    // 设置目标时间和倒计时模式
    .setWhen(targetTime)
    .setShowWhen(true)
    .setUsesChronometer(true)        // 使用计时器
    .setChronometerCountDown(true)   // 倒计时模式

// 状态chip会自动显示倒计时时间
```

---

## 其他重要 API

### 操作按钮 (Action)
```kotlin
// 最多3个操作按钮
builder.addAction(
    Notification.Action.Builder(
        Icon.createWithResource(context, R.drawable.ic_action),
        "按钮文字",
        pendingIntent
    ).build()
)
```

### 大图标 (Large Icon)
```kotlin
builder.setLargeIcon(Icon.createWithResource(context, R.drawable.ic_large))
// 或从 Bitmap
builder.setLargeIcon(Icon.createWithBitmap(bitmap))
```

### 状态 Chip (Short Critical Text)
```kotlin
// 在状态栏显示简短信息（最多7个字符效果最佳）
builder.setShortCriticalText("A086")
```

### 删除监听 (Delete Intent)
```kotlin
// 检测用户关闭通知
builder.setDeleteIntent(deletePendingIntent)
```

### 检测提升状态
```kotlin
// 检查通知是否已被提升
notification.flags and Notification.FLAG_PROMOTED_ONGOING != 0

// 验证通知是否具备提升资格
notification.hasPromotableCharacteristics()

// 检查应用是否可以发布推广通知
notificationManager.canPostPromotedNotifications()
```

---

## 注意事项

1. 不要频繁发布用户不需要的实时更新，用户可在系统设置中禁用
2. 使用 `setDeleteIntent` 检测用户关闭通知
3. 不要重新发布用户已关闭的通知
4. 低版本 Android 需要回退到普通进度条：`builder.setProgress(max, progress, false)`

## 参考链接

- https://developer.android.com/develop/ui/views/notifications/live-update
- https://developer.android.com/about/versions/16/features/progress-centric-notifications
- https://developer.android.com/reference/android/app/Notification.ProgressStyle
- https://developer.android.com/reference/android/app/Notification.ProgressStyle.Segment
- https://developer.android.com/reference/android/app/Notification.ProgressStyle.Point
