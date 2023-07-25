package com.buysell.screens.home

import android.app.Activity
import android.util.DisplayMetrics
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
import com.buysell.utils.Extentions


class ProductShowAdatper(val context: Activity, var list:List<AllProductsList>, val click:Click):
    RecyclerView.Adapter<ProductShowAdatper.MyViewHolder>(){
    class  MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val img: AppCompatImageView =itemView.findViewById(R.id.imgProuctList)
        val txtPrice: AppCompatTextView =itemView.findViewById(R.id.txtPriceProductList)
        val txtTitle: AppCompatTextView =itemView.findViewById(R.id.txtTitleTextProductList)
        val txtSubTitle: AppCompatTextView =itemView.findViewById(R.id.txtSubTextProductList)
        val imgLike: AppCompatImageView =itemView.findViewById(R.id.imgLikeProductList)
        val view: ConstraintLayout =itemView.findViewById(R.id.viewProductList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=
            LayoutInflater.from(context).inflate(R.layout.prouct_list_home_rv,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val displayMetrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        holder.img.layoutParams.height=width/2
        holder.img.layoutParams.width=width/2
        Glide.with(context.baseContext).load(ContextCompat.getDrawable(context,R.drawable.no_img_found)).into(holder.img)
//        Glide.with(context.baseContext).load(list[holder.absoluteAdapterPosition].images[0].images).into(holder.img).onLoadFailed(ContextCompat.getDrawable(context,R.drawable.no_img_found))
        holder.txtPrice.text=Extentions.formatPricee(list[holder.absoluteAdapterPosition].price.toLong()?:0)
        holder.txtTitle.text=list[holder.absoluteAdapterPosition].title
        holder.txtSubTitle.text=list[holder.absoluteAdapterPosition].location
        if(list[holder.absoluteAdapterPosition].liked){
            holder.imgLike.setColorFilter(ContextCompat.getColor(context, R.color.red))
        }
        else{
            holder.imgLike.setColorFilter(ContextCompat.getColor(context, R.color.white))
        }
        holder.imgLike.setOnClickListener {
            click.onLike(list[holder.absoluteAdapterPosition],holder.absoluteAdapterPosition)
        }
        holder.view.setOnClickListener {
            click.onClick(list[holder.absoluteAdapterPosition])
        }
    }
    override fun getItemCount(): Int {
        return list.size
    }
    interface Click{
        fun onClick(value:AllProductsList)
        fun onLike(value:AllProductsList,index:Int)
    }
}