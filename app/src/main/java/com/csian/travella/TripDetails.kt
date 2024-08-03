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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


class TripDetailsActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)

        setupActionBar()
        setupRetrofit()
        populateDetails()
        fetchHighlights()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Trip Details"
        }
    }

    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://127.0.0.1:8000") // Replace with your API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    private fun fetchHighlights() {
        val tvHighlights = findViewById<TextView>(R.id.tvHighlights)
        lifecycleScope.launch {
            try {
                val highlights = withContext(Dispatchers.IO) {
                    apiService.getHighlights()
                }
                tvHighlights.text = highlights.joinToString("\n") { "• $it" }
            } catch (e: Exception) {
                tvHighlights.text = "Failed to load highlights: ${e.message}"
            }
        }
    }

    private fun populateDetails() {
        // In a real app, you'd get this data from an intent or a view model
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

        // Load image
        val ivDestination = findViewById<ImageView>(R.id.ivDestination)
        val tvDestinationName = findViewById<TextView>(R.id.tvDestinationName)
        val rbRating = findViewById<RatingBar>(R.id.rbRating)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val tvPrice = findViewById<TextView>(R.id.tvPrice)
        val tvHighlights = findViewById<TextView>(R.id.tvHighlights)
        val tvNotes = findViewById<TextView>(R.id.tvNotes)

        Glide.with(this).load(imageUrl).into(ivDestination)

        // Set texts
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

interface ApiService {
    @GET("highlights") // Replace with your actual endpoint
    suspend fun getHighlights(): List<String>
}
