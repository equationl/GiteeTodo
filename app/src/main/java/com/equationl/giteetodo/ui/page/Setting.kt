package com.equationl.giteetodo.ui.page

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equationl.giteetodo.constants.ChooseRepoType
import com.equationl.giteetodo.data.repos.model.response.Label
import com.equationl.giteetodo.ui.LocalNavController
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.common.RouteParams
import com.equationl.giteetodo.ui.widgets.ExpandableItem
import com.equationl.giteetodo.ui.widgets.TopBar
import com.equationl.giteetodo.util.Utils.toColor
import com.equationl.giteetodo.util.pin
import com.equationl.giteetodo.viewmodel.SettingOption
import com.equationl.giteetodo.viewmodel.SettingViewAction
import com.equationl.giteetodo.viewmodel.SettingViewEvent
import com.equationl.giteetodo.viewmodel.SettingViewModel
import com.equationl.giteetodo.viewmodel.SettingViewState
import com.equationl.giteetodo.viewmodel.WidgetSettingModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineState = rememberCoroutineScope()

    val widgetManager = AppWidgetManager.getInstance(context)

    val widgetProviders = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        widgetManager.getInstalledProvidersForPackage(context.packageName, null)
    } else {
        listOf()
    }

    DisposableEffect(Unit) {
        viewModel.dispatch(SettingViewAction.InitSetting(context))
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
        SettingContent(viewModel, viewState, it, widgetProviders)
    }
}

@Composable
fun SettingContent(
    viewModel: SettingViewModel,
    viewState: SettingViewState,
    paddingValues: PaddingValues,
    widgetProviders: List<AppWidgetProviderInfo>
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
            WidgetSettingList(viewModel = viewModel, viewState = viewState, widgetProviders)
        }
    }
}

@Composable
private fun WidgetSettingList(
    viewModel: SettingViewModel,
    viewState: SettingViewState,
    widgetProviders: List<AppWidgetProviderInfo>
) {
    if (viewState.widgetSettingMap.isEmpty()) {
        Text(text = "还没有添加小组件，您可以在系统桌面长按添加或点击下方“立即添加小组件”")
    }
    else {
        viewState.widgetSettingMap.forEach { (_, model) ->
            ExpandableItem(title = "小组件${model.appWidgetId}", modifier = Modifier.padding(12.dp)) {
                WidgetSettingContent(viewModel, model, viewState.existLabels)
            }
        }
    }

    AddWidgetContent(widgetProviders)
}

@Composable
private fun AddWidgetContent(widgetProviders: List<AppWidgetProviderInfo>) {
    ExpandableItem(title = "立即添加小组件", modifier = Modifier.padding(12.dp)) {
        widgetProviders.forEachIndexed { index, providerInfo ->
            val context = LocalContext.current
            val label = providerInfo.loadLabel(context.packageManager)
            val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (providerInfo.loadDescription(context)?:"").toString()
            } else {
                "小组件 $index"
            }
            val preview = painterResource(id = providerInfo.previewImage)
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        providerInfo.pin(context)
                    }
                }
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.padding(end = 8.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Image(painter = preview, contentDescription = description)
                }
            }
        }
    }
}

@Composable
private fun WidgetSettingContent(
    viewModel: SettingViewModel,
    model: WidgetSettingModel,
    existLabels: List<Label>
) {
    val navController = LocalNavController.current

    Text(text = "更新小组件设置后需要手动点击桌面上小组件的刷新按钮方可生效", fontSize = 12.sp, modifier = Modifier.padding(8.dp, 4.dp))

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                ChooseRepoType.currentWidgetAppId = model.appWidgetId
                navController.navigate("${Route.REPO_LIST}?${RouteParams.PAR_REPO_CHOOSE_TYPE}=${ChooseRepoType.WIDGET_SETTING}")
            }
    ) {
        Text(text = "使用仓库")
        Row {
            Text(
                text = model.repoName,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .widthIn(0.dp, 100.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                Icons.AutoMirrored.Outlined.ArrowRight,
                contentDescription = model.repoName,
            )
        }
    }

    ExpandableItem(title = "最大显示数量", endText = model.showNum.toString()) {
        SettingOption.maxShowNum.forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected =  model.showNum == it,
                    onClick = { viewModel.dispatch(SettingViewAction.ChoiceANewNum(model.appWidgetId, it)) }
                )
                Text(text = it.toString(), modifier = Modifier.padding(2.dp))
            }
        }
    }
    var labelTipText = ""
    for (label in model.filterLabels) {
        labelTipText += label.name + " "
    }
    ExpandableItem(title = "筛选标签", endText = labelTipText.ifBlank { "无" }) {
        existLabels.forEach {
            // 不能直接对比 label 对象，因为 label 中的某些数值（例如更新日期）可能会变化，所以使用 id 对比
            val isChecked = model.filterLabels.indexOfFirst { label -> label.id == it.id } != -1
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked ->
                        viewModel.dispatch(SettingViewAction.ChoiceANewLabel(model.appWidgetId, it, isChecked))
                    }
                )
                Text(text = it.name, color = "#${it.color}".toColor)
            }
        }
    }
    ExpandableItem(title = "筛选状态", endText = model.filterState) {
        SettingOption.availableState.forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    model.filterState == it.des,
                    onClick = { viewModel.dispatch(SettingViewAction.ChoiceANewState(model.appWidgetId, it.des)) }
                )
                Text(text = it.humanName, modifier = Modifier.padding(2.dp))
            }
        }
    }
}