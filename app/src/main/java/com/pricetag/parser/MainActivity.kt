package com.pricetag.parser

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.pricetag.parser.data.AppDatabase
import com.pricetag.parser.data.CsvExporter
import com.pricetag.parser.data.ParsedDraft
import com.pricetag.parser.data.RoomRepository
import com.pricetag.parser.ui.CameraScreen
import com.pricetag.parser.ui.ConfirmScreen
import com.pricetag.parser.ui.HistoryScreen
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

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

private val exportTimestampFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

@Composable
private fun PriceTagApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val appScope = rememberCoroutineScope()
    val db = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "price_tag.db")
            .build()
    }
    val repo = remember { RoomRepository(db.scanDao()) }

    var activeSessionId by remember { mutableStateOf<String?>(null) }
    var latestDraft by remember { mutableStateOf<ParsedDraft?>(null) }
    var exportStatus by remember { mutableStateOf<String?>(null) }
    var pendingCsv by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        repo.refresh()
        activeSessionId = repo.sessions().firstOrNull()?.id ?: repo.startSession().id
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri: Uri? ->
        val csv = pendingCsv
        if (uri == null || csv == null) {
            exportStatus = "Экспорт отменён"
            pendingCsv = null
            return@rememberLauncherForActivityResult
        }

        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(csv.toByteArray(Charsets.UTF_8))
            } ?: error("Не удалось открыть файл для записи")
        }
            .onSuccess {
                exportStatus = "CSV сохранён"
            }
            .onFailure { error ->
                exportStatus = "Ошибка экспорта: ${error.message}"
            }

        pendingCsv = null
    }

    fun triggerExport(csvText: String, suffix: String) {
        pendingCsv = csvText
        val fileName = "price_tag_${suffix}_${LocalDateTime.now().format(exportTimestampFormatter)}.csv"
        exportLauncher.launch(fileName)
    }

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
                                appScope.launch { repo.refresh() }
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
                        appScope.launch {
                            activeSessionId = repo.startSession().id
                        }
                    },
                    onDraftReady = { latestDraft = it },
                    activeSessionId = activeSessionId ?: "—",
                )
            }
            composable(AppRoute.Confirm.route) {
                ConfirmScreen(
                    draft = latestDraft,
                    onSave = { name, price, pricePerKg, weightVolume ->
                        activeSessionId?.let { sessionId ->
                            appScope.launch {
                                repo.addItem(
                                    sessionId = sessionId,
                                    productName = name,
                                    price = price,
                                    pricePerKg = pricePerKg,
                                    weightOrVolume = weightVolume,
                                )
                                navController.navigate(AppRoute.History.route)
                            }
                        }
                    },
                )
            }
            composable(AppRoute.History.route) {
                HistoryScreen(
                    items = repo.items(),
                    sessions = repo.sessions(),
                    onExportAll = {
                        val csv = CsvExporter.export(repo.itemsForSession(sessionId = null))
                        triggerExport(csv, "all")
                    },
                    onExportCurrentFilter = { sessionId ->
                        val csv = CsvExporter.export(repo.itemsForSession(sessionId = sessionId))
                        val suffix = sessionId?.take(8) ?: "all"
                        triggerExport(csv, suffix)
                    },
                    exportStatus = exportStatus,
                )
            }
        }
    }
}
