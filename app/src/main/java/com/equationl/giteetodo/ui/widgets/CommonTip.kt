package com.equationl.giteetodo.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.equationl.giteetodo.R

@Composable
fun ListEmptyContent(title: String, text: String = "", onRefresh: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_result))
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
        LottieAnimation(
            composition,
            { progress },
            modifier = Modifier.heightIn(0.dp, 300.dp).noRippleClickable(onClick = onRefresh)
        )
        LinkText(text = title, onClick = onRefresh)
        if (text.isNotBlank()) {
            Text(text = text, fontSize = 10.sp, modifier = Modifier.padding(top = 16.dp), lineHeight = 14.sp)
        }
    }
}

@Composable
fun LoadDataContent(text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
        LottieAnimation(
            composition,
            { progress }
        )
        Text(text, Modifier.padding(top = 18.dp))
    }
}