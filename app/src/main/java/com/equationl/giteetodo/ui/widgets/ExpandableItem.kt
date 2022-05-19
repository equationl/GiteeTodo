package com.equationl.giteetodo.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableItem(
    title: String,
    modifier: Modifier = Modifier,
    endText: String = "",
    subItemStartPadding: Int = 8,
    subItem: @Composable () -> Unit
) {
    var isShowSubItem by remember { mutableStateOf(false) }

    val arrowRotateDegrees: Float by animateFloatAsState(if (isShowSubItem) 90f else 0f)

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
                        modifier = modifier.padding(end = 4.dp).widthIn(0.dp, 100.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
                Icon(
                    Icons.Outlined.ArrowRight,
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
