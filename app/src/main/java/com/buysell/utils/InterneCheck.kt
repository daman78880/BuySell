package com.buysell.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.buysell.utils.Extentions.TAG
import com.buysell.utils.Extentions.showNoInternetDialog
import com.buysell.utils.Extentions.stopNoInternetConnectionDialog

class InterneCheck : BroadcastReceiver() {

    @Override
    override fun onReceive(p0: Context, p1: Intent?) {
        if (checkInternet(p0)) {
        stopNoInternetConnectionDialog()
        }
        else{
        showNoInternetDialog(p0)
        }
    }

    fun checkInternet( context:Context):Boolean {
      val   serviceManager =  ServiceManager(context);
        return serviceManager.isNetworkAvailable()
    }
}
class ServiceManager(var context: Context) {

    fun isNetworkAvailable():Boolean
       {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
}