package com.example.testtask.presentation.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testtask.R
import com.example.testtask.presentation.common.StarWarsCard
import com.example.testtask.presentation.common.swTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailsScreen(
    uiState: CharacterDetailsUiState,
    onBackClick: () -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(uiState.details?.name ?: stringResource(R.string.details_title_fallback)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.details_back),
                        )
                    }
                },
                colors = swTopAppBarColors(),
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            uiState.details == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        uiState.errorMessage ?: stringResource(R.string.details_empty),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            else -> {
                val details = requireNotNull(uiState.details)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    details.localHeroDrawableRes()?.let { resId ->
                        item {
                            CharacterHeroPortrait(
                                drawableRes = resId,
                                contentDescription = stringResource(R.string.cd_character_portrait, details.name),
                            )
                        }
                    }
                    item { SectionTitle(stringResource(R.string.details_section_basic)) }
                    item { InfoCard(stringResource(R.string.details_label_birth_year), details.birthYear) }
                    item {
                        InfoCard(
                            stringResource(R.string.details_label_height),
                            stringResource(R.string.details_height_value, details.height),
                        )
                    }
                    item {
                        InfoCard(
                            stringResource(R.string.details_label_mass),
                            stringResource(R.string.details_mass_value, details.mass),
                        )
                    }
                    item { InfoCard(stringResource(R.string.details_label_gender), details.gender) }
                    item { SectionTitle(stringResource(R.string.details_section_species)) }
                    item {
                        InfoCard(
                            "",
                            details.speciesText ?: stringResource(R.string.details_no_species),
                        )
                    }
                    item { SectionTitle(stringResource(R.string.details_section_homeworld)) }
                    item {
                        InfoCard(
                            "",
                            details.homeworldName ?: stringResource(R.string.details_no_homeworld),
                        )
                    }
                    item { SectionTitle(stringResource(R.string.details_section_films)) }
                    if (details.films.isEmpty()) {
                        item { InfoCard("", stringResource(R.string.details_no_films)) }
                    } else {
                        items(details.films, key = { it.id }) { film ->
                            StarWarsCard {
                                Text(
                                    film.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    film.openingCrawl,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterHeroPortrait(
    drawableRes: Int,
    contentDescription: String,
) {
    val shape = RoundedCornerShape(12.dp)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .border(border, shape)
            .clip(shape),
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun InfoCard(label: String, value: String) {
    StarWarsCard {
        if (label.isNotBlank()) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
