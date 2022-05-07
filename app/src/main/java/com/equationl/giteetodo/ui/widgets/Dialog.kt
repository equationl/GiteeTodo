package com.equationl.giteetodo.ui.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material.MaterialRichText

@Composable
fun BaseAlertDialog(
    message: String,
    title: String? = null,
    confirmText: String = "Ok",
    dismissText: String? = null,
    cancelAble: Boolean = true,
    onRequestDismiss: () -> Unit) {

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = {
            if (cancelAble) onRequestDismiss.invoke()
        },
        title = {
            if (title != null) {
                Text(title)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                MaterialRichText {
                    Markdown(
                        message
                    )
                }
            }
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
        modifier = Modifier
            .padding(vertical = 30.dp)
            .fillMaxWidth()
            .wrapContentHeight()
    )
}