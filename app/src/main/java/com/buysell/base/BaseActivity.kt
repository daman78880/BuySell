package com.buysell.base

import android.content.Context
import android.content.DialogInterface
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.buysell.R
import com.buysell.utils.Extentions.TAG
import com.buysell.utils.InterneCheck


open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBoadCastt()
    }

    fun registerBoadCastt(){
        val  receiver = InterneCheck()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(receiver,  IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(receiver,  IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        val  receiver = InterneCheck()
        try {
            unregisterReceiver(receiver)
        }
        catch (e:Exception){
            Log.i(TAG,"Unable to unreguster because ${e.message}")
        }
    }
}