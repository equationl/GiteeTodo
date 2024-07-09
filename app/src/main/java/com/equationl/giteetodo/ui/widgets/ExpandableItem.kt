package com.equationl.giteetodo.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 可展开的列表
 *
 * @param title 列表标题
 * @param modifier Modifier
 * @param endText 列表标题的尾部文字，默认为空
 * @param subItemStartPadding 子项距离 start 的 padding 值
 * @param subItem 子项
 * */
@Composable
fun ExpandableItem(
    title: String,
    modifier: Modifier = Modifier,
    endText: String = "",
    subItemStartPadding: Int = 8,
    subItem: @Composable () -> Unit
) {
    var isShowSubItem by rememberSaveable { mutableStateOf(false) }

    val arrowRotateDegrees: Float by animateFloatAsState(if (isShowSubItem) 90f else 0f, label = "rotateArrow")

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isShowSubItem = !isShowSubItem
                }
        ) {
            Text(text = title)
            Row {
                if (endText.isNotBlank()) {
                    Text(text = endText,
                        modifier = modifier
                            .padding(end = 4.dp)
                            .widthIn(0.dp, 100.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowRight,
                    contentDescription = title,
                    modifier = Modifier.rotate(arrowRotateDegrees)
                )
            }
        }

        AnimatedVisibility(visible = isShowSubItem) {
            Column(modifier = Modifier.padding(start = subItemStartPadding.dp)) {
                subItem()
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewExpandableItem() {
    Column(modifier = Modifier.fillMaxSize()) {
        ExpandableItem(title = "title", modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "111111111111")
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "222222222222")
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "333333333333")
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "444444444444")
            }
        }
    }
}
