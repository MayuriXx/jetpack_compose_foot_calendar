package com.example.jetpack_compose_foot_calendar.ui.common

/**
 * Generic UI state holder used by all ViewModels in the application.
 *
 * Screens observe a `StateFlow<UiState<T>>` and render different content depending on which
 * subclass is active, providing a clean pattern for handling loading, success, and error states.
 *
 * @param T The type of data carried by the [Success] state.
 */
sealed class UiState<out T> {

    /** The data is being fetched. No data is available yet. */
    object Loading : UiState<Nothing>()

    /**
     * The data has been successfully loaded.
     *
     * @property data The loaded payload.
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * An error occurred while loading the data.
     *
     * @property message Human-readable error description, typically from the exception message.
     */
    data class Error(val message: String) : UiState<Nothing>()
}
