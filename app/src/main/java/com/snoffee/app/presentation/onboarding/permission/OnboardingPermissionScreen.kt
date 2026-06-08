package com.snoffee.app.presentation.onboarding.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeSurfaceOverlay
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.presentation.onboarding.component.ProgressSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun OnboardingPermissionScreen(
    onNextClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showPermissionDeniedMessage by remember {
        mutableStateOf(false)
    }
    val healthPermissions = setOf(
        HealthPermission.getReadPermission(
            SleepSessionRecord::class
        )
    )

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            onNextClick()
        }

    fun requestNotificationOrNext() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            onNextClick()
        }
    }

    fun checkHealthPermissionAndContinue() {
        CoroutineScope(Dispatchers.Main).launch {
            val grantedPermissions =
                HealthConnectClient
                    .getOrCreate(context)
                    .permissionController
                    .getGrantedPermissions()

            if (grantedPermissions.containsAll(healthPermissions)) {
                showPermissionDeniedMessage = false
                requestNotificationOrNext()
            }
        }
    }

    val healthPermissionLauncher =
        rememberLauncherForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { grantedPermissions ->

            if (grantedPermissions.containsAll(healthPermissions)) {
                showPermissionDeniedMessage = false
                requestNotificationOrNext()
            } else {
                showPermissionDeniedMessage = true
            }
        }

    DisposableEffect(lifecycleOwner, showPermissionDeniedMessage) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && showPermissionDeniedMessage) {
                checkHealthPermissionAndContinue()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SnoffeeBgBase)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = "뒤로가기",
                    tint = SnoffeeTextMain
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ProgressSection("단계 1 / 4", 0.22f)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = "권한 동의",
                color = SnoffeeTextMain,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Snoffee의 개인화된 건강 분석 서비스를 제공받기 위해 아래의 권한이 필요합니다.",
                color = SnoffeeTextMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            PermissionCard(
                icon = Icons.Outlined.Notifications,
                title = "알림 권한",
                description = "수면 유도 시간 및 최적의 기상 알림 제공"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                icon = Icons.Outlined.Watch,
                title = "기기 연결",
                description = "워치 등 웨어러블 기기의 데이터 실시간 동기화"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionCard(
                icon = Icons.Outlined.FavoriteBorder,
                title = "건강 데이터",
                description = "심박수 및 활동량을 바탕으로 한 카페인 반감기 계산"
            )

            Spacer(modifier = Modifier.weight(1f))

            if (showPermissionDeniedMessage) {
                Text(
                    text = "수면 데이터 권한이 거부되었어요.\n아래 버튼을 눌러 Health Connect에서 권한을 허용해주세요.",
                    color = SnoffeeTextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Button(
                onClick = {
                    if (showPermissionDeniedMessage) {
                        openHealthConnectSettingsOrStore(context)
                        return@Button
                    }

                    when (HealthConnectClient.getSdkStatus(context)) {
                        HealthConnectClient.SDK_AVAILABLE -> {
                            healthPermissionLauncher.launch(healthPermissions)
                        }

                        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                            openHealthConnectStore(context)
                        }

                        else -> {
                            openHealthConnectStore(context)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SnoffeePrimary
                )
            ) {
                Text(
                    text = if (showPermissionDeniedMessage) {
                        "Health Connect 설정 열기  →"
                    } else {
                        "권한 허용하고 다음으로  →"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "권한은 설정에서 언제든지 변경할 수 있습니다.",
                modifier = Modifier.fillMaxWidth(),
                color = SnoffeeTextMuted,
                fontSize = 12.sp
            )
        }
    }
}

private fun openHealthConnectSettingsOrStore(
    context: Context
) {
    try {
        context.startActivity(
            Intent(
                HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS
            )
        )
    } catch (e: Exception) {
        openHealthConnectStore(context)
    }
}

private fun openHealthConnectStore(
    context: Context
) {
    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.google.android.apps.healthdata")
            )
        )
    } catch (e: Exception) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
            )
        )
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .background(SnoffeeSurface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(SnoffeeSurfaceOverlay, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SnoffeePrimary
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = SnoffeeTextMain,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                color = SnoffeeTextMuted,
                fontSize = 13.sp
            )
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    SnoffeeSurfaceOverlay,
                    RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        SnoffeePrimary,
                        RoundedCornerShape(50)
                    )
            )
        }
    }
}