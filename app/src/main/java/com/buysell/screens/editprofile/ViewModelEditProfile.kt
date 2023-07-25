package com.buysell.screens.editprofile

import SharedPref
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.buysell.R
import com.buysell.base.Constant
import com.buysell.repository.RepositoryImplementation
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.buysell.utils.Extentions.showKeyBoard
import com.buysell.utils.Extentions.validateInput
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.UnsupportedEncodingException
import javax.inject.Inject

@HiltViewModel
class ViewModelEditProfile @Inject constructor(private val repositoryImplementation: RepositoryImplementation) :
    ViewModel() {
    var name = ObservableField<String>("")
    var number = ObservableField<String>("")
    var verifedNumberTxt = ObservableField<String>("Verify")
    var email = ObservableField<String>("")
    var code = ObservableField<String>("+91")

    private val editNameResponseMutable: MutableStateFlow<ApiState> =
        MutableStateFlow(ApiState.Empty)
    var nameResponse: StateFlow<ApiState> = editNameResponseMutable

    private val editVerifyNumberResponseMutable: MutableStateFlow<ApiState> =
        MutableStateFlow(ApiState.Empty)
    var verifyNumberResponse: StateFlow<ApiState> = editVerifyNumberResponseMutable

    private val editUpdateNumberResponseMutable: MutableStateFlow<ApiState> =
        MutableStateFlow(ApiState.Empty)
    var updateNumberResponse: StateFlow<ApiState> = editUpdateNumberResponseMutable

    var list: ArrayList<PojoCountrys>? = null
    var selectedCountryCode: String? = null
    var value = 1


    fun onClick(view: View) {
        when (view.id) {
            R.id.btnSaveEditProfile -> {
                if (name.get().toString().trim().isNotEmpty()) {
                    val nameResult=validateInput(name.get().toString().trim(),5,20)
                    if(nameResult==null) {
                        val jsonObject = JsonObject()
                        jsonObject.addProperty("name", name.get()?.trim())
                        val token = SharedPref(view.context).getString(Constant.TOKEN_kEY)
                        hitChangeNameApi(token, jsonObject)
                    }else{
                        Toast.makeText(view.context, nameResult, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(view.context, "Please enter name", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.txtCountryCodeEditProfile -> {
                list = getCountries(view.context)
                countryCodeDialog(view.context)
            }

            R.id.txtVerifyNumberEditProfile -> {
                if (SharedPref(view.context).getString(Constant.NUMBER_KEY).isNotEmpty()) {
                    if (SharedPref(view.context).getBoolean(Constant.VERIFIED_NUMBER)) {

                    } else {
                        numberVerifyDialog(view.context)
                    }
                } else {
                    if (number.get().toString().trim().length == 10) {
                        val token = SharedPref(view.context).getString(Constant.TOKEN_kEY)
                        val jsonObject = JsonObject()
                        jsonObject.addProperty("phoneNumber", number.get().toString().trim())
                        jsonObject.addProperty("countryCode", code.get().toString().trim())
                        hitUpdateNumberApi(token, jsonObject)
                    } else {
                        Toast.makeText(view.context,
                            "Please enter valid number",
                            Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

    fun getCountries(context: Context): ArrayList<PojoCountrys> {
        var countries: ArrayList<PojoCountrys>? = null
        var inputStream: InputStream = context.resources.openRawResource(R.raw.countries)
        try {
            val reader: Reader = InputStreamReader(inputStream, "UTF-8")
            val gson = Gson()
            countries = gson.fromJson(reader, object : TypeToken<ArrayList<PojoCountrys>>() {}.type)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return countries!!
    }

    private fun countryCodeDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.rv_country_code_dialog)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(context,
            android.R.color.transparent))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(false)
        var adapter: AdapterCountryNameCode? = null
        var cancelBtn = dialog.findViewById<AppCompatTextView>(R.id.txtTempCancelCountryCodeDialog)
        var searchView = dialog.findViewById<AppCompatEditText>(R.id.etNameCountryCodeDialog)
        val rvCountry = dialog.findViewById<RecyclerView>(R.id.rvcountryDialog)
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        if (list != null) {
            adapter =
                AdapterCountryNameCode(context, list!!, object : AdapterCountryNameCode.Click {
                    override fun onClick(name: String, codee: String) {
                        selectedCountryCode = codee
                        code.set(codee)
                        dialog.dismiss()
                    }
                })
            rvCountry.adapter = adapter
        }
        searchView.doOnTextChanged { text, start, before, count ->
            Log.i("sdfjnaskjdfa","text $text")
            adapter?.filter?.filter(text)
        }
        dialog.show()
    }


    fun hitChangeNameApi(token: String, jsonObject: JsonObject) {
        CoroutineScope(Dispatchers.IO).launch {
            editNameResponseMutable.value = ApiState.Loading
            repositoryImplementation.changeName(token, jsonObject)
                .catch { e ->
                    editNameResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    editNameResponseMutable.value = ApiState.Success(data)
                }
        }
    }

    fun hitUpdateNumberApi(token: String, jsonObject: JsonObject) {
        CoroutineScope(Dispatchers.IO).launch {
            editUpdateNumberResponseMutable.value = ApiState.Loading
            repositoryImplementation.updateNumber(token, jsonObject)
                .catch { e ->
                    editUpdateNumberResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    editUpdateNumberResponseMutable.value = ApiState.Success(data)
                }
        }
    }

    fun hitVerifyNumberApi(token: String, jsonObject: JsonObject) {
        CoroutineScope(Dispatchers.IO).launch {
            editVerifyNumberResponseMutable.value = ApiState.Loading
            repositoryImplementation.verifyNumber(token, jsonObject)
                .catch { e ->
                    editVerifyNumberResponseMutable.value = ApiState.Failure(e)
                }.collect { data ->
                    editVerifyNumberResponseMutable.value = ApiState.Success(data)
                }
        }
    }


    private fun showOtpKeyBoard(
        editOne: AppCompatEditText,
        editTwo: AppCompatEditText,
        editThree: AppCompatEditText,
        editFour: AppCompatEditText, context: Context,
    ) {
        if (value == 1) {
            editOne.requestFocus()
            showKeyBoard(editOne, context)
        } else if (value == 2) {
            editTwo.requestFocus()
            showKeyBoard(editOne, context)
        } else if (value == 3) {
            editThree.requestFocus()
            showKeyBoard(editOne, context)
        } else if (value == 4) {
            editFour.requestFocus()
            showKeyBoard(editOne, context)
        } else if (value == 5) {
            editFour.requestFocus()
            showKeyBoard(editOne, context)
        } else {
            editOne.requestFocus()
            showKeyBoard(editOne, context)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun numberVerifyDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_verify_otp)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(context,
            android.R.color.transparent))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(false)
        val verifyNumberBtn = dialog.findViewById<AppCompatButton>(R.id.btnVerifyOtpDialog)
        val resendBtn = dialog.findViewById<AppCompatTextView>(R.id.txtResendOtpDialog)
        val cancelBtn = dialog.findViewById<AppCompatImageView>(R.id.btnCancelOtpDialog)
        val editOne = dialog.findViewById<AppCompatEditText>(R.id.etFirstOtpDilog)
        val editTwo = dialog.findViewById<AppCompatEditText>(R.id.etSecondOtpDilog)
        val editThree = dialog.findViewById<AppCompatEditText>(R.id.etThirdOtpDilog)
        val editFour = dialog.findViewById<AppCompatEditText>(R.id.etFourOtpDilog)
        value = 1
        editOne.setOnTouchListener { v, event ->
            if (MotionEvent.ACTION_UP == event?.action) {
                showOtpKeyBoard(editOne, editTwo, editThree, editFour, context)
            }
            true
        }
        editTwo.setOnTouchListener { v, event ->
            if (MotionEvent.ACTION_UP == event?.action) {
                showOtpKeyBoard(editOne, editTwo, editThree, editFour, context)
            }
            true
        }
        editThree.setOnTouchListener { v, event ->
            if (MotionEvent.ACTION_UP == event?.action) {
                showOtpKeyBoard(editOne, editTwo, editThree, editFour, context)
            }
            true
        }
        editFour.setOnTouchListener { v, event ->
            if (MotionEvent.ACTION_UP == event?.action) {
                showOtpKeyBoard(editOne, editTwo, editThree, editFour, context)
            }
            true
        }
        editOne.doOnTextChanged { text, start, before, count ->
            if (count == 0) {
                value -= 1
            } else {
                if (value == 0) {
                    value += 2
                } else {
                    value += 1
                }
            }
            showOtpKeyBoard(editOne, editTwo, editThree, editFour, context)
        }
        editTwo.doOnTextChanged { text, start, before, count ->
            if (count == 0) {
                value -= 1
            } else {
                value += 1
            }
            showOtpKeyBoard(editOne, editTwo, editThree, editFour, context)
        }
        editThree.doOnTextChanged { text, start, before, count ->
            if (count == 0) {
                value -= 1
            } else {
                value += 1
            }
            showOtpKeyBoard(editOne, editTwo, editThree, editFour, context)
        }
        editFour.doOnTextChanged { text, start, before, count ->
            if (count == 0) {
                value -= 1
            } else {
                value += 1
            }
            showOtpKeyBoard(editOne, editTwo, editThree, editFour, context)
        }
        editOne.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (value > 1) {
                        if (editOne.text?.isEmpty()!!) {
                            value -= 1
                        }
                    }
                }
                if (keyCode == KeyEvent.KEYCODE_0 || keyCode == KeyEvent.KEYCODE_1 || keyCode == KeyEvent.KEYCODE_3 ||
                    keyCode == KeyEvent.KEYCODE_2 ||
                    keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_5 || keyCode == KeyEvent.KEYCODE_6 ||
                    keyCode == KeyEvent.KEYCODE_7 || keyCode == KeyEvent.KEYCODE_8 || keyCode == KeyEvent.KEYCODE_9
                ) {
                    if (editOne.text?.isNotEmpty()!!) {
                        value += 1
                    }
                }
                showOtpKeyBoard(editOne, editTwo, editThree, editFour, context)
                return false
            }

        })
        editTwo.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (value >= 2) {
                        if (editTwo.text?.isEmpty()!!) {
                            value -= 1
                        }
                    }
                }
                if (keyCode == KeyEvent.KEYCODE_0 || keyCode == KeyEvent.KEYCODE_1 || keyCode == KeyEvent.KEYCODE_3 ||
                    keyCode == KeyEvent.KEYCODE_2 ||
                    keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_5 || keyCode == KeyEvent.KEYCODE_6 ||
                    keyCode == KeyEvent.KEYCODE_7 || keyCode == KeyEvent.KEYCODE_8 || keyCode == KeyEvent.KEYCODE_9
                ) {
                    if (editTwo.text?.isNotEmpty()!!) {
                        value += 1
                    }
                }
                showOtpKeyBoard(editOne, editTwo, editThree, editFour, context)
                return false
            }
        })
        editThree.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (value == 3) {
                        Log.i(TAG, "3 minus")
                        if (editThree.text?.isEmpty()!!) {
                            value -= 1
                        }
                    }
                }
                if (keyCode == KeyEvent.KEYCODE_0 || keyCode == KeyEvent.KEYCODE_1 || keyCode == KeyEvent.KEYCODE_3 ||
                    keyCode == KeyEvent.KEYCODE_2 ||
                    keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_5 || keyCode == KeyEvent.KEYCODE_6 ||
                    keyCode == KeyEvent.KEYCODE_7 || keyCode == KeyEvent.KEYCODE_8 || keyCode == KeyEvent.KEYCODE_9
                ) {
                    if (editThree.text?.isNotEmpty()!!) {
                        value += 1
                    }
                }
                showOtpKeyBoard(editOne, editTwo, editThree, editFour, context)
                return false
            }

        })
        editFour.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (value >= 4) {
                        if (editFour.text?.isEmpty()!!) {
                            value -= 1
                        }
                    }
                }
                showOtpKeyBoard(editOne, editTwo, editThree, editFour, context)
                return false
            }
        })
        cancelBtn.setOnClickListener {
            Extentions.hideKeyboardd(context)
            dialog.dismiss()
        }
        verifyNumberBtn.setOnClickListener {
            val otp = "${editOne.text}${editTwo.text}${editThree.text}${editFour.text}"
            if (otp.length == 4) {
                val jsonObject = JsonObject()
                jsonObject.addProperty("mobileOtp", otp)
                val token = SharedPref(context).getString(Constant.TOKEN_kEY)
                hitVerifyNumberApi(token, jsonObject)
                Extentions.hideKeyboardd(context)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please enter otp", Toast.LENGTH_SHORT).show()
            }
        }
        resendBtn.setOnClickListener {
            Extentions.hideKeyboardd(context)
            dialog.dismiss()
        }
        dialog.show()
    }


}