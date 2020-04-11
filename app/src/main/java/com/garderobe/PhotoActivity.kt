package com.garderobe

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.huawei.hiai.vision.image.docrefine.DocRefine
import com.huawei.hiai.vision.visionkit.common.Frame

class PhotoActivity : AppCompatActivity() {

    private lateinit var photoPreview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)
        photoPreview = findViewById(R.id.photoPreview)
        // Todo: check if bitmap not null
        val bitmap = intent.extras?.getParcelable<Bitmap>("image")!!
        photoPreview.setImageBitmap(bitmap)
    }

    private fun correctSkew(bitmap: Bitmap): Bitmap {
        val docResolution = DocRefine(this)
        val frame = Frame()
        frame.bitmap = bitmap
        val jsonDoc = docResolution.docDetect(frame, null)
        val docCoordinates = docResolution.convertResult(jsonDoc)
        val result = docResolution.docRefine(frame, docCoordinates, null)
        return result.bitmap
    }
}
