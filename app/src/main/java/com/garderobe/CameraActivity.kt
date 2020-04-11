package com.garderobe

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Size
import android.view.TextureView
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.Executors

// This is an arbitrary number we are using to keep track of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts.
private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class CameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        cameraPreview = findViewById(R.id.cameraPreview)

        if (allPermissionsGranted()) {
            cameraPreview.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var cameraPreview: TextureView



    private fun startCamera() {
        val previewConfig = PreviewConfig.Builder().apply {
            // TODO: set some reasonable resolution that will work across all devices that will be used
            setTargetResolution(Size(640, 480))
        }.build()

        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener {
            val parent = cameraPreview.parent as ViewGroup
            parent.removeView(cameraPreview)
            parent.addView(cameraPreview, 0)

            cameraPreview.surfaceTexture = it.surfaceTexture
        }

        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)

        findViewById<ImageButton>(R.id.photo_button).setOnClickListener{
            val file = File(externalMediaDirs.first(),"${System.currentTimeMillis()}.jpg")

            imageCapture.takePicture(file, executor,
                object: ImageCapture.OnImageSavedListener{
                    override fun onError(
                        imageCaptureError: ImageCapture.ImageCaptureError,
                        message: String,
                        cause: Throwable?
                    ) {
                        cameraPreview.post{
                            Toast.makeText(baseContext, "Photo capture failed: $message", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onImageSaved(file: File) {
                        cameraPreview.post{
                            Toast.makeText(baseContext, "Photo capture succeeded", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }

        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraPreview.post { startCamera() }
            } else {
                Toast.makeText(this, "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
}
