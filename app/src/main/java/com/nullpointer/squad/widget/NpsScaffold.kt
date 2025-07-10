package com.nullpointer.squad.widget

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp

@Composable
fun NpsScaffold(
    content: @Composable (PaddingValues) -> Unit,
    topAppBar: @Composable () -> Unit,
    floatingActionButton: (@Composable () -> Unit)? = null,
    containerColor: Color = Color.White,
    withBottomPadding: Boolean = true,
    ) {
    val modifier = if (!withBottomPadding) Modifier.statusBarsPadding()
    else Modifier
        .statusBarsPadding()
        .systemBarsPadding()
        .navigationBarsPadding()
    Scaffold(
        modifier = modifier,
        topBar = topAppBar,
        floatingActionButton = floatingActionButton ?: {},
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = containerColor,
        content = content,
    )
}