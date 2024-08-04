package com.csian.travella

import android.accessibilityservice.AccessibilityButtonController.AccessibilityButtonCallback
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.csian.travella.MushTryActivity.Place
import com.bumptech.glide.Glide


class MushTryItemsAdapter(private val context: Context, private val dataSet: MutableList<Place>, private val actionButtonCallback: (Place) -> Unit) :
    RecyclerView.Adapter<MushTryItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val photo: ImageView = view.findViewById(R.id.item_photo)
        val titleTextView: TextView = view.findViewById(R.id.item_title_textview)
        val despTextView: TextView = view.findViewById(R.id.item_desp_textview)
        val actionButton: Button = view.findViewById(R.id.action_button)
        val card: View = view.findViewById(R.id.item)

    }
    val apikey = "AIzaSyAze5W3PTx9epav2EtmInOp7eh4p9UGvbQ"

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_tourist_interest_suggestion_card, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.titleTextView.text = dataSet[position].name
        viewHolder.despTextView.text = dataSet[position].vicinity

        val imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=${dataSet[position].photo?.photo_reference}&key=$apikey"
        Glide.with(context)
            .load(imageUrl)
            .into(viewHolder.photo)

        viewHolder.actionButton.setOnClickListener {
            actionButtonCallback(dataSet[position])
            dataSet.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dataSet.size)
        }

        viewHolder.card.setOnClickListener {
            val intent = Intent(context, TripDetailsActivity::class.java)
            intent.putExtra("place_id", dataSet[position].place_id)
            intent.putExtra("name", dataSet[position].name)
            intent.putExtra("vicinity", dataSet[position].vicinity)
            intent.putExtra("photo_reference", dataSet[position].photo?.photo_reference)
            intent.putExtra("rating", dataSet[position].rating)
            intent.putExtra("lat", dataSet[position].geometry.location.lat)
            intent.putExtra("lng", dataSet[position].geometry.location.lng)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = dataSet.size

    fun updateEvents(newItemList: MutableList<Place>) {
        dataSet.clear()
        dataSet.addAll(newItemList)
        notifyDataSetChanged()
    }
}
