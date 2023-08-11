package edu.msudenver.cs3013.project03.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import edu.msudenver.cs3013.project03.prefs.PreferenceApplication
import edu.msudenver.cs3013.project03.prefs.PreferenceViewModel
import edu.msudenver.cs3013.project03.R
import edu.msudenver.cs3013.project03.prefs.PreferenceWrapper
import edu.msudenver.cs3013.project03.sharedViewModel.LocationsViewModel

class TopMenuFragment : Fragment() {

    // TODO-d Requirement: Implement a preference fragment for the app that allows the user to save a preferences
    private lateinit var preferenceViewModel: PreferenceViewModel
    private lateinit var locationsViewModel: LocationsViewModel
    private lateinit var listOfButtons: List<MaterialButton>
    private lateinit var preferenceWrapper: PreferenceWrapper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_top_menu, container, false)

        // TODO-Completed: Initialize the ParkedViewModel to be used in the MapsFragment
        locationsViewModel =
            ViewModelProvider(requireActivity()).get(LocationsViewModel::class.java)

        // TODO-Completed: Initialize PreferenceWrapper  and PreferenceViewModel to save dark mode preference and observe changes to the dark mode preference in AccountFragment
        preferenceWrapper =
            (requireActivity().application as PreferenceApplication).preferenceWrapper

        preferenceViewModel = ViewModelProvider(this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PreferenceViewModel(preferenceWrapper) as T
                }
            }).get(
            PreferenceViewModel::
            class.java
        )

        /*
        * TODO-Completed:
        *   - Set a click listener on the list of buttons in the current view to use the text to
        *     update the location in the LocationsViewModel.
        *   - The text of the button is the location to be used in the MapsActivity
        * */
        listOfButtons = listOf(
            view.findViewById(R.id.button_1), // Restaurants (remove "s" from text for location query)
            view.findViewById(R.id.button_2), // Hotels (remove "s" from text for location query)
            view.findViewById(R.id.button_3), // Bars (remove "s" from text for location query)
            view.findViewById(R.id.button_4)  // Museums (remove "s" from text for location query)
        )

        listOfButtons.forEach { button ->
            button.setOnClickListener {
                val buttonText = button.text.toString()
                // TODO-Completed: Update the location in the LocationsViewModel.
                //  - drop last character "s" from the button text to get the location
                locationsViewModel.setLocationQuery(buttonText.lowercase().dropLast(1))
            }
        }

        // Observe changes to the dark mode preference in AccountFragment and update the switch widget
        preferenceViewModel.saveDarkModeEnabled()
            .observe(viewLifecycleOwner, Observer
            { isDarkModeEnabled ->
                /*
                * TODO-Completed:
                *   - Set the text color and background color based on the dark mode preference
                *   - set the stroke color of the material buttons based on the dark mode preference
                *       dark mode gets white stroke and light mode gets black stroke
                * */
                val textColor = if (isDarkModeEnabled) Color.WHITE else Color.BLACK
                setTextViewColor(view, textColor)

                val backgroundColor = if (isDarkModeEnabled) Color.BLACK else Color.WHITE
                view.findViewById<View>(R.id.general_layout).setBackgroundColor(backgroundColor)

                val strokeColor = if (isDarkModeEnabled) Color.WHITE else Color.BLACK
                setButtonStrokeColor(view, strokeColor)
            })
        return view
    }

    /*
    * TODO-Completed: setTextViewColor and setButtonStrokeColor are helper methods to set the text color of all TextViews in the fragment
    *  - Method to set the text color of all TextViews in the fragment
    *  - Method to set the stroke color of all MaterialButtons in the fragment
    *  -
    * */
    private fun setTextViewColor(view: View, textColor: Int) {
        if (view is ViewGroup) {
            val count = view.childCount
            for (i in 0 until count) {
                val child = view.getChildAt(i)
                if (child is TextView) {
                    child.setTextColor(textColor)
                } else if (child is ViewGroup) {
                    setTextViewColor(child, textColor)
                }
            }
        }
    }

    private fun setButtonStrokeColor(view: View, btnStrokeColor: Int) {
        if (view is ViewGroup) {
            view.children.forEach { child ->
                if (child is MaterialButton) {
                    child.strokeColor = ColorStateList.valueOf(btnStrokeColor)
                } else if (child is ViewGroup) {
                    setButtonStrokeColor(child, btnStrokeColor)
                }
            }
        }
    }
}
