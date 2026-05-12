package com.snoffee.app.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen(onNavigateToCaffeineInput: () -> Unit) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("홈 화면 (임시)")
            Button(
                onClick = { onNavigateToCaffeineInput() },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Text("카페인 입력 화면으로 이동")
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
//    HomeScreen()
}