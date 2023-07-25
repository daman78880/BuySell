package com.buysell.screens.user_profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.buysell.R
import com.buysell.utils.Extentions

class AdatperUserProfile(val context: Context, val list:List<Data>,val click:Click):RecyclerView.Adapter<AdatperUserProfile.MyViewHolder>() {
    class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val img:AppCompatImageView=itemView.findViewById(R.id.imgUserProfile)
        val title:AppCompatTextView=itemView.findViewById(R.id.txtTitleUserProfile)
        val address:AppCompatTextView=itemView.findViewById(R.id.txtAddressUserProfile)
        val postedTime:AppCompatTextView=itemView.findViewById(R.id.txtTimePostedUserProfile)
        val price:AppCompatTextView=itemView.findViewById(R.id.txtPriceUserProfile)
        val likeLayout:ConstraintLayout=itemView.findViewById(R.id.cLImgLikeUserProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.rv_user_profile_list,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.likeLayout.visibility=View.GONE
        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.no_img_found)).into(holder.img)
        holder.title.text=list[holder.absoluteAdapterPosition].title
        holder.address.text=list[holder.absoluteAdapterPosition].location
        holder.price.text=Extentions.formatPricee(list[holder.absoluteAdapterPosition].price.toLong())
        holder.postedTime.text=Extentions.getPostAgoTime(list[holder.absoluteAdapterPosition].createdAt)
        holder.itemView.setOnClickListener {
            click.onClick(list[holder.absoluteAdapterPosition].id)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface Click{
        fun  onClick(id:Int)
    }
}