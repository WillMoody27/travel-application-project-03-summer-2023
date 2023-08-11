package edu.msudenver.cs3013.project03.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.msudenver.cs3013.project03.prefs.PreferenceApplication
import edu.msudenver.cs3013.project03.prefs.PreferenceViewModel
import edu.msudenver.cs3013.project03.R

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var preferenceViewModel: PreferenceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Initialize PreferenceWrapper to save dark mode preference
        val preferenceWrapper =
            (this.application as PreferenceApplication).preferenceWrapper

        // Initialize ViewModel to save dark mode preference and observe changes to the dark mode preference in AccountFragment
        preferenceViewModel = ViewModelProvider(this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PreferenceViewModel(preferenceWrapper) as T
                }
            }).get(
            PreferenceViewModel::
            class.java
        )

        // Observe changes to the dark mode preference in AccountFragment and update the switch widget
        preferenceViewModel.saveDarkModeEnabled()
            .observe(this, Observer
            { isDarkModeEnabled ->
                // Set the bottom navigation background, text color, and icon tint color based on the dark mode preference
                bottomNavTheme(isDarkModeEnabled)
            })

        /*
         * TODO-Saved to Shared Preferences Requirement:
         *   Launch and activity if it is the first time the app is opened when launched on button
         *    click in IntroActivity and save isFirstTimeOpened preference to SharedPreferences
         *    set to false on clicking the button to prevent the IntroActivity from launching
         *    again on app launch
         */
        preferenceViewModel.saveFirstTimeOpened()
            .observe(this, Observer { isFirstTimeOpened ->
                if (isFirstTimeOpened) {
                    val intent = Intent(this, IntroActivity::class.java)
                    startActivity(intent)
                }
            })
        // Invoke function for bottom Navigation
        bottomNavigation()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment))
    }


    // TODO-d Requirement: Implement bottom navigation for the app
    private fun bottomNavigation() {

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.app_map, R.id.app_account
            )
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        findViewById<BottomNavigationView>(R.id.navigation_view)?.setupWithNavController(
            navController
        )
    }

    // TODO-7-27-23: Set the theme of the bottom navigation menu based on the dark mode preference
    private fun bottomNavTheme(isDarkModeEnabled: Boolean) {
        // Set the text color based on the dark mode preference
        val textColor = if (isDarkModeEnabled) Color.WHITE else Color.BLACK
        setTextViewColor(findViewById(R.id.drawer_layout), textColor)

        // Set the bottom navigation background, text color, and icon tint color based on the dark mode preference
        val backgroundColor =
            if (isDarkModeEnabled) Color.BLACK else Color.WHITE
        findViewById<View>(R.id.drawer_layout).setBackgroundColor(backgroundColor)
        val bottomNavigationView =
            findViewById<BottomNavigationView>(
                R.id.navigation_view
            )
        bottomNavigationView.setBackgroundColor(backgroundColor)
        bottomNavigationView.itemIconTintList =
            if (isDarkModeEnabled) getColorStateList(
                R.color.white
            ) else getColorStateList(R.color.black)
    }

    // TODO-7-27-23: Method to set the text color of all TextViews in the fragment - Create
    //  a Utils class and move this method to it and call it from the Utils class to reuse
    //  it in other fragments and activities
    //  - Method to set the text color of all TextViews in the fragment
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