package com.buysell.screens.account

import SharedPref
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.base.Constant
import com.buysell.databinding.FragmentAccountBinding
import com.buysell.screens.login.PojoLogin
import com.buysell.utils.ApiState
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import setToolBar
import java.io.File

class AccountFragment : BaseFragment() {
    private lateinit var binding: FragmentAccountBinding
    private val viewModelAccount: ViewModelAccount by viewModels()
    private var imageUriSecond: Uri? = null
    private val CAMERA_RESULT = 112
    private val GALLERY_RESULT = 1512
    private var token:String?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAccountBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Extentions.stopProgress()
        init()
    }


    private fun init() {
        val appBarTitle = requireActivity().resources.getString(R.string.myProfile)
        setToolBar(binding.appBarAccount, requireContext(), true, false, appBarTitle, 10f, true)
        clickListner()
        binding.apply {
            token=SharedPref(requireContext()).getString(Constant.TOKEN_kEY)
            model=viewModelAccount
            imgUserImageAccount.clipToOutline = true
            cameraLayoutAccount.clipToOutline = true
            txtUserNameAccount.text=SharedPref(requireContext()).getString(Constant.NAME_KEY)
            txtUserCreatedSinceAccount.text=SharedPref(requireContext()).getString(Constant.MEMBER_SINCE)
           setProfile()
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted  {
            viewModelAccount.updateProfilePicResponse.collect { data ->
                when (data) {
                    is ApiState.Loading -> {
                        Extentions.showProgress(requireContext())
                    }
                    is ApiState.Failure -> {
                        Extentions.stopProgress()
                        if (data.msg is HttpException) {
                            val res = Extentions.getFailedMsg(data.msg)
                            Log.i(TAG, "Error ${res?.message} , Error code:${res?.errorCode} , localized msg:${res?.localizedMessage}")
                            Toast.makeText(requireContext(), "${res?.message}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed due to ${data.msg}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Success -> {
                        Extentions.stopProgress()
                        val response = data.data as JsonObject
                        val data: PojoLogin = Gson().fromJson(response, PojoLogin::class.java)
                        Log.i(TAG,"Account upload Image response is $response")
                        if (data.status  == 200) {
//                            Log.i(TAG,"token is ${data.token}")
                            Extentions.saveLoginData(requireContext(),data.data)
                            setProfile()
                        }
                        else {
                                Toast.makeText(requireContext(), data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Empty -> {

                    }
                }
            }
        }
    }

    private fun clickListner() {
        binding.apply {
            cLayoutEditAccount.setOnClickListener {
                findNavController().navigate(R.id.action_accountFragment_to_editProfileFragment)
            }
            cLayoutSettingAccount.setOnClickListener {
                findNavController().navigate(R.id.action_accountFragment_to_settingFragment)
            }
            cLayoutHelpAndSupportAccount.setOnClickListener {
                findNavController().navigate(R.id.action_accountFragment_to_helpAndSupportFragment)
            }
            cLayoutNotificationAccount.setOnClickListener {
                findNavController().navigate(R.id.action_accountFragment_to_notificationFragment)
            }
            cameraLayoutAccount.setOnClickListener{
                chooseImageDialog(requireContext())
            }
        }
    }
    private fun chooseImageDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_choose_from_upload_image)
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.color.transparent))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(false)
        val cancelBtn = dialog.findViewById<AppCompatTextView>(R.id.btnCancelChooseImageDialog)
        val selectPhotoBtn = dialog.findViewById<AppCompatTextView>(R.id.btnSelectPhotoChooseImageDialog)
        val takePhotoBtn = dialog.findViewById<AppCompatTextView>(R.id.btnTakePhotoChooseImageDialog)
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        selectPhotoBtn.setOnClickListener {
            getPermission(1)
            dialog.dismiss()
        }
        takePhotoBtn.setOnClickListener {
            getPermission(0)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun getPermission(position: Int) = if (position == 0) {
        val permissionArr= arrayListOf(Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        requestPermssionnq(requireActivity(),0,permissionArr).observe(requireActivity()
        ) {
            val data = it as Int
            if (data == 1) {
                featchImageFromCamera()
            }
        }
    } else {
        val permissionArr= arrayListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE)
        requestPermssionnq(requireActivity(),1,permissionArr).observe(requireActivity()
        ) {
            val data = it as Int
            if (data == 2) {
                getImageFromGallery()
            }
        }
    }
    private fun featchImageFromCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUriSecond = activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriSecond)
        featchImageFromCameraResult.launch(intent)

    }

    private val featchImageFromCameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val f = Extentions.getFilePath(requireContext(),imageUriSecond!!)
                val fi = Extentions.imageCompressor(requireContext(), File(f.toString()))
                val body=getRequestBodyForUpload(fi)
                viewModelAccount.hitUpdateProfileApi(token!!, arrayListOf(body))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun getImageFromGallery(){
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getImageFromGalleryResult.launch(i)
    }
    private val getImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val path = result.data?.data?.let { getRealPathFromURI(requireContext(), it) }
                val fi =
                    path?.let { File(it) }?.let { Extentions.imageCompressor(requireContext(), it) }
//                Glide.with(requireContext()).load(fi).into(binding.imgUserImageAccount)
                val body= fi?.let { getRequestBodyForUpload(it) }
                if(body!=null)
                    viewModelAccount.hitUpdateProfileApi(token!!, arrayListOf(body))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            if ("content" == contentUri.scheme) {
                val proj = arrayOf(MediaStore.Images.Media.DATA)
                cursor = context.contentResolver.query(contentUri, proj, null, null, null)
                val column_index: Int = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)!!
                cursor.moveToFirst()
                cursor.getString(column_index)
            } else {
                contentUri.path
            }
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
    }
    fun getRequestBodyForUpload(file:File):MultipartBody.Part?{
        val requestFile: RequestBody =
            file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("images", file.name, requestFile)
        return body
    }
private fun setProfile(){
    val url=SharedPref(requireContext()).getString(Constant.PROFILE_URL)
    Glide.with(requireContext()).load(ContextCompat.getDrawable(requireContext(),R.drawable.no_img_found)).into(binding.imgUserImageAccount)
    if(url.isNotEmpty()){
        Glide.with(requireContext()).load(ContextCompat.getDrawable(requireContext(),R.drawable.no_img_found)).into(binding.imgUserImageAccount)
    }
}

}