package com.tiagohenriques.project.timermasterlab

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.tiagohenriques.project.timermasterlab.resources.Res
import com.tiagohenriques.project.timermasterlab.resources.group_race_img
import com.tiagohenriques.project.timermasterlab.resources.time_trial_img
import com.tiagohenriques.project.timermasterlab.data.AppDataContainer
import com.tiagohenriques.project.timermasterlab.data.models.Type
import com.tiagohenriques.project.timermasterlab.platform.openExternalUrl
import com.tiagohenriques.project.timermasterlab.ui.ActiveRaceScreen
import com.tiagohenriques.project.timermasterlab.ui.SettingsScreen
import com.tiagohenriques.project.timermasterlab.ui.SessionHistoryScreen
import com.tiagohenriques.project.timermasterlab.ui.StartListEditorScreen
import com.tiagohenriques.project.timermasterlab.ui.theme.TimerMasterLabTheme
import kotlinx.coroutines.launch

@Composable
fun App() {
    val container = remember { AppDataContainer() }
    val isDarkTheme by container.userPreferencesRepository.isDarkTheme.collectAsState(initial = true)
    val isFileLoggingEnabled by container.userPreferencesRepository.isFileLoggingEnabled.collectAsState(initial = true)
    val scope = rememberCoroutineScope()
    val backStack = remember { mutableStateListOf(AppDestination.HOME) }
    var activeRaceMode by remember { androidx.compose.runtime.mutableStateOf(Type.TIME_TRIAL) }
    var settingsError by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val destination = backStack.last()

    fun navigate(target: AppDestination) {
        backStack.add(target)
    }

    fun navigateUp() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    LaunchedEffect(container) {
        container.userPreferencesRepository.logDiagnostic("KMP app started")
    }

    TimerMasterLabTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(
                topBar = {
                    SharedTopAppBar(
                        screenTitle = destination.title,
                        canNavigateBack = backStack.size > 1,
                        onNavigateBack = ::navigateUp,
                    )
                },
                containerColor = MaterialTheme.colorScheme.background,
            ) { contentPadding ->
                when (destination) {
                    AppDestination.HOME -> HomeScreen(
                        modifier = Modifier.fillMaxSize().padding(contentPadding),
                        onTimeTrial = { navigate(AppDestination.TIME_TRIAL) },
                        onGroupRace = { navigate(AppDestination.GROUP_RACE) },
                        onSettings = { navigate(AppDestination.SETTINGS) },
                    )
                    AppDestination.TIME_TRIAL -> ModeMenuScreen(
                        title = "Time Trial",
                        icon = Icons.Default.Timer,
                        image = Res.drawable.time_trial_img,
                        modifier = Modifier.fillMaxSize().padding(contentPadding),
                        onStartList = { navigate(AppDestination.TIME_TRIAL_START_LIST) },
                        onBeginRace = {
                            activeRaceMode = Type.TIME_TRIAL
                            navigate(AppDestination.TIME_TRIAL_RACE)
                        },
                        onResults = { navigate(AppDestination.TIME_TRIAL_RESULTS) },
                    )
                    AppDestination.GROUP_RACE -> ModeMenuScreen(
                        title = "Group Race",
                        icon = Icons.Default.Groups,
                        image = Res.drawable.group_race_img,
                        modifier = Modifier.fillMaxSize().padding(contentPadding),
                        onStartList = { navigate(AppDestination.GROUP_RACE_START_LIST) },
                        onBeginRace = {
                            activeRaceMode = Type.GROUP_RACE
                            navigate(AppDestination.GROUP_RACE_RACE)
                        },
                        onResults = { navigate(AppDestination.GROUP_RACE_RESULTS) },
                    )
                    AppDestination.TIME_TRIAL_START_LIST -> StartListEditorScreen(
                        mode = Type.TIME_TRIAL,
                        athletesRepository = container.athletesRepository,
                        startListRepository = container.startListEntryRepository,
                        onSaved = ::navigateUp,
                        modifier = Modifier.fillMaxSize().padding(16.dp).padding(contentPadding),
                    )
                    AppDestination.GROUP_RACE_START_LIST -> StartListEditorScreen(
                        mode = Type.GROUP_RACE,
                        athletesRepository = container.athletesRepository,
                        startListRepository = container.startListEntryRepository,
                        onSaved = ::navigateUp,
                        modifier = Modifier.fillMaxSize().padding(16.dp).padding(contentPadding),
                    )
                    AppDestination.TIME_TRIAL_RACE,
                    AppDestination.GROUP_RACE_RACE -> ActiveRaceScreen(
                        mode = activeRaceMode,
                        container = container,
                        onExit = ::navigateUp,
                        modifier = Modifier.fillMaxSize().padding(16.dp).padding(contentPadding),
                    )
                    AppDestination.TIME_TRIAL_RESULTS,
                    AppDestination.GROUP_RACE_RESULTS -> SessionHistoryScreen(
                        container = container,
                        modifier = Modifier.fillMaxSize().padding(16.dp).padding(contentPadding),
                    )
                    AppDestination.SETTINGS -> Column(
                        modifier = Modifier.fillMaxSize().padding(contentPadding),
                    ) {
                        SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { enabled ->
                                scope.launch {
                                    try {
                                        container.userPreferencesRepository.saveThemePreference(enabled)
                                        settingsError = null
                                    } catch (exception: Exception) {
                                        settingsError = exception.message
                                            ?: "Could not save theme setting."
                                    }
                                }
                            },
                            isFileLoggingEnabled = isFileLoggingEnabled,
                            onFileLoggingChange = { enabled ->
                                scope.launch {
                                    try {
                                        container.userPreferencesRepository.saveFileLoggingEnabled(enabled)
                                        settingsError = null
                                        container.userPreferencesRepository.logDiagnostic(
                                            "File logging ${if (enabled) "enabled" else "disabled"}",
                                        )
                                    } catch (exception: Exception) {
                                        settingsError = exception.message
                                            ?: "Could not save file logging setting."
                                    }
                                }
                            },
                            onPrivacyPolicyClick = {
                                try {
                                    openExternalUrl(
                                        "https://www.termsfeed.com/live/6afbc378-f1fa-4b51-8efc-f33599b2c992",
                                    )
                                    settingsError = null
                                } catch (exception: Exception) {
                                    settingsError = exception.message
                                        ?: "Could not open the privacy policy."
                                }
                            },
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        )
                        settingsError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SharedTopAppBar(
    screenTitle: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer Master Lab",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(30.dp),
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Timer Master Lab",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                    )
                    Text(
                        text = screenTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                        maxLines = 1,
                    )
                }
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ),
            )
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
    )
}

