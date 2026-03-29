package com.example.testtask.presentation.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.testtask.R

@Composable
fun RootContent(modifier: Modifier = Modifier) {
    val viewModel: RootViewModel = hiltViewModel()
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    when (destination) {
        RootDestination.Splash -> SplashScreen(modifier)
        RootDestination.Onboarding -> OnboardingScreen(
            modifier = modifier,
            onComplete = viewModel::completeOnboarding,
        )
        RootDestination.Main -> AppNavGraph(modifier)
    }
}

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var logoVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        logoVisible = true
    }
    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "splashLogoAlpha",
    )
    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.88f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "splashLogoScale",
    )
    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(R.drawable.splash).build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        AsyncImage(
            model = ImageRequest.Builder(context).data(R.drawable.logo).build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .size(500.dp)
                .graphicsLayer {
                    alpha = logoAlpha
                    scaleX = logoScale
                    scaleY = logoScale
                },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onComplete: () -> Unit,
) {
    val pages = listOf(
        "file:///android_asset/onboarding/hero.svg" to R.string.onboarding_page1_text,
        "file:///android_asset/onboarding/death_star.svg" to R.string.onboarding_page2_text,
        "file:///android_asset/onboarding/darth_vader.svg" to R.string.onboarding_page3_text,
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val context = LocalContext.current
    val yellow = colorResource(R.color.sw_yellow)
    val dotInactive = colorResource(R.color.onboarding_dot_inactive)
    val illustrationTint = colorResource(R.color.onboarding_illustration_tint)

    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(R.drawable.splash).build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(pages[page].first)
                        .decoderFactory(SvgDecoder.Factory())
                        .build(),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(illustrationTint),
                    modifier = Modifier.size(200.dp),
                )
                Text(
                    text = stringResource(pages[page].second),
                    color = yellow,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 28.dp),
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (pagerState.currentPage == pages.lastIndex) {
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = yellow,
                        contentColor = colorResource(R.color.black),
                    ),
                ) {
                    Text(stringResource(R.string.onboarding_continue))
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(pages.size) { index ->
                    val color = if (pagerState.currentPage == index) yellow else dotInactive
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                }
            }
        }
    }
}
