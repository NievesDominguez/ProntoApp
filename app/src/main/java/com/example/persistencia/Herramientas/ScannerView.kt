package com.example.persistencia.Herramientas

import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraScannerView(
    modifier: Modifier = Modifier,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // AndroidView permite usar vistas nativas dentro de Compose
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            // Vista donde se mostrará la cámara
            val previewView = PreviewView(ctx)

            // Obtener el proveedor de cámara (CameraX)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                // Comprobar si se tiene acceso a la cámara
                val cameraProvider = cameraProviderFuture.get()

                // Configurar la vista previa de la cámara
                val preview = Preview.Builder().build().also {
                    // Enlazar la preview con el PreviewView
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Cliente de ML Kit para leer códigos de barras
                val scanner = BarcodeScanning.getClient()

                // Configurar el análisis de imágenes frame por frame
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                // Analizador que procesa cada frame de la cámara
                analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                    val mediaImage = imageProxy.image

                    // Si hay imagen válida, la envía a ML Kit
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        // Procesar la imagen con ML Kit
                        scanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                // Si ML Kit detecta códigos, los recorre
                                for (barcode in barcodes) {
                                    barcode.rawValue?.let { value ->
                                        // Devolver el código detectado al composable padre
                                        onBarcodeDetected(value)
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                // Cerrar el frame para que CameraX siga funcionando
                                imageProxy.close()
                            }
                    }
                    else { imageProxy.close() }
                }

                // Selecciona la cámara trasera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Desvincular cualquier uso previo de la cámara
                cameraProvider.unbindAll()

                // Vincular la cámara a la vista previa y al análisis
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    analysis
                )

            }, ContextCompat.getMainExecutor(ctx))

            // Devolver la vista de la cámara
            previewView
        }
    )
}
