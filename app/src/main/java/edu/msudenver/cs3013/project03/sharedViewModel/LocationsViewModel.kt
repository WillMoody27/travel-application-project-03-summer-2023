package edu.msudenver.cs3013.project03.sharedViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationsViewModel : ViewModel() {

    // Used to communicate between fragments (TopMenuFragment and MapsFragment to query the location results)
    private val _Location = MutableLiveData<String>()

    val queryLocation: LiveData<String>
        get() = _Location

    fun setLocationQuery(location: String) {
        _Location.value = location
    }
}

