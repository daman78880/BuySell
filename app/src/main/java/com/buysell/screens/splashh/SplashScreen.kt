package com.buysell.screens.splashh

import SharedPref
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.buysell.MainActivity
import com.buysell.R
import com.buysell.base.Constant

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        init()
    }
    private fun init(){
        Handler(Looper.myLooper()!!).postDelayed({ startActivity(Intent(this@SplashScreen, MainActivity::class.java))finish() }, 2000)
    }
}