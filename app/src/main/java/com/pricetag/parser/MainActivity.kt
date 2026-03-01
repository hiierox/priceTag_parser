package com.pricetag.parser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.pricetag.parser.data.AppDatabase
import com.pricetag.parser.data.ParsedDraft
import com.pricetag.parser.data.RoomRepository
import com.pricetag.parser.ui.CameraScreen
import com.pricetag.parser.ui.ConfirmScreen
import com.pricetag.parser.ui.HistoryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PriceTagApp()
        }
    }
}

private enum class AppRoute(val route: String, val label: String) {
    Camera("camera", "Сканирование"),
    Confirm("confirm", "Подтверждение"),
    History("history", "История"),
}

@Composable
private fun PriceTagApp() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "price_tag.db")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }
    val repo = remember { RoomRepository(db.scanDao()) }
    var activeSessionId by remember {
        mutableStateOf(
            repo.sessions().firstOrNull()?.id ?: repo.startSession().id,
        )
    }
    var latestDraft by remember { mutableStateOf<ParsedDraft?>(null) }

    Scaffold(
        bottomBar = {
            val backStack by navController.currentBackStackEntryAsState()
            val currentRoute = backStack?.destination?.route ?: AppRoute.Camera.route
            NavigationBar {
                AppRoute.entries.forEach { route ->
                    NavigationBarItem(
                        selected = currentRoute == route.route,
                        onClick = {
                            navController.navigate(route.route)
                            if (route == AppRoute.History) {
                                repo.refresh()
                            }
                        },
                        label = { Text(route.label) },
                        icon = {},
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Camera.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppRoute.Camera.route) {
                CameraScreen(
                    onOpenConfirm = { navController.navigate(AppRoute.Confirm.route) },
                    onStartNewSession = {
                        activeSessionId = repo.startSession().id
                    },
                    onDraftReady = { latestDraft = it },
                    activeSessionId = activeSessionId,
                )
            }
            composable(AppRoute.Confirm.route) {
                ConfirmScreen(
                    draft = latestDraft,
                    onSave = { name, price, pricePerKg, weightVolume ->
                        repo.addItem(
                            sessionId = activeSessionId,
                            productName = name,
                            price = price,
                            pricePerKg = pricePerKg,
                            weightOrVolume = weightVolume,
                        )
                        navController.navigate(AppRoute.History.route)
                    },
                )
            }
            composable(AppRoute.History.route) {
                HistoryScreen(items = repo.items(), sessions = repo.sessions())
            }
        }
    }
}
