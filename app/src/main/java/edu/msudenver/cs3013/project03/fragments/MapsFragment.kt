package edu.msudenver.cs3013.project03.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.msudenver.cs3013.project03.LocationAdapter
import edu.msudenver.cs3013.project03.LocationUiModel
import edu.msudenver.cs3013.project03.api.OverpassService
import edu.msudenver.cs3013.project03.R
import edu.msudenver.cs3013.project03.activities.TodoActivity
import edu.msudenver.cs3013.project03.api.OverpassResponse
import edu.msudenver.cs3013.project03.prefs.PreferenceViewModel
import edu.msudenver.cs3013.project03.sharedViewModel.LocationsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

private const val DEFAULT_LOCATION = "restaurant"
private const val DEFAULT_TYPE = "amenity"


class MapsFragment : Fragment(), OnMapReadyCallback {

    /*
    * TODO-complete:
    *  - Initialize the ActivityResultLauncher to request the location permission
    *  - Initialize requestPermissionLauncher to register for the result of the permission request
    *  - Initialize the GoogleMap variable mMap as nullable to hold a reference to the map
    *  - Initialize the marker variable as nullable to hold a reference to the pinpoint marker on the map
    *  - Initialize the fusedLocationProviderClient to get the user's location when the map is ready
    *  - Initialize the RecyclerView and the LocationAdapter
    * */
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var mMap: GoogleMap
    private var marker: Marker? = null
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
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

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationAdapter = LocationAdapter(
            layoutInflater,
            object : LocationAdapter.OnClickListener {
                override fun onItemClick(locationData: LocationUiModel) =
                    showLocationContent(locationData)
            }
        )
        recyclerView.adapter = locationAdapter // Set the adapter for the recycler view

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    getLoc()
                } else {
                    showPermissionRationale {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            }

        view.findViewById<MaterialButton>(R.id.listButton).setOnClickListener {



            Navigation.findNavController(view).navigate(R.id.action_app_map_to_locationListFragment)
        }
    }

    override fun onResume() {
        super.onResume()

        // Hide the bottom navigation bar when this fragment is resumed
        activity?.findViewById<BottomNavigationView>(R.id.navigation_view)?.visibility =
            View.VISIBLE
    }

    /*
    * TODO-Requirement:
    *  - Update Update your onMapReady(GoogleMap) function to set an OnMapClickListener event
    *  - on mMap, which will add a marker to the clicked location or move the existing marker to the clicked location
    *  - Invoke the addOrMoveSelectedPosIcon(latLng)() function from the onMapReady() function.
    *  - getLoc() function to retrieve the user’s location and update the map location and zoom level
    * */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap.apply {
            setOnMapClickListener { latLng ->
                marker?.position =
                    latLng // Add a marker to the clicked location or move the existing marker to the clicked location
            }
        }
        when {
            hasLocPermission() -> getLoc()
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            -> {
                showPermissionRationale {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }

            else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /*
* TODO-Completed:
*  - Add the showLocationContent(locationData) function to show the content of the location
*       when the user clicks on a location card in the recycler view
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

        mMap.setOnMyLocationButtonClickListener {
            getLoc()
            true
        }

        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    updateMapLocation(userLocation)

                    addUserMarkerAtLocation(userLocation, "You are here")
                    /*
                     * TODO-Requirement:
                     *  - Get the location from the observed LiveData in the LocationsViewModel
                     *    - Update the map location and zoom level based on selected location type.
                     *  - Fetch and display locations based on the user's location and the type selected.
                     */
                    locationsViewModel =
                        ViewModelProvider(requireActivity()).get(LocationsViewModel::class.java)
                    locationsViewModel.queryLocation.observe(viewLifecycleOwner) { selected ->
                        val type = when (selected) {
                            "restaurant", "bar" -> "amenity"
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
    *  - data is the query to get restaurants, attractions, museums, and other points of interest within
    *     a 0.1 degree radius of the user's location.
    *  - Use your Retrofit API service to make the Overpass API request
    *  - Add locations to list of restaurant locations
    * */
    private fun fetchAndDisplayLocations(
        currentLocation: LatLng,
        type: String,
        locationSelected: String
    ) {

        mMap.clear() // clear markers before adding new ones

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

                            // Add markers only if the zoom level is within the desired range
                            val markerOptions = MarkerOptions()
                                .position(userLocation)
                                .title(element.tags.name)
                                .snippet("Lat: ${element.lon}, Long: ${element.lat}")

                            // Set the icon based on the location type
                            queryIconSelected(locationSelected, markerOptions)
                            mMap.addMarker(markerOptions)

                            // Increment the counter to track the number of locations added to the map max = 10 locations
                            count++
                        }
                    }
                    // TODO-Completed: If you want to zoom the map to include all the restaurant markers, you can calculate the bounds
                    //  and use LatLngBounds.Builder to create a bounds object and then animate the camera.
                    if (mapLocations.isNotEmpty()) {
                        val builder = LatLngBounds.Builder()
                        for (location in mapLocations) {
                            builder.include(location)
                        }
                        val bounds = builder.build()
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                        // TODO-Completed: This is used to get the list of locations to display
                        //  to the user in the recycler view.
                        getLocationList(response)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying locations: ${e.message}")
            }
        }
    }

    // TODO: This function will determine the icon to use based on the location type (query from user)
    private fun queryIconSelected(locationSelected: String, markerOptions: MarkerOptions) {
        when (locationSelected) {
            "restaurant" -> markerOptions.icon(getBitmapDescriptorFromVector(R.drawable.food_icon))
            "hotel" -> markerOptions.icon(getBitmapDescriptorFromVector(R.drawable.hotel_icon))
            "bar" -> markerOptions.icon(getBitmapDescriptorFromVector(R.drawable.bar_icon))
            "museum" -> markerOptions.icon(getBitmapDescriptorFromVector(R.drawable.museum_icon))
            else -> markerOptions.icon(getBitmapDescriptorFromVector(R.drawable.point))
        }
    }


    /*
    * TODO-Requirement-Extra Credit:
    *  - Update the getLocationList() function to display the list of location response to the user within a test view
    *  - Update the locationList based on the locationResponse
    *  - The UiModel is used to display the location details (i.e. name, address, etc.) to the user in the recycler view
    *  - setData is used to update the locationAdapter with the locationUiList to display the location details to the user
    *   - Filter based on criteria, then display the locationResponse with locations that include a name, phone, and website (credible locations)
    * */
    private fun getLocationList(locationResponse: OverpassResponse) {
        var count = 0
        // Observe the selected currency outside of the map function
        preferenceViewModel =
            ViewModelProvider(requireActivity()).get(PreferenceViewModel::class.java)
        preferenceViewModel.getSelectedCurrency()
            .observe(viewLifecycleOwner) { selectedCurrency ->

                // TODO-Completed: This is the currency selected by the user that was saved in the shared preferences
                //  as Radio Button selection
                val currencySymbol = when (selectedCurrency) {
                    "USD" -> "$"
                    "EURO" -> "€"
                    "GBP" -> "£"
                    "JPY" -> "¥"
                    else -> "$"
                }

                // TODO-Completed: Update the locationAdapter with the locationUiList to display the location details to the user
                val locationUiList = locationResponse.elements.filter { element ->
                    !element.tags.name.isNullOrBlank() && !element.tags.phone.isNullOrBlank() || !element.tags.website.isNullOrBlank()
                }.map { element ->
                    val locationUiModel = LocationUiModel(
                        "${count + 1}",
                        if (!element.tags.name.isNullOrBlank()) "Location: ${element.tags.name}" else "N/A",
                        if (!element.tags.phone.isNullOrBlank()) "Phone: ${element.tags.phone}" else "N/A",
                        if (!element.tags.website.isNullOrBlank()) "Website: ${element.tags.website}" else "N/A",
                        "Currency: $currencySymbol"
                    )
                    count++
                    locationUiModel // Add the locationUiModel to the list after incrementing the count
                }
                locationAdapter.setData(locationUiList)
            }
    }


    /*
    * TODO-complete:
    *  - Add the addMarkerAtLocation(location, title, markerIcon) function to add a marker at the given location with the specified BitmapDescriptor
    *  - addMarkerAtLocation() function should allows the markerIcon to be null and use a default value of null
    *  - .title(title) and .position(location) are for the marker title and position
    *  - Update the design of the marker to use a custom icon and bitMapDescriptorFromVector() function
    *    - set color of marker for user's current location
    * */
    private fun addUserMarkerAtLocation(
        location: LatLng, title: String, markerIcon: BitmapDescriptor? = null
    ) = mMap.addMarker(
        MarkerOptions()
            .title(title)
            .position(location)
            .apply {
                markerIcon?.let {
                    icon(markerIcon)
                    icon(defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                }
            }
    )

   // TODO-Completed: Added updateMapLocation() function to update the map location and zoom level
    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 7f))
    }

    // TODO-Completed: Added showPermissionRationale() function to show a dialog to the user to explain why the app needs the location permission
    private fun showPermissionRationale(
        positiveAction: () -> Unit
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle("Location permission")
            .setMessage("We need your permission to find your current position")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                positiveAction()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }

    /*
    * TODO-Completed:
    *  Added hasLocPermission() function to check if the app has the location permission
    * */
    private fun hasLocPermission() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED

    // TODO-Completed: Added getBitmapDescriptorFromVector() function to convert a vector drawable to a BitmapDescriptor
    private fun getBitmapDescriptorFromVector(@DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor? {
        val bitmap =
            ContextCompat.getDrawable(requireContext(), vectorDrawableResourceId)
                ?.let { vectorDrawable ->
                    vectorDrawable
                        .setBounds(
                            0,
                            0,
                            vectorDrawable.intrinsicWidth,
                            vectorDrawable.intrinsicHeight
                        )

                    val drawableWithTint = DrawableCompat.wrap(vectorDrawable)

                    val bitmap = Bitmap.createBitmap(
                        vectorDrawable.intrinsicWidth,
                        vectorDrawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    drawableWithTint.draw(canvas)
                    bitmap
                } ?: return null

        return BitmapDescriptorFactory.fromBitmap(bitmap).also {
            // Bitmap is recycled to avoid a memory leak
            bitmap.recycle()
        }
    }
}
