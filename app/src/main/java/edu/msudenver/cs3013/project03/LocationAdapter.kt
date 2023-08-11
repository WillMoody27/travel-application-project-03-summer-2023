package edu.msudenver.cs3013.project03

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class LocationAdapter(

    // This is the layoutInflater that is passed in from the Activity
    private val layoutInflater: LayoutInflater,

    // 7/15/23 OnClickListener is used to handle the click events on the items in the RecyclerView
    private val onClickListener: OnClickListener

) : RecyclerView.Adapter<LocationViewHolder>() {

    // TODO-d: 3. Swipe delete functionality - define read-only variable within our adapter
    val swipeToDeleteCallback = SwipeToDeleteCallback()

    // List of data that is passed in from the ViewModel
    private val locationData = mutableListOf<LocationUiModel>()


    // SetData is called by the ViewModel to update the data in the adapter
    fun setData(locationListData: List<LocationUiModel>) {
        locationData.clear()
        locationData.addAll(locationListData)
        notifyDataSetChanged()
    }

    // TODO-d: 1. Swipe delete functionality
    fun removeItem(position: Int) {
        locationData.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = layoutInflater.inflate(R.layout.item_location, parent, false)

        return LocationViewHolder(
            view,
            // 7/15/23 This allows us to pass the onClickListener to the LocationViewHolder to select a category item
            object : LocationViewHolder.OnClickListener {
                override fun onClick(locationsData: LocationUiModel) =
                    onClickListener.onItemClick(locationsData)
            }
        )
    }

    override fun getItemCount() = locationData.size

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {

        holder.bindData(locationData[position])
    }

    // TODO-d: 2. Swipe delete functionality onSwipeToDeleteCallback
    //  - Innerclass declaration
    inner class SwipeToDeleteCallback :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        // TODO-d: 4. Swipe delete functionality - Override getMovementFlags to allow for swipe left and right
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) = if (viewHolder is LocationViewHolder) {
            makeMovementFlags(
                ItemTouchHelper.ACTION_STATE_IDLE,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) or makeMovementFlags(
                ItemTouchHelper.ACTION_STATE_SWIPE,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            )
        } else {
            0
        }

        // TODO-Above and Beyond: Swipe delete functionality - Override onSwiped to handle the swipe action of saving or deleting an article
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            // Get the position of the item that was swiped
            val position = viewHolder.adapterPosition

            // Determine the direction of the swipe
            val message = when (direction) {
                ItemTouchHelper.LEFT -> "Location Deleted"
                ItemTouchHelper.RIGHT -> "Location Saved"
                else -> "Unknown action"
            }

            // Temporarily save the item in a variable before removing it
            val item = getItem(position)
            removeItem(position)

            // Use snack-bar to display a message to the user instead of a toast
            val snackbar = Snackbar.make(
                viewHolder.itemView,
                message,
                Snackbar.LENGTH_LONG
            )

            /*
            * TODO-Above and Beyond: Allows the user to undo the swipe delete action
            * */
            snackbar.setAction("Undo") {
                // Restore the item at its previous position
                addItem(position, item)
            }
            snackbar.show()
        }

        // TODO-Above and Beyond: getItem will return the item at the specified position from which the item was swiped
        private fun getItem(position: Int): LocationUiModel {
            return locationData[position]
        }

        // removeItem will remove the item at the specified position and notify when it is removed
        private fun removeItem(position: Int) {
            // Remove the item from your list
            locationData.removeAt(position)
            // Notify the adapter about the item removal
            notifyItemRemoved(position)
        }

        // addItem will add the item at the specified position and notify when it is added
        private fun addItem(position: Int, item: LocationUiModel) {
            // Insert the item back into your list at the specified position
            locationData.add(position, item)
            // Notify the adapter about the item insertion
            notifyItemInserted(position)
        }

        // TODO-Above and Beyond:  Implement an onChildDraw method to allow users to see the background color as they swipe both left and right
        override fun onChildDraw(
            canvas: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {

            // Get the itemView from the viewHolder and create a ColorDrawable for the background
            val itemView = viewHolder.itemView
            val background = ColorDrawable()

            if (dX < 0) {
                // Swipe left (delete) and add icon
                background.color = Color.rgb(255, 0, 0)
            } else if (dX > 0) {
                // Swipe right (save)
                background.color = Color.rgb(20, 164, 255)
            }

            // Draw the red delete background
            background.setBounds(
                itemView.left,
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(canvas)

            super.onChildDraw(
                canvas,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }
    }

    // 7/15/23 Interface for handling click events on the items in the RecyclerView
    interface OnClickListener {
        fun onItemClick(locationData: LocationUiModel)
    }
}