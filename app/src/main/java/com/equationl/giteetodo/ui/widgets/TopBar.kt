package com.equationl.giteetodo.ui.widgets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.equationl.giteetodo.viewmodel.CurrentPager

@Composable
fun TopBar(title: String, navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack, actions: @Composable RowScope.() -> Unit = {}, onBack: () -> Unit) {
    TopAppBar (
        title = {
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(navigationIcon, "返回")
            }
        },
        actions = actions
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeTopBar(title: String, navigationIcon: ImageVector, currentPager: CurrentPager, actions: @Composable RowScope.() -> Unit = {}, onBack: () -> Unit) {
    TopAppBar (
        title = {
            AnimatedContent(
                targetState = title,
                transitionSpec = {
                    if (currentPager == CurrentPager.HOME_ME) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    }
                    else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "topBarAnimation"
            ) { targetTitle ->
                Text(text = targetTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(navigationIcon, "返回")
            }
        },
        actions = actions
    )
}