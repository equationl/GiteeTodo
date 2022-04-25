package com.equationl.giteetodo.ui.widgets

import androidx.compose.foundation.layout.*
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
fun ListEmptyContent(text: String = "没有找到仓库数据，点击刷新", onRefresh: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_result))
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
        LottieAnimation(
            composition,
            progress,
            modifier = Modifier.heightIn(0.dp, 300.dp)
        )
        LinkText(text = text, onClick = onRefresh)
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