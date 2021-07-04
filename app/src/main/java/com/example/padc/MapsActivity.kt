package com.example.padc

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.maps.android.SphericalUtil
import com.google.maps.android.data.kml.KmlContainer
import com.google.maps.android.data.kml.KmlLayer
import com.google.maps.android.data.kml.KmlPolygon
import kotlin.math.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    lateinit var mapsViewModel:MapsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapsViewModel = ViewModelProvider(this).get(MapsViewModel::class.java)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        val layer = KmlLayer(mMap, R.raw.allowed_area, this)
        layer.addLayerToMap()
        accessContainers(layer.containers)
        layer.removeLayerFromMap()

        // Constrain the camera target to the Adelaide bounds.
        val cameraPosition = CameraPosition.Builder()
                .target(mapsViewModel.latLangArray[0]) // Sets the center of the map to Mountain View
                .zoom(13f)            // Sets the zoom
                .bearing(90f)         // Sets the orientation of the camera to east
                .tilt(10f)            // Sets the tilt of the camera to 30 degrees
                .build()              // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        mMap.setOnMapClickListener { pressedPoint ->
            cleanMap()


            //Check if point is inside the Polygon else check distance
            mapsViewModel.totalMinPoint = null
            if (mapsViewModel.pointInPolygon(pressedPoint)) {
                mapsViewModel.pressedMarker = mMap.addMarker(MarkerOptions().position(pressedPoint)
                        .icon(BitmapDescriptorFactory.defaultMarker(HUE_GREEN)))
                Toast.makeText(this, "Point is inside area", Toast.LENGTH_SHORT).show()
            } else {


                mapsViewModel.checkAllSideMinDistance(pressedPoint)

                mapsViewModel.pressedMarker = mMap.addMarker(MarkerOptions().position(pressedPoint)
                        .icon(BitmapDescriptorFactory.defaultMarker(HUE_RED)))
                mapsViewModel.totalMinPoint?.let {
                    mapsViewModel.polyline = mMap.addPolyline(PolylineOptions().add(pressedPoint, it).color(Color.RED))
                    mapsViewModel.marker = mMap.addMarker(MarkerOptions().position(it)
                            .icon(BitmapDescriptorFactory.defaultMarker()))
                }

                val kmDistance = round(SphericalUtil.computeDistanceBetween(pressedPoint, mapsViewModel.totalMinPoint)) / 1000
                Toast.makeText(this, "Minimum distance to polygon is ${kmDistance} km", LENGTH_SHORT).show()
            }
        }
    }

    private fun cleanMap() {
        mapsViewModel.marker?.remove()
        mapsViewModel.polyline?.remove()
        mapsViewModel.pressedMarker?.remove()
    }

    //Access Kml file container
    private fun accessContainers(containers: Iterable<KmlContainer>) {
        for (container in containers) {
            if (container.hasPlacemarks()) {
                container.placemarks.forEach {
                    if (it.hasGeometry()) {
                        if (it.geometry is KmlPolygon) {
                            mapsViewModel.latLangArray.addAll((it.geometry as KmlPolygon).outerBoundaryCoordinates)
                            mMap.addPolygon(PolygonOptions().addAll(mapsViewModel.latLangArray))
                        }
                    }
                }
            }
            if (container.hasContainers()) {
                accessContainers(container.containers)
            }
        }
    }

}

