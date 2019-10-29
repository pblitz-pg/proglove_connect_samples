package de.proglove.example.sdk

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.proglove.sdk.ConnectionStatus
import de.proglove.sdk.IServiceOutput
import de.proglove.sdk.PgError
import de.proglove.sdk.PgManager
import de.proglove.sdk.button.ButtonPress
import de.proglove.sdk.button.IButtonOutput
import de.proglove.sdk.display.IDisplayOutput
import de.proglove.sdk.display.IPgSetScreenCallback
import de.proglove.sdk.display.PgScreenData
import de.proglove.sdk.display.PgTemplateField
import de.proglove.sdk.scanner.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.feedback_selection_layout.*
import kotlinx.android.synthetic.main.take_image_layout.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * PG SDK example for a scanner.
 */
class SdkActivity : AppCompatActivity(), IScannerOutput, IServiceOutput, IDisplayOutput, IButtonOutput {

    private val logger = Logger.getLogger("sample-logger")
    private val pgManager = PgManager(logger)

    private var serviceConnectionState = ServiceConnectionStatus.DISCONNECTED
    private var scannerConnected = false
    private var displayConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pgManager.subscribeToServiceEvents(this)
        pgManager.subscribeToScans(this)
        pgManager.subscribeToDisplayEvents(this)
        pgManager.subscribeToButtonPresses(this)

        serviceConnectBtn.setOnClickListener {
            pgManager.ensureConnectionToService(this.applicationContext)
            updateButtonStates()
        }

        connectScannerRegularBtn.setOnClickListener {
            if (scannerConnected) {
                pgManager.disconnectScanner()
            } else {
                pgManager.startPairing()
            }
        }

        connectScannerPinnedBtn.setOnClickListener {
            if (scannerConnected) {
                pgManager.disconnectScanner()
            } else {
                pgManager.startPairingFromPinnedActivity(this)
            }
        }

