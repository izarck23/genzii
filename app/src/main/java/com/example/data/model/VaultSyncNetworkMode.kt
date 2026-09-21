package com.example.data.model

/**
 * User-configurable network preference for syncing vault files with Firebase Firestore & Storage.
 * Helps users conserve cellular data by restricting heavy sync operations to Wi-Fi only.
 */
enum class VaultSyncNetworkMode(
    val id: String,
    val title: String,
    val subtitle: String
) {
    WIFI_ONLY(
        id = "WIFI_ONLY",
        title = "Wi-Fi only",
        subtitle = "Sync vault files only when connected to Wi-Fi to preserve mobile data"
    ),
    ALWAYS(
        id = "ALWAYS",
        title = "Always",
        subtitle = "Sync vault files continuously over both Wi-Fi and mobile data"
    );

    companion object {
        fun fromString(value: String?): VaultSyncNetworkMode {
            return when (value?.uppercase()) {
                ALWAYS.name, "ALWAYS" -> ALWAYS
                else -> WIFI_ONLY
            }
        }
    }
}
