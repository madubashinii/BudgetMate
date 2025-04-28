package com.example.personalfinancetracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_splash_screen)

        val imageView: ImageView = findViewById(R.id.imageView)

        // Load animation
        val zoomFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        imageView.startAnimation(zoomFadeIn)

        Handler().postDelayed({
            // Directly go to Onboarding screen
            startActivity(Intent(this@SplashScreen, LoginActivity::class.java))
            finish() // Close SplashActivity
        }, 2000) // Delay for 3 seconds

    }
}