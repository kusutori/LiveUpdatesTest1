package com.example.liveupdatestest1

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.liveupdatestest1.ui.theme.LiveUpdatesTest1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiveUpdatesTest1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LiveUpdatesScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LiveUpdatesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val hasPermission = remember { mutableStateOf(hasPostNotificationPermission(context)) }
    val progressState = remember { mutableIntStateOf(0) }
    val maxProgress = 100

    // 分段通知状态
    val segmentStep = remember { mutableIntStateOf(0) }
    val segmentProgress = remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission.value = granted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "通知权限：${if (hasPermission.value) "已授予" else "未授予"}")

        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }) {
            Text(text = "请求通知权限")
        }

        HorizontalDivider()

        // === 完整进度通知（下载类） ===
        Text(text = "📥 完整进度通知（下载类）")
        Text(text = "当前进度：${progressState.intValue}%")

        Button(onClick = {
            progressState.intValue = 0
            postLiveUpdate(context, progressState.intValue, maxProgress, "已开始")
        }) {
            Text(text = "开始下载")
        }

        Button(onClick = {
            val next = (progressState.intValue + 10).coerceAtMost(maxProgress)
            progressState.intValue = next
            postLiveUpdate(context, progressState.intValue, maxProgress, "进度 ${next}%")
        }) {
            Text(text = "推进进度 +10%")
        }

        Button(onClick = {
            cancelLiveUpdate(context)
        }) {
            Text(text = "结束下载通知")
        }

        HorizontalDivider()

        // === 分段进度通知（外卖/打车类） ===
        Text(text = "🚗 分段进度通知（外卖/打车类）")
        val stepNames = listOf("下单确认", "商家准备", "骑手取餐", "配送中")
        val currentStepName = stepNames.getOrElse(segmentStep.intValue) { "已完成" }
        Text(text = "当前阶段：$currentStepName (${segmentProgress.intValue}%)")

        Button(onClick = {
            segmentStep.intValue = 0
            segmentProgress.intValue = 0
            postSegmentedLiveUpdate(context, 0, 0, "订单已确认，商家即将开始准备")
        }) {
            Text(text = "开始外卖订单")
        }

        Button(onClick = {
            val nextProgress = segmentProgress.intValue + 25
            if (nextProgress >= 100) {
                // 进入下一阶段
                if (segmentStep.intValue < 3) {
                    segmentStep.intValue += 1
                    segmentProgress.intValue = 0
                } else {
                    segmentProgress.intValue = 100
                }
            } else {
                segmentProgress.intValue = nextProgress
            }
            val statusText = when (segmentStep.intValue) {
                0 -> "订单已确认，商家即将开始准备"
                1 -> "商家正在准备您的餐品..."
                2 -> "骑手正在取餐..."
                3 -> "骑手正在配送，预计10分钟送达"
                else -> "已送达"
            }
            postSegmentedLiveUpdate(context, segmentStep.intValue, segmentProgress.intValue, statusText)
        }) {
            Text(text = "推进阶段 +25%")
        }

        Button(onClick = {
            if (segmentStep.intValue < 3) {
                segmentStep.intValue += 1
                segmentProgress.intValue = 0
                val statusText = when (segmentStep.intValue) {
                    1 -> "商家正在准备您的餐品..."
                    2 -> "骑手正在取餐..."
                    3 -> "骑手正在配送，预计10分钟送达"
                    else -> "已送达"
                }
                postSegmentedLiveUpdate(context, segmentStep.intValue, segmentProgress.intValue, statusText)
            }
        }) {
            Text(text = "跳到下一阶段")
        }

        Button(onClick = {
            cancelSegmentedLiveUpdate(context)
        }) {
            Text(text = "结束外卖通知")
        }

        HorizontalDivider()

        // === 出行类通知 - 飞机 ===
        val flightProgress = remember { mutableIntStateOf(0) }
        Text(text = "✈️ 出行通知 - 飞机航班")
        Text(text = "航班进度：${flightProgress.intValue}%")

        Button(onClick = {
            flightProgress.intValue = 0
            postFlightLiveUpdate(context, 0, "北京", "上海", "登机中，请前往登机口")
        }) {
            Text(text = "开始航班追踪")
        }

        Button(onClick = {
            val next = (flightProgress.intValue + 15).coerceAtMost(100)
            flightProgress.intValue = next
            val statusText = when {
                next < 10 -> "登机中，请前往登机口"
                next < 30 -> "飞机已起飞"
                next < 70 -> "飞行中，预计1小时后到达"
                next < 90 -> "即将降落，请系好安全带"
                else -> "已安全抵达上海"
            }
            postFlightLiveUpdate(context, next, "北京", "上海", statusText)
        }) {
            Text(text = "推进航班 +15%")
        }

        Button(onClick = {
            cancelTravelLiveUpdate(context)
        }) {
            Text(text = "结束航班通知")
        }

        HorizontalDivider()

        // === 出行类通知 - 火车 ===
        val trainStation = remember { mutableIntStateOf(0) }
        val trainProgress = remember { mutableIntStateOf(0) }
        val trainStations = listOf("北京南", "济南西", "南京南", "上海虹桥")
        Text(text = "🚄 出行通知 - 火车")
        val currentTrainStation = trainStations.getOrElse(trainStation.intValue) { "终点" }
        Text(text = "当前站点：$currentTrainStation (${trainProgress.intValue}%)")

        Button(onClick = {
            trainStation.intValue = 0
            trainProgress.intValue = 0
            postTrainLiveUpdate(context, 0, 0, trainStations, "列车已发车，下一站：济南西")
        }) {
            Text(text = "开始火车追踪")
        }

        Button(onClick = {
            val nextProgress = trainProgress.intValue + 30
            if (nextProgress >= 100) {
                if (trainStation.intValue < trainStations.size - 1) {
                    trainStation.intValue += 1
                    trainProgress.intValue = 0
                } else {
                    trainProgress.intValue = 100
                }
            } else {
                trainProgress.intValue = nextProgress
            }
            val currentIdx = trainStation.intValue
            val nextStation = trainStations.getOrElse(currentIdx + 1) { "终点站" }
            val statusText = if (currentIdx >= trainStations.size - 1) {
                "已到达终点站：${trainStations.last()}"
            } else {
                "正在前往：$nextStation"
            }
            postTrainLiveUpdate(context, trainStation.intValue, trainProgress.intValue, trainStations, statusText)
        }) {
            Text(text = "推进火车 +30%")
        }

        Button(onClick = {
            if (trainStation.intValue < trainStations.size - 1) {
                trainStation.intValue += 1
                trainProgress.intValue = 0
                val currentIdx = trainStation.intValue
                val nextStation = trainStations.getOrElse(currentIdx + 1) { "终点站" }
                val statusText = if (currentIdx >= trainStations.size - 1) {
                    "已到达终点站：${trainStations.last()}"
                } else {
                    "列车到站：${trainStations[currentIdx]}，下一站：$nextStation"
                }
                postTrainLiveUpdate(context, trainStation.intValue, trainProgress.intValue, trainStations, statusText)
            }
        }) {
            Text(text = "到达下一站")
        }

        Button(onClick = {
            cancelTravelLiveUpdate(context)
        }) {
            Text(text = "结束火车通知")
        }

        HorizontalDivider()

        // === 取餐码通知（无进度条，BigTextStyle + 按钮） ===
        Text(text = "🎫 取餐码通知（无进度条）")
        Text(text = "使用 BigTextStyle + 操作按钮")

        Button(onClick = {
            postPickupCodeLiveUpdate(
                context,
                pickupCode = "A086",
                storeName = "星巴克咖啡(国贸店)",
                statusText = "您的饮品已制作完成，请到吧台取餐"
            )
        }) {
            Text(text = "显示取餐码")
        }

        Button(onClick = {
            postPickupCodeLiveUpdate(
                context,
                pickupCode = "B123",
                storeName = "麦当劳(三里屯店)",
                statusText = "您的餐品正在制作中，预计3分钟完成"
            )
        }) {
            Text(text = "显示另一个取餐码")
        }

        Button(onClick = {
            cancelPickupCodeLiveUpdate(context)
        }) {
            Text(text = "取消取餐码通知")
        }

        HorizontalDivider()

        // === 倒计时通知（使用 chronometer） ===
        Text(text = "⏱️ 倒计时通知")
        Text(text = "使用 setUsesChronometer + setChronometerCountDown")

        Button(onClick = {
            // 设置5分钟后的时间
            val targetTime = System.currentTimeMillis() + 5 * 60 * 1000
            postCountdownLiveUpdate(
                context,
                targetTimeMillis = targetTime,
                title = "演唱会即将开始",
                statusText = "周杰伦2025巡回演唱会 · 北京站\n请提前入场，演出即将开始"
            )
        }) {
            Text(text = "开始5分钟倒计时")
        }

        Button(onClick = {
            // 设置30秒后的时间
            val targetTime = System.currentTimeMillis() + 30 * 1000
            postCountdownLiveUpdate(
                context,
                targetTimeMillis = targetTime,
                title = "限时优惠即将结束",
                statusText = "您的专属优惠券即将过期，快去下单！"
            )
        }) {
            Text(text = "开始30秒倒计时")
        }

        Button(onClick = {
            cancelCountdownLiveUpdate(context)
        }) {
            Text(text = "取消倒计时通知")
        }

        HorizontalDivider()

        // === 通话通知（CallStyle） ===
        val isOngoingCall = remember { mutableStateOf(false) }
        Text(text = "📞 通话通知（CallStyle）")
        Text(text = "状态：${if (isOngoingCall.value) "通话中" else "待机"}")

        Button(onClick = {
            isOngoingCall.value = false
            postCallLiveUpdate(context, "张三", false)
        }) {
            Text(text = "模拟来电")
        }

        Button(onClick = {
            isOngoingCall.value = true
            postCallLiveUpdate(context, "张三", true)
        }) {
            Text(text = "接听（通话中）")
        }

        Button(onClick = {
            isOngoingCall.value = false
            cancelCallLiveUpdate(context)
        }) {
            Text(text = "挂断")
        }

        HorizontalDivider()

        // === 运动健康通知 ===
        val workoutStartTime = remember { mutableStateOf(0L) }
        val workoutDistance = remember { mutableIntStateOf(0) }
        val workoutCalories = remember { mutableIntStateOf(0) }
        Text(text = "🏃 运动健康通知")
        Text(text = "距离：${workoutDistance.intValue / 1000f}km · 卡路里：${workoutCalories.intValue}kcal")

        Button(onClick = {
            workoutStartTime.value = System.currentTimeMillis()
            workoutDistance.intValue = 0
            workoutCalories.intValue = 0
            postWorkoutLiveUpdate(
                context,
                startTimeMillis = workoutStartTime.value,
                calories = 0,
                distance = 0,
                targetDistance = 5000,
                sportType = "running"
            )
        }) {
            Text(text = "开始跑步（目标5km）")
        }

        Button(onClick = {
            workoutDistance.intValue += 500
            workoutCalories.intValue += 35
            postWorkoutLiveUpdate(
                context,
                startTimeMillis = workoutStartTime.value,
                calories = workoutCalories.intValue,
                distance = workoutDistance.intValue,
                targetDistance = 5000,
                sportType = "running"
            )
        }) {
            Text(text = "跑了500米（+35kcal）")
        }

        Button(onClick = {
            cancelWorkoutLiveUpdate(context)
        }) {
            Text(text = "结束运动")
        }

        HorizontalDivider()

        // === 打车行程通知 ===
        val rideshareStep = remember { mutableIntStateOf(0) }
        val rideshareProgress = remember { mutableIntStateOf(0) }
        val rideshareEta = remember { mutableIntStateOf(15) }
        Text(text = "🚕 打车行程通知")
        val rideshareStatus = when (rideshareStep.intValue) {
            0 -> "等待接单"
            1 -> "司机前往"
            2 -> "行驶中"
            3 -> "即将到达"
            else -> "已完成"
        }
        Text(text = "阶段：$rideshareStatus · 预计${rideshareEta.intValue}分钟")

        Button(onClick = {
            rideshareStep.intValue = 0
            rideshareProgress.intValue = 0
            rideshareEta.intValue = 15
            postRideshareLiveUpdate(context, 0, 0, "王师傅", "京A·12345", 15)
        }) {
            Text(text = "呼叫打车")
        }

        Button(onClick = {
            rideshareProgress.intValue += 30
            if (rideshareProgress.intValue >= 100) {
                if (rideshareStep.intValue < 3) {
                    rideshareStep.intValue += 1
                    rideshareProgress.intValue = 0
                    rideshareEta.intValue = (rideshareEta.intValue - 3).coerceAtLeast(1)
                } else {
                    rideshareProgress.intValue = 100
                }
            }
            postRideshareLiveUpdate(
                context,
                rideshareStep.intValue,
                rideshareProgress.intValue,
                "王师傅",
                "京A·12345",
                rideshareEta.intValue
            )
        }) {
            Text(text = "推进行程 +30%")
        }

        Button(onClick = {
            if (rideshareStep.intValue < 3) {
                rideshareStep.intValue += 1
                rideshareProgress.intValue = 0
                rideshareEta.intValue = (rideshareEta.intValue - 3).coerceAtLeast(1)
                postRideshareLiveUpdate(
                    context,
                    rideshareStep.intValue,
                    rideshareProgress.intValue,
                    "王师傅",
                    "京A·12345",
                    rideshareEta.intValue
                )
            }
        }) {
            Text(text = "跳到下一阶段")
        }

        Button(onClick = {
            cancelRideshareLiveUpdate(context)
        }) {
            Text(text = "结束行程")
        }

        HorizontalDivider()

        // === 体育比赛实况通知 ===
        val sportsPeriod = remember { mutableIntStateOf(0) }
        val sportsProgress = remember { mutableIntStateOf(0) }
        val homeScore = remember { mutableIntStateOf(0) }
        val awayScore = remember { mutableIntStateOf(0) }
        Text(text = "⚽ 体育比赛实况")
        val periodName = when (sportsPeriod.intValue) {
            0 -> "上半场"
            1 -> "中场休息"
            2 -> "下半场"
            else -> "比赛结束"
        }
        Text(text = "曼联 ${homeScore.intValue} - ${awayScore.intValue} 利物浦 · $periodName")

        Button(onClick = {
            sportsPeriod.intValue = 0
            sportsProgress.intValue = 0
            homeScore.intValue = 0
            awayScore.intValue = 0
            postSportsLiveUpdate(
                context, "曼联", "利物浦",
                0, 0, 0, 0, "0'"
            )
        }) {
            Text(text = "开始比赛")
        }

        Button(onClick = {
            sportsProgress.intValue += 20
            if (sportsProgress.intValue >= 100) {
                if (sportsPeriod.intValue < 3) {
                    sportsPeriod.intValue += 1
                    sportsProgress.intValue = 0
                }
            }
            // 随机进球
            if ((0..10).random() > 7) {
                if ((0..1).random() == 0) homeScore.intValue++ else awayScore.intValue++
            }
            val matchTime = when (sportsPeriod.intValue) {
                0 -> "${sportsProgress.intValue * 45 / 100}'"
                1 -> "HT"
                2 -> "${45 + sportsProgress.intValue * 45 / 100}'"
                else -> "FT"
            }
            postSportsLiveUpdate(
                context, "曼联", "利物浦",
                homeScore.intValue, awayScore.intValue,
                sportsPeriod.intValue, sportsProgress.intValue,
                matchTime
            )
        }) {
            Text(text = "推进比赛 +20%")
        }

        Button(onClick = {
            homeScore.intValue++
            val matchTime = when (sportsPeriod.intValue) {
                0 -> "${sportsProgress.intValue * 45 / 100}'"
                1 -> "HT"
                2 -> "${45 + sportsProgress.intValue * 45 / 100}'"
                else -> "FT"
            }
            postSportsLiveUpdate(
                context, "曼联", "利物浦",
                homeScore.intValue, awayScore.intValue,
                sportsPeriod.intValue, sportsProgress.intValue,
                matchTime
            )
        }) {
            Text(text = "曼联进球！")
        }

        Button(onClick = {
            awayScore.intValue++
            val matchTime = when (sportsPeriod.intValue) {
                0 -> "${sportsProgress.intValue * 45 / 100}'"
                1 -> "HT"
                2 -> "${45 + sportsProgress.intValue * 45 / 100}'"
                else -> "FT"
            }
            postSportsLiveUpdate(
                context, "曼联", "利物浦",
                homeScore.intValue, awayScore.intValue,
                sportsPeriod.intValue, sportsProgress.intValue,
                matchTime
            )
        }) {
            Text(text = "利物浦进球！")
        }

        Button(onClick = {
            cancelSportsLiveUpdate(context)
        }) {
            Text(text = "结束比赛通知")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "提示：Android 16 上会以 Live Updates 方式展示，分段通知会显示不同颜色的进度条。")
        Text(text = "新增：取餐码使用 BigTextStyle，倒计时使用 chronometer，通话使用 CallStyle。")
    }
}

@Preview(showBackground = true)
@Composable
fun LiveUpdatesPreview() {
    LiveUpdatesTest1Theme {
        LiveUpdatesScreen()
    }
}

private fun hasPostNotificationPermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}