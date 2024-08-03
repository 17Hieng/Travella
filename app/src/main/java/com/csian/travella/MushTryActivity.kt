package com.csian.travella;

import android.os.Bundle;
import android.os.Handler
import android.os.Looper
import android.util.Log

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Carousel.Adapter
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MushTryActivity: AppCompatActivity() {
    private lateinit var adapter: MushTryItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mush_try)

        // Get Data
        val location = intent.getStringExtra("location")
        val startDate = intent.getStringExtra("startDate")
        val endDate = intent.getStringExtra("endDate")
        val startTime = intent.getStringExtra("startTime")
        val endTime = intent.getStringExtra("endTime")


        val itemView = findViewById<RecyclerView>(R.id.id_musttry_items)
        itemView.layoutManager = LinearLayoutManager(this)
        adapter = MushTryItemsAdapter(mutableListOf())
        itemView.adapter = adapter

        (itemView.adapter as MushTryItemsAdapter).updateEvents(mutableListOf())

    }

    private fun getItemDetail(state: String, callback: (List<String>?) -> Unit) {
        val client = OkHttpClient()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "https://www.malaysia.travel/explore?state=${state}"
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