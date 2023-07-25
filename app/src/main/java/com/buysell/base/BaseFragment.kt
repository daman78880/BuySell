package com.buysell.base

import SharedPref
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.*

@AndroidEntryPoint
open class BaseFragment : Fragment(), OnCompleteListener<Location> {
    lateinit var client: FusedLocationProviderClient
    var locationAddress = MutableLiveData<String>()
    var updateLocation = MutableLiveData<Boolean>()

    @SuppressLint("SuspiciousIndentation")
    fun requestPermssionForLocation() {
        Dexter.withContext(activity).withPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
            .withListener(object : MultiplePermissionsListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0?.areAllPermissionsGranted() == true) {
                        Extentions.showProgress(requireContext())
                        client = LocationServices.getFusedLocationProviderClient(requireContext())
                        client.lastLocation.addOnCompleteListener(this@BaseFragment)
                    } else {
                        if (p0?.isAnyPermissionPermanentlyDenied == true) {
                            showSettingsDialog()
                        } else {
                            requestPermssionForLocation()
                        }
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?,
                ) {
                    p1!!.continuePermissionRequest()
                }
            })
            .withErrorListener {
            }.onSameThread().check()
    }
    override fun onComplete(p0: Task<Location>) {
        val location: Location? = p0.result
        if (location == null) {
            val mLocationRequest = LocationRequest()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationRequest.interval = 0
            mLocationRequest.fastestInterval = 0
            mLocationRequest.numUpdates = 1
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            } else {
                client.requestLocationUpdates(
                    mLocationRequest, object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            val mLastLocation: Location? = locationResult.lastLocation
                            Log.i(
                                Extentions.TAG, "newLoction is ${mLastLocation?.longitude} and lati ${mLastLocation?.latitude}"
                            )
                            getAddress(true, mLastLocation?.latitude!!, mLastLocation.longitude)
                        }
                    },
                    Looper.myLooper()
                )
            }
        } else {
            val perOne = (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            val perTwo = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (perOne && perTwo) {
                Log.i(
                    Extentions.TAG,
                    "Location is ${location.longitude} and lati ${location.latitude}"
                )
                getAddress(true, location.latitude, location.longitude)
            }
        }
    }
    fun getAddress(progressDialog: Boolean, latt: Double?, lngg: Double?) {
        Log.i("asbskanbjkajfnbdsa ","inside get address of location ")
        Log.i(TAG,"dialog $progressDialog , lat $latt , lng $lngg")
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latt!!, lngg!!, 1)
            val obj: Address = addresses!![0]
            var add: String = obj.getAddressLine(0)
            Log.i("asfkdjbajksfdba","obj    $obj")
            Log.i("asfkdjbajksfdba","" +
                    "adminArea  ${obj.adminArea}\n" +
                    "subAdminArea   ${obj.subAdminArea}\n" +
                    "locality   ${obj.locality}\n" +
                    "locale ${obj.locale}\n" +
                    "extras ${obj.extras}\n" +
                    "featureName    ${obj.featureName}\n" +
                    "thoroughfare   ${obj.thoroughfare}\n" +
                    "subLocality    ${obj.subLocality}\n" +
                    "premises   ${obj.premises}\n" +
                                                 "subThoroughfare   ${obj.subThoroughfare}\\n"+
                                                 "maxAddressLineIndex   ${obj.maxAddressLineIndex}\\n")

            add = """
            $add
            ${obj.countryName}
            """.trimIndent()
            add = """
            $add
            ${obj.countryCode}
            """.trimIndent()
            add = """
            $add
            ${obj.adminArea} // punjab
            """.trimIndent()
            add = """
            $add
            ${obj.postalCode} // 160071
            """.trimIndent()
            add = """
            $add
            ${obj.subAdminArea} // Ropar division
            """.trimIndent()
            add = """
            $add
            ${obj.locality} // sahibzada ajit singh nagar
            """.trimIndent()
            add = """
            $add
            ${obj.subThoroughfare}
            """.trimIndent()
            val addressLocation = "${obj.subLocality},${obj.locality} "
            Log.i(TAG,"addrss form functin is \n$addressLocation")
            if(progressDialog){
                Extentions.stopProgress()
            }

            locationAddress.value = addressLocation
                Log.i(TAG,"saving latlang in shareprefrence function called")
                SharedPref(requireContext()).saveString(Constant.LOCATION_NAME, addressLocation)
                SharedPref(requireContext()).saveString(Constant.LATITUDE_LOCATION, latt.toString())
                SharedPref(requireContext()).saveString(Constant.LONGITUDE_LOCATION, lngg.toString())
            if(SharedPref(requireContext()).getBoolean(Constant.UPDATE_LOCATION)){
                SharedPref(requireContext()).saveBoolean(Constant.UPDATE_LOCATION,false)
                updateLocation.value=true
            }

        } catch (e: IOException) {
            e.printStackTrace()
            Log.i(Extentions.TAG, "Error : ${e.message}")
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }
    fun checkLocationSetting() {
        Log.i(TAG,"getting location funcation called")
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            Log.i(TAG, "request permission run ")
            requestPermssionForLocation()
        }
        task.addOnFailureListener { exception ->
            val statusCode = (exception as ApiException).statusCode
            when (statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    val rae = exception as ResolvableApiException
                    // below code for auto again  request gpsEnable funcationality stop other wise comment below line
//                    rae.startResolutionForResult(context, 123)
                    // for activity result pass
//                        // resolvableApiException.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTING)
//                        // for fragment result pass
                    startIntentSenderForResult(
                        rae.resolution.intentSender,
                        123,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                    Log.d(TAG, "checkSetting: RESOLUTION_REQUIRED")
                } catch (sie: IntentSender.SendIntentException) {

                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    Log.i(
                        TAG,
                        "Location settings are inadequate, and cannot be fixed here. Fix in Settings."
                    )
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            123 -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Log.i(TAG,"on gps")
                        requestPermssionForLocation()
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.i(TAG,"cancel on gps")
                        checkLocationSetting()
                    }
                }
            }
        }
    }
    fun showSettingsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Need  Location Permissions")
        builder.setMessage("You have forcefully denied some of the required permissions for this action. Please open setting, go to permissions and allow them.")
        builder.setPositiveButton("SETTINGS") { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivityForResult(intent, 101)
        }
        builder.setCancelable(false)
        builder.show()
    }
    fun requestPermssionnq(context: Activity,position: Int, permission: ArrayList<String>):LiveData<Int>
    {
        var response=MutableLiveData<Int>()
        Dexter.withContext(context).withPermissions(permission)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0?.areAllPermissionsGranted() == true) {
                        if (position == 0) {
                            response.value=1
                        }
                        else {
                            response.value=2
                        }
                    }else {
                        if (p0?.isAnyPermissionPermanentlyDenied == true) {
                            if (position == 0) {
                                val per =
                                    "Required file and media and camera permission for click and image access ."
                                showSettingDialog(context, per)

                            } else {
                                val per = "Required file and media permission for image access."
                                showSettingDialog(context, per)
                            }
                        }
                        else {
                            requestPermssionnq(context, position, permission)
                        }
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?,
                ) {
                    p1?.continuePermissionRequest()
                }
            }).withErrorListener {
                Log.i(TAG,"inside error ${it.name}")
                if (position == 0) {
                    val per = "Required file and media and camera permission for click and image access ."
                    showSettingDialog(context, per)

                } else {
                    val per = "Required file and media permission for image access."
                    showSettingDialog(context, per)
                }
            }.onSameThread().check()
        return response
    }
    fun showSettingDialog(context: Activity,title: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage("You have forcefully denied some of the required permissions for this action. Please open setting, go to permissions and allow them.")
        builder.setPositiveButton("SETTINGS") { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivityForResult(intent, 101)
        }
        builder.setNegativeButton("Cancel"
        ) { dialog: DialogInterface, which: Int ->
            BaseFragment().requestPermssionForLocation()
            SharedPref(context).saveBoolean(Constant.DIALOG_OPENED,true)
            dialog.cancel()
        }
        builder.show()
    }
}