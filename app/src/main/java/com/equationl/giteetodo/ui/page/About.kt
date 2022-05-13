package com.equationl.giteetodo.ui.page

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.equationl.giteetodo.constants.DefaultText
import com.equationl.giteetodo.ui.widgets.TopBar
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material.MaterialRichText

@Composable
fun AboutScreen(navController: NavHostController) {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar("关于") {
                    navController.popBackStack()
                }
            }
        )
        {
            MaterialRichText(modifier = Modifier.padding(it).padding(8.dp)) {
                Markdown(content = DefaultText.AboutContent.trimIndent())
            }
        }
    }
}