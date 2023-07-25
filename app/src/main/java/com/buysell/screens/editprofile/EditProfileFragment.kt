package com.buysell.screens.editprofile

import SharedPref
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.base.Constant
import com.buysell.databinding.FragmentEditProfileBinding
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.AndroidEntryPoint
import hideKeyBoard
import retrofit2.HttpException
import setToolBar

@AndroidEntryPoint
class EditProfileFragment : BaseFragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private val viewModelEditProfile: ViewModelEditProfile by viewModels()
    var value = 1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        val appBarTitle = requireActivity().resources.getString(R.string.edit_profile)
        setToolBar(binding.appBarEditProfile, requireContext(), true, false, appBarTitle, 10f)
        binding.model = viewModelEditProfile
        clickListeners()
        setName()
        checkVerifiedEmail()
        checkVerifiedNumber()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted  {
            viewModelEditProfile.nameResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(TAG,
                                "Error ${res?.message} , Error code:${res?.errorCode} , localized msg:${res?.localizedMessage}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        val data: PojoNameChange = Gson().fromJson(response, PojoNameChange::class.java)
                        if (data.status == 200) {
                            if (data.success) {
                                SharedPref(requireContext()).saveString(Constant.NAME_KEY,
                                    viewModelEditProfile.name.get().toString())
                                setName()
                                Toast.makeText(requireContext(),
                                    "Password changed",
                                    Toast.LENGTH_SHORT).show()
                            }
                        } else if (data.status == 201) {
                            Toast.makeText(requireContext(), "${data.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelEditProfile.verifyNumberResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(TAG,
                                "Error ${res?.message} , Error code:${res?.errorCode} , localized msg:${res?.localizedMessage}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        if (response.has("status")) {
                            if (response.get("status").toString().toInt() == 200) {
                                SharedPref(requireContext()).saveBoolean(Constant.VERIFIED_NUMBER, true)
                                checkVerifiedEmail()
                                checkVerifiedNumber()
                            }  else {
                                Toast.makeText(requireContext(),
                                    "${response.get("message")}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModelEditProfile.updateNumberResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(TAG,
                                "Error ${res?.message} , Error code:${res?.errorCode} , localized msg:${res?.localizedMessage}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(requireContext(),
                                "Failed due to ${data.msg}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        if (response.has("status")) {
                            if (response.get("status").toString().toInt() == 200) {
                                SharedPref(requireContext()).saveString(Constant.NUMBER_KEY, viewModelEditProfile.number.get().toString().trim())
                                checkVerifiedEmail()
                                checkVerifiedNumber()
                                if (!SharedPref(requireContext()).getBoolean(Constant.VERIFIED_NUMBER)) {
                                    numberVerifyDialog(requireContext())
                                }
                            }  else {
                                Toast.makeText(requireContext(),
                                    "${response.get("message")}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
    }

    private fun clickListeners() {
        binding.apply {
            viewEditProfile.setOnClickListener {
                requireActivity().hideKeyBoard()
            }
        }
    }

    private fun checkVerifiedEmail() {
        binding.apply {
            val email = SharedPref(requireContext()).getString(Constant.EMAIL_KEY)
            txtVerifiedEmailEditProfile.text =
                requireActivity().resources.getString(R.string.verified)
            txtVerifiedEmailEditProfile.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.greenColor))
            if (email.isNotEmpty()) {
                viewModelEditProfile.email.set(email)
            }
        }
    }

    private fun setName() {
        binding.apply {
            val name = SharedPref(requireContext()).getString(Constant.NAME_KEY)
            if (name.isNotEmpty()) {
                viewModelEditProfile.name.set(name)
            }
        }
    }

    private fun checkVerifiedNumber() {
        binding.apply {
            val numberVerified = SharedPref(requireContext()).getBoolean(Constant.VERIFIED_NUMBER)
            if (numberVerified) {
                viewModelEditProfile.verifedNumberTxt.set( requireActivity().resources.getString(R.string.verified))
                txtVerifyNumberEditProfile.setTextColor(ContextCompat.getColor(requireContext(), R.color.greenColor))

            } else {
                viewModelEditProfile.verifedNumberTxt.set( requireActivity().resources.getString(R.string.verify))
                txtVerifyNumberEditProfile.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_Txt))
            }
            val number = SharedPref(requireContext()).getString(Constant.NUMBER_KEY)
            if (number.isNotEmpty()) {
                viewModelEditProfile.number.set(number)
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
            Extentions.showKeyBoard(editOne, context)
        } else if (value == 2) {
            editTwo.requestFocus()
            Extentions.showKeyBoard(editOne, context)
        } else if (value == 3) {
            editThree.requestFocus()
            Extentions.showKeyBoard(editOne, context)
        } else if (value == 4) {
            editFour.requestFocus()
            Extentions.showKeyBoard(editOne, context)
        } else if (value == 5) {
            editFour.requestFocus()
            Extentions.showKeyBoard(editOne, context)
        } else {
            editOne.requestFocus()
            Extentions.showKeyBoard(editOne, context)
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
                viewModelEditProfile.hitVerifyNumberApi(token, jsonObject)
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
