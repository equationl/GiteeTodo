package com.equationl.giteetodo.ui.widgets

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 *
 * @author Harsh2098
 * @link https://github.com/Harsh2098/SwipeableActionCard/
 *
 * @param mainCard Composable which displays the main card content
 * @param leftSwipeCard Composable to display action card when main card is swiped left
 * @param rightSwipeCard Composable to display action card when main card is swiped right
 *
 * @param leftSwiped Method to be invoked when main card is swiped to left anchor
 * @param rightSwiped Method to be invoked when main card is swiped to right anchor
 *
 * @param animationSpec animation to use when animating main card back to default position
 * @param thresholds will be used to determine which state to animate to when swiping stops
 * @param velocityThreshold velocity threshold in dp per second
 */

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableActionCard(
    mainCard: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leftSwipeCard: (@Composable () -> Unit)? = null,
    rightSwipeCard: (@Composable () -> Unit)? = null,
    leftSwiped: (() -> Unit)? = null,
    rightSwiped: (() -> Unit)? = null,
    animationSpec: AnimationSpec<Float> = tween(250),
    thresholds: (from: SwipeCardState, to: SwipeCardState) -> ThresholdConfig = { _, _ ->
        FractionalThreshold(
            0.6f
        )
    },
    velocityThreshold: Dp = 125.dp
) {
    ConstraintLayout(modifier = modifier) {
        val (mainCardRef, actionCardRef) = createRefs()
        val swipeableState = rememberSwipeableState(
            initialValue = SwipeCardState.DEFAULT,
            animationSpec = animationSpec
        )
        val coroutineScope = rememberCoroutineScope()

        /* Tracks if left or right action card to be shown */
        val swipeLeftCardVisible = remember { mutableStateOf(false) }

        /* Disable swipe when card is animating back to default position */
        val swipeEnabled = remember { mutableStateOf(true) }

        val maxWidthInPx = with(LocalDensity.current) {
            LocalConfiguration.current.screenWidthDp.dp.toPx()
        }

        val anchors = hashMapOf(0f to SwipeCardState.DEFAULT)
        if (leftSwipeCard != null)
            anchors[-maxWidthInPx] = SwipeCardState.LEFT

        if (rightSwipeCard != null)
            anchors[maxWidthInPx] = SwipeCardState.RIGHT

        /* This surface is for action card which is below the main card */
        Surface(
            color = Color.Transparent,
            content = if (swipeLeftCardVisible.value) {
                leftSwipeCard
            } else {
                rightSwipeCard
            } ?: {},
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(actionCardRef) {
                    top.linkTo(mainCardRef.top)
                    bottom.linkTo(mainCardRef.bottom)
                    height = Dimension.fillToConstraints
                }
        )

        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    var offset = swipeableState.offset.value.roundToInt()
                    if (offset < 0 && leftSwipeCard == null) offset = 0
                    if (offset > 0 && rightSwipeCard == null) offset = 0
                    IntOffset(offset, 0)
                }
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    orientation = Orientation.Horizontal,
                    enabled = swipeEnabled.value,
                    thresholds = thresholds,
                    velocityThreshold = velocityThreshold
                )
                .constrainAs(mainCardRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }) {

            if (swipeableState.currentValue == SwipeCardState.LEFT && !swipeableState.isAnimationRunning) {
                leftSwiped?.invoke()
                coroutineScope.launch {
                    swipeEnabled.value = false
                    swipeableState.animateTo(SwipeCardState.DEFAULT)
                    swipeEnabled.value = true
                }
            } else if (swipeableState.currentValue == SwipeCardState.RIGHT && !swipeableState.isAnimationRunning) {
                rightSwiped?.invoke()
                coroutineScope.launch {
                    swipeEnabled.value = false
                    swipeableState.animateTo(SwipeCardState.DEFAULT)
                    swipeEnabled.value = true
                }
            }

            swipeLeftCardVisible.value = swipeableState.offset.value <= 0

            mainCard()
        }
    }
}

enum class SwipeCardState {
    DEFAULT,
    LEFT,
    RIGHT
}