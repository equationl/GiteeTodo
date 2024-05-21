package com.equationl.giteetodo.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.equationl.giteetodo.constants.DefaultText
import com.equationl.giteetodo.ui.LocalNavController
import com.equationl.giteetodo.ui.widgets.TopBar
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText

@Composable
fun AboutScreen() {
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            TopBar("关于") {
                navController.popBackStack()
            }
        }
    )
    {
        Column(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
        ) {
            RichText(modifier = Modifier
                .padding(it)
                .padding(8.dp)) {
                Markdown(content = DefaultText.AboutContent.trimIndent())
            }
        }
    }
}