package com.equationl.giteetodo.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun LinkText(
    text: String,
    fontSize: TextUnit = 12.sp,
    color: Color = MaterialTheme.colors.primary,
    onClick: () -> Unit) {

    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        modifier = Modifier.noRippleClickable(onClick = onClick) )
}