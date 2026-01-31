package com.example.liveupdatestest1

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.SystemClock

const val LIVE_UPDATE_CHANNEL_ID = "live_updates"
const val LIVE_UPDATE_NOTIFICATION_ID = 1001
const val SEGMENTED_NOTIFICATION_ID = 1002
const val TRAVEL_NOTIFICATION_ID = 1003
const val PICKUP_CODE_NOTIFICATION_ID = 1004
const val COUNTDOWN_NOTIFICATION_ID = 1005
const val CALL_NOTIFICATION_ID = 1006
const val WORKOUT_NOTIFICATION_ID = 1007
const val RIDESHARE_NOTIFICATION_ID = 1008
const val SPORTS_NOTIFICATION_ID = 1009

fun ensureLiveUpdateChannel(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    val existing = manager.getNotificationChannel(LIVE_UPDATE_CHANNEL_ID)
    if (existing == null) {
        val channel = NotificationChannel(
            LIVE_UPDATE_CHANNEL_ID,
            "Live Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Android 16 Live Updates"
        manager.createNotificationChannel(channel)
    }
}

fun postLiveUpdate(context: Context, progress: Int, max: Int, status: String) {
    ensureLiveUpdateChannel(context)

    val builder = Notification.Builder(context, LIVE_UPDATE_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("实时更新示例")
        .setContentText(status)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setCategory(Notification.CATEGORY_PROGRESS)
        .setShowWhen(false)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        val style = Notification.ProgressStyle()
            .setStyledByProgress(true)
            .setProgress(progress)
        builder.setStyle(style)
        // Use extras to request promoted ongoing (key = "android.requestPromotedOngoing")
        val extras = Bundle()
        extras.putBoolean("android.requestPromotedOngoing", true)
        builder.addExtras(extras)
        builder.setShortCriticalText("$progress%")
    } else {
        builder.setProgress(max, progress, false)
    }

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.notify(LIVE_UPDATE_NOTIFICATION_ID, builder.build())
}

fun cancelLiveUpdate(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.cancel(LIVE_UPDATE_NOTIFICATION_ID)
}

/**
 * 分段进度通知（外卖/打车场景）
 * @param currentStep 当前步骤 0-3: 0=下单, 1=商家准备, 2=骑手取餐, 3=配送中
 * @param stepProgress 当前步骤内进度 0-100
 */
fun postSegmentedLiveUpdate(context: Context, currentStep: Int, stepProgress: Int, statusText: String) {
    ensureLiveUpdateChannel(context)

    val builder = Notification.Builder(context, LIVE_UPDATE_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("外卖配送")
        .setContentText(statusText)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setCategory(Notification.CATEGORY_PROGRESS)
        .setShowWhen(false)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        // 4个阶段，每段长度100，总长度400
        // 阶段：下单确认(100) -> 商家准备(100) -> 骑手取餐(100) -> 配送中(100)
        val segmentLength = 100
        val totalProgress = currentStep * segmentLength + stepProgress

        val style = Notification.ProgressStyle()
            .setStyledByProgress(true)
            .setProgress(totalProgress)
            // 添加4个分段，用不同颜色表示不同阶段
            .addProgressSegment(Notification.ProgressStyle.Segment(segmentLength).setColor(Color.parseColor("#4CAF50")))  // 绿色-下单
            .addProgressSegment(Notification.ProgressStyle.Segment(segmentLength).setColor(Color.parseColor("#FF9800")))  // 橙色-准备
            .addProgressSegment(Notification.ProgressStyle.Segment(segmentLength).setColor(Color.parseColor("#2196F3")))  // 蓝色-取餐
            .addProgressSegment(Notification.ProgressStyle.Segment(segmentLength).setColor(Color.parseColor("#9C27B0")))  // 紫色-配送
            // 添加里程碑点
            .addProgressPoint(Notification.ProgressStyle.Point(100).setColor(Color.WHITE))  // 商家
            .addProgressPoint(Notification.ProgressStyle.Point(200).setColor(Color.WHITE))  // 骑手取餐
            .addProgressPoint(Notification.ProgressStyle.Point(300).setColor(Color.WHITE))  // 即将送达
            .addProgressPoint(Notification.ProgressStyle.Point(400).setColor(Color.WHITE))  // 已送达

        builder.setStyle(style)
        val extras = Bundle()
        extras.putBoolean("android.requestPromotedOngoing", true)
        builder.addExtras(extras)

        val chipText = when (currentStep) {
            0 -> "下单中"
            1 -> "准备中"
            2 -> "取餐中"
            3 -> "配送中"
            else -> "已完成"
        }
        builder.setShortCriticalText(chipText)
    } else {
        // 低版本回退
        val total = 400
        val current = currentStep * 100 + stepProgress
        builder.setProgress(total, current, false)
    }

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.notify(SEGMENTED_NOTIFICATION_ID, builder.build())
}

fun cancelSegmentedLiveUpdate(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.cancel(SEGMENTED_NOTIFICATION_ID)
}

/**
 * 出行类通知 - 飞机航班
 * @param progress 当前飞行进度 0-100
 * @param departure 出发地
 * @param arrival 目的地
 * @param statusText 状态文本
 */
fun postFlightLiveUpdate(
    context: Context,
    progress: Int,
    departure: String,
    arrival: String,
    statusText: String
) {
    ensureLiveUpdateChannel(context)

    val builder = Notification.Builder(context, LIVE_UPDATE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_flight)
        .setContentTitle("$departure → $arrival")
        .setContentText(statusText)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setCategory(Notification.CATEGORY_PROGRESS)
        .setShowWhen(false)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        val style = Notification.ProgressStyle()
            .setStyledByProgress(true)
            .setProgress(progress)
            // 起点图标（出发地）
            .setProgressStartIcon(Icon.createWithResource(context, R.drawable.ic_location))
            // 终点图标（目的地）
            .setProgressEndIcon(Icon.createWithResource(context, R.drawable.ic_location))
            // 追踪器图标（飞机）
            .setProgressTrackerIcon(Icon.createWithResource(context, R.drawable.ic_flight))
            // 单一蓝色进度条
            .addProgressSegment(Notification.ProgressStyle.Segment(100).setColor(Color.parseColor("#03A9F4")))

        builder.setStyle(style)
        val extras = Bundle()
        extras.putBoolean("android.requestPromotedOngoing", true)
        builder.addExtras(extras)

        val chipText = when {
            progress < 10 -> "登机中"
            progress < 30 -> "起飞"
            progress < 70 -> "飞行中"
            progress < 90 -> "即将降落"
            else -> "已降落"
        }
        builder.setShortCriticalText(chipText)
    } else {
        builder.setProgress(100, progress, false)
    }

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.notify(TRAVEL_NOTIFICATION_ID, builder.build())
}

