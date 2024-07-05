package com.equationl.giteetodo.ui.page

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.equationl.giteetodo.ui.LocalNavController
import com.equationl.giteetodo.util.getImageLoader
import kotlin.math.absoluteValue

private const val MinScale = 1f
private const val MaxScale = 10f

private var isRequestSuccess = false
private var cacheKey: String? = null

@Composable
fun ImageScreen(
    image: String
) {
    val context = LocalContext.current
    val navController = LocalNavController.current

    var isAlreadyTap = remember { false }
    var imgSize: Size = remember { Size.Unspecified }

    var scale by remember { mutableStateOf(1f) }
    var offset  by remember { mutableStateOf(Offset.Zero) }


    val state =
        rememberTransformableState(onTransformation = { zoomChange, panChange, _ ->
            scale = (zoomChange * scale).coerceAtLeast(MinScale).coerceAtMost(MaxScale)
            offset = calOffset(imgSize, scale, offset + panChange)
        })

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = Color.Black,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(image)
                .placeholder(android.R.drawable.stat_sys_download)
                .error(android.R.drawable.stat_notify_error)
                .build(),
            contentDescription = "previewImage",
            contentScale = ContentScale.Fit,
            onError = {
                Log.e("ImageScreen", "ImageScreen: ${it.result.throwable.stackTraceToString()}")
            },
            onSuccess = {
                isRequestSuccess = true
                cacheKey = it.result.diskCacheKey
                Log.i("el", "ImageScreen: cacheKey = $cacheKey")
            },
            imageLoader = context.getImageLoader(),
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = state)
                .graphicsLayer {
                    imgSize = size // 这个 size 是整个 AsyncImage 的 size 而非实际图像的 size
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale < 1f) {
                                scale = 1f
                            } else if (scale == 1f) {
                                scale = 2f
                            } else if (scale > 1f) {
                                scale = 1f
                            }

                            offset = Offset.Zero
                        },
                        onTap = {
                            if (!isAlreadyTap) {
                                isAlreadyTap = true
                                cacheKey = null
                                isRequestSuccess = false
                                navController.popBackStack()
                            }
                        },
                    )
                },
        )
    }
}

/**
 * 避免图片偏移超出屏幕
 * */
private fun calOffset(
    imgSize: Size,
    scale: Float,
    offset: Offset,
): Offset {
    if (imgSize == Size.Unspecified) return Offset.Zero
    val px = imgSize.width * (scale - 1f) / 2f
    val py = imgSize.height * (scale - 1f) / 2f
    var np = offset
    val xDiff = np.x.absoluteValue - px
    val yDiff = np.y.absoluteValue - py
    if (xDiff > 0)
        np = np.copy(x = px * np.x.absoluteValue / np.x)
    if (yDiff > 0)
        np = np.copy(y = py * np.y.absoluteValue / np.y)
    return np
}