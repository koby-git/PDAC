package com.example.padc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.SphericalUtil

const val PERMISSION_REQUEST_FINE_LOCATION = 147

class MainActivity : AppCompatActivity() {

    private lateinit var layout: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        layout = findViewById(R.id.main_layout)

        val nextBtn = findViewById<Button>(R.id.next_btn)
        nextBtn.setOnClickListener {
            // Check if the Camera permission has been granted
            if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Permission is already available, start camera preview

                startActivity(Intent(this, MapsActivity::class.java))
            } else {
                // Permission is missing and must be requested.
                requestCameraPermission()
            }
        }

    }


    private fun requestCameraPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            layout.showSnackbar(getString(R.string.fine_location_required),
                    Snackbar.LENGTH_INDEFINITE, getString(R.string.ok)) {

                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSION_REQUEST_FINE_LOCATION)
            }

        } else {
            layout.showSnackbar(getString(R.string.camera_permission_not_available), Snackbar.LENGTH_SHORT)

            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                layout.showSnackbar(R.string.fine_permission_granted, Snackbar.LENGTH_SHORT)
                startActivity(Intent(this, MapsActivity::class.java))
            } else {
                // Permission request was denied.
                layout.showSnackbar(R.string.fine_location_permission_denied, Snackbar.LENGTH_SHORT)
            }
        }
    }

}

