package com.csian.travella

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MushTryActivity : AppCompatActivity() {
    private lateinit var adapter: MushTryItemsAdapter
    private var apiKey2: String = "AIzaSyAze5W3PTx9epav2EtmInOp7eh4p9UGvbQ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mush_try)

        val itemView = findViewById<RecyclerView>(R.id.id_musttry_items)
        itemView.layoutManager = LinearLayoutManager(this)
        adapter = MushTryItemsAdapter(mutableListOf())
        itemView.adapter = adapter

        (itemView.adapter as MushTryItemsAdapter).updateEvents(mutableListOf())

        fetchTouristAttractions("Kuala Lumpur", locations["Kuala Lumpur"]!!.first, locations["Kuala Lumpur"]!!.second, apiKey2, 20000)
    }

    @Serializable
    data class Place(
        val place_id: String,
        val name: String,
        val rating: Float? = null,
        val vicinity: String
    )

    @Serializable
    data class PlacesResponse(
        val results: List<Place>
    )

    val locations = mapOf(
        "Kuala Lumpur" to Pair(3.139, 101.6869),
        "Labuan" to Pair(5.2847, 115.2289),
        "Putrajaya" to Pair(2.9296, 101.6556),
        "Johor" to Pair(1.4927, 103.7414),
        "Kedah" to Pair(6.1185, 100.3674),
        "Kelantan" to Pair(6.1162, 102.2108),
        "Malacca" to Pair(2.1896, 102.2504),
        "Negeri Sembilan" to Pair(2.7256, 102.1652),
        "Pahang" to Pair(3.9514, 103.3200),
        "Penang" to Pair(5.4142, 100.3288),
        "Perak" to Pair(4.5970, 101.0901),
        "Perlis" to Pair(6.4363, 100.1951),
        "Sabah" to Pair(5.9754, 116.0734),
        "Sarawak" to Pair(1.5536, 110.3590),
        "Selangor" to Pair(3.0738, 101.5183),
        "Terengganu" to Pair(5.3220, 103.1730)
    )

    private fun fetchTouristAttractions(locationName: String, lat: Double, lng: Double, apiKey: String, radius: Int) {
        Log.i("Tourist", "$lat, $lng")
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$lat,$lng&radius=$radius&type=tourist_attraction&key=$apiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonResponse = response.body?.string()
                val placesResponse = Json { ignoreUnknownKeys = true }.decodeFromString<PlacesResponse>(jsonResponse!!)
                val sortedPlaces = placesResponse.results.sortedByDescending { it.rating ?: 0.0f }

                println("Tourist Attractions in $locationName:")
                sortedPlaces.forEach { place ->
                    Log.i("Try", "Name: ${place.name}, Rating: ${place.rating}, Vicinity: ${place.vicinity}")
                }
            }
        })
    }

    private fun getItemDetail(state: String, callback: (List<String>?) -> Unit) {
        val client = OkHttpClient()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "https://www.malaysia.travel/explore?state=$state"
                val request = Request.Builder().url(urlString).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        try {
                            val jsonResponse = JSONObject(it)
                            val status = jsonResponse.getString("status")
                            if (status == "OK") {
                                val results = jsonResponse.getJSONArray("results")
                                val urls = mutableListOf<String>()

                                for (i in 0 until results.length()) {
                                    val item = results.getJSONObject(i)
                                    val url = item.getString("url")
                                    urls.add(url)
                                }

                                // Run callback on the main thread with the list of URLs
                                Handler(Looper.getMainLooper()).post {
                                    callback(urls)
                                }
                            } else {
                                Log.e("Geocoding", "Geocoding request failed with status: $status")
                                Handler(Looper.getMainLooper()).post {
                                    callback(null)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Geocoding", "JSON parsing failed: ${e.message}")
                            Handler(Looper.getMainLooper()).post {
                                callback(null)
                            }
                        }
                    }
                } else {
                    Log.e("Geocoding", "HTTP request failed with response code: ${response.code}")
                    Handler(Looper.getMainLooper()).post {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("Geocoding", "Geocoding request failed: ${e.message}")
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }
}