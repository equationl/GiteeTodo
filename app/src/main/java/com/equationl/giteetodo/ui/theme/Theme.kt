package com.equationl.giteetodo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(
    //primary = Color.Red,
    //primaryVariant = Purple700,
    //secondary = Teal200
)

private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun GiteeTodoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    val systemUiCtrl = rememberSystemUiController()
    //systemUiCtrl.setStatusBarColor(colors.primary)
    //systemUiCtrl.setNavigationBarColor(colors.baseBackground)
    systemUiCtrl.setSystemBarsColor(colors.systemBar)

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@get:Composable
val Colors.baseBackground: Color
    get() = if (isLight) LightGray else DarkGray

@get:Composable
val Colors.systemBar: Color
    get() = if (isLight) LightColorPalette.primary else DarkColorPalette.primary
