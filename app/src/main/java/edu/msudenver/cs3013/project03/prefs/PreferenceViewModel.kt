package edu.msudenver.cs3013.project03.prefs

import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

// TODO-7-25-23: Implement the PreferenceViewModel class to store the preferenceWrapper
class PreferenceViewModel(private val preferenceWrapper: PreferenceWrapper) : ViewModel() {


    // TODO-7-25-23: Implement the fun isDarkModeEnabled() putBoolean() method to save the dark mode preference
    fun isDarkModeEnabled(boolean: Boolean) {
        preferenceWrapper.isDarkModeEnabled(boolean)
    }

    // TODO-7-25-23: Implement the saveDarkModeEnabled() method to save the dark mode preference using LiveData
    fun saveDarkModeEnabled(): LiveData<Boolean> {
        return preferenceWrapper.saveDarkModeEnabled()
    }

    // For determining the currency selected by the user radio button
    // Function to save the selected currency in SharedPreferences
    fun saveCurrency(currency: String) {
        preferenceWrapper.saveCurrency(currency)
    }

    // Function to get the previously selected currency from SharedPreferences
    fun getSelectedCurrency(): LiveData<String> {
        return preferenceWrapper.getSelectedCurrency()
    }

    // For launching an activity if it is the first time the app is opened
    fun isFirstTimeOpened(boolean: Boolean){
        preferenceWrapper.saveFirstTimeOpened()
    }

    // For launching an activity if it is the first time the app is opened
    fun saveFirstTimeOpened(): LiveData<Boolean> {
        return preferenceWrapper.isFirstTimeOpened()
    }


}