private data class HomeAction(
    val title: String,
    val icon: ImageVector,
    val startColor: Color,
    val endColor: Color,
    val onClick: () -> Unit,
)

@Composable
private fun HomeScreen(
    modifier: Modifier,
    onTimeTrial: () -> Unit,
    onGroupRace: () -> Unit,
    onSettings: () -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val actions = listOf(
            HomeAction(
                "Time Trial",
                Icons.Default.Timer,
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.primary,
                onTimeTrial,
            ),
            HomeAction(
                "Group Race",
                Icons.Default.Groups,
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.secondary,
                onGroupRace,
            ),
            HomeAction(
                "Settings",
                Icons.Default.Settings,
                MaterialTheme.colorScheme.tertiaryContainer,
                MaterialTheme.colorScheme.tertiary,
                onSettings,
            ),
        )
        LazyVerticalGrid(
            columns = if (maxWidth < 520.dp) GridCells.Fixed(1) else GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(actions) { action ->
                Card(
                    onClick = action.onClick,
                    modifier = Modifier.fillMaxWidth().height(84.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(action.startColor, action.endColor)))
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            action.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

private data class ModeAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
private fun ModeMenuScreen(
    title: String,
    icon: ImageVector,
    image: DrawableResource,
    modifier: Modifier,
    onStartList: () -> Unit,
    onBeginRace: () -> Unit,
    onResults: () -> Unit,
) {
    val actions = listOf(
        ModeAction("Start List", Icons.AutoMirrored.Filled.ListAlt, onStartList),
        ModeAction("Begin $title", Icons.Default.PlayArrow, onBeginRace),
        ModeAction("Sessions Results", Icons.Default.Analytics, onResults),
    )
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().height(132.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                            ),
                        ),
                )
                Row(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                    )
                }
            }
        }
        Text(
            text = "Actions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp),
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 12.dp, top = 4.dp),
        ) {
            items(actions.size) { index ->
                val action = actions[index]
                Card(
                    onClick = action.onClick,
                    modifier = Modifier.fillMaxWidth().height(68.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                action.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary,
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            action.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
        }
    }
}

private enum class AppDestination(val title: String) {
    HOME("Home"),
    TIME_TRIAL("Time Trial"),
    GROUP_RACE("Group Race"),
    TIME_TRIAL_START_LIST("Start List"),
    GROUP_RACE_START_LIST("Start List"),
    TIME_TRIAL_RACE("Begin Time Trial"),
    GROUP_RACE_RACE("Begin Group Race"),
    TIME_TRIAL_RESULTS("Sessions Results"),
    GROUP_RACE_RESULTS("Sessions Results"),
    SETTINGS("Settings"),
}
