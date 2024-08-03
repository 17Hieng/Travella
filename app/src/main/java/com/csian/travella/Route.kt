package com.csian.travella

import PolylineEncoding
import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class Permission {

    companion object{
        val LOCATION_PERMISSION_REQUEST_CODE = 1

        fun requestFineLocationPermission(activity: Activity){
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }
}

class Graph(val size: Int) {
    val adjacencyMatrix = Array(size) { DoubleArray(size) { Double.MAX_VALUE } }

    fun addEdge(from: Int, to: Int, weight: Double) {
        adjacencyMatrix[from][to] = weight
        adjacencyMatrix[to][from] = weight
    }
}


data class LocationData(val id: String, val name: String, val latLng: LatLng)

class RouteActivity : AppCompatActivity(), OnMapReadyCallback{

    // COLLECTIONS
    private val markerPropertiesMap = HashMap<Marker, String>()
    private var polylines = ArrayList<Polyline>()


    // OBJECT
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var predictionsAdapter: PredictionsAdapter

    // PRIMITIVE TYPE
    private var apiKey2: String = "AIzaSyAze5W3PTx9epav2EtmInOp7eh4p9UGvbQ"


    // VIEWS
    private lateinit var searchTextField: EditText
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var map: SupportMapFragment


    private var locations = mutableListOf<LocationData>()



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)


        init()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

    }

    private suspend fun dijkstra(graph: Graph, startVertex: Int): Pair<DoubleArray, IntArray> {
        val minDistances = DoubleArray(graph.size) { Double.MAX_VALUE }
        val previous = IntArray(graph.size) { -1 }
        val visited = BooleanArray(graph.size)

        minDistances[startVertex] = 0.0

        for (i in 0 until graph.size) {
            val u = getMinDistanceVertex(minDistances, visited)
            if (u == -1) break
            visited[u] = true

            for (v in 0 until graph.size) {
                if (!visited[v] && graph.adjacencyMatrix[u][v] != Double.MAX_VALUE &&
                    minDistances[u] + graph.adjacencyMatrix[u][v] < minDistances[v]
                ) {
                    minDistances[v] = minDistances[u] + graph.adjacencyMatrix[u][v]
                    previous[v] = u
                }
            }
        }

        return Pair(minDistances, previous)
    }


    private fun getMinDistanceVertex(distances: DoubleArray, visited: BooleanArray): Int {
        var minDistance = Double.MAX_VALUE
        var minDistanceVertex = -1

        for (i in distances.indices) {
            if (!visited[i] && distances[i] < minDistance) {
                minDistance = distances[i]
                minDistanceVertex = i
            }
        }

        return minDistanceVertex
    }


    suspend fun findShortestRoute(locations: List<LocationData>, apiKey: String) {
        Log.i("Shortest", locations.size.toString())
        val graph = Graph(locations.size)

        for (i in locations.indices) {
            for (j in i + 1 until locations.size) {
                try {
                    val distance = getDistance(locations[i].latLng, locations[j].latLng, apiKey)
                    Log.d("GraphConstruction", "Distance from ${locations[i].name} to ${locations[j].name}: $distance meters")
                    if (distance < Double.MAX_VALUE) {
                        graph.addEdge(i, j, distance)
                    } else {
                        Log.e("GraphConstruction", "Invalid distance from ${locations[i].name} to ${locations[j].name}")
                    }
                } catch (e: Exception) {
                    Log.e("GraphConstruction", "Failed to get distance: ${e.message}")
                }
            }
        }

        val startVertex = 0 // Change this to the desired starting location index
        val (distances, previous) = dijkstra(graph, startVertex)

        // Track the shortest path from the start vertex to all other vertices
        val shortestPath = mutableListOf<Int>()
        var currentVertex = locations.size - 1

        while (currentVertex != -1) {
            shortestPath.add(currentVertex)
            currentVertex = previous[currentVertex]
        }
        shortestPath.reverse()

        // Add logging to check the shortestPath content
        Log.d("ShortestPath", "Shortest path: $shortestPath")

        if (shortestPath.size < 2) {
            Log.e("ShortestPath", "Error: Shortest path has less than 2 locations")
            runOnUiThread {
                Toast.makeText(this, "Error: Shortest path has less than 2 locations", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Mark and draw routes based on the shortest path found
        for (i in 1 until shortestPath.size) {
            val from = locations[shortestPath[i - 1]]
            val to = locations[shortestPath[i]]
            getAndDrawRoutes(from.latLng, to.latLng, apiKey)
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
            if (decodedPolyline.isEmpty()) {
                Log.e("Polyline", "Decoded polyline is empty")
            } else {
                val polyline = this.googleMap.addPolyline(polylineOptions)
                polylines.add(polyline)
                Log.i("Polyline", "Polyline added to map with ${decodedPolyline.size} points")
            }
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
                Log.i("Directions", "$origin, $destination :: ${directionsResponse.routes!!.firstOrNull()?.legs!!.firstOrNull()?.distance.toString()}")

                if (directionsResponse.status == "ZERO_RESULTS") {
                    runOnUiThread {
                        Toast.makeText(this, "The route is not available.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val polylinePoints =
                        directionsResponse.routes?.firstOrNull()?.overview_polyline?.points
                    drawRoutes(polylinePoints, "driving")
                }
            } else {
                Log.e("Directions", "Failed to fetch directions: Response is null")
            }
        }
    }

    private fun getCoordinateWithAddress(address: String, apiKey: String, callback: (LatLng?) -> Unit) {
        val client = OkHttpClient()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "https://maps.googleapis.com/maps/api/geocode/json?address=${address}&key=${apiKey}"
                val request = Request.Builder().url(urlString).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        val jsonResponse = JSONObject(it)

                        val status = jsonResponse.getString("status")
                        if (status == "OK") {
                            val results = jsonResponse.getJSONArray("results")
                            if (results.length() > 0) {
                                val location = results.getJSONObject(0)
                                    .getJSONObject("geometry")
                                    .getJSONObject("location")
                                val lat = location.getDouble("lat")
                                val lng = location.getDouble("lng")
                                val latLng = LatLng(lat, lng)

                                // Run callback on main thread
                                Handler(Looper.getMainLooper()).post {
                                    callback(latLng)
                                }
                            } else {
                                Log.e("Geocoding", "No results found for the given address.")
                                Handler(Looper.getMainLooper()).post {
                                    callback(null)
                                }
                            }
                        } else {
                            Log.e("Geocoding", "Geocoding request failed with status: $status")
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
            .url("$url?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&key=$apiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Directions", "Failed to fetch directions: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.i("Directions", "Directions API response received")
                    callback(responseBody)
                } else {
                    Log.e("Directions", "Directions API request failed with response code: ${response.code}")
                    callback(null)
                }
            }
        })
    }

    private suspend fun getDistance(from: LatLng, to: LatLng, apiKey: String): Double {
        val url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=${from.latitude},${from.longitude}&destinations=${to.latitude},${to.longitude}&key=$apiKey"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val jsonResponse = JSONObject(response.body?.string() ?: "")
            val rows = jsonResponse.getJSONArray("rows")
            if (rows.length() > 0) {
                val elements = rows.getJSONObject(0).getJSONArray("elements")
                if (elements.length() > 0) {
                    val distance = elements.getJSONObject(0).getJSONObject("distance").getDouble("value")
                    return@withContext distance / 1000 // Return distance in kilometers
                }
            }
            Double.MAX_VALUE
        }
    }



    private fun init() {
        getViews()
        initListeners()
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





        CoroutineScope(Dispatchers.Main).launch {
            locations.add(LocationData(UUID.randomUUID().toString(), "Test", LatLng(5.461359499999999,100.2174133)))
            locations.add(LocationData(UUID.randomUUID().toString(), "Test", LatLng(4.6258304,101.0889885)))
            locations.add(LocationData(UUID.randomUUID().toString(), "Test", LatLng(5.2783403,100.2419407)))
            locations.add(LocationData(UUID.randomUUID().toString(), "Test", LatLng(5.358289,100.316313)))
            locations.add(LocationData(UUID.randomUUID().toString(), "Test", LatLng(5.4734004,100.2460575)))

            for (location in locations) {
                markLocationOnMap(location.latLng.latitude, location.latLng.longitude)
            }

            findShortestRoute(locations, apiKey2)
        }


    }


    private fun isLatLng(value: String): Boolean {
        return parseLatLng(value) != null
    }

    private fun getViews(){
        searchTextField = findViewById(R.id.search_textfield)
        searchRecyclerView = findViewById(R.id.search_recyclerview)
        map = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    }

    private fun initListeners(){

        searchRecyclerView.layoutManager = LinearLayoutManager(this)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey2)
        }
        placesClient = Places.createClient(this)
        predictionsAdapter = PredictionsAdapter { prediction ->
            // Handle place selection
            val placeId = prediction.placeId
            val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
            val request = com.google.android.libraries.places.api.net.FetchPlaceRequest.newInstance(placeId, placeFields)

            placesClient.fetchPlace(request).addOnSuccessListener { response ->
                val place = response.place
                place.latLng?.let {
                    Log.i("Places", "Place selected: ${place.name}, ${place.address}, ${place.latLng}")
                    val location = "${place.name}, ${place.address}"
                    searchTextField.setText("")
                    searchRecyclerView.adapter = null
                    markLocationOnMap(place.latLng.latitude, place.latLng.longitude)
                    locations.add(LocationData(UUID.randomUUID().toString(), place.name, place.latLng))
                    if (locations.size > 1)
                        getAndDrawRoutes(locations[locations.size - 2].latLng, locations[locations.size - 1].latLng, apiKey2)
                }
            }.addOnFailureListener { exception ->
                Log.e("Places", "Place not found: ${exception.message}")
            }
        }
        searchTextField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchRecyclerView.adapter = predictionsAdapter
                if (s.isNullOrEmpty()) {
                    searchRecyclerView.adapter = null
                    return
                }
                fetchNearbyPlaces(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchNearbyPlaces(query: String) {
        val token = AutocompleteSessionToken.newInstance()
        val requestBuilder = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .setTypesFilter(listOf("tourist_attraction"))

        fetchCurrentLocation {
            val latLng = it?.let { it1 -> LatLng(it1.latitude, it.longitude) }
            requestBuilder.setOrigin(latLng)

            val radiusInMeters = 5_000.0
            requestBuilder.setLocationBias(
                RectangularBounds.newInstance(
                    SphericalUtil.computeOffset(latLng, radiusInMeters, 225.0), // Southwest point
                    SphericalUtil.computeOffset(latLng, radiusInMeters, 45.0)   // Northeast point
                ))

            val request = requestBuilder.build()

            placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
                predictionsAdapter.submitList(response.autocompletePredictions)
            }.addOnFailureListener { exception ->
                Log.e("Places", "Autocomplete prediction fetch failed: ${exception.message}")
            }
        }
    }



    private fun fetchCurrentLocation(callback: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                callback(it)
            }
        }
    }

    private fun markLocationOnMap(latitude: Double, longitude: Double) {
        if (this::googleMap.isInitialized) {
            runOnUiThread {
                val newMarker = MarkerOptions()
                    .position(LatLng(latitude, longitude))
                    .title("Test")

                val marker = googleMap.addMarker(newMarker)
                marker?.let {
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
        } else {
            // If googleMap is not initialized yet, wait for the map to be ready
            map.getMapAsync { map ->
                googleMap = map
                markLocationOnMap(latitude, longitude)
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