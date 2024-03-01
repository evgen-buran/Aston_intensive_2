package com.buranchikov.astoncolorwheel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    val TAG = "myLog"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val btnReset = findViewById<Button>(R.id.btnReset)
//        btnReset.setOnClickListener {
//
//        }
        val colorWheel = ColorWheel(this, null)

        Log.d(TAG, "onCreate: $colorWheel.imageView")
    }
}