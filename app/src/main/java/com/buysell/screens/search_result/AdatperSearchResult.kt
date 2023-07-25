package com.buysell.screens.search_result

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
import com.buysell.screens.search_screen.Data
import com.buysell.utils.Extentions

class AdatperSearchResult(val context: Context,val list:List<Data>,val click:Click):RecyclerView.Adapter<AdatperSearchResult.MyViewHolder>() {
    class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val img:AppCompatImageView=itemView.findViewById(R.id.imgSearchResult)
        val title:AppCompatTextView=itemView.findViewById(R.id.txtTitleSearchResult)
        val address:AppCompatTextView=itemView.findViewById(R.id.txtAddressSearchResult)
        val postedTime:AppCompatTextView=itemView.findViewById(R.id.txtTimePostedSearchResult)
        val price:AppCompatTextView=itemView.findViewById(R.id.txtPriceSearchResult)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.rv_search_result_list,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        Glide.with(context).load(list[holder.absoluteAdapterPosition].images[0]).into(holder.img)
        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.no_img_found)).into(holder.img)
        holder.title.text=list[holder.absoluteAdapterPosition].title
        holder.address.text=list[holder.absoluteAdapterPosition].location
        holder.price.text=Extentions.formatPricee(list[holder.absoluteAdapterPosition].price.toLong()?:0)
        holder.postedTime.text=Extentions.getPostAgoTime(list[holder.absoluteAdapterPosition].createdAt)
        holder.itemView.setOnClickListener {
            click.onClick(list[holder.absoluteAdapterPosition])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface Click{
        fun onClick(value:Data)
    }
}