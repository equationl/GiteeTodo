package com.equationl.giteetodo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.equationl.giteetodo.R
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.ui.widgets.BlurImage

val testData = listOf(
    RepoItemData("equation/testRepo1", 10, "仓库1", "2022年04月22日 12:00"),
    RepoItemData("equation/kjhsadj", 0, "测试仓库", "2022年04月22日 12:00"),
    RepoItemData("equation/loasjdlihj_,lansdk", 1, "这是一个超标的仓库名字，我很长很长", "2022年04月22日 12:00"),
    RepoItemData("equation9498451541/98618151asdasdoliasndbiouabnsdiubasdbuasdb", 30, "今日待办", "2022年04月22日 12:00"),
    RepoItemData("equation/loasjdlihj_,lansdk45", 1, "这是我的待办", "2022年04月22日 12:00")
)

@Composable
fun RepoListScreen(navController: NavHostController) {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar("REPO LIST", actions = {
                    IconButton(onClick = {
                        /*TODO*/
                        navController.navigate(Route.REPO_DETAIL)
                    }) {
                        Icon(Icons.Outlined.LibraryAdd, "添加仓库")
                    }
                }) {
                    // TODO 点击返回
                    navController.popBackStack()
                }
            })
        {
            RepoListContent(testData)
        }
    }
}

@Composable
fun RepoListContent(repoList: List<RepoItemData>) {
    // TODO
    Column(
        Modifier
            .fillMaxSize()
            .background(baseBackground)) {
        LazyColumn {
            for (item in repoList) {
                item(key = item.path) {
                    RepoItem(item)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RepoItem(itemData: RepoItemData) {
    Card(onClick = { /*TODO*/ }, modifier = Modifier.padding(32.dp), shape = RoundedCornerShape(16.dp), elevation = 5.dp) {
        Column {

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)) {
                BlurImage(
                    paint = painterResource(id = R.drawable.bg2),
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
                        val text = if (itemData.notClosedCount > 0) "${itemData.notClosedCount}项未完成" else "已全部完成"
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
                    Text(text = itemData.createDate, fontSize = 10.sp)
                }
            }

        }
    }
}

data class RepoItemData(
    val path: String,
    val notClosedCount: Int,
    val name: String,
    val createDate: String
)

@Preview
@Composable
fun PreviewRepoList() {
    RepoListScreen(rememberNavController())
}