package com.equationl.giteetodo.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun TopBar(title: String, navigationIcon: ImageVector = Icons.Filled.ArrowBack, actions: @Composable RowScope.() -> Unit = {}, onBack: () -> Unit) {
    TopAppBar (
        title = {
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(navigationIcon, "返回")
            }
        },
        actions = actions
    )
}