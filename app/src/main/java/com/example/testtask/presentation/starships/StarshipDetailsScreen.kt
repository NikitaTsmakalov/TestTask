package com.example.testtask.presentation.starships

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testtask.R
import com.example.testtask.presentation.common.StarWarsCard
import com.example.testtask.presentation.common.swTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarshipDetailsScreen(
    uiState: StarshipDetailsUiState,
    onBackClick: () -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(uiState.details?.name ?: stringResource(R.string.starship_details_title_fallback)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.details_back_generic),
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionTitle(stringResource(R.string.starship_section_basic))
                    InfoCard(stringResource(R.string.starship_label_model), details.model)
                    InfoCard(stringResource(R.string.starship_label_manufacturer), details.manufacturer)
                    InfoCard(stringResource(R.string.starship_label_cost), details.costInCredits)
                    InfoCard(stringResource(R.string.starship_label_length), details.length)
                    InfoCard(stringResource(R.string.starship_label_crew), details.crew)
                    InfoCard(stringResource(R.string.starship_label_passengers), details.passengers)
                    InfoCard(stringResource(R.string.starship_label_class), details.starshipClass)
                }
            }
        }
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