/**
 * 出行类通知 - 火车
 * @param currentStation 当前站点索引 (0-based)
 * @param stationProgress 当前站点到下一站的进度 0-100
 * @param stations 站点列表
 * @param statusText 状态文本
 */
fun postTrainLiveUpdate(
    context: Context,
    currentStation: Int,
    stationProgress: Int,
    stations: List<String>,
    statusText: String
) {
    ensureLiveUpdateChannel(context)

    val departure = stations.firstOrNull() ?: "出发"
    val arrival = stations.lastOrNull() ?: "到达"

    val builder = Notification.Builder(context, LIVE_UPDATE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_train)
        .setContentTitle("$departure → $arrival")
        .setContentText(statusText)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setCategory(Notification.CATEGORY_PROGRESS)
        .setShowWhen(false)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        // 每个区间长度100，总长度 = (站点数-1) * 100
        val segmentLength = 100
        val totalSegments = stations.size - 1
        val totalProgress = currentStation * segmentLength + stationProgress

        val style = Notification.ProgressStyle()
            .setStyledByProgress(true)
            .setProgress(totalProgress)
            // 起点图标
            .setProgressStartIcon(Icon.createWithResource(context, R.drawable.ic_location))
            // 终点图标
            .setProgressEndIcon(Icon.createWithResource(context, R.drawable.ic_location))
            // 追踪器图标（火车）
            .setProgressTrackerIcon(Icon.createWithResource(context, R.drawable.ic_train))

        // 添加分段（每站之间一个分段，交替颜色）
        val colors = listOf(
            Color.parseColor("#4CAF50"),  // 绿色
            Color.parseColor("#2196F3"),  // 蓝色
            Color.parseColor("#FF9800"),  // 橙色
            Color.parseColor("#9C27B0")   // 紫色
        )
        for (i in 0 until totalSegments) {
            style.addProgressSegment(
                Notification.ProgressStyle.Segment(segmentLength)
                    .setColor(colors[i % colors.size])
            )
        }

        // 添加中间站点（不包括起点和终点）
        for (i in 1 until stations.size - 1) {
            style.addProgressPoint(
                Notification.ProgressStyle.Point(i * segmentLength)
                    .setColor(Color.WHITE)
            )
        }

        builder.setStyle(style)
        val extras = Bundle()
        extras.putBoolean("android.requestPromotedOngoing", true)
        builder.addExtras(extras)

        val currentStationName = stations.getOrElse(currentStation) { "行驶中" }
        builder.setShortCriticalText(currentStationName)
    } else {
        val total = (stations.size - 1) * 100
        val current = currentStation * 100 + stationProgress
        builder.setProgress(total, current, false)
    }

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.notify(TRAVEL_NOTIFICATION_ID, builder.build())
}

