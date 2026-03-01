package com.pricetag.parser.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.pricetag.parser.data.OcrDraftParser
import com.pricetag.parser.data.ParsedDraft
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    onOpenConfirm: () -> Unit,
    onStartNewSession: () -> Unit,
    onDraftReady: (ParsedDraft) -> Unit,
    activeSessionId: String,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var latestRecognizedText by remember { mutableStateOf<String?>(null) }
    var analysisRunning by remember { mutableStateOf(false) }
    var lastAnalyzedMs by remember { mutableLongStateOf(0L) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Live-сканирование", style = MaterialTheme.typography.headlineSmall)
        Text("Текущая сессия: ${activeSessionId.take(8)}…")

        if (hasCameraPermission) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .border(2.dp, Color.Green),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val executor = Executors.newSingleThreadExecutor()
                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val analysis = ImageAnalysis.Builder().build().also { analyzer ->
                            analyzer.setAnalyzer(executor) { imageProxy ->
                                val now = System.currentTimeMillis()
                                if (analysisRunning || now - lastAnalyzedMs < 1500L) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                val mediaImage = imageProxy.image
                                if (mediaImage == null) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                analysisRunning = true
                                val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                recognizer.process(input)
                                    .addOnSuccessListener { visionText ->
                                        val text = visionText.text.trim()
                                        if (text.isNotEmpty()) {
                                            latestRecognizedText = text
                                            onDraftReady(OcrDraftParser.fromRecognizedText(text))
                                        }
                                    }
                                    .addOnCompleteListener {
                                        lastAnalyzedMs = System.currentTimeMillis()
                                        analysisRunning = false
                                        imageProxy.close()
                                    }
                            }
                        }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis,
                        )
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .border(1.dp, Color.Gray),
                contentAlignment = Alignment.Center,
            ) {
                Text("Нужен доступ к камере")
            }
        }

        Text(
            text = "Последний OCR: ${latestRecognizedText?.take(120) ?: "—"}",
            style = MaterialTheme.typography.bodySmall,
        )

        Button(onClick = onOpenConfirm) {
            Text("Открыть экран подтверждения")
        }

        Button(onClick = onStartNewSession) {
            Text("Начать новую сессию")
        }
    }

    DisposableEffect(Unit) {
        onDispose { }
    }
}
