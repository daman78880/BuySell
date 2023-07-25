package com.buysell.screens.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.buysell.R
import com.buysell.utils.Extentions

class NotificationAdapter(val context:Context,val list:ArrayList<NotificationPojo>):RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {
class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
    val view:ConstraintLayout=itemView.findViewById(R.id.viewNotification)
    val title:AppCompatTextView=itemView.findViewById(R.id.txtTitleNotification)
    val msg:AppCompatTextView=itemView.findViewById(R.id.txtMessageNotification)
    val timeAgo:AppCompatTextView=itemView.findViewById(R.id.txtAgoTimingNotification)
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.rv_notification_list,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.view.setOnClickListener {
            Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
        }
        holder.title.text=list[holder.absoluteAdapterPosition].title
        holder.msg.text=list[holder.absoluteAdapterPosition].msg
        holder.timeAgo.text=Extentions.getPostAgoTimeNew(list[holder.absoluteAdapterPosition].time.toLong())
    }

    override fun getItemCount(): Int {
        return list.size
    }
}