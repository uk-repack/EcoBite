package com.example.ecobite.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EcoBiteTopBar(
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(MaterialTheme.colorScheme.surface) // 👈 cleaner than primary
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {

            // navigation icon (left)
            Box(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                navigationIcon()
            }

            // title (center)
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge, // 👈 modern typography
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            // actions (right)
            Box(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                actions()
            }
        }

        // 👇 subtle divider (modern touch)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}