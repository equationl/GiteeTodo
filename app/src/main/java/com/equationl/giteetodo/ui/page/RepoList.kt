package com.equationl.giteetodo.ui.page

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.equationl.giteetodo.R
import com.equationl.giteetodo.data.user.model.response.Repo
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.ui.widgets.*
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.viewmodel.RepoListViewAction
import com.equationl.giteetodo.viewmodel.RepoListViewEvent
import com.equationl.giteetodo.viewmodel.RepoListViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private const val TAG = "el, RepoList"

@Composable
fun RepoListScreen(
    navController: NavHostController,
    viewModel: RepoListViewModel = hiltViewModel()
) {
    val activity = (LocalContext.current as? Activity)
    val viewState = viewModel.viewStates
    val scaffoldState = rememberScaffoldState()
    val coroutineState = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is RepoListViewEvent.ShowMessage -> {
                    coroutineState.launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                    }
                }
                is RepoListViewEvent.Goto -> {
                    navController.navigate(it.route)
                }
                is RepoListViewEvent.ShowDeleteRepoMsg -> {
                    coroutineState.launch {
                        when (scaffoldState.snackbarHostState.showSnackbar(message = it.message, actionLabel = "????????????")) {
                            SnackbarResult.Dismissed -> {  }
                            SnackbarResult.ActionPerformed -> {
                                uriHandler.openUri(it.deleteUrl)
                            }
                        }
                    }
                }
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar("????????????", actions = {
                    IconButton(onClick = {
                        navController.navigate(Route.REPO_DETAIL)
                    }) {
                        Icon(Icons.Outlined.LibraryAdd, "????????????")
                    }
                }) {
                    val lastQueue = navController.backQueue[navController.backQueue.size - 2]
                    if (lastQueue.destination.route?.contains(Route.HOME) == true) { // ????????????????????????????????????????????????????????????
                        navController.popBackStack()
                    }
                    else {
                        activity?.finish()
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                    Snackbar(snackbarData = snackBarData)
                }})
        {
            RepoListContent(viewState.repoFlow, viewModel, it)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RepoListContent(
    repoFlow: Flow<PagingData<Repo>>,
    viewModel: RepoListViewModel,
    paddingValues: PaddingValues
) {
    val repoList = repoFlow.collectAsLazyPagingItems()

    Log.i(TAG, "RepoListContent: loadState=${repoList.loadState}")

    if (repoList.loadState.refresh is LoadState.Error) {
        viewModel.dispatch(RepoListViewAction.SendMsg("???????????????"+ (repoList.loadState.refresh as LoadState.Error).error.message))
    }

    if (repoList.itemCount < 1) {
        if (repoList.loadState.refresh == LoadState.Loading) {
            LoadDataContent("??????????????????????????????")
        }
        else {
            ListEmptyContent("???????????????????????????????????????????????????????????????????????????") {
                repoList.refresh()
            }
        }
    }
    else {
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colors.baseBackground)) {
            LazyColumn {
                itemsIndexed(repoList, key = {_, item -> item.fullName}) { _, item ->
                    if (item != null && item.namespace.type == "personal") {  // ?????????????????????????????????
                        SwipeableActionCard(
                            mainCard = {
                                RepoItem(item) {
                                    viewModel.dispatch(RepoListViewAction.ChoiceARepo(it))
                                }
                            },
                            leftSwipeCard = {
                                Card(Modifier.padding(32.dp),
                                    shape = RoundedCornerShape(16.dp), elevation = 5.dp) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .background(MaterialTheme.colors.error),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text("??????????????????", color = MaterialTheme.colors.background)
                                        Icon(Icons.Filled.Delete, contentDescription = "??????", tint = MaterialTheme.colors.background)
                                    }
                                }
                            },
                            leftSwiped = {
                                viewModel.dispatch(RepoListViewAction.DeleteRepo(item.fullName))
                            }
                        )
                    }
                }

                item {
                    when (repoList.loadState.append) {
                        is LoadState.NotLoading -> {}
                        LoadState.Loading -> {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                Text("?????????")
                            }
                        }
                        is LoadState.Error -> {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                LinkText("???????????????????????????") {
                                    repoList.retry()
                                }
                                viewModel.dispatch(RepoListViewAction.SendMsg("???????????????"+ (repoList.loadState.append as LoadState.Error).error.toString()))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RepoItem(itemData: Repo, onClick: (path: String) -> Unit) {
    Card(onClick = { onClick.invoke(itemData.fullName) },
        modifier = Modifier.padding(32.dp), shape = RoundedCornerShape(16.dp), elevation = 5.dp) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)) {
                BlurImage(
                    paint = painterResource(R.drawable.bg2),  // ????????????????????????????????????????????????????????????????????????????????????????????????
                    contentDescription = "background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Column(modifier = Modifier
                    .fillMaxSize()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp), horizontalArrangement = Arrangement.Start) {
                        Text(text = itemData.path, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Row(
                        Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val text = if (itemData.openIssuesCount > 0) "${itemData.openIssuesCount}????????????" else "???????????????"
                        Text(text = text, color = Color.White)
                    }
                }
            }
            Row(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = itemData.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 4.dp))
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = Utils.getDateTimeString(itemData.createdAt), fontSize = 10.sp)
                }
            }

        }
    }
}