package com.csian.travella

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction

class PredictionsAdapter(private val onItemClicked: (AutocompletePrediction) -> Unit) :
    ListAdapter<AutocompletePrediction, PredictionsAdapter.PredictionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.location_suggestion_item_layout, parent, false)
        return PredictionViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: PredictionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PredictionViewHolder(itemView: View, private val onItemClicked: (AutocompletePrediction) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.title_textview)
        private var addressTextView: TextView = itemView.findViewById(R.id.address_textview)

        fun bind(prediction: AutocompletePrediction) {
            titleTextView.text = prediction.getPrimaryText(null)
            addressTextView.text = prediction.getSecondaryText(null)
            itemView.setOnClickListener {
                onItemClicked(prediction)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<AutocompletePrediction>() {
            override fun areItemsTheSame(oldItem: AutocompletePrediction, newItem: AutocompletePrediction): Boolean {
                return oldItem.placeId == newItem.placeId
            }

            override fun areContentsTheSame(oldItem: AutocompletePrediction, newItem: AutocompletePrediction): Boolean {
                return oldItem.getFullText(null) == newItem.getFullText(null)
            }
        }
    }
}

