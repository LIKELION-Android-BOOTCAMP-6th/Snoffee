package com.snoffee.app.core.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.SnoffeeAppbar
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeeTextMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoffeeAppBar(
    title: String,
    onNotificationClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 홈에서만 로고 출력
                if (title == "Snoffee") {
                    Icon(
                        //로고 변경
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = SnoffeeTextMain
                    )
                )
            }
        },
//        actions = {
//            IconButton(onClick = onNotificationClick) {
//                Icon(
//                    imageVector = Icons.Outlined.Notifications,
//                    contentDescription = "알림",
//                    tint = SnoffeePrimary
//                )
//            }
//        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = SnoffeeAppbar,
            titleContentColor = SnoffeeTextMain,
            actionIconContentColor = SnoffeePrimary
        )
    )
}