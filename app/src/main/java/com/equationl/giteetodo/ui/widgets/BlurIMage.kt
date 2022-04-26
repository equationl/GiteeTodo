package com.equationl.giteetodo.ui.widgets

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.equationl.giteetodo.R

@Composable
fun BlurImage(
    paint: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.None) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Image(painter = paint,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier.blur(16.dp)
        )
    }
    else {
        // TODO 适配安卓 12 以下机型的 blur 效果
        Image(painter = paint,
            contentDescription = contentDescription,
            contentScale = contentScale,
            alpha = 0.5f,
            modifier = modifier
        )
    }
}

@Preview
@Composable
fun PreviewBlur() {
    BlurImage(
        paint = painterResource(id = R.drawable.bg2),
        contentDescription = "background",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}