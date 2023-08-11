package edu.msudenver.cs3013.project03.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import edu.msudenver.cs3013.project03.R

class TodoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        getLocationDetails()

        // Finish the TodoActivity when the button is clicked
        findViewById<MaterialButton>(R.id.back_button).setOnClickListener() {
            finish()
        }
    }

    // TODO-Completed: Get the bundle from the MapsFragment and display the location details
    private fun getLocationDetails() {
        val args = this.intent.extras
        val count = args?.getString("locationCount")
        val name = args?.getString("locationName")
        val phone = args?.getString("locationPhone")
        val website = args?.getString("locationWebsite")


        // TODO-complete: Set the text for the location count, name, phone, and website
        findViewById<TextView>(R.id.locationCountTextView)?.text = "#$count"
        findViewById<TextView>(R.id.locationNameTextView)?.text = name
        findViewById<TextView>(R.id.locationPhoneTextView)?.text = phone
        findViewById<TextView>(R.id.locationWebsiteTextView)?.text = website
    }
}