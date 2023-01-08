package com.gmail.jbs.reread

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ScannerActivity : AppCompatActivity() {
    private var imageview: ImageView? = null
    private var resultScreen: TextView? = null
    private var snapBtn: Button? = null
    private var detectBtn: Button? = null
    private var imageBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        imageview = findViewById(R.id.imageView)
        resultScreen = findViewById(R.id.resultScreen)
        snapBtn = findViewById(R.id.snapBtn)
        detectBtn = findViewById(R.id.detectBtn)
        detectBtn!!.setOnClickListener(View.OnClickListener { detectText() })
        snapBtn!!.setOnClickListener(View.OnClickListener {
            if (checkPermission()) {
                captureImage()
            } else {
                requestPermission()
            }
        })
    }

    private fun checkPermission(): Boolean {
        val camerPermision =
            ContextCompat.checkSelfPermission(applicationContext, permission.CAMERA)
        return camerPermision == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val PERMISSION_CODE = 200
        ActivityCompat.requestPermissions(this, arrayOf(permission.CAMERA), PERMISSION_CODE)
    }

    private fun captureImage() {
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePicture.resolveActivity(packageManager) != null) {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            val cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (cameraPermission) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
                captureImage()
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val extras = data!!.extras
            imageBitmap = extras!!["data"] as Bitmap?
            imageview!!.setImageBitmap(imageBitmap)
        }
    }

    private fun detectText() {
        val image = InputImage.fromBitmap(imageBitmap!!, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = recognizer.process(image).addOnSuccessListener { text: Text ->
            val result1 = StringBuilder()
            for (block in text.textBlocks) {
                val blockText = block.text
                val blockCornerPoint = block.cornerPoints
                val blockFrame = block.boundingBox
                for (line in block.lines) {
                    val lineText = line.text
                    val lineCornerPoint = line.cornerPoints
                    val lineRect = line.boundingBox
                    for (element in line.elements) {
                        val elementText = element.text
                        result1.append(elementText)
                    }
                    resultScreen!!.text = blockText
                }
            }
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
}