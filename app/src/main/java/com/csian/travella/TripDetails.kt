package com.csian.travella

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class TripDetailsActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)

        setupActionBar()
        populateDetails()
        fetchHighlights()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Trip Details"
        }
    }

    private fun fetchHighlights() {
        val tvHighlights = findViewById<TextView>(R.id.tvHighlights)
        val tvNotes = findViewById<TextView>(R.id.tvNotes)
        lifecycleScope.launch {
            try {
                val highlights = withContext(Dispatchers.IO) {
                    fetchHighlightsFromApi()
                }

                val hl = highlights!!.message

                val h2 = hl!!.split("\n")

                tvHighlights.text = h2[1] ?: "No highlights found"
                tvNotes.text = h2[2] ?: "No notes to take of"

            } catch (e: Exception) {
                tvHighlights.text = "Failed to load highlights: ${e.message}"
                tvNotes.text = "Failed to load notes"
            }
        }
    }

    private fun fetchHighlightsFromApi(): HighlightsResponse? {
        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/reviewAnalysis/ChIJfbX2aiOuEmsRS3rtwHGqAaI") // Replace with your actual URL
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body?.string()?.let {
                    return try {
                        gson.fromJson(it, HighlightsResponse::class.java)
                    } catch (e: JsonSyntaxException) {
                        null
                    }
                }
            }
            return null
        }
    }

    private fun populateDetails() {
        val imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTefcSW2FQQUgvkCcp2gz3mhQ6rBCkQDYEiUw&s"
        val name = "Penang Hill"
        val rating = 4.0f
        val description = "Penang Hill is a hill resort comprising a group of peaks."
        val price = "RM20 per person"
        val highlights = """
            • Penang Hill Railway: Ride the funicular train to the top for a scenic journey.
            • The Habitat: Experience the rainforest with canopy walkways, nature trails, and a treetop walk.
            • David Brown's Restaurant and Tea Terraces: Enjoy a meal or afternoon tea with a great view.
        """.trimIndent()
        val notes = "Sunny day. Suggested to wear sunscreen and long sleeve."

        val ivDestination = findViewById<ImageView>(R.id.ivDestination)
        val tvDestinationName = findViewById<TextView>(R.id.tvDestinationName)
        val rbRating = findViewById<RatingBar>(R.id.rbRating)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val tvPrice = findViewById<TextView>(R.id.tvPrice)
        val tvHighlights = findViewById<TextView>(R.id.tvHighlights)
        val tvNotes = findViewById<TextView>(R.id.tvNotes)

        Glide.with(this).load(imageUrl).into(ivDestination)

        tvDestinationName.text = name
        rbRating.rating = rating
        tvDescription.text = description
        tvPrice.text = price
        tvHighlights.text = highlights
        tvNotes.text = notes
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

data class HighlightsResponse(
    val message: String
)
