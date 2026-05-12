package com.snoffee.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snoffee.app.presentation.home.component.CaffeineGauge

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddCaffeineClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadResidualCaffeine()
    }

    HomeContent(
        uiState = uiState,
        onAddCaffeineClick = onAddCaffeineClick,
        onRetryClick = {
            viewModel.loadResidualCaffeine()
        }
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onAddCaffeineClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F5F2))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Snoffee",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF2E2A2A)
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            uiState.isLoading -> {
                LoadingState()
            }

            uiState.errorMessage != null -> {
                ErrorState(
                    message = uiState.errorMessage,
                    onRetryClick = onRetryClick
                )
            }

            uiState.isEmpty -> {
                EmptyState(
                    onAddCaffeineClick = onAddCaffeineClick
                )
            }

            else -> {
                SuccessState(
                    uiState = uiState,
                    onAddCaffeineClick = onAddCaffeineClick
                )
            }
        }
    }
}

@Composable
private fun SuccessState(
    uiState: HomeUiState,
    onAddCaffeineClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "현재 잔류 카페인",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF2E2A2A)
            )

            Spacer(modifier = Modifier.height(20.dp))

            CaffeineGauge(
                residualCaffeineMg = uiState.residualCaffeineMg,
                riskLevel = uiState.riskLevel
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddCaffeineClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "카페인 추가")
            }
        }
    }
}

@Composable
private fun EmptyState(
    onAddCaffeineClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "아직 기록이 없어요",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF2E2A2A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "오늘 마신 카페인을 기록해보세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8C6E63)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddCaffeineClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "카페인 입력하기")
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "잔류량을 불러오지 못했어요",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFD96C6C)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2E2A2A)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetryClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "다시 시도")
            }
        }
    }
}