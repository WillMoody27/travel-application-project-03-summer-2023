package edu.msudenver.cs3013.project03.prefs

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import java.util.Currency

// The preferenceWrapper object is used to save the dark mode preference and observe changes to the dark mode preference in AccountFragment
class PreferenceWrapper (private val sharedPreferences: SharedPreferences) {

    // The isDarkModeEnabled object is a MutableLiveData object, this means that it can be updated
    private val isDarkModeEnabled = MutableLiveData<Boolean>()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            // The underscore is a placeholder for the sharedPreferences parameter
            //  - only being notified by the sharedPreferences object
            when (key) {
                THEME_KEY -> {
                    isDarkModeEnabled.postValue(sharedPreferences.getBoolean(THEME_KEY, false))
                }
            }
            when(key) {
                FIRST_TIME_KEY -> {
                    isDarkModeEnabled.postValue(sharedPreferences.getBoolean(FIRST_TIME_KEY, true))
                }
            }
        }
    }


    // TODO-7-25-23: Implement the fun isDarkModeEnabled() putBoolean() method to save the dark mode preference
    fun isDarkModeEnabled(boolean: Boolean) {
        sharedPreferences.edit()
            .putBoolean(THEME_KEY, boolean)
            .apply()
    }

    // TODO-7-25-23: Implement the saveDarkModeEnabled() method to save the dark mode preference
    fun saveDarkModeEnabled(): MutableLiveData<Boolean> {
        isDarkModeEnabled.postValue(sharedPreferences.getBoolean(THEME_KEY, false))
        return isDarkModeEnabled
    }

    // For determining the currency selected by the user radio button
    fun saveCurrency(currency: String) {
        sharedPreferences.edit()
            .putString(CURRENCY_KEY, currency)
            .apply()
    }

    // Function to get the previously selected currency from SharedPreferences
    fun getSelectedCurrency(): MutableLiveData<String> {
        val selectedCurrency = MutableLiveData<String>()
        selectedCurrency.postValue(sharedPreferences.getString(CURRENCY_KEY, "USD"))
        return selectedCurrency
    }

    // For launching an activity if it is the first time the app is opened
    fun isFirstTimeOpened(): MutableLiveData<Boolean> {
        val isFirstTimeOpened = MutableLiveData<Boolean>()
        isFirstTimeOpened.postValue(sharedPreferences.getBoolean(FIRST_TIME_KEY, true))
        return isFirstTimeOpened
    }

    // For launching an activity if it is the first time the app is opened
    fun saveFirstTimeOpened() {
        sharedPreferences.edit()
            .putBoolean(FIRST_TIME_KEY, false)
            .apply()
    }


    // TODO-7-25-23: Store the THEME_KEY in a companion object to be used in the PreferenceWrapper class
    companion object {
        private const val CURRENCY_KEY = "keyCurrency"
        private const val THEME_KEY = "keyTheme"
        private const val FIRST_TIME_KEY = "keyFirstTime"
    }
}

