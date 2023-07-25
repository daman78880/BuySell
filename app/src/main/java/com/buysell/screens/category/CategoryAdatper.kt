package com.buysell.screens.category

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.buysell.R

class CategoryAdatper(val context:Activity, val list:List<PojoCategoryData>, val click:Click):RecyclerView.Adapter<CategoryAdatper.MyViewHolder>() {
    class MyViewHolder(val itemView:View):RecyclerView.ViewHolder(itemView){
        val icon: AppCompatImageView =itemView.findViewById(R.id.imgIconCategory)
        val title:AppCompatTextView=itemView.findViewById(R.id.txt_rv_list_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.rv_category_list,parent,false)
        return MyViewHolder(view)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text=list[holder.absoluteAdapterPosition].category
//        Glide.with(context).load(list[holder.absoluteAdapterPosition].icon).into(holder.icon)
        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.no_img_found)).into(holder.icon)
        holder.itemView.setOnClickListener {
            click.onClick(list[holder.absoluteAdapterPosition])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface Click{
        fun onClick(position:PojoCategoryData)
    }
}