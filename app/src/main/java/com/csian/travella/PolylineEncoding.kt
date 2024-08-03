import com.google.android.gms.maps.model.LatLng
import java.util.ArrayList

object PolylineEncoding {
    fun decode(polyline: String): List<LatLng> {
        val len = polyline.length
        var index = 0
        val decoded = ArrayList<LatLng>()
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = polyline[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = polyline[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            decoded.add(latLng)
        }
        return decoded
    }
}
