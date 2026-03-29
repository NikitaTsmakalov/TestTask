package com.example.testtask.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.testtask.R
import com.example.testtask.presentation.common.StarfieldBackground
import com.example.testtask.presentation.characters.CharactersScreen
import com.example.testtask.presentation.characters.CharactersViewModel
import com.example.testtask.presentation.details.CharacterDetailsScreen
import com.example.testtask.presentation.details.CharacterDetailsViewModel
import com.example.testtask.presentation.planets.PlanetDetailsScreen
import com.example.testtask.presentation.planets.PlanetDetailsViewModel
import com.example.testtask.presentation.planets.PlanetsScreen
import com.example.testtask.presentation.planets.PlanetsViewModel
import com.example.testtask.presentation.starships.StarshipDetailsScreen
import com.example.testtask.presentation.starships.StarshipDetailsViewModel
import com.example.testtask.presentation.starships.StarshipsScreen
import com.example.testtask.presentation.starships.StarshipsViewModel
import com.example.testtask.presentation.search.GlobalSearchScreen
import com.example.testtask.presentation.search.GlobalSearchViewModel
import com.example.testtask.presentation.recent.RecentViewsScreen
import com.example.testtask.presentation.recent.RecentViewsViewModel

private const val CHARACTERS_ROUTE = "characters"
private const val CHARACTER_ROUTE = "character/{characterId}"
private const val PLANETS_ROUTE = "planets"
private const val PLANET_ROUTE = "planet/{planetId}"
private const val STARSHIPS_ROUTE = "starships"
private const val STARSHIP_ROUTE = "starship/{starshipId}"
private const val SEARCH_ROUTE = "search"
private const val RECENT_ROUTE = "recent"

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in setOf(
        CHARACTERS_ROUTE,
        PLANETS_ROUTE,
        STARSHIPS_ROUTE,
        RECENT_ROUTE,
    )

    val navBarColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Box(modifier = modifier.fillMaxSize()) {
        StarfieldBackground()
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = Color(0xCC0A0A12),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        windowInsets = NavigationBarDefaults.windowInsets,
                    ) {
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == CHARACTERS_ROUTE } == true,
                            onClick = { navController.navigate(CHARACTERS_ROUTE) { launchSingleTop = true; popUpTo(CHARACTERS_ROUTE) { inclusive = false } } },
                            icon = { Icon(Icons.Default.Groups, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_characters)) },
                            colors = navBarColors,
                        )
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == PLANETS_ROUTE } == true,
                            onClick = { navController.navigate(PLANETS_ROUTE) { launchSingleTop = true; popUpTo(CHARACTERS_ROUTE) { inclusive = false } } },
                            icon = { Icon(Icons.Default.Public, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_planets)) },
                            colors = navBarColors,
                        )
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == STARSHIPS_ROUTE } == true,
                            onClick = { navController.navigate(STARSHIPS_ROUTE) { launchSingleTop = true; popUpTo(CHARACTERS_ROUTE) { inclusive = false } } },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.starship),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                            label = { Text(stringResource(R.string.tab_starships)) },
                            colors = navBarColors,
                        )
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == RECENT_ROUTE } == true,
                            onClick = { navController.navigate(RECENT_ROUTE) { launchSingleTop = true; popUpTo(CHARACTERS_ROUTE) { inclusive = false } } },
                            icon = { Icon(Icons.Default.History, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_recent)) },
                            colors = navBarColors,
                        )
                    }
                }
            },
        ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CHARACTERS_ROUTE,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(CHARACTERS_ROUTE) {
                val viewModel: CharactersViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                CharactersScreen(
                    uiState = uiState,
                    onCharacterClick = { id -> navController.navigate("character/$id") },
                    onQueryChanged = viewModel::onQueryChanged,
                    onOpenGlobalSearch = {
                        navController.navigate(SEARCH_ROUTE) { launchSingleTop = true }
                    },
                    onRefresh = viewModel::refresh,
                    onLoadNextPage = viewModel::loadNextPage,
                )
            }

            composable(PLANETS_ROUTE) {
                val viewModel: PlanetsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                PlanetsScreen(
                    uiState = uiState,
                    onPlanetClick = { id -> navController.navigate("planet/$id") },
                    onQueryChanged = viewModel::onQueryChanged,
                    onOpenGlobalSearch = {
                        navController.navigate(SEARCH_ROUTE) { launchSingleTop = true }
                    },
                    onRefresh = viewModel::refresh,
                    onLoadNextPage = viewModel::loadNextPage,
                )
            }

            composable(STARSHIPS_ROUTE) {
                val viewModel: StarshipsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                StarshipsScreen(
                    uiState = uiState,
                    onStarshipClick = { id -> navController.navigate("starship/$id") },
                    onQueryChanged = viewModel::onQueryChanged,
                    onOpenGlobalSearch = {
                        navController.navigate(SEARCH_ROUTE) { launchSingleTop = true }
                    },
                    onRefresh = viewModel::refresh,
                    onLoadNextPage = viewModel::loadNextPage,
                )
            }

            composable(SEARCH_ROUTE) {
                val viewModel: GlobalSearchViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                GlobalSearchScreen(
                    uiState = uiState,
                    onQueryChanged = viewModel::onQueryChanged,
                    onItemClick = { item ->
                        when (item.type) {
                            "character" -> navController.navigate("character/${item.id}")
                            "planet" -> navController.navigate("planet/${item.id}")
                            "starship" -> navController.navigate("starship/${item.id}")
                        }
                    },
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable(RECENT_ROUTE) {
                val viewModel: RecentViewsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                RecentViewsScreen(
                    uiState = uiState,
                    onItemClick = { item ->
                        when (item.type) {
                            "character" -> navController.navigate("character/${item.itemId}")
                            "planet" -> navController.navigate("planet/${item.itemId}")
                            "starship" -> navController.navigate("starship/${item.itemId}")
                        }
                    },
                )
            }

            composable(
                route = CHARACTER_ROUTE,
                arguments = listOf(navArgument("characterId") { type = NavType.IntType }),
            ) {
                val viewModel: CharacterDetailsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                CharacterDetailsScreen(uiState = uiState, onBackClick = navController::popBackStack)
            }

            composable(
                route = PLANET_ROUTE,
                arguments = listOf(navArgument("planetId") { type = NavType.IntType }),
            ) {
                val viewModel: PlanetDetailsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                PlanetDetailsScreen(uiState = uiState, onBackClick = navController::popBackStack)
            }

            composable(
                route = STARSHIP_ROUTE,
                arguments = listOf(navArgument("starshipId") { type = NavType.IntType }),
            ) {
                val viewModel: StarshipDetailsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                StarshipDetailsScreen(uiState = uiState, onBackClick = navController::popBackStack)
            }
        }
        }
    }
}