fun cancelTravelLiveUpdate(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.cancel(TRAVEL_NOTIFICATION_ID)
}

/**
 * 取餐码通知 - 使用 BigTextStyle 而非 ProgressStyle
 * 这是一个没有进度条的实时通知，只有状态文字和操作按钮
 * 典型场景：取餐码、登机牌、入场二维码等
 * 
 * @param pickupCode 取餐码
 * @param storeName 店铺名称
 * @param statusText 状态描述
 */
fun postPickupCodeLiveUpdate(
    context: Context,
    pickupCode: String,
    storeName: String,
    statusText: String
) {
    ensureLiveUpdateChannel(context)

    // 创建"已取餐"操作按钮的 PendingIntent
    // 实际应用中应该指向一个 BroadcastReceiver 来处理这个动作
    val pickupIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_PICKED_UP"
        putExtra("pickup_code", pickupCode)
    }
    val pickupPendingIntent = PendingIntent.getActivity(
        context,
        0,
        pickupIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = Notification.Builder(context, LIVE_UPDATE_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("$storeName - 取餐码 $pickupCode")
        .setContentText(statusText)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setCategory(Notification.CATEGORY_STATUS)
        // 添加大图标（可以是店铺Logo或二维码）
        .setLargeIcon(Icon.createWithResource(context, R.drawable.ic_qrcode))
        // 添加操作按钮
        .addAction(
            Notification.Action.Builder(
                Icon.createWithResource(context, R.drawable.ic_check),
                "已取餐",
                pickupPendingIntent
            ).build()
        )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        // 使用 BigTextStyle 而不是 ProgressStyle
        // 实时通知支持: 标准样式, BigTextStyle, CallStyle, ProgressStyle
        val style = Notification.BigTextStyle()
            .bigText("取餐码: $pickupCode\n$statusText\n\n请在柜台出示此码取餐")
            .setBigContentTitle(storeName)
            .setSummaryText("等待取餐")

        builder.setStyle(style)
        
        // 请求提升为实时通知
        val extras = Bundle()
        extras.putBoolean("android.requestPromotedOngoing", true)
        builder.addExtras(extras)
        
        // 状态chip显示取餐码
        builder.setShortCriticalText(pickupCode)
    } else {
        builder.setStyle(
            Notification.BigTextStyle()
                .bigText("取餐码: $pickupCode\n$statusText\n\n请在柜台出示此码取餐")
        )
    }

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.notify(PICKUP_CODE_NOTIFICATION_ID, builder.build())
}

fun cancelPickupCodeLiveUpdate(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.cancel(PICKUP_CODE_NOTIFICATION_ID)
}

/**
 * 倒计时通知 - 使用 setWhen + chronometer
 * 适用于：预约时间提醒、限时优惠、演唱会开场倒计时等
 * 
 * @param targetTimeMillis 目标时间（毫秒时间戳）
 * @param title 标题
 * @param statusText 状态描述
 */
