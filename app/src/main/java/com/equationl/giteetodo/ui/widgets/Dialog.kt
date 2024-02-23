package com.equationl.giteetodo.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText

@Composable
fun BaseMsgDialog(
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
            Column(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)) {
                RichText {
                    Markdown(
                        message
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onRequestDismiss) {
                Text(confirmText, color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            if (dismissText != null) {
                TextButton(onClick = onRequestDismiss) {
                    Text(dismissText, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        modifier = Modifier
            .padding(vertical = 30.dp)
            .fillMaxWidth()
            .wrapContentHeight()
    )
}