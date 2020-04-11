package com.garderobe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

// This is an arbitrary number we are using to keep track of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts.
private const val REQUEST_CODE_PERMISSIONS = 10
private const val REQUEST_CAPTURE_IMAGE = 100
// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        findViewById<ImageButton>(R.id.cameraButton).setOnClickListener {
            val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (pictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            if (data != null && data.extras != null) {
                val imageBitmap: Bitmap = data.extras!!.get("data") as Bitmap
                val intent = Intent(this, PhotoActivity::class.java)
                intent.putExtra("image", imageBitmap)
                startActivity(intent)
            }
        }
    }

    fun goToCameraView(view: View) {
        startActivity(Intent(this, PhotoActivity::class.java))
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Toast.makeText(this, "Permissions granted by the user.",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
}
