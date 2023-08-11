package edu.msudenver.cs3013.project03.fragments

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import edu.msudenver.cs3013.project03.LocationAdapter
import edu.msudenver.cs3013.project03.LocationUiModel
import edu.msudenver.cs3013.project03.R
import edu.msudenver.cs3013.project03.activities.TodoActivity
import edu.msudenver.cs3013.project03.api.OverpassResponse
import edu.msudenver.cs3013.project03.api.OverpassService
import edu.msudenver.cs3013.project03.prefs.PreferenceViewModel
import edu.msudenver.cs3013.project03.sharedViewModel.LocationsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val DEFAULT_LOCATION = "restaurant"
private const val DEFAULT_TYPE = "amenity"

class LocationListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var locationAdapter: LocationAdapter
    private lateinit var preferenceViewModel: PreferenceViewModel
    private lateinit var locationsViewModel: LocationsViewModel
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    // TODO-Requirement: Retrofit and Overpass API + MoshiConverterFactory + OverpassService
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://overpass-api.de/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
    private val service by lazy {
        retrofit.create(OverpassService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location_list, container, false)
    }


    // TODO-Completed: Hide the bottom navigation bar when this fragment is resumed
    override fun onResume() {
        super.onResume()

        // Hide the bottom navigation bar when this fragment is resumed
        activity?.findViewById<BottomNavigationView>(R.id.navigation_view)?.visibility =
            View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        * TODO-Completed:
        *  - Initialize the recycler view for the locations and update the orientation to horizontal
        *  - Obtain the SupportMapFragment to get notified when the map is ready to be used.
        *  - Create the location adapter with the layout inflater, image loader,
        *       and click listener for each location card in the recycler view.
        *  - requestPermissionLauncher will only be called with the result of the permission is granted
        * */

        recyclerView = view.findViewById(R.id.recycler_view_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        locationAdapter = LocationAdapter(
            layoutInflater,
            object : LocationAdapter.OnClickListener {
                override fun onItemClick(locationData: LocationUiModel) =
                    showLocationContent(locationData)
            }
        )
        recyclerView.adapter = locationAdapter // Set the adapter for the recycler view

        getLoc()

        view.findViewById<MaterialButton>(R.id.back_button).setOnClickListener() {
            Navigation.findNavController(view).navigate(R.id.action_locationListFragment_to_app_map)
        }

        // TODO-d: Swipe to save, delete, and undo locations from the recycler view in the LocationListFragment
        val itemTouchHelper = ItemTouchHelper(locationAdapter.swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    /*
* TODO-Completed:
*  - Add the showLocationContent(locationData) function to show the content of the location when the user clicks on a location card in the recycler view
* */
    private fun showLocationContent(locationData: LocationUiModel) {
        val args = Bundle().apply {
            putString("locationCount", locationData.count)
            putString("locationName", locationData.name)
            putString("locationPhone", locationData.phone)
            putString("locationWebsite", locationData.website)

        }
        Intent(requireContext(), TodoActivity::class.java).apply {
            putExtras(args)
            startActivity(this)
        }
    }

    /*
    * TODO-Requirement:
    *  - Update getLocation() function to retrieve the user’s location
    *  - fusedLocationProviderClient to get the user's location when the map is ready
    *  - success listener to update the map location and zoom level
    *  - userLocation is obtained from the location parameter in the success listener
    *  - updateMapLocation(userLocation) function to update the map location and zoom level
    *  - addMarkerAtLocation() function to add a marker at the user's location
    *  - fetchAndDisplayRestaurants() function to fetch and display restaurants based on user's location
    * */
    @SuppressLint("MissingPermission")
    private fun getLoc() {
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLocation = LatLng(location.latitude, location.longitude)

                    locationsViewModel =
                        ViewModelProvider(requireActivity()).get(LocationsViewModel::class.java)
                    locationsViewModel.queryLocation.observe(viewLifecycleOwner) { selected ->
                        val type = when (selected) {
                            "restaurant","bar" -> "amenity"
                            "hotel", "attraction", "museum" -> "tourism"
                            else -> "amenity"
                        }
                        fetchAndDisplayLocations(userLocation, type, selected)
                    }
                    fetchAndDisplayLocations(userLocation, DEFAULT_TYPE, DEFAULT_LOCATION)
                }
            }
    }

    /*
    * TODO-Requirement:
    *  - Update fetchAndDisplayRestaurants() function to fetch and display restaurants based on user's location
    *  - Query Overpass API to get restaurants data
    *  - data is the query to get restaurants, attractions, mueums, and other points of interest within
    *     a 0.1 degree radius of the user's location.
    *  - Use your Retrofit API service to make the Overpass API request
    *  - Add locations to list of restaurant locations
    * */
    private fun fetchAndDisplayLocations(
        currentLocation: LatLng,
        type: String,
        locationSelected: String
    ) {

        // TODO-Completed: Dynamically update the query based on the user's location and the type selected.
        val data = """
        [out:json];
        (
            node["$type"="$locationSelected"]
                (${currentLocation.latitude - 0.1},${currentLocation.longitude - 0.1},
                ${currentLocation.latitude + 0.1},${currentLocation.longitude + 0.1});
        );
        out;
    """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use your Retrofit API service to make the Overpass API request
                val response = service.getLocations(data)
                withContext(Dispatchers.Main) {
                    val mapLocations = mutableListOf<LatLng>()
                    var count = 0 // Counter to track the number of locations added to the map

                    if (response != null) {
                        for (element in response.elements) {
                            val userLocation = LatLng(element.lat, element.lon)
                            mapLocations.add(userLocation)

                            // Increment the counter to track the number of locations added to the map max = 10 locations
                            count++
                        }
                    }
                    getLocationList(response)

                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error querying locations: ${e.message}")
            }
        }
    }

    private fun getLocationList(locationResponse: OverpassResponse) {
        var count = 0
        // Observe the selected currency outside of the map function
        preferenceViewModel =
            ViewModelProvider(requireActivity()).get(PreferenceViewModel::class.java)
        preferenceViewModel.getSelectedCurrency()
            .observe(viewLifecycleOwner) { selectedCurrency ->

                // TODO-Completed: This is the currency selected by the user that was saved in the shared preferences as Radio Button selection
                val currencySymbol = when (selectedCurrency) {
                    "USD" -> "$"
                    "EURO" -> "€"
                    "GBP" -> "£"
                    "JPY" -> "¥"
                    else -> "$"
                }

                // TODO-Completed: Update the locationAdapter with the locationUiList to display the location details to the user
                val locationUiList = locationResponse.elements.filter { element ->
                    !element.tags.name.isNullOrBlank() && !element.tags.phone.isNullOrBlank() && !element.tags.website.isNullOrBlank()
                }.map { element ->
                    val locationUiModel = LocationUiModel(
                        "${count + 1}",
                        if (!element.tags.name.isNullOrBlank()) "Location: ${element.tags.name}" else "",
                        if (!element.tags.phone.isNullOrBlank()) "Phone: ${element.tags.phone}" else "",
                        if (!element.tags.website.isNullOrBlank()) "Website: ${element.tags.website}" else "",
                        "Currency: $currencySymbol"
                    )
                    count++
                    locationUiModel // Add the locationUiModel to the list after incrementing the count
                }
                locationAdapter.setData(locationUiList)
            }
    }
}