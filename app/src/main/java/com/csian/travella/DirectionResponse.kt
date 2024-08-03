package com.csian.travella
import kotlinx.serialization.Serializable

@Serializable
data class GeocodedWaypoint(
    val geocoder_status: String? = null,
    val place_id: String? = null,
    val types: List<String>? = null
)

@Serializable
data class Bounds(
    val northeast: Location,
    val southwest: Location
)

@Serializable
data class Location(
    val lat: Double,
    val lng: Double
)

@Serializable
data class Distance(
    val text: String,
    val value: Int
)

@Serializable
data class Duration(
    val text: String,
    val value: Int
)

@Serializable
data class Step(
    val distance: Distance,
    val duration: Duration,
    val end_location: Location,
    val html_instructions: String,
    val maneuver: String? = null, // Use @Optional annotation
    val polyline: Polyline,
    val start_location: Location,
    val travel_mode: String
)


@Serializable
data class Polyline(
    val points: String
)

@Serializable
data class Leg(
    val distance: Distance,
    val duration: Duration,
    val duration_in_traffic: Duration? = null,
    val end_address: String,
    val end_location: Location,
    val start_address: String,
    val start_location: Location,
    val steps: List<Step>
)

@Serializable
data class Route(
    val bounds: Bounds,
    val legs: List<Leg>,
    val overview_polyline: Polyline,
    val summary: String,
    val warnings: List<String>,
    val waypoint_order: List<Int>
)

@Serializable
data class DirectionResponse(
    val geocoded_waypoints: List<GeocodedWaypoint>? = null,
    val routes: List<Route>? = null,
    val status: String? = null
)
