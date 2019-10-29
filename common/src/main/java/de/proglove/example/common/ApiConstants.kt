package de.proglove.example.common

/**
 * These are the constants used by the intent API.
 */
object ApiConstants {

    const val ACTION_SCANNER_STATE_INTENT = "com.proglove.api.SCANNER_STATE"
    const val ACTION_BARCODE_INTENT = "com.proglove.api.BARCODE"
    const val ACTION_GET_STATE_INTENT = "com.proglove.api.GET_SCANNER_STATE"
    const val ACTION_FEEDBACK_PLAY_SEQUENCE_INTENT = "com.proglove.api.PLAY_FEEDBACK"
    const val ACTION_SCANNER_CONFIG = "com.proglove.api.CONFIG"
    const val ACTION_SCANNER_SET_CONFIG = "com.proglove.api.SET_CONFIG"

    const val ACTION_DISCONNECT_DISPLAY_INTENT = "com.proglove.api.DISPLAY_DISCONNECT"
    const val ACTION_GET_DISPLAY_STATE_INTENT = "com.proglove.api.GET_DISPLAY_STATE"
    const val ACTION_DISPLAY_STATE_INTENT = "com.proglove.api.DISPLAY_STATE"
    const val ACTION_BUTTON_PRESSED_INTENT = "com.proglove.api.DISPLAY_BUTTON"
    const val ACTION_SET_SCREEN_INTENT = "com.proglove.api.SET_DISPLAY_SCREEN"
    const val ACTION_SET_SCREEN_RESULT_INTENT = "com.proglove.api.SET_DISPLAY_SCREEN_RESULT"

    const val EXTRA_SCANNER_STATE = "com.proglove.api.extra.SCANNER_STATE"
    const val EXTRA_DATA_STRING_PG = "com.proglove.api.extra.BARCODE_DATA"
    const val EXTRA_SYMBOLOGY_STRING_PG = "com.proglove.api.extra.BARCODE_SYMBOLOGY"

    const val EXTRA_CONFIG_BUNDLE = "com.proglove.api.extra.CONFIG_BUNDLE"
    const val EXTRA_CONFIG_DISABLE_SCAN_FEEDBACK_STRING = "com.proglove.api.extra.config.DISABLE_SCAN_FEEDBACK"

    const val EXTRA_DISPLAY_TEMPLATE_ID = "com.proglove.api.extra.TEMPLATE_ID"
    const val EXTRA_DISPLAY_DATA = "com.proglove.api.extra.DATA"
    const val EXTRA_DISPLAY_SEPARATOR = "com.proglove.api.extra.SEPARATOR"
    const val EXTRA_DISPLAY_DURATION = "com.proglove.api.extra.DURATION"
    const val EXTRA_DISPLAY_STATE = "com.proglove.api.extra.DISPLAY_STATE"
    const val EXTRA_DISPLAY_BUTTON = "com.proglove.api.extra.DISPLAY_BUTTON"
    const val EXTRA_DISPLAY_DEVICE = "com.proglove.api.extra.DISPLAY_DEVICE_NAME"
    const val EXTRA_DISPLAY_SET_SCREEN_SUCCESS = "com.proglove.api.extra.DISPLAY_SET_SCREEN_SUCCESS"
    const val EXTRA_DISPLAY_SET_SCREEN_ERROR_TEXT = "com.proglove.api.extra.DISPLAY_SET_SCREEN_ERROR"
    const val EXTRA_FEEDBACK_SEQUENCE_ID = "com.proglove.api.extra.FEEDBACK_SEQUENCE_ID"
}