fun postCountdownLiveUpdate(
    context: Context,
    targetTimeMillis: Long,
    title: String,
    statusText: String
) {
    ensureLiveUpdateChannel(context)

    val builder = Notification.Builder(context, LIVE_UPDATE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_timer)
        .setContentTitle(title)
        .setContentText(statusText)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setCategory(Notification.CATEGORY_EVENT)
        // 设置目标时间，使用倒计时模式
        .setWhen(targetTimeMillis)
        .setShowWhen(true)
        .setUsesChronometer(true)
        .setChronometerCountDown(true)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        // 使用 BigTextStyle
        val style = Notification.BigTextStyle()
            .bigText(statusText)
            .setBigContentTitle(title)

        builder.setStyle(style)
        
        // 请求提升为实时通知
        val extras = Bundle()
        extras.putBoolean("android.requestPromotedOngoing", true)
        builder.addExtras(extras)
        
        // chip 会自动显示倒计时时间
    }

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.notify(COUNTDOWN_NOTIFICATION_ID, builder.build())
}

fun cancelCountdownLiveUpdate(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.cancel(COUNTDOWN_NOTIFICATION_ID)
}

/**
 * 通话通知 - 使用 CallStyle
 * 这是实时通知支持的另一种样式，适用于：语音通话、视频通话、VoIP
 * 
 * 注意：CallStyle 通知有特殊要求，必须满足以下条件之一：
 * 1. 来自前台服务 (Foreground Service)
 * 2. 来自用户发起的任务 (User-initiated Job)
 * 3. 使用 fullScreenIntent
 * 
 * 这里使用 fullScreenIntent 来满足要求
 * 
 * @param callerName 来电者姓名
 * @param isOngoing 是否正在通话中（true=通话中，false=来电中）
 */
fun postCallLiveUpdate(
    context: Context,
    callerName: String,
    isOngoing: Boolean
) {
    ensureLiveUpdateChannel(context)

    // 创建来电者 Person 对象
    val caller = Person.Builder()
        .setName(callerName)
        .setIcon(Icon.createWithResource(context, R.drawable.ic_person))
        .setImportant(true)
        .build()

    // 挂断 Intent
    val hangupIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_HANGUP"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val hangupPendingIntent = PendingIntent.getActivity(
        context, 1, hangupIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 接听 Intent
    val answerIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_ANSWER"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val answerPendingIntent = PendingIntent.getActivity(
        context, 2, answerIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 全屏 Intent - CallStyle 必需
    val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_INCOMING_CALL"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val fullScreenPendingIntent = PendingIntent.getActivity(
        context, 3, fullScreenIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 根据状态创建不同的 CallStyle
    val callStyle = if (isOngoing) {
        // 通话进行中
        Notification.CallStyle.forOngoingCall(caller, hangupPendingIntent)
    } else {
        // 来电中
        Notification.CallStyle.forIncomingCall(caller, hangupPendingIntent, answerPendingIntent)
    }

    val builder = Notification.Builder(context, LIVE_UPDATE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_call)
        .setContentTitle(if (isOngoing) "正在通话" else "来电")
        .setContentText(callerName)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setCategory(Notification.CATEGORY_CALL)
        .setStyle(callStyle)
        // CallStyle 必须设置 fullScreenIntent
        .setFullScreenIntent(fullScreenPendingIntent, true)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        val extras = Bundle()
        extras.putBoolean("android.requestPromotedOngoing", true)
        builder.addExtras(extras)
        builder.setShortCriticalText(if (isOngoing) "通话中" else "来电")
    }

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.notify(CALL_NOTIFICATION_ID, builder.build())
}

fun cancelCallLiveUpdate(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.cancel(CALL_NOTIFICATION_ID)
}

/**
 * 运动健康记录通知 - 计时器 + 进度
 * 适用于：跑步、骑行、健身等运动记录
 * 
 * @param startTimeMillis 运动开始时间（毫秒时间戳）
 * @param calories 消耗卡路里
 * @param distance 距离（米）
 * @param targetDistance 目标距离（米）
 * @param sportType 运动类型（running/cycling/walking）
 */
