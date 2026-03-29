package com.example.testtask.presentation.starships

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.example.testtask.R
import com.example.testtask.domain.model.Starship
import com.example.testtask.presentation.common.InfoBanner
import com.example.testtask.presentation.common.SkeletonCard
import com.example.testtask.presentation.common.StarWarsCard
import com.example.testtask.presentation.common.shouldLoadMore
import com.example.testtask.presentation.common.swOutlinedTextFieldColors
import com.example.testtask.presentation.common.swTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun StarshipsScreen(
    uiState: StarshipsUiState,
    onStarshipClick: (Int) -> Unit,
    onQueryChanged: (String) -> Unit,
    onOpenGlobalSearch: () -> Unit,
    onRefresh: () -> Unit,
    onLoadNextPage: () -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
    )
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.shouldLoadMore() }
            .collect { shouldLoadMore -> if (shouldLoadMore) onLoadNextPage() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.starships_title)) },
                actions = {
                    IconButton(onClick = onOpenGlobalSearch) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.cd_open_global_search),
                        )
                    }
                    if (uiState.isRefreshing) {
                        IconButton(onClick = {}, enabled = false) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                colors = swTopAppBarColors(),
            )
        },
    ) { innerPadding ->
        val bannerText = uiState.errorMessage ?: uiState.infoMessage
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(
                visible = bannerText != null,
                enter = fadeIn(animationSpec = tween(durationMillis = 250)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300)),
            ) {
                InfoBanner(text = bannerText.orEmpty())
            }
            uiState.lastUpdatedLabel?.let { updated ->
                Text(
                    updated,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (uiState.errorMessage != null) {
                TextButton(onClick = onRefresh) { Text(stringResource(R.string.retry_action)) }
            }

            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.starships_search_label)) },
                singleLine = true,
                colors = swOutlinedTextFieldColors(),
                trailingIcon = {
                    if (uiState.isRemoteSearchLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
            ) {
                when {
                    uiState.isLoading -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(8) { SkeletonCard() }
                        }
                    }

                    uiState.items.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                stringResource(R.string.starships_empty),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            uiState.infoMessage?.let {
                                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(uiState.items, key = { it.id }) { starship ->
                                StarshipCard(starship = starship, onClick = onStarshipClick)
                            }
                            if (uiState.isAppending) {
                                items(1) { SkeletonCard() }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StarshipCard(
    starship: Starship,
    onClick: (Int) -> Unit,
) {
    StarWarsCard(onClick = { onClick(starship.id) }) {
        Text(
            starship.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "${starship.model} • ${starship.starshipClass}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

