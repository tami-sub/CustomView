package com.example.customview

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {
    private lateinit var clockView: ClockView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clockView = ClockView(this, circleColor = getColor(R.color.orange))
        findViewById<FrameLayout>(R.id.frame_layout).addView(clockView)
        findViewById<LinearLayout>(R.id.main_linear_layout).orientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                LinearLayout.VERTICAL
            } else LinearLayout.HORIZONTAL
    }
}