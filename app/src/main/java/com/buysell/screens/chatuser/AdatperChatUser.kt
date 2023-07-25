package com.buysell.screens.chatuser

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.buysell.R
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.getPostAgoTimeNew

class AdatperChatUser(val context: Context, val list:ArrayList<PojoBuyingChatUser>, val click:Clicks):RecyclerView.Adapter<AdatperChatUser.MyViewHolder>() {
    inner class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val userImage:AppCompatImageView=itemView.findViewById(R.id.imgUserImageRvChatUser)
        val title:AppCompatTextView=itemView.findViewById(R.id.txtUserNameRvChatUser)
        val address:AppCompatTextView=itemView.findViewById(R.id.txtUserBioRvChatUser)
        val postedTime:AppCompatTextView=itemView.findViewById(R.id.txtLasTimeOnlineTimeRvChatUser)
        val userProductImage:AppCompatImageView=itemView.findViewById(R.id.imgSendImageRvChatUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.rv_selling_list,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.userImage.clipToOutline=true
        holder.userProductImage.clipToOutline=true
        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.no_img_found)).into(holder.userImage)
        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.no_img_found)).into(holder.userProductImage)
        holder.title.text=list[holder.absoluteAdapterPosition].userName
        holder.address.text=list[holder.absoluteAdapterPosition].msg
        val time=(System.currentTimeMillis()/1000)-list[holder.absoluteAdapterPosition].time
        holder.postedTime.text=getPostAgoTimeNew(time*1000)
        // for nothing
        holder.itemView.setOnClickListener {
            click.onClick(list[holder.absoluteAdapterPosition])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface Clicks{
        fun  onClick(data:PojoBuyingChatUser)
    }
}