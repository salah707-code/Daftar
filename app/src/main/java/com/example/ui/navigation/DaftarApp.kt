package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.DaftarTheme
import com.example.ui.viewmodel.DaftarViewModel

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    CUSTOMERS("العملاء", Icons.Filled.People, Icons.Outlined.People),
    ALERTS("التنبيهات", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    HISTORY("التاريخ", Icons.Filled.History, Icons.Outlined.History),
    REPORTS("التقارير", Icons.Filled.Assessment, Icons.Outlined.Assessment),
    SETTINGS("الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings)
}

sealed class Screen {
    object Main : Screen()
    data class CustomerDetail(val customerId: Long) : Screen()
    data class AddEditCustomer(val customerId: Long?) : Screen()
    object Showcase : Screen()
}

@Composable
fun DaftarApp(
    viewModel: DaftarViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val isUnlocked by viewModel.isAppUnlocked.collectAsState()
    val unreadAlertsCount by viewModel.unreadAlertsCount.collectAsState(initial = 0)

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    var currentTab by remember { mutableStateOf(MainTab.CUSTOMERS) }

    // Always enforce Arabic RTL layout direction for the entire app
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        DaftarTheme(
            themeMode = settings.themeMode,
            colorTheme = settings.colorTheme
        ) {
            if (settings.isLockEnabled && !isUnlocked) {
                LockScreen(viewModel = viewModel)
            } else {
                Scaffold(
                    modifier = modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentScreen == Screen.Main) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 3.dp,
                                modifier = Modifier.navigationBarsPadding()
                            ) {
                                MainTab.values().forEach { tab ->
                                    val isSelected = currentTab == tab
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentTab = tab },
                                        icon = {
                                            if (tab == MainTab.ALERTS && unreadAlertsCount > 0) {
                                                BadgedBox(
                                                    badge = {
                                                        Badge {
                                                            Text(
                                                                text = unreadAlertsCount.toString(),
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                        contentDescription = tab.title
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = tab.title
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = tab.title,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (val screen = currentScreen) {
                            Screen.Main -> {
                                when (currentTab) {
                                    MainTab.CUSTOMERS -> CustomersScreen(
                                        viewModel = viewModel,
                                        onNavigateToAddCustomer = {
                                            currentScreen = Screen.AddEditCustomer(null)
                                        },
                                        onNavigateToCustomerDetail = { id ->
                                            currentScreen = Screen.CustomerDetail(id)
                                        },
                                        onNavigateToEditCustomer = { id ->
                                            currentScreen = Screen.AddEditCustomer(id)
                                        },
                                        onOpenShowcase = {
                                            currentScreen = Screen.Showcase
                                        }
                                    )
                                    MainTab.ALERTS -> AlertsScreen(
                                        viewModel = viewModel,
                                        onNavigateToCustomer = { id ->
                                            currentScreen = Screen.CustomerDetail(id)
                                        }
                                    )
                                    MainTab.HISTORY -> HistoryScreen(
                                        viewModel = viewModel,
                                        onNavigateToCustomer = { id ->
                                            currentScreen = Screen.CustomerDetail(id)
                                        }
                                    )
                                    MainTab.REPORTS -> ReportsScreen(
                                        viewModel = viewModel,
                                        onNavigateToCustomer = { id ->
                                            currentScreen = Screen.CustomerDetail(id)
                                        }
                                    )
                                    MainTab.SETTINGS -> SettingsScreen(
                                        viewModel = viewModel,
                                        onOpenShowcase = {
                                            currentScreen = Screen.Showcase
                                        }
                                    )
                                }
                            }
                            is Screen.CustomerDetail -> CustomerDetailScreen(
                                customerId = screen.customerId,
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = Screen.Main },
                                onNavigateToEdit = { id ->
                                    currentScreen = Screen.AddEditCustomer(id)
                                }
                            )
                            is Screen.AddEditCustomer -> AddEditCustomerScreen(
                                customerId = screen.customerId,
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = Screen.Main }
                            )
                            Screen.Showcase -> ShowcaseScreen(
                                onNavigateBack = { currentScreen = Screen.Main }
                            )
                        }
                    }
                }
            }
        }
    }
}
