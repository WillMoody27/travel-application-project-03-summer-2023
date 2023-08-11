package edu.msudenver.cs3013.project03.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import edu.msudenver.cs3013.project03.prefs.PreferenceApplication
import edu.msudenver.cs3013.project03.prefs.PreferenceViewModel
import edu.msudenver.cs3013.project03.R

class AccountFragment : Fragment() {

    private lateinit var preferenceViewModel: PreferenceViewModel
    private lateinit var switchWidget: Switch
    private lateinit var generalLayout: View
    private lateinit var listOfRadioButtons: List<RadioButton>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        val preferenceWrapper =
            (requireActivity().application as PreferenceApplication).preferenceWrapper

        preferenceViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PreferenceViewModel(preferenceWrapper) as T
            }
        }).get(PreferenceViewModel::class.java)

        // TODO-d: Initialize views from the layout
        switchWidget = view.findViewById(R.id.switch_widget)
        generalLayout = view.findViewById(R.id.general_layout)

        // TODO-d: Initialize the list of radio buttons
        listOfRadioButtons = listOf(
            view.findViewById(R.id.currency_radio_button_usd),
            view.findViewById(R.id.currency_radio_button_euro),
            view.findViewById(R.id.currency_radio_button_pound),
            view.findViewById(R.id.currency_radio_button_yen)
        )

        // Functions to setup the dark mode switch and currency radio buttons
        setupDarkModeSwitch(view)
        setupCurrencyRadioButtons()

        return view
    }

    /*
    * TODO-Completed: This function sets up the dark mode switch
    *  - Sets a click listener on the switch
    *  - Gets the state of the switch and saves it to the shared preferences
    *  - Gets the state of the switch from the shared preferences and sets the switch to checked
    *  - Sets the text color and background color of the layout
    *  - Calls the modeMessage function to display a toast message
    *  - Sets the status bar color
    *  - Calls the setTextViewColor function to set the text color of the text views
    *  - Calls the modeMessage function to display a toast message
    *  - Calls the preferenceViewModel.saveDarkModeEnabled function to save the dark mode enabled state
    *  - Observes the preferenceViewModel.saveDarkModeEnabled function to set the switch state
    *  - Calls the setTextViewColor function to set the text color of the text views
    *  - Sets the background color of the layout
    */
    private fun setupDarkModeSwitch(view: View) {
        // TODO-d: This function will be called when the switch is clicked and will save the state of the switch to the shared preferences
        switchWidget.setOnClickListener {
            val isDarkModeEnabled = switchWidget.isChecked
            preferenceViewModel.isDarkModeEnabled(isDarkModeEnabled)
            preferenceViewModel.saveDarkModeEnabled()

            val textColor = if (isDarkModeEnabled) Color.WHITE else Color.BLACK
            setTextViewColor(view, textColor)

            val backgroundColor = if (isDarkModeEnabled) Color.BLACK else Color.WHITE
            generalLayout.setBackgroundColor(backgroundColor)

            val window = requireActivity().window
            window.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.black)

            modeSelectedMessage(isDarkModeEnabled)
        }

        preferenceViewModel.saveDarkModeEnabled()
            .observe(viewLifecycleOwner, Observer { isDarkModeEnabled ->
                switchWidget.isChecked = isDarkModeEnabled
                val textColor = if (isDarkModeEnabled) Color.WHITE else Color.BLACK
                setTextViewColor(view, textColor)

                val backgroundColor = if (isDarkModeEnabled) Color.BLACK else Color.WHITE
                generalLayout.setBackgroundColor(backgroundColor)
            })
    }


    /*
    * TODO-Completed: This function sets up the currency radio buttons
    *  - Sets a click listener on each radio button
    *  - Gets the text from the radio button and saves it to the shared preferences
    *  - Gets the selected currency from the shared preferences and sets the radio button to checked
    *  - Sets the currency symbol in the text view
    * */
    private fun setupCurrencyRadioButtons() {
        // TODO-d: This function will be called when the radio button is clicked and will save the currency to the shared preferences
        listOfRadioButtons.forEach { radioButton ->
            radioButton.setOnClickListener {
                val currency = radioButton.text.toString()
                preferenceViewModel.saveCurrency(currency)
            }
        }
        preferenceViewModel.getSelectedCurrency()
            .observe(viewLifecycleOwner, Observer { selectedCurrency ->
                listOfRadioButtons.forEach { radioButton ->
                    if (radioButton.text.toString() == selectedCurrency) {
                        radioButton.isChecked = true
                    }
                }
            })
    }

    private fun modeSelectedMessage(isDarkModeEnabled: Boolean) {
        val message = "Dark Mode Preference: ${if (isDarkModeEnabled) "Enabled" else "Disabled"}"
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

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
}
