package edu.msudenver.cs3013.project03

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// This is the ViewHolder that is used by the Adapter This will be used to bind the data to the view
class LocationViewHolder(
    private val containerView: View,
    private val onClickListener: OnClickListener,
//    private val locationImageLoader: LocationImageLoader,

) : RecyclerView.ViewHolder(containerView) {

    // Location Name Text
    private val locationNameText: TextView
            by lazy { containerView.findViewById(R.id.item_list_location_name) }
    // Location Phone Text
    private val locationPhoneText: TextView
            by lazy { containerView.findViewById(R.id.item_list_location_phone) }
    // Location Website Text
    private val locationWebsiteText: TextView
            by lazy { containerView.findViewById(R.id.item_list_location_website) }
    // Location Count
    private val locationCount: TextView
            by lazy { containerView.findViewById(R.id.location_count) }
    // App Currency
    private val locationCurrency: TextView
            by lazy { containerView.findViewById(R.id.currency_text) }


    fun bindData(LocationData: LocationUiModel) {

        // 7/15/23 Set the onClickListener for the view
        containerView.setOnClickListener { onClickListener.onClick(LocationData) }
        // Load the image
//        locationImageLoader.loadImage(newsData.urlToImage, imgText)

        // TODO-complete: Set the text of the locations name
        locationNameText.text = LocationData.name
        // TODO-complete: Set the text of the locations phone
        locationPhoneText.text = LocationData.phone
        // TODO-complete: Set the text of the locations website
        locationWebsiteText.text = LocationData.website
        // TODO-complete: Set the text of the locations count
        locationCount.text = LocationData.count
        // TODO-complete: Set the text of the locations currency
        locationCurrency.text = LocationData.currency
    }

    // 7/15/23 Interface Added for the onClickListener
    interface OnClickListener {
        fun onClick(LocationData: LocationUiModel)
    }
}
