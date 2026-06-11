package com.snoffee.app.core.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.snoffee.app.core.ui.theme.SnoffeeAppbar
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeeTextMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoffeeAppBar(
    title: String
) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 홈에서만 로고 출력
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = SnoffeeTextMain
                    )
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = SnoffeeAppbar,
            titleContentColor = SnoffeeTextMain,
            actionIconContentColor = SnoffeePrimary
        )
    )
}