package com.roadmemo.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.roadmemo.app.R
import com.roadmemo.app.ui.theme.RoadMemoBackground

@Composable
fun RoadMemoLaunchScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RoadMemoBackground),
    ) {
        Image(
            painter = painterResource(R.drawable.roadmemo_splash_image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
    }
}
