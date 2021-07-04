package com.example.padc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapsViewModel :ViewModel(){

    val latLangArray = ArrayList<LatLng>()
    var marker: Marker? = null
    var pressedMarker: Marker? = null
    var polyline: Polyline? = null
    var totalMinPoint: LatLng? = null
    private var minPoint: LatLng? = null

    /*
        Point in polygon if the point intersect with the polygon side an odd time in one of the
        direction(,up,down,right,left)

        We need to check only one side
    */
    fun pointInPolygon(point: LatLng): Boolean {
        var crossCounter = 0
        for (i in 0..latLangArray.size - 2) {
            //Check how many time the latitude line cross the polygon

            val x1 = latLangArray[i].latitude
            val x2 = latLangArray[i + 1].latitude
            val y1 = latLangArray[i].longitude
            val y2 = latLangArray[i + 1].longitude

            val pX = point.latitude
            val pY = point.longitude

            if (pX in x1..x2
                || (pX in x2..x1)) {
                if (pY < y1 || pY < y2)
                    crossCounter++
            }

        }
        if (crossCounter % 2 != 0 && crossCounter != 0) {
            return true
        }

        return false
    }

    /*
       - Using with SphericalUtil for math the calculation

       Checking all side of the polygon and run recursive function to get minimum distance
       between polygon pressed point
       The recursive function get the middle of the side and
       we continue checking for middle point until the precision that we want is over.
   */
    private fun checkMinPrecision(pointA: LatLng, pointB: LatLng, pressedPoint: LatLng, precision: Int): LatLng? {
        val distanceToPointA = SphericalUtil.computeDistanceBetween(pressedPoint, pointA)
        val distanceToPointB = SphericalUtil.computeDistanceBetween(pressedPoint, pointB)
        return if (precision == 0) {
            if (distanceToPointA < distanceToPointB) {
                pointA
            } else {
                pointB
            }
        } else {
            val midPoint = LatLngBounds.builder().include(pointA).include(pointB)
                .build().center
            if (distanceToPointA < distanceToPointB) {
                checkMinPrecision(pointA, midPoint, pressedPoint, precision - 1)
            } else {
                checkMinPrecision(midPoint, pointB, pressedPoint, precision - 1)
            }
        }

    }

    fun checkAllSideMinDistance(pressedPoint: LatLng){
        //Run on Background thread
        viewModelScope.launch(Dispatchers.IO) {
            for (i in 0..latLangArray.size - 2) {

                var pointA = latLangArray[i]
                var pointB = latLangArray[i + 1]

                var precision = 30

                minPoint = checkMinPrecision(pointA, pointB, pressedPoint, precision)

                /*Compare and check for the minimum distance between all polygon sides*/
                if (totalMinPoint == null) {
                    totalMinPoint = minPoint
                } else {
                    val minDistance = SphericalUtil.computeDistanceBetween(
                        minPoint,
                        pressedPoint
                    )
                    val totalMinDistance = SphericalUtil.computeDistanceBetween(
                        totalMinPoint,
                        pressedPoint
                    )
                    if (minDistance < totalMinDistance) {
                        totalMinPoint = minPoint
                    }
                }
            }
        }
     }


}