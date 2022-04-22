package com.equationl.giteetodo.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.equationl.giteetodo.R

@Composable
fun ListEmptyContent(onRefresh: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        // TODO 加个动画咯
        LinkText(text = "没有找到仓库数据，点击刷新", onClick = onRefresh)
    }
}

@Composable
fun LoadDataContent(text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
        LottieAnimation(
            composition,
            progress
        )
        Text(text, Modifier.padding(top = 18.dp))
    }
}

@Preview
@Composable
fun PreviewEmptyContent() {
    ListEmptyContent {

    }
}

@Preview
@Composable
fun PreviewLoadData() {
    LoadDataContent(text = "加载中咯")
}