fun postWorkoutLiveUpdate(
    context: Context,
    startTimeMillis: Long,
    calories: Int,
    distance: Int,
    targetDistance: Int,
    sportType: String = "running"
) {
    ensureLiveUpdateChannel(context)

    val sportIcon = when (sportType) {
        "cycling" -> R.drawable.ic_cycling
        "walking" -> R.drawable.ic_walking
        else -> R.drawable.ic_running
    }

    val sportName = when (sportType) {
        "cycling" -> "骑行"
        "walking" -> "步行"
        else -> "跑步"
    }

    val distanceKm = distance / 1000f
    val targetKm = targetDistance / 1000f
    val progress = ((distance.toFloat() / targetDistance) * 100).toInt().coerceIn(0, 100)

    val builder = Notification.Builder(context, LIVE_UPDATE_CHANNEL_ID)
        .setSmallIcon(sportIcon)
        .setContentTitle("$sportName 中")
        .setContentText("${String.format("%.2f", distanceKm)}km · $calories kcal")
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setCategory(Notification.CATEGORY_WORKOUT)
        // 计时器：显示运动时长
        .setWhen(startTimeMillis)
        .setShowWhen(true)
        .setUsesChronometer(true)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        val style = Notification.ProgressStyle()
            .setStyledByProgress(true)
            .setProgress(progress)
            .setProgressTrackerIcon(Icon.createWithResource(context, sportIcon))
            // 渐变色：从绿色到蓝色
            .addProgressSegment(Notification.ProgressStyle.Segment(50).setColor(Color.parseColor("#4CAF50")))
            .addProgressSegment(Notification.ProgressStyle.Segment(50).setColor(Color.parseColor("#2196F3")))
            // 目标线
            .addProgressPoint(Notification.ProgressStyle.Point(100).setColor(Color.parseColor("#FF5722")))

        builder.setStyle(style)
        
        val extras = Bundle()
        extras.putBoolean("android.requestPromotedOngoing", true)
        builder.addExtras(extras)
        
        builder.setShortCriticalText("${String.format("%.1f", distanceKm)}km")
    } else {
        builder.setProgress(100, progress, false)
    }

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.notify(WORKOUT_NOTIFICATION_ID, builder.build())
}

fun cancelWorkoutLiveUpdate(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.cancel(WORKOUT_NOTIFICATION_ID)
}

/**
 * 打车行程通知 - 分段进度
 * 适用于：滴滴、Uber等网约车
 * 
 * @param step 当前阶段 0=等待接单 1=司机前往 2=行驶中 3=即将到达
 * @param stepProgress 当前阶段进度 0-100
 * @param driverName 司机姓名
 * @param carPlate 车牌号
 * @param eta 预计到达时间（分钟）
 */
fun postRideshareLiveUpdate(
    context: Context,
    step: Int,
    stepProgress: Int,
    driverName: String,
    carPlate: String,
    eta: Int
) {
    ensureLiveUpdateChannel(context)

    val statusText = when (step) {
        0 -> "正在为您匹配司机..."
        1 -> "$driverName 正在赶来，预计${eta}分钟到达"
        2 -> "行驶中，预计${eta}分钟到达目的地"
        3 -> "即将到达目的地"
        else -> "行程结束"
    }

    val builder = Notification.Builder(context, LIVE_UPDATE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_car)
        .setContentTitle("打车行程 · $carPlate")
        .setContentText(statusText)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setCategory(Notification.CATEGORY_NAVIGATION)
        .setShowWhen(false)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        val segmentLength = 100
        val totalProgress = step * segmentLength + stepProgress

        val style = Notification.ProgressStyle()
            .setStyledByProgress(true)
            .setProgress(totalProgress)
            // 4个阶段
            .addProgressSegment(Notification.ProgressStyle.Segment(segmentLength).setColor(Color.parseColor("#9E9E9E")))  // 等待
            .addProgressSegment(Notification.ProgressStyle.Segment(segmentLength).setColor(Color.parseColor("#FF9800")))  // 司机前往
            .addProgressSegment(Notification.ProgressStyle.Segment(segmentLength).setColor(Color.parseColor("#4CAF50")))  // 行驶中
            .addProgressSegment(Notification.ProgressStyle.Segment(segmentLength).setColor(Color.parseColor("#2196F3")))  // 即将到达
            // 追踪器
            .setProgressTrackerIcon(Icon.createWithResource(context, R.drawable.ic_car))
            // 起终点
            .setProgressStartIcon(Icon.createWithResource(context, R.drawable.ic_location))
            .setProgressEndIcon(Icon.createWithResource(context, R.drawable.ic_flag))
            // 里程碑
            .addProgressPoint(Notification.ProgressStyle.Point(100).setColor(Color.WHITE))
            .addProgressPoint(Notification.ProgressStyle.Point(200).setColor(Color.WHITE))
            .addProgressPoint(Notification.ProgressStyle.Point(300).setColor(Color.WHITE))

        builder.setStyle(style)
        
        val extras = Bundle()
        extras.putBoolean("android.requestPromotedOngoing", true)
        builder.addExtras(extras)
        
        val chipText = when (step) {
            0 -> "匹配中"
            1 -> "${eta}分钟"
            2 -> "${eta}分钟"
            3 -> "到达"
            else -> "完成"
        }
        builder.setShortCriticalText(chipText)
    } else {
        val total = 400
        val current = step * 100 + stepProgress
        builder.setProgress(total, current, false)
    }

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.notify(RIDESHARE_NOTIFICATION_ID, builder.build())
}

fun cancelRideshareLiveUpdate(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.cancel(RIDESHARE_NOTIFICATION_ID)
}

/**
 * 体育比赛实况通知 - 分段进度 + 比分显示
 * 适用于：足球、篮球、网球等体育比赛
 * 
 * @param homeTeam 主队名称
 * @param awayTeam 客队名称
 * @param homeScore 主队得分
 * @param awayScore 客队得分
 * @param period 当前阶段 0=上半场 1=中场休息 2=下半场 3=比赛结束
 * @param periodProgress 阶段进度 0-100
 * @param matchTime 比赛时间显示（如 "45'+2"）
 */
fun postSportsLiveUpdate(
    context: Context,
    homeTeam: String,
    awayTeam: String,
    homeScore: Int,
    awayScore: Int,
    period: Int,
    periodProgress: Int,
    matchTime: String
) {
    ensureLiveUpdateChannel(context)

    val periodName = when (period) {
        0 -> "上半场"
        1 -> "中场休息"
        2 -> "下半场"
        3 -> "比赛结束"
        else -> "进行中"
    }

    val builder = Notification.Builder(context, LIVE_UPDATE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_sports)
        .setContentTitle("$homeTeam $homeScore - $awayScore $awayTeam")
        .setContentText("$periodName · $matchTime")
        .setOngoing(period < 3)  // 比赛结束后不再 ongoing
        .setOnlyAlertOnce(true)
        .setCategory(Notification.CATEGORY_EVENT)
        .setShowWhen(false)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        // 比赛分为：上半场(45) + 中场(10) + 下半场(45) = 100
        val totalProgress = when (period) {
            0 -> (periodProgress * 45 / 100)           // 上半场 0-45
            1 -> 45 + (periodProgress * 10 / 100)      // 中场 45-55
            2 -> 55 + (periodProgress * 45 / 100)      // 下半场 55-100
            else -> 100
        }

        val style = Notification.ProgressStyle()
            .setStyledByProgress(true)
            .setProgress(totalProgress)
            // 上半场 - 绿色
            .addProgressSegment(Notification.ProgressStyle.Segment(45).setColor(Color.parseColor("#4CAF50")))
            // 中场休息 - 灰色
            .addProgressSegment(Notification.ProgressStyle.Segment(10).setColor(Color.parseColor("#9E9E9E")))
            // 下半场 - 蓝色
            .addProgressSegment(Notification.ProgressStyle.Segment(45).setColor(Color.parseColor("#2196F3")))
            // 追踪器图标（足球）
            .setProgressTrackerIcon(Icon.createWithResource(context, R.drawable.ic_sports))
            // 中场标记点
            .addProgressPoint(Notification.ProgressStyle.Point(45).setColor(Color.WHITE))
            .addProgressPoint(Notification.ProgressStyle.Point(55).setColor(Color.WHITE))

        builder.setStyle(style)
        
        val extras = Bundle()
        extras.putBoolean("android.requestPromotedOngoing", true)
        builder.addExtras(extras)
        
        // 比分作为 chip 显示
        builder.setShortCriticalText("$homeScore-$awayScore")
    } else {
        builder.setProgress(100, when (period) {
            0 -> periodProgress / 2
            1 -> 50
            2 -> 50 + periodProgress / 2
            else -> 100
        }, false)
    }

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.notify(SPORTS_NOTIFICATION_ID, builder.build())
}

fun cancelSportsLiveUpdate(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.cancel(SPORTS_NOTIFICATION_ID)
}
