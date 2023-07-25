package com.buysell.screens.upload_image

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.buysell.R

class AdapterUploadPhoto(val context: Context, val list: ArrayList<String>,val screenwidth:Int, val click: Click) :
    RecyclerView.Adapter<AdapterUploadPhoto.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: AppCompatImageView = itemView.findViewById(R.id.imgRvUploadPhoto)
        val btnCancel: ConstraintLayout = itemView.findViewById(R.id.btnCancelImageRvUpload)
        val tempImage: AppCompatImageView = itemView.findViewById(R.id.imgTempCameraRvUploadPhoto)
        val tempImageName: AppCompatTextView = itemView.findViewById(R.id.txtTempNameRvUploadPhoto)
    }


    fun getPhotoList():ArrayList<String>
    {
        return list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view =
            LayoutInflater.from(context).inflate(R.layout.rv_upload_photo_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val width =screenwidth / 3.5
        holder.img.layoutParams.height = width.toInt()
        holder.img.layoutParams.width = width.toInt()
        holder.btnCancel.clipToOutline = true

        holder.img.setImageDrawable(ContextCompat.getDrawable(context,android.R.color.transparent))

        holder.tempImage.setImageDrawable(ContextCompat.getDrawable(context,
            android.R.color.transparent))
        if (position== 0) {
           // holder.img.background = null
            holder.img.background = ActivityCompat.getDrawable(context, R.color.blueTakePhotoColor)
            holder.tempImage.background =
                ContextCompat.getDrawable(context, R.drawable.ic_baseline_photo_camera_24)
            holder.tempImage.setColorFilter(ContextCompat.getColor(context,
                R.color.liteBluetakePhotoColor))
            holder.tempImageName.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.tempImageName.text = "Take Photo"
            holder.btnCancel.visibility = View.GONE
            holder.tempImage.visibility = View.VISIBLE
            holder.tempImageName.visibility = View.VISIBLE
        } else
        if (position == 1) {

            holder.img.background = ActivityCompat.getDrawable(context, R.color.darkBlue)
            holder.tempImage.setImageDrawable(ContextCompat.getDrawable(context,
                R.drawable.dome_image))
            holder.tempImageName.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.tempImageName.text = "Select Photo"
            holder.btnCancel.visibility = View.GONE
            holder.tempImage.visibility = View.VISIBLE
            holder.tempImageName.visibility = View.VISIBLE
        }
        else
        if (position > 1) {
            holder.tempImageName.visibility = View.GONE
            if (list[position].isNotEmpty()) {
                Glide.with(context).load(list[position]).into(holder.img)
                holder.tempImage.visibility = View.GONE
                holder.btnCancel.visibility = View.VISIBLE
            } else {
                holder.img.background =
                    ActivityCompat.getDrawable(context, R.color.liteGreayForEtBackground)
                holder.tempImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.dome_image))
                holder.tempImage.visibility = View.VISIBLE
                holder.btnCancel.visibility = View.GONE



            }
        }
        holder.itemView.setOnClickListener {
            if (position == 0 || position == 1) {
                click.onClick(position)
            }
        }
        holder.btnCancel.setOnClickListener {
            click.onDelete(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface Click {
        fun onClick(position: Int)
        fun onDelete(position: Int)
    }
}