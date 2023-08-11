package edu.msudenver.cs3013.project03.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import edu.msudenver.cs3013.project03.prefs.PreferenceApplication
import edu.msudenver.cs3013.project03.prefs.PreferenceViewModel
import edu.msudenver.cs3013.project03.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        /*
        * TODO-d Requirement: Implement a splash screen for the app this screen should only show
        *   if the app is opened for the first time
        * */
        findViewById<MaterialButton>(R.id.IntroButton).setOnClickListener {
            val preferenceWrapper =
                (application as PreferenceApplication).preferenceWrapper
            val preferenceViewModel = PreferenceViewModel(preferenceWrapper)
            preferenceViewModel.isFirstTimeOpened(false)

            finish()
        }
    }
}