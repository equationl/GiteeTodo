package com.equationl.giteetodo.ui.widgets

import android.net.Uri
import androidx.compose.runtime.Composable
import com.equationl.giteetodo.ui.LocalNavController
import com.equationl.giteetodo.ui.common.Route
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichTextScope

@Composable
fun RichTextScope.CommonMarkDown(
    content: String,
) {
    val navController = LocalNavController.current
    Markdown(
        content,
        onImgClicked = {
            navController.navigate("${Route.IMAGE_PREVIEW}/${Uri.encode(it)}")
        }
    )
}