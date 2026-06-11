package com.snoffee.app.presentation.onboarding.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextDisabled
import com.snoffee.app.core.ui.theme.SnoffeeTextHint
import com.snoffee.app.core.ui.theme.SnoffeeTextMain

@Composable
fun TimeSettingCard(title: String, time: String, iconRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SnoffeeBgBase, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = SnoffeePrimary
                    )
                }
                Column {
                    Text(text = title, fontSize = 12.sp, color = SnoffeeTextHint)
                    Text(
                        text = time,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = SnoffeeTextMain
                    )
                }
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = SnoffeeTextDisabled,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}