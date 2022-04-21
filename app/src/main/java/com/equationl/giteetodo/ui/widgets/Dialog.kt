package com.equationl.giteetodo.ui.widgets

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BaseAlertDialog(
    title: String,
    message: String,
    confirmText: String = "Ok",
    dismissText: String? = null,
    cancelAble: Boolean = true,
    onRequestDismiss: () -> Unit) {

    AlertDialog(
        onDismissRequest = {
            if (cancelAble) onRequestDismiss.invoke()
        },
        title = {
            Text(title)
        },
        text = {
            Text(
                message, modifier = Modifier.verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(onClick = onRequestDismiss) {
                Text(confirmText, color = MaterialTheme.colors.primary)
            }
        },
        dismissButton = {
            if (dismissText != null) {
                TextButton(onClick = onRequestDismiss) {
                    Text(dismissText, color = MaterialTheme.colors.primary)
                }
            }
        },
        //modifier = Modifier.padding(vertical = 32.dp)
    )
}

@Preview
@Composable
fun Preview() {
    BaseAlertDialog("我是标题", "我是内容") {}
}