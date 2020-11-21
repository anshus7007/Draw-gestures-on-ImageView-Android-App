package com.anshu.intern

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.gesture.GestureOverlayView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    private var gestureOverlayView: GestureOverlayView? = null
    private var redrawButton: Button? = null
    private var saveButton: Button? = null
    lateinit var img_pick_btn:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        img_pick_btn = findViewById(R.id.pick_image)
        img_pick_btn.setOnClickListener {
            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED
                ) {
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                } else {
                    //permission already granted
                    pickImageFromGallery();
                }
            } else {
                //system OS is < Marshmallow
                pickImageFromGallery();
            }
        }

        title = "dev2qa.com - Android Capture Signature By Gesture."
        init()
        gestureOverlayView!!.addOnGesturePerformedListener(CustomGestureListener())
        redrawButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                gestureOverlayView!!.clear(false)
            }
        })
        saveButton!!.setOnClickListener {
            checkPermissionAndSaveSignature()
        }
    }
    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1
        private val PERMISSION_CODE = 1001;
    }
    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }


    private fun init() {
        if (gestureOverlayView == null) {
            gestureOverlayView = findViewById<View>(R.id.sign_pad) as GestureOverlayView
        }
        if (redrawButton == null) {
            redrawButton = findViewById<View>(R.id.redraw_button) as Button
        }
        if (saveButton == null) {
            saveButton = findViewById<View>(R.id.save_button) as Button
        }
    }
    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup granted
                    pickImageFromGallery()

                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION -> {
                if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
                    val grantResultsLength = grantResults.size
                    if (grantResultsLength > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        println("Under external srorage")
                        saveSignature()
                    } else {
                        Toast.makeText(applicationContext, "You denied write external storage permission.", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            var bmImg:Bitmap?= null
            val pic=data?.data
            val picInputStream: InputStream? = pic?.let { contentResolver.openInputStream(it) }
            val real_img= BitmapFactory.decodeStream(picInputStream)
            val ob:BitmapDrawable =  BitmapDrawable(resources, real_img)
            sign_pad.background=ob

        }
    }

    private fun checkPermissionAndSaveSignature() {
         if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
            ) {
//                Log.v(TAG, "Permission is granted")
                    saveSignature()
            } else {
//                Log.v(TAG, "Permission is revoked")
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        2
                )
                Toast.makeText(this, "Nope", Toast.LENGTH_SHORT).show()


            }
        } else { //permission is automatically granted on sdk<23 upon installation
            saveSignature()
        }
    }


    private fun saveSignature() {

            // First destroy cached image.
            gestureOverlayView!!.destroyDrawingCache()

            // Enable drawing cache function.
            gestureOverlayView!!.isDrawingCacheEnabled = true

            // Get drawing cache bitmap.
            val drawingCacheBitmap = gestureOverlayView!!.drawingCache

            // Create a new bitmap
            val bitmap = Bitmap.createBitmap(drawingCacheBitmap)

            var stream: FileOutputStream? =null
            val storageDirectory=Environment.getExternalStorageDirectory()
            val dir=File(storageDirectory.absolutePath.toString() + "/Intern")
            if (!dir.exists()) {
                dir.mkdirs();
            }
            val fileName= String.format("%d.png", System.currentTimeMillis())
            val file=File(dir, fileName)
            try {
                println("Hello")
                stream= FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream!!)
                println("Bye")
                Toast.makeText(this, "Save", Toast.LENGTH_SHORT).show()

            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            try {
                stream!!.flush()

            }
            catch (e: IOException)
            {
                e.printStackTrace()
            }
            try {
                stream!!.close()

            }
            catch (e: IOException)
            {
                e.printStackTrace()
            }
    }



}