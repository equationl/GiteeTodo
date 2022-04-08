package com.equationl.giteetodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equationl.giteetodo.data.repos.model.request.CreateIssues
import com.equationl.giteetodo.ui.theme.GiteeTodoTheme
import com.equationl.giteetodo.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GiteeTodoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    CreateIssues()
                }
            }
        }
    }
}

@Composable
fun CreateIssues() {
    val viewModel: MainViewModel = viewModel()
    Column {
        Button(onClick = {
            viewModel.createIssues("equation",
                CreateIssues(
                    "86c4fcc11047e066d3d1822616abd262",
                    "test2",
                    "233333"
                )
            )
        }) {
            Text(text = "新建")
        }
        Text(text = viewModel.issues?.toString() ?: "init NUll")
    }
}