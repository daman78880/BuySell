package com.buysell.utils

import SharedPref
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent.getActivity
import android.content.*
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.buysell.R
import com.buysell.base.Constant
import com.buysell.screens.login.Data
import com.developers.imagezipper.ImageZipper
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.internal.managers.FragmentComponentManager
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object Extentions {
    const val TAG = "checkStautsLogq"
    var dialog: Dialog? = null
    var alertDialog: AlertDialog?=null
    var tempChatId:String?=null

    data class FilterPriceSortModel( var priceFrom:Int?=null,var priceTo:Int?=null)
    data class FilterSortModel(var newest:Boolean=true, var closest:Boolean=false, var lowToHigh:Boolean=false, var highToLow:Boolean=false)

    fun compareSecondsToCurrentTime(seconds: Long): String {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        val millis = seconds * 1000
        val date = Date(millis)
        calendar.time = date

        return when {
            calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) -> "Today"
            calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) - 1 -> "Yesterday"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        }
    }

    fun formatPricee(amount: Long): String {
        val format: NumberFormat = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 0
        format.currency = Currency.getInstance("INR")
        return format.format(amount)
    }

     fun convertSecond(secondtTime: Long): String? {
         val date = java.util.Date(secondtTime * 1000)
         val format = SimpleDateFormat("hh:mm a")
         return format.format(date)
    }
    @SuppressLint("SimpleDateFormat")
    fun getPostAgoTimeNew(time: Long): String {
        var returnTime = ""
            val seconds: Long = time / 1000
            val minutes: Long = time /60000
            val hours: Long = minutes/60
            val days: Long = hours/24
            val years: Long = days/365
            if (years > 0) {
                returnTime = "$years year ago"
            } else if (days > 0) {
                if (days > 1) {
                    returnTime = "$days days ago"
                } else {
                    returnTime = "$days day ago"
                }
            } else if (hours > 0) {
                if (hours > 1) {
                    returnTime = "$hours hours ago"
                } else {
                    returnTime = "$hours hour ago"
                }
            } else if (minutes > 0) {
                if (minutes > 1) {
                    returnTime = "$minutes mins ago"
                } else {
                    returnTime = "$minutes min ago"
                }
            } else {
                returnTime = "$seconds sec ago"
            }
        return returnTime
    }


    @SuppressLint("SimpleDateFormat")
    fun getPostAgoTime(time: String): String {
        var returnTime = ""
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateStr = time
        try {
            val date = format.parse(dateStr)
            val time = System.currentTimeMillis() - (date?.time ?: 0)
            Log.i(TAG,"current time ${System.currentTimeMillis()}\nget time ${date?.time} minues time$time")
            Log.i(TAG, "time is remender is $time")
            val seconds: Long = ((time / 1000) % 60)
            val minutes: Long = ((time / (1000 * 60)) % 60)
            val hours: Long = ((time / (1000 * 60 * 60)) % 24)
            val years: Long = (time / (1000L * 60 * 60 * 24 * 365))
            val days: Long = ((time / (1000 * 60 * 60 * 24)) % 365)
            Log.i(TAG,"\nsec $seconds\nmin$minutes,\nhour$hours,\nyear $years\ndays$days")
            /*if(years>0){
                returnTime="Posted $years year, $days days, $hours hours, $minutes min ago"
            }else if(days>0){
                returnTime="Posted $days days, $hours hours, $minutes min ago"
            }else if(hours>0){
                returnTime="Posted $hours hours, $minutes min ago"
            }else if(minutes>0){
                returnTime="Posted $minutes min ago"
            }else{
                returnTime="Posted $seconds sec ago"
            }*/
            if (years > 0) {
                returnTime = "Posted $years year ago"
            } else if (days > 0) {
                if (days > 1) {
                    returnTime = "Posted $days days ago"
                } else {
                    returnTime = "Posted $days day ago"
                }
            } else if (hours > 0) {
                if (hours > 1) {
                    returnTime = "Posted $hours hours ago"
                } else {
                    returnTime = "Posted $hours hour ago"
                }
            } else if (minutes > 0) {
                if (minutes > 1) {
                    returnTime = "Posted $minutes mins ago"
                } else {
                    returnTime = "Posted $minutes min ago"
                }
            } else {
                returnTime = "Posted $seconds sec ago"
            }
        } catch (e: ParseException) {
            Log.i(Extentions.TAG, "eoror ${e.printStackTrace()}")
        }
        return returnTime
    }

    fun hideKeyboardd(context: Context) {
        val activity = FragmentComponentManager.findActivity(context) as Activity
//        val activity = context as AppCompatActivity
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    fun showNoInternetDialog(context: Context){
        try {
            if (alertDialog == null) {
                alertDialog = AlertDialog.Builder(context).create()
                alertDialog?.setTitle("Info")
                alertDialog?.setMessage("Internet not available, Cross check your internet connectivity and try again")
//      alertDialog.setIcon(R.drawable.alerticon);
                alertDialog?.setCancelable(false)
                alertDialog?.show()
            } else {

                if (!alertDialog?.isShowing!!) {
                    alertDialog?.show()
                }
            }
        }catch (e:Exception){
            Log.i(TAG,"something error occured durng showNoInterntDialog e${e.message}")
        }
    }
    fun showProgress(context: Context) {
        if (dialog == null) {
            dialog = Dialog(context)
            dialog!!.setContentView(R.layout.loader)
            dialog!!.setCancelable(false)
            dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog!!.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            dialog!!.show()
        } else {
            val contextt = FragmentComponentManager.findActivity(context) as Activity
            if (!contextt.isFinishing) {
                if (!(dialog?.isShowing!!)) {
                    try {
                        dialog?.show()
                    } catch (e: Exception) {
                        Log.i(TAG, "Catch error in showing dialog error msg : ${e.message}")
                    }
                }
            }

        }
    }

    fun stopNoInternetConnectionDialog(){
        if(alertDialog!=null){
            alertDialog!!.dismiss()
        }
    }
    fun stopProgress() {
        if (dialog != null)
            dialog!!.dismiss()
    }


    fun validatePassword(password: String, minLength: Int, maxLength: Int): String? {
        if (password.length < minLength) {
            return "Password is too short, minimum length is $minLength"
        }
        if (password.length > maxLength) {
            return "Password is too long, maximum length is $maxLength"
        }
        return null // Password is valid
    }

    fun validateInput(input: String, minLength: Int, maxLength: Int): String? {
        val pattern = "^[a-zA-Z ]*\$".toRegex() // regular expression to match only alphabets and spaces
        if (!input.matches(pattern)) {
            return "Input can only contain alphabets and spaces"
        }
        if (input.length < minLength) {
            return "Input is too short, minimum length is $minLength"
        }
        if (input.length > maxLength) {
            return "Input is too long, maximum length is $maxLength"
        }
        return null // Input is valid
    }
    fun getFailedMsg(e: HttpException): Extentions.NetworkErrorException? {
        var msg: Extentions.NetworkErrorException? = null
        when (e.code()) {
            502 -> {
                msg = NetworkErrorException(e.code(), "internal error!")
            }
            401 -> {
                throw AuthenticationException("authentication error!")
            }
            400 -> {
                msg = NetworkErrorException.parseException(e)
            }
        }
        return msg
    }

    open class NetworkErrorException(
        val errorCode: Int = -1,
        val errorMessage: String,
        val response: String = ""
    ) : Exception() {
        override val message: String
            get() = localizedMessage

        override fun getLocalizedMessage(): String {
            return errorMessage
        }

        companion object {
            fun parseException(e: HttpException): NetworkErrorException {
                Log.i(TAG, "inside network error excetpion")
                val errorBody = e.response()?.errorBody()?.string()

                return try {//here you can parse the error body that comes from server
                    NetworkErrorException(e.code(), JSONObject(errorBody!!).getString("message"))
                } catch (_: Exception) {
                    NetworkErrorException(e.code(), "unexpected error!!ً")
                }
            }
        }
    }

    class AuthenticationException(authMessage: String) :
        NetworkErrorException(errorMessage = authMessage)

    fun getFilePath(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } else
            return null
    }

    fun imageCompressor(context: Context, file: File): File {
        val imageCompress = ImageZipper(context)
            .setQuality(100)
            .setMaxWidth(700)
            .setMaxHeight(700)
            .compressToFile(file)
        return imageCompress
    }
    fun saveLoginData(context: Context, data: Data) {
        if (data.id != null) {
            SharedPref(context).saveInt(Constant.ID_kEY, data.id)
        }
        if (data.name != null) {
            SharedPref(context).saveString(
                Constant.NAME_KEY,
                data.name
            )
        }
        if (data.email != null) {
            SharedPref(context).saveString(Constant.EMAIL_KEY, data.email)
        }
        if (data.memberSince != null) {
            SharedPref(context).saveString(
                Constant.MEMBER_SINCE,
                data.memberSince
            )
        }
        if (data.isEmailverified != null) {
            SharedPref(context).saveBoolean(
                Constant.VERIFIED_EMAIL,
                data.isEmailverified
            )
        }
        if (data.isMobileverified != null) {
            SharedPref(context).saveBoolean(Constant.VERIFIED_NUMBER, data.isMobileverified)
            if (data.isMobileverified) {
                SharedPref(context).saveString(Constant.NUMBER_KEY, data.phoneNumber.toString())
            }
        }
        if (data.loginType != null) {
            SharedPref(context).saveString(Constant.LOGIN_TYPE, data.loginType)
        }


        if ((data.profileUrl != null) && data.profileUrl.toString().isNotEmpty()) {
            SharedPref(context).saveString(Constant.PROFILE_URL, data.profileUrl.toString())
        }
        if ((data.countryCode != null) && data.countryCode.toString().isNotEmpty()) {
            SharedPref(context).saveString(Constant.COUNTRY_CODE, data.countryCode.toString())
        }
        if ((data.fcmToken != null) && data.fcmToken.toString().isNotEmpty()) {
            SharedPref(context).saveString(Constant.FCM_TOKEN, data.fcmToken.toString())
        }
        if ((data.socialToken != null) && data.socialToken.toString().isNotEmpty()) {
            SharedPref(context).saveString(Constant.SOCIAL_TOKEN, data.socialToken.toString())
        }
        if ((data.socialmediaId != null) && data.socialmediaId.toString().isNotEmpty()) {
            SharedPref(context).saveString(
                Constant.SOCIAL_MEDIA_TOKEN,
                data.socialmediaId.toString()
            )
        }
        subscribeTopicForNotification(context)
    }
    fun showKeyBoard(editText: AppCompatEditText, context: Context) {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
    fun unSubscribedTopic(context:Context){
        val topicId="topic"+SharedPref(context).getInt(Constant.ID_kEY).toString()
//        val topicId=SharedPref(context).getInt(Constant.ID_kEY).toString()
        Log.i("asnasjkfnadsfksnfd","unSubscriped topic is->$topicId")
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicId).addOnSuccessListener {
            Log.i("asnasjkfnadsfksnfd","unSubscribe topic sucessfull")
        }.addOnFailureListener {
            Log.i("asnasjkfnadsfksnfd","unSubscribe topic failed due to ${it.message}")
        }
    }
     fun subscribeTopicForNotification(context: Context) {
         val token="topic"+SharedPref(context).getInt(Constant.ID_kEY).toString()
//         val token=SharedPref(context).getInt(Constant.ID_kEY).toString()
         Log.i("asnasjkfnadsfksnfd","topic is->$token")
//         context.startService(Intent(context,MyFirebaseMessagingService::class.java))
         FirebaseMessaging.getInstance().subscribeToTopic(token).addOnSuccessListener {
            Log.i("asnasjkfnadsfksnfd","subscriped check")
        }.addOnFailureListener {
            Log.i("asnasjkfnadsfksnfd","not subscribed check")
        }
    }

}