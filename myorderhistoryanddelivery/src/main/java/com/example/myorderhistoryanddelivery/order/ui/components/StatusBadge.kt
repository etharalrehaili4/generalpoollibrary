package com.example.myorderhistoryanddelivery.order.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
@Composable
fun statusBadge(
    text: String,
    color: Color,
) {
    Box(
        modifier = Modifier.padding(start = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                ),
        )
    }
}
