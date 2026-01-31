package com.example.liveupdatestest1

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
