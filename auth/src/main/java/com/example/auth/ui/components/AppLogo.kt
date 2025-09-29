package com.example.auth.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import com.example.auth.R

@Composable
fun appLogo() {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "App Logo",
        modifier = Modifier.size(dimensionResource(R.dimen.appLogoSize)),
        contentScale = ContentScale.Fit, // keeps aspect ratio and avoids cropping
    )
}