package de.proglove.example.intent.interfaces

import de.proglove.example.intent.enums.DeviceConnectionStatus

/**
 * Scanner device callback.
 */
interface IIntentScannerOutput {

    /**
     * The default scan feedback enabled setter/getter.
     */
    var defaultFeedbackEnabled: Boolean

    /**
     * A callback method, that is called only once for each scanned barcode.
     *
     * @param barcode The scanned barcode as a string representation, based on the currently configured scanner
     * settings.
     * @param symbology (Optional) Symbology of the scanned barcode.
     */
    fun onBarcodeScanned(barcode: String, symbology: String?)

    /**
     * A more detailed callback for the scanner status.
     *
     * To understand the current state (searching for devices, timeouts, etc) subscribe to this method.
     *
     * @param status The reported status of the device.
     */
    fun onScannerStateChanged(status: DeviceConnectionStatus)
}