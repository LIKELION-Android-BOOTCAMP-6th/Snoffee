package com.snoffee.app.presentation.caffeine.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.core.ui.theme.SnoffeeTheme
import com.snoffee.app.domain.model.DrinkItem

@Composable
fun DrinkListItem(
    drink: DrinkItem,
    onClick: (DrinkItem) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val colorScheme = SnoffeeTheme.colorScheme
    val borderColor =
        if (isSelected) colorScheme.onSecondaryContainer else colorScheme.outlineVariant
    val bgColor =
        if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.1f) else colorScheme.surface

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(drink) },
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘 영역
            Surface(
                shape = CircleShape,
                color = colorScheme.surfaceVariant,
                modifier = Modifier.size(36.dp)
            ) {
                // todo :: 아이콘 추가 (필요하다면)
            }

            Spacer(modifier = Modifier.width(15.dp))

            // 이름 + 브랜드
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = drink.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = drink.brand,
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 카페인 수치
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${drink.caffeineMg}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                    lineHeight = 18.sp
                )
                Text(
                    text = "MG",
                    fontSize = 13.sp,
                    color = colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDrinkListItem() {
    val sampleDrink1 = DrinkItem(
        foodId = "starbucks_001",
        name = "아이스 카페 아메리카노",
        category = "커피",
        brand = "스타벅스",
        caffeineMg = 150.0,
        servingSize = 355.0,
        totalCaffeine = 150.0,
        totalSize = 355.0
    )

    val sampleDrink2 = DrinkItem(
        foodId = "twosome_002",
        name = "아이스 로얄 밀크티",
        category = "티 라떼",
        brand = "투썸플레이스",
        caffeineMg = 95.0,
        servingSize = 414.0,
        totalCaffeine = 95.0,
        totalSize = 414.0
    )

    SnoffeeTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "기본 상태", fontSize = 12.sp, color = Color.Gray)
            DrinkListItem(
                drink = sampleDrink1,
                onClick = {},
                isSelected = false
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "선택된 상태", fontSize = 12.sp, color = Color.Gray)
            DrinkListItem(
                drink = sampleDrink2,
                onClick = {},
                isSelected = true
            )
        }
    }
}