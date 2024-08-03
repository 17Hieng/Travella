package com.csian.travella

import PolylineEncoding
import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.csian.travella.DirectionResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.type.DateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Permission {

    companion object{
        val LOCATION_PERMISSION_REQUEST_CODE = 1
        fun isFineLocationPermissionGranted(context: Context): Boolean{
            return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
        }

        fun requestFineLocationPermission(activity: Activity){
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }
}

class RouteActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // COLLECTIONS
    private val markerPropertiesMap = HashMap<Marker, String>()
    private var polylines = ArrayList<Polyline>()


    // OBJECT
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

    // PRIMITIVE TYPE
    private var apiKey2: String = "AIzaSyAze5W3PTx9epav2EtmInOp7eh4p9UGvbQ"


    // VIEWS
    private lateinit var locationTextField: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)


        init()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        googleMap.setOnMarkerClickListener(this)

        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null // Use default info window
            }

            override fun getInfoContents(marker: Marker): View {
                // Inflate custom layout for info window
                val view = layoutInflater.inflate(R.layout.event_info_popup_in_map, null)
                val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
                val snippetTextView = view.findViewById<TextView>(R.id.snippetTextView)

                titleTextView.text = marker.title
                snippetTextView.text = marker.snippet


                return view
            }
        })

        googleMap.setOnMapLongClickListener { latLng ->
            val locationCoordinate = latLng.latitude.toString() + "," + latLng.longitude.toString()
        }

        googleMap.setOnPoiClickListener { poi ->
            val locationCoordinate =
                poi.latLng.latitude.toString() + "," + poi.latLng.longitude.toString()
        }

    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val projection = googleMap.projection
        val markerLatLng = marker.position
        val markerScreenPosition: Point = projection.toScreenLocation(markerLatLng)

        val popupView = layoutInflater.inflate(R.layout.event_info_popup_in_map, null)

        val titleTextView = popupView.findViewById<TextView>(R.id.titleTextView)
        val snippetTextView = popupView.findViewById<TextView>(R.id.snippetTextView)

        titleTextView.text = marker.title
        snippetTextView.text = marker.snippet


        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        val popupWidth = (screenWidth * 0.75).toInt()
        val popupHeight = 600

        val popupWindow = PopupWindow(popupView, popupWidth, popupHeight, true)

        val offsetX = markerScreenPosition.x - popupWidth / 2
        val offsetY = markerScreenPosition.y - popupHeight - 50

        popupWindow.showAtLocation(
            mapFragment.requireView(),
            Gravity.TOP or Gravity.START,
            offsetX,
            offsetY
        )

        return true
    }


    private fun deleteLocation(marker: Marker) {
        marker.let {
            it.remove()
            markerPropertiesMap.remove(it)

            drawAllRoutes()
        }
    }

    private fun drawAllRoutes() {
        runOnUiThread {
            for (polyline in polylines) {
                polyline.remove()
            }

            polylines.clear()
            if (markerPropertiesMap.size > 1) {
                val markersList = markerPropertiesMap.keys.toList()

                for (j in 0 until markersList.size - 1) {
                    val origin = markersList[j].position
                    val destination = markersList[j + 1].position

                    getAndDrawRoutes(
                        origin,
                        destination,
                        apiKey2
                    )

                }
            }
        }
    }

    private fun drawRoutes(polylinePoints: String?, transportMode: String) {
        val decodedPolyline = PolylineEncoding.decode(polylinePoints ?: "")

        val polylineOptions =
            if (transportMode.lowercase() == "walking") {
                PolylineOptions().apply {
                    width(18f)
                    color(Color.parseColor("#007aff"))
                    pattern(listOf(Dot(), Gap(20f)))
                    addAll(decodedPolyline)
                }
            } else {
                PolylineOptions().apply {
                    width(15f)
                    color(Color.parseColor("#007aff"))
                    addAll(decodedPolyline)
                }
            }



        runOnUiThread {
            val polyline = this.googleMap.addPolyline(polylineOptions)
            polylines.add(polyline)
        }
    }

    private fun getAndDrawRoutes(
        origin: LatLng,
        destination: LatLng,
        apiKey: String
    ) {
        getDirections(origin, destination, apiKey) { response ->
            if (response != null) {
                val json = Json { ignoreUnknownKeys = true }
                val directionsResponse =
                    json.decodeFromString<DirectionResponse>(response)


                if (directionsResponse.status == "ZERO_RESULTS") {
                    val toast =
                        Toast.makeText(this, "No route found.", Toast.LENGTH_SHORT)
                    toast.show()
                } else {
                    val polylinePoints =
                        directionsResponse.routes?.firstOrNull()?.overview_polyline?.points
                    drawRoutes(polylinePoints, "driving")
                }
            } else {
                println("Error: Failed to fetch directions")
            }
        }
    }

    private fun getCoordinateWithLocationName(
        location: LatLng,
        placeName: String,
        apiKey: String,
        callback: (Double, Double) -> Unit
    ) {
        if (isLatLng(placeName)) {
            val latLngPair = parseLatLng(placeName)
            val (latitude, longitude) = latLngPair!!
            callback(latitude, longitude)
            return
        }


        val client = OkHttpClient()

        val url = HttpUrl.Builder()
            .scheme("https")
            .host("maps.googleapis.com")
            .addPathSegments("maps/api/place/textsearch/json")
            .addQueryParameter("query", placeName)
            .addQueryParameter("location", "${location.latitude},${location.longitude}")
            .addQueryParameter("radius", "2000") // You can adjust the radius as needed
            .addQueryParameter("rankby", "prominence")
            .addQueryParameter("key", apiKey)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                val jsonObject = json?.let { JSONObject(it) }
                val results = jsonObject?.getJSONArray("results")
                if (results != null && results.length() > 0) {
                    // Calculate the distance to each place and find the closest one
                    var closestPlace: JSONObject? = null
                    var minDistance = Double.MAX_VALUE
                    for (i in 0 until results.length()) {
                        val place = results.getJSONObject(i)
                        val locationObj = place.getJSONObject("geometry").getJSONObject("location")
                        val placeLocation =
                            LatLng(locationObj.getDouble("lat"), locationObj.getDouble("lng"))
                        val distance = getDistanceBetweenPoints(
                            location.latitude,
                            location.longitude,
                            placeLocation.latitude,
                            placeLocation.longitude
                        )
                        if (distance < minDistance) {
                            minDistance = distance
                            closestPlace = place
                        }
                    }
                    if (closestPlace != null) {
                        val location =
                            closestPlace.getJSONObject("geometry").getJSONObject("location")
                        val latitude = location.getDouble("lat")
                        val longitude = location.getDouble("lng")
                        callback(latitude, longitude)
                    } else {
                        // Handle no results
                    }
                } else {
                    // Handle no results
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }
        })
    }

    private fun getCurrentLocation(callback: (Double, Double) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Permission.requestFineLocationPermission(this)
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    callback(location.latitude, location.longitude)
                } else {
                    //
                }
            }
            .addOnFailureListener { e ->
                //
            }
    }

    private fun getDirections(
        origin: LatLng,
        destination: LatLng,
        apiKey: String,
        callback: (String?) -> Unit
    ) {
        val url = "https://maps.googleapis.com/maps/api/directions/json"

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("$url?&destination=${destination.latitude},${destination.longitude}&origin=${origin.latitude},${origin.longitude}&key=$apiKey")
            .post("".toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    callback(responseBody)
                } else {
                    callback(null)
                }
            }
        })
    }

    private fun getDistanceBetweenPoints(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Double {
        val earthRadius = 6371 // Radius of the Earth in kilometers

        val dLat = Math.toRadians(endLat - startLat)
        val dLng = Math.toRadians(endLng - startLng)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(startLat)) * cos(Math.toRadians(endLat)) *
                sin(dLng / 2) * sin(dLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c // Distance in kilometers
    }

    private fun init() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        GlobalScope.launch {


            drawAllRoutes()
        }

        // Mark Current Location
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        getCurrentLocation { latitude, longitude ->
            drawAllRoutes()
        }
    }


    private fun isLatLng(value: String): Boolean {
        return parseLatLng(value) != null
    }

    private fun markLocationOnMap(
        latitude: Double,
        longitude: Double
    ) {
        runOnUiThread {
            if (this::googleMap.isInitialized) {

                val newMarker = MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .title("Test")

                val marker = googleMap.addMarker(newMarker)
                marker?.let {
                    // Associate the marker with its calendar event in the markerPropertiesMap
                    markerPropertiesMap[it] = ""
                }

                val builder = LatLngBounds.Builder()

                // Include all markers' positions in the builder
                for (entry in markerPropertiesMap.entries) {
                    builder.include(entry.key.position)
                }

                val bounds = builder.build()
                val padding = 100
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                googleMap.moveCamera(cameraUpdate)
            }
        }
    }

    private fun parseLatLng(latLngString: String): Pair<Double, Double>? {
        val regex = """^([-+]?\d{1,3}(?:\.\d+)?),([-+]?\d{1,3}(?:\.\d+)?)$""".toRegex()
        val matchResult = regex.find(latLngString)
        return matchResult?.let { result ->
            val (latitude, longitude) = result.destructured
            Pair(latitude.toDouble(), longitude.toDouble())
        }
    }

    private fun pickTimeFromTimePickerDialog(
        time: Calendar? = null,
        callback: (Int, Int) -> Unit
    ) {
        val defaultTime = time ?: Calendar.getInstance()

        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                callback(hourOfDay, minute)
            },
            defaultTime.get(Calendar.HOUR_OF_DAY),
            defaultTime.get(Calendar.MINUTE),
            false
        )
        timePicker.show()
    }

}