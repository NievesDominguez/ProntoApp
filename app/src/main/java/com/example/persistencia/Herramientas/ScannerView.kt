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

            // Obtenemos el proveedor de cámara (CameraX)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                // Ya tenemos acceso a la cámara
                val cameraProvider = cameraProviderFuture.get()

                // Configuramos la vista previa de la cámara
                val preview = Preview.Builder().build().also {
                    // Enlazamos la preview con el PreviewView
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Cliente de ML Kit para leer códigos de barras
                val scanner = BarcodeScanning.getClient()

                // Configuramos el análisis de imágenes (frame por frame)
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                // Analizador que procesa cada frame de la cámara
                analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                    val mediaImage = imageProxy.image

                    // Si hay imagen válida, la enviamos a ML Kit
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        // Procesamos la imagen con ML Kit
                        scanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                // Si ML Kit detecta códigos, los recorremos
                                for (barcode in barcodes) {
                                    barcode.rawValue?.let { value ->
                                        // Devolvemos el código detectado al Composable padre
                                        onBarcodeDetected(value)
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                // Cerramos el frame para que CameraX siga funcionando
                                imageProxy.close()
                            }
                    }
                    else { imageProxy.close() }
                }

                // Seleccionamos la cámara trasera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Desvinculamos cualquier uso previo de la cámara
                cameraProvider.unbindAll()

                // Vinculamos la cámara a la vista previa y al análisis
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    analysis
                )

            }, ContextCompat.getMainExecutor(ctx))

            // Devolvemos la vista de la cámara
            previewView
        }
    )
}
