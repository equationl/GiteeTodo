package com.equationl.giteetodo.ui.page

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.equationl.giteetodo.R
import com.equationl.giteetodo.data.user.model.response.User
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.widgets.ListEmptyContent
import com.equationl.giteetodo.viewmodel.ProfileViewAction
import com.equationl.giteetodo.viewmodel.ProfileViewEvent
import com.equationl.giteetodo.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ProfileScreen(
    navHostController: NavHostController,
    scaffoldState: ScaffoldState,
    repoPath: String,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates
    val coroutineState = rememberCoroutineScope()

    DisposableEffect(Unit) {
        viewModel.dispatch(ProfileViewAction.ReadUserInfo)
        onDispose {  }
    }

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is ProfileViewEvent.ShowMessage) {
                coroutineState.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
            else if (it is ProfileViewEvent.Goto) {
                navHostController.navigate(it.route)
            }
        }
    }

    if (viewState.user != null) {
        Column(Modifier.fillMaxSize()) {
            ProFileContent(viewState.user, navHostController, repoPath, viewModel)
        }
    }
    else {
        ListEmptyContent(title = "?????????????????????????????????") {
            viewModel.dispatch(ProfileViewAction.ReadUserInfo)
        }
    }
}

@Composable
fun ProFileContent(user: User, navHostController: NavHostController, repoPath: String, viewModel: ProfileViewModel) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.avatarUrl)
                .placeholder(R.drawable.ic_unknown_user)
                .build(),
            contentDescription = "??????",
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
                .clip(CircleShape)
                .border(BorderStroke(1.dp, MaterialTheme.colors.primary), shape = CircleShape)
        )

        Text(user.name, modifier = Modifier.padding(top = 8.dp))

        ProfileInfoCard {
            ProfileInfoItem(
                icon = Icons.Filled.Person,
                iconDescription = "??????",
                title = "??????",
                content = "@"+user.login
            )

            ProfileInfoItem(
                icon = Icons.Filled.Email,
                iconDescription = "??????",
                title = "??????",
                content = if (user.email.isNullOrBlank()) "?????????" else user.email
            )

            ProfileInfoItem(
                icon = Icons.Filled.Description,
                iconDescription = "??????",
                title = "??????",
                content = if (user.bio.isNullOrBlank()) "?????????" else user.bio
            )
        }

        ProfileInfoCard {

            ProfileInfoItem(
                icon = Icons.Filled.Label,
                iconDescription = "????????????",
                title = "????????????"
            ) {
                navHostController.navigate("${Route.LABEL_MG}/${URLEncoder.encode(repoPath, StandardCharsets.UTF_8.toString())}")
            }

            Divider()

            ProfileInfoItem(
                icon = Icons.Filled.Info,
                iconDescription = "??????",
                title = "??????"
            ) {
                navHostController.navigate(Route.ABOUT)
            }

            Divider()

            ProfileInfoItem(
                icon = Icons.Filled.Settings,
                iconDescription = "??????",
                title = "??????"
            ) {
                navHostController.navigate(Route.SETTING)
            }
        }
    }
}

@Composable
fun ProfileInfoCard(content: @Composable () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp, 8.dp)
        .background(MaterialTheme.colors.background), shape = RoundedCornerShape(16.dp), elevation = 5.dp) {
        Column(Modifier.padding(4.dp)) {
            content.invoke()
        }
    }
}

@Composable
fun ProfileInfoItem(icon: ImageVector, iconDescription: String, title: String, content: String? = null, onClick: (() -> Unit)? = null) {
    var modifier = Modifier
        .fillMaxWidth()
        .padding(start = 32.dp, end = 8.dp, bottom = 4.dp, top = 4.dp)
    if (onClick != null) {
        modifier = modifier.clickable(onClick = onClick)
    }

    Row(modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = iconDescription)
            Text(title, modifier = Modifier.padding(start = 4.dp), fontWeight = FontWeight.Bold)
        }
        if (content != null) {
            Text(text = content, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
        }
    }
}