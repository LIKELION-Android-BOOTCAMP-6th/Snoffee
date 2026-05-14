package com.snoffee.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snoffee.app.core.ui.theme.*
import com.snoffee.app.presentation.home.component.CaffeineGauge

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAddCaffeineClick: () -> Unit = {},
    onViewAllClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadResidualCaffeine()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SnoffeeBgBase),
        contentPadding =PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item {
            val error = uiState.errorMessage
            when {
                uiState.isLoading -> LoadingState()
                error != null -> ErrorState(error) { viewModel.loadResidualCaffeine() }
                else -> SuccessState(
                    uiState = uiState,
                    onAddCaffeineClick = onAddCaffeineClick
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
            ) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("데일리 권고 섭취량", color = SnoffeeTextDisabled)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp, bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("최근 기록", style = MaterialTheme.typography.titleLarge, color = SnoffeeTextMain)
                TextButton(onClick = onViewAllClick) {
                    Text("전체보기", color = SnoffeeTextMuted)
                }
            }
        }

        items(uiState.recentLogs.size) { index ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 1.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
            ) {
                Text(text = uiState.recentLogs[index], modifier = Modifier.padding(18.dp), color = SnoffeeTextMain)
            }
        }
    }
}

@Composable
private fun SuccessState(uiState: HomeUiState, onAddCaffeineClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)) {
        Column(modifier = Modifier.padding(vertical = 30.dp, horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("현재 체내 카페인 잔량", style = MaterialTheme.typography.titleLarge, color = SnoffeeTextMain)
            Spacer(modifier = Modifier.height(30.dp))
            CaffeineGauge(uiState.residualCaffeineMg, uiState.riskLevel)
            Spacer(modifier = Modifier.height(48.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                InfoColumn("대사 예상 시간", uiState.metabolismTime)
                Box(modifier = Modifier.width(1.dp).height(30.dp).background(SnoffeeDivider))
                InfoColumn("잔류 농도", uiState.concentrationLevel)
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(onClick = onAddCaffeineClick, modifier = Modifier.fillMaxWidth()) { Text("카페인 추가") }
        }
    }
}

@Composable
private fun EmptyState(onAddCaffeineClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("아직 기록이 없어요", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onAddCaffeineClick, modifier = Modifier.fillMaxWidth()) { Text("카페인 입력하기") }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = SnoffeePrimary)
    }
}

@Composable
private fun ErrorState(message: String, onRetryClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("오류 발생: $message", color = SnoffeeError)
            Button(onClick = onRetryClick) { Text("다시 시도") }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = SnoffeeTextHint)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SnoffeeTextMain)
    }
}