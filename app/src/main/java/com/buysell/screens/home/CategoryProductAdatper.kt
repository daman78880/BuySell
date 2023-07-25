package com.buysell.screens.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.buysell.R
import com.buysell.screens.category.PojoCategoryData

class CategoryProductAdatper(val context: Context, val list:List<PojoCategoryData>, val click: Click):RecyclerView.Adapter<CategoryProductAdatper.MyViewHolder>(){
class  MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
    val img:AppCompatImageView=itemView.findViewById(R.id.imgCategoryHome)
    val txtDetail:AppCompatTextView=itemView.findViewById(R.id.txtCategoryHome)
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.rv_category_home_list_layout,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        Glide.with(context).load(list[holder.absoluteAdapterPosition].icon).into(holder.img).onLoadFailed(ContextCompat.getDrawable(context,R.drawable.no_img_found))
        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.no_img_found)).into(holder.img)
        holder.txtDetail.text=list[holder.absoluteAdapterPosition].category
        holder.itemView.setOnClickListener {
            click.onClick(list[holder.absoluteAdapterPosition])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface Click{
        fun onClick(value:PojoCategoryData)
    }
}