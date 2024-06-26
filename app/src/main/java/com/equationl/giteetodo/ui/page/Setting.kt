package com.equationl.giteetodo.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equationl.giteetodo.ui.LocalNavController
import com.equationl.giteetodo.ui.widgets.ExpandableItem
import com.equationl.giteetodo.ui.widgets.TopBar
import com.equationl.giteetodo.util.Utils.toColor
import com.equationl.giteetodo.viewmodel.SettingOption
import com.equationl.giteetodo.viewmodel.SettingViewAction
import com.equationl.giteetodo.viewmodel.SettingViewEvent
import com.equationl.giteetodo.viewmodel.SettingViewModel
import com.equationl.giteetodo.viewmodel.SettingViewState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineState = rememberCoroutineScope()

    DisposableEffect(Unit) {
        viewModel.dispatch(SettingViewAction.InitSetting)
        onDispose {  }
    }

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is SettingViewEvent.Goto) {
                navController.navigate(it.route)
            }
            else if (it is SettingViewEvent.ShowMessage) {
                coroutineState.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar("设置") {
                navController.popBackStack()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    )
    {
        SettingContent(viewModel, viewState, it)
    }
}

@Composable
fun SettingContent(
    viewModel: SettingViewModel,
    viewState: SettingViewState,
    paddingValues: PaddingValues
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 6.dp)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ExpandableItem(title = "远程仓库", modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "暂时不支持设置远程仓库")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(12.dp, 0.dp))


        ExpandableItem(title = "小组件设置", modifier = Modifier.padding(12.dp)) {
            Text(text = "更新小组件设置后需要手动点击桌面上小组件的刷新按钮方可生效", fontSize = 12.sp, modifier = Modifier.padding(8.dp, 4.dp))

            ExpandableItem(title = "最大显示数量", endText = viewState.currentShowNum.toString()) {
                SettingOption.maxShowNum.forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected =  viewState.currentShowNum == it,
                            onClick = { viewModel.dispatch(SettingViewAction.ChoiceANewNum(it)) }
                        )
                        Text(text = it.toString(), modifier = Modifier.padding(2.dp))
                    }
                }
            }
            var labelTipText = ""
            for (label in viewState.currentChoiceLabel) {
                labelTipText += label.name + " "
            }
            ExpandableItem(title = "筛选标签", endText = labelTipText.ifBlank { "无" }) {
                viewState.existLabels.forEach {
                    // 不能直接对比 label 对象，因为 label 中的某些数值（例如更新日期）可能会变化，所以使用 id 对比
                    val isChecked = viewState.currentChoiceLabel.indexOfFirst { label -> label.id == it.id } != -1
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { isChecked ->
                                viewModel.dispatch(SettingViewAction.ChoiceANewLabel(it, isChecked))
                            }
                        )
                        Text(text = it.name, color = "#${it.color}".toColor)
                    }
                }
            }
            ExpandableItem(title = "筛选状态", endText = viewState.currentState) {
                SettingOption.availableState.forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            viewState.currentState == it.des,
                            onClick = { viewModel.dispatch(SettingViewAction.ChoiceANewState(it.des)) }
                        )
                        Text(text = it.humanName, modifier = Modifier.padding(2.dp))
                    }
                }
            }
        }
    }
}