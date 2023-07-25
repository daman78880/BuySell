
 package com.buysell.screens.productdetail

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
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
import com.buysell.screens.home.AllProductsList
import com.buysell.screens.home.HomeFragment
import com.buysell.screens.home.PojoProductShow
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.TAG


 class RelatedProductAdatper(val context: Context,val width:Int, val list:ArrayList<AllProductsList>, val click:Click):
    RecyclerView.Adapter<RelatedProductAdatper.MyViewHolder>(){
    class  MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val img: AppCompatImageView =itemView.findViewById(R.id.imgProuctList)
        val txtPrice: AppCompatTextView =itemView.findViewById(R.id.txtPriceProductList)
        val txtTitle: AppCompatTextView =itemView.findViewById(R.id.txtTitleTextProductList)
        val txtSubTitle: AppCompatTextView =itemView.findViewById(R.id.txtSubTextProductList)
        val imgLike: AppCompatImageView =itemView.findViewById(R.id.imgLikeProductList)
        val like: ConstraintLayout =itemView.findViewById(R.id.cLImgLikeProductList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=
            LayoutInflater.from(context).inflate(R.layout.prouct_list_home_rv,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        Log.i(TAG,"inside setting adapter")
        holder.img.layoutParams.height=width/2
        holder.img.layoutParams.width=width/2
//        Glide.with(context).load(list[position].images[0].images).into(holder.img)
        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.no_img_found)).into(holder.img)
        holder.txtPrice.text=Extentions.formatPricee(list[holder.absoluteAdapterPosition].price.toLong()?:0)
        holder.txtTitle.text=list[holder.absoluteAdapterPosition].title
        holder.txtSubTitle.text=list[holder.absoluteAdapterPosition].location
        if(list[holder.absoluteAdapterPosition].liked){
            holder.imgLike.setColorFilter(ContextCompat.getColor(context, R.color.red))
        }
        else{
            holder.imgLike.setColorFilter(ContextCompat.getColor(context, R.color.white))
        }
        holder.itemView.setOnClickListener {
            click.onClick(list[holder.absoluteAdapterPosition])
        }
        holder.like.setOnClickListener {
            click.onLikeClick(list[holder.absoluteAdapterPosition],holder.absoluteAdapterPosition)
        }
    }
    override fun getItemCount(): Int {
        return list.size
    }
    interface Click{
        fun onClick(value: AllProductsList)
        fun onLikeClick(value: AllProductsList,position:Int)
    }
}