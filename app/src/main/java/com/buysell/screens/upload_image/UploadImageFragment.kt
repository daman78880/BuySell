package com.buysell.screens.upload_image

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.anilokcun.uwmediapicker.UwMediaPicker
import com.buysell.R
import com.buysell.base.BaseFragment
import com.buysell.databinding.FragmentUploadImageBinding
import com.buysell.screens.productdetail.PojoProductDetail
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import setToolBar
import java.io.File


class UploadImageFragment : BaseFragment() {
    private var rvPhotoAdapter: AdapterUploadPhoto?=null
    private lateinit var binding: FragmentUploadImageBinding
    private lateinit var listImages: ArrayList<String>

    // for image url get from camera
    private var imageUriSecond: Uri? = null
    private val CAMERA_RESULT = 112
    private var selectedPosition = -1
    private var previousBundle:Bundle?=null
    private var passDataClass:PojoProductDetail?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentUploadImageBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        if(arguments!=null)
        previousBundle=arguments

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            passDataClass=previousBundle?.getParcelable("passDataClass",PojoProductDetail::class.java)
        }
        else{
            passDataClass=previousBundle?.getParcelable("passDataClass")
        }

        listImages = ArrayList()
        for (i in 0 until 12) {
            if (i == 0) {
                listImages.add("cameraImage")
            } else if (i == 1) {
                listImages.add("galleryImage")

            } else {
                listImages.add("")
            }
        }
        if(passDataClass!=null && passDataClass?.data?.title?.isNotEmpty()!!){
            for(i in  0  until  passDataClass?.data?.images?.size!!){
                listImages[i+2]=passDataClass?.data?.images!![i].images
            }
        }
        val appBarTitle=requireActivity().resources.getString(R.string.uploadImage)
        setToolBar(binding.appBarUploadImage,requireContext(),true,false,appBarTitle,10f)

        setAdapter()
        clickListener()
        binding.apply {
        }
    }

    private fun setAdapter() {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        var width = displayMetrics.widthPixels
        binding.apply {
           rvPhotoAdapter = AdapterUploadPhoto(requireActivity(),
                listImages,width,
                object : AdapterUploadPhoto.Click {
                    override fun onClick(position: Int) {
                        selectedPosition = position
                        if (position == 0) {
                            val permissionArr= arrayListOf(Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                            requestPermssionnq(requireActivity(),0,permissionArr).observe(requireActivity(),
                                Observer {
                                    val data= it as Int
                                    if(data ==1){
                                        featchImageFromCamera()
                                    }
                                })
                        }
                        else {

                            val permissionArr= arrayListOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                            requestPermssionnq(requireActivity(),1,permissionArr).observe(requireActivity(),
                                Observer {
                                    val data= it as Int
                                    if( data==2){
                                        getImageFormGallery()
                                    }
                                })
                        }
                    }

                    override fun onDelete(position: Int) {
                        selectedPosition = position
                        deleteImage(position)
                    }
                })

            rvUploadPhotos.adapter=rvPhotoAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteImage(position1: Int) {
        rvPhotoAdapter?.getPhotoList()?.removeAt(position1)
        rvPhotoAdapter?.notifyDataSetChanged()
        rvPhotoAdapter!!.getPhotoList().add(rvPhotoAdapter!!.getPhotoList().size,"")
        rvPhotoAdapter!!.notifyDataSetChanged()
    }

    private fun clickListener() {
        binding.apply {
            appBarUploadImage.tbBackBtn.setOnClickListener {
                findNavController().popBackStack()
            }
            btnNextUploadPhotos.setOnClickListener {
                if(listImages[2].isNotEmpty()){
                    val tempList=ArrayList<String>()
                    for(i in 2 until  listImages.size){
                        if(listImages[i].isNotEmpty()){
                            tempList.add(listImages[i])
                        }
                    }
                    previousBundle?.putStringArrayList("images",tempList)
                    findNavController().navigate(R.id.action_uploadImageFragment_to_bidFragment,previousBundle)
                }
                else{
                    Toast.makeText(requireContext(), "Please select images", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_RESULT && resultCode == Activity.RESULT_OK) {
            try {
                val f = Extentions.getFilePath(requireContext(),imageUriSecond!!)
                val fi = Extentions.imageCompressor(requireContext(),File(f.toString()))
                for (i in 0 until listImages.size) {
                    if (selectedPosition > 1) {
                        listImages[selectedPosition] = fi.path
                        binding.rvUploadPhotos.adapter?.notifyDataSetChanged()
                        break
                    } else {
                        if (i != 0 && i != 1 && listImages[i].isEmpty()) {
                            listImages[i] = fi.path
//                            setAdapter()
                            binding.rvUploadPhotos.adapter?.notifyDataSetChanged()
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getImageFormGallery() {
        val listsize = listImages.filter { it == "" }
        UwMediaPicker
            .with(this)
            .setGalleryMode(UwMediaPicker.GalleryMode.ImageGallery)
            .setGridColumnCount(3)
            .setMaxSelectableMediaCount(listsize.size)
            .setLightStatusBar(true)
            .enableImageCompression(true)
            .setCompressionMaxWidth(1280F)
            .setCompressionMaxHeight(720F)
            .setCompressFormat(Bitmap.CompressFormat.JPEG)
            .setCompressionQuality(100)
            .setCancelCallback { }
            .launch { selectedMediaList ->
                selectedMediaList!!.forEachIndexed { index, uwMediaPickerMediaModel ->
                    val path = uwMediaPickerMediaModel.mediaPath
                    for (i in 2 until listImages.size) {
                        if (listImages[i].isEmpty()) {
                            listImages[i] = path
                            break
                        }
                    }
                }
//                setAdapter()
                binding.rvUploadPhotos.adapter?.notifyDataSetChanged()
            }
    }

    private fun featchImageFromCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUriSecond = activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriSecond)
        startActivityForResult(intent, CAMERA_RESULT)
    }
}