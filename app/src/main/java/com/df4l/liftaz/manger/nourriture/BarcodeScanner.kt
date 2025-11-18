package com.df4l.liftaz.manger.nourriture

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BarcodeScanner(
    appContext: Context
) {

    /**
     * From the docs: If you know which barcode formats you expect to read, you can improve the
     * speed of the barcode detector by configuring it to only detect those formats.
     */
    private val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_ALL_FORMATS
        )
        .build()

    private val scanner = GmsBarcodeScanning.getClient(appContext, options)

    suspend fun startScan(): String? = suspendCancellableCoroutine { cont ->
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                // On renvoie la valeur du barcode
                cont.resume(barcode.rawValue)
            }
            .addOnCanceledListener {
                // On renvoie null si l'utilisateur annule
                cont.resume(null)
            }
            .addOnFailureListener { e ->
                // On renvoie l'exception
                cont.resumeWithException(e)
            }
    }

}