        triggerFeedbackButton.setOnClickListener {
            val selectedFeedbackId = getFeedbackId()
            pgManager.triggerFeedback(
                predefinedFeedback = selectedFeedbackId,
                callback = object : IPgFeedbackCallback {

                    override fun onSuccess() {
                        logger.log(Level.INFO, "Feedback successfully played.")
                    }

                    override fun onError(error: PgError) {
                        val errorMessage = "An Error occurred during triggerFeedback: $error"
                        logger.log(Level.WARNING, errorMessage)
                        runOnUiThread {
                            Toast.makeText(this@SdkActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
        // setting first Item as selected by default
        radioGroup.check(feedbackId1RB.id)

        // set image configurations
        setDefaultImageConfigurations()
        takeImageButton.setOnClickListener {
            takeImage()
        }

        defaultFeedbackSwitch.setOnClickListener {
            val config = PgScannerConfig(isDefaultScanAckEnabled = defaultFeedbackSwitch.isChecked)

            defaultFeedbackSwitch.isEnabled = false

            pgManager.setScannerConfig(config, object : IPgScannerConfigCallback {

                override fun onScannerConfigSuccess(config: PgScannerConfig) {
                    runOnUiThread {
                        logger.log(Level.INFO, "Successfully updated config on scanner")
                        defaultFeedbackSwitch.isEnabled = true
                    }
                }

                override fun onError(error: PgError) {
                    runOnUiThread {
                        val errorMessage = "Could not set config on scanner: $error"
                        logger.log(Level.WARNING, errorMessage)
                        Toast.makeText(this@SdkActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        // restore old state
                        defaultFeedbackSwitch.toggle()
                        defaultFeedbackSwitch.isEnabled = true
                    }
                }
            })
        }

        disconnectD3Btn.setOnClickListener {
            pgManager.disconnectDisplay()
        }

        val loggingCallback = object : IPgSetScreenCallback {

            override fun onError(error: PgError) {
                runOnUiThread {
                    Toast.makeText(this@SdkActivity, "Got error setting text: $error", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onSuccess() {
                runOnUiThread {
                    Toast.makeText(this@SdkActivity, "set screen successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }

        sendTestScreenD3Btn.setOnClickListener {
            pgManager.setScreen(
                data = PgScreenData(
                    "PG1", arrayOf(
                        PgTemplateField(1, "Bezeichnung", "Kopfairbag"),
                        PgTemplateField(2, "Fahrzeug-Typ", "Hatchback"),
                        PgTemplateField(3, "Teilenummer", "K867 86 027 H3")
                    )
                ),
                callback = loggingCallback
            )
        }

        sendTestScreenD3Btn2.setOnClickListener {
            pgManager.setScreen(
                data = PgScreenData(
                    "PG2", arrayOf(
                        PgTemplateField(1, "Stueck", "1 Pk"),
                        PgTemplateField(2, "Bezeichnung", "Gemuesemischung"),
                        PgTemplateField(3, "Stueck", "420"),
                        PgTemplateField(4, "Bezeichnung", "Fruechte Muesli"),
                        PgTemplateField(5, "Stueck", "30"),
                        PgTemplateField(6, "Bezeichnung", "Gebaeck-Stangen")
                    )
                ),
                callback = loggingCallback
            )
        }

        sendTestScreenD3BtnFailing.setOnClickListener {
            pgManager.setScreen(
                data = PgScreenData(
                    "PG1", arrayOf(
                        PgTemplateField(1, "now this is the story", "all about how"),
                        PgTemplateField(2, "my life got flipped", "turned upside down"),
                        PgTemplateField(3, "and I'd like to take", "a minute just sit right there"),
                        PgTemplateField(4, "I'll tell you how I become", "the prince of a town called Bel Air")
                    )
                ),
                callback = loggingCallback
            )
        }
    }

    private fun setDefaultImageConfigurations() {
        val imageConfig = PgImageConfig()
        jpegQualityEditText.setText(imageConfig.jpegQuality.toString())
        val defaultTimeout = DEFAULT_IMAGE_TIMEOUT
        timeoutEditText.setText(defaultTimeout.toString())
    }

    private fun takeImage() {
        var timeout = DEFAULT_IMAGE_TIMEOUT
        var quality = 20

        try {
            timeout = timeoutEditText.text.toString().toInt()
            quality = jpegQualityEditText.text.toString().toInt()
        } catch (e: NumberFormatException) {
            logger.log(Level.WARNING, "use positive numbers only")
        }

        val resolution = when (resolutionRadioGroup.checkedRadioButtonId) {
            R.id.highResolution -> ImageResolution.RESOLUTION_1280_960
            R.id.mediumResolution -> ImageResolution.RESOLUTION_640_480
            R.id.lowResolution -> ImageResolution.RESOLUTION_320_240
            else -> ImageResolution.values()[1]
        }

        val config = PgImageConfig(quality, resolution)
        val imageCallback = object : IPgImageCallback {
            override fun onImageReceived(image: PgImage) {
                val bmp = BitmapFactory.decodeByteArray(image.bytes, 0, image.bytes.size)
                runOnUiThread {
                    imageTaken.setImageBitmap(bmp)
                }
            }

            override fun onError(error: PgError) {
                runOnUiThread {
                    Toast.makeText(this@SdkActivity, "error code is $error", Toast.LENGTH_LONG).show()
                }
            }
        }
        pgManager.takeImage(config, timeout, imageCallback)
    }


    private fun getFeedbackId(): PgPredefinedFeedback {
        return when (radioGroup.checkedRadioButtonId) {
            feedbackId1RB.id -> PgPredefinedFeedback.SUCCESS
            feedbackId2RB.id -> PgPredefinedFeedback.ERROR
            feedbackId3RB.id -> PgPredefinedFeedback.SPECIAL_1
            else -> PgPredefinedFeedback.ERROR
        }
    }

    override fun onResume() {
        super.onResume()

        pgManager.ensureConnectionToService(this.applicationContext)
        updateButtonStates()
    }

    private fun updateButtonStates() {
        runOnUiThread {
            updateServiceConnectionButtonState()
            updateScannerConnectionButtonState()
            updateDisplayConnectionUiState()
        }
    }

    private fun updateDisplayConnectionUiState() {
        when {
            serviceConnectionState != ServiceConnectionStatus.CONNECTED -> displayStateOutput.setText(R.string.display_disconnected)
            displayConnected -> displayStateOutput.setText(R.string.display_connected)
            else -> displayStateOutput.setText(R.string.display_disconnected)
        }
    }

    private fun updateScannerConnectionButtonState() {
        when (serviceConnectionState) {
            ServiceConnectionStatus.CONNECTING -> {
                serviceConnectBtn.isEnabled = false
                serviceConnectBtn.setText(R.string.service_connecting)
            }
            ServiceConnectionStatus.CONNECTED -> {
                logger.log(Level.INFO, "Connection to ProGlove SDK Service successful.")

                serviceConnectBtn.isEnabled = false
                serviceConnectBtn.setText(R.string.service_connected)
            }
            ServiceConnectionStatus.DISCONNECTED -> {
                serviceConnectBtn.isEnabled = true
                serviceConnectBtn.setText(R.string.connect_service)
            }
        }
    }

    private fun updateServiceConnectionButtonState() {
        when (serviceConnectionState) {
            ServiceConnectionStatus.CONNECTING -> {
                serviceConnectBtn.isEnabled = false
                serviceConnectBtn.setText(R.string.service_connecting)

                connectScannerPinnedBtn.setText(R.string.pair_scanner)
                connectScannerRegularBtn.setText(R.string.pair_scanner)
            }
            ServiceConnectionStatus.CONNECTED -> {
                logger.log(Level.INFO, "Connection to ProGlove SDK Service successful.")

                serviceConnectBtn.isEnabled = false
                serviceConnectBtn.setText(R.string.service_connected)

                connectScannerPinnedBtn.setText(R.string.scanner_connected)
                connectScannerRegularBtn.setText(R.string.scanner_connected)
            }
            ServiceConnectionStatus.DISCONNECTED -> {
                serviceConnectBtn.isEnabled = true
                serviceConnectBtn.setText(R.string.connect_service)

                connectScannerPinnedBtn.setText(R.string.pair_scanner)
                connectScannerRegularBtn.setText(R.string.pair_scanner)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        pgManager.unsubscribeFromScans(this)
        pgManager.unsubscribeFromDisplayEvents(this)
        pgManager.unsubscribeFromServiceEvents(this)
        pgManager.unsubscribeFromButtonPresses(this)
    }

    /*
     * IServiceOutput Implementation BEGIN
     */

    override fun onServiceConnected() {
        runOnUiThread {
            serviceConnectionState = ServiceConnectionStatus.CONNECTED
            logger.log(Level.INFO, "serviceConnectionState: $serviceConnectionState")
            updateButtonStates()
        }
    }

    override fun onServiceDisconnected() {
        runOnUiThread {
            serviceConnectionState = ServiceConnectionStatus.DISCONNECTED
            logger.log(Level.INFO, "serviceConnectionState: $serviceConnectionState")
            updateButtonStates()
        }
    }

    /*
     * IServiceOutput Implementation END
     */

    /*
     * IScannerOutput Implementation:
     */

    override fun onBarcodeScanned(barcodeScanResults: BarcodeScanResults) {
        runOnUiThread {
            inputField.text = barcodeScanResults.barcodeContent
            barcodeScanResults.symbology?.let { symbology ->
                symbologyResult.text = symbology
                if (symbology.isNotEmpty()) {
                    Toast.makeText(
                        this,
                        "Got barcode: ${barcodeScanResults.barcodeContent} with symbology ${barcodeScanResults.symbology}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Got barcode: ${barcodeScanResults.barcodeContent} with no symbology",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onScannerConnected() {
        runOnUiThread {
            scannerConnected = true
            updateButtonStates()
        }
    }

    override fun onScannerDisconnected() {
        runOnUiThread {
            scannerConnected = false

            // Connecting a new scanner will reset this config to default, which is true
            defaultFeedbackSwitch.isChecked = true
            updateButtonStates()
        }
    }

    override fun onScannerStateChanged(status: ConnectionStatus) {
        runOnUiThread {
            Toast.makeText(this, "Scanner State: $status", Toast.LENGTH_SHORT).show()
        }
    }

    /*
     * End of IScannerOutput Implementation
     */

    /*
     * IDisplayOutput Implementation:
     */

    override fun onDisplayConnected() {
        Log.i("DISPLAY", "connected")
        displayConnected = true
        updateButtonStates()
    }

    override fun onDisplayDisconnected() {
        Log.i("DISPLAY", "disconnected")
        displayConnected = false
        updateButtonStates()
    }

    override fun onDisplayStateChanged(status: ConnectionStatus) {
        Log.i("DISPLAY", "newState: $status")
        runOnUiThread {
            Toast.makeText(this, "Display State: $status", Toast.LENGTH_SHORT).show()
        }
    }

    /*
     * End of IDisplayOutput Implementation
     */

    /*
     * IButtonOutput Implementation:
     */
    override fun onButtonPressed(buttonPressed: ButtonPress) {
        runOnUiThread {
            Toast.makeText(this, "Button Pressed: ${buttonPressed.id}", Toast.LENGTH_SHORT).show()
        }
    }
    /*
     * End of IButtonOutput Implementation
     */

    companion object {

        const val DEFAULT_IMAGE_TIMEOUT = 10000
    }
}

