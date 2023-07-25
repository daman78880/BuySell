package com.buysell.screens.myads

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.buysell.R
import com.buysell.utils.Extentions

class AdatperMyAds(val context: Context, val list:List<Data>,val click:Clicks):RecyclerView.Adapter<AdatperMyAds.MyViewHolder>() {
    inner class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val view:ConstraintLayout=itemView.findViewById(R.id.viewMyAds)
        val img:AppCompatImageView=itemView.findViewById(R.id.imgMyAds)
        val title:AppCompatTextView=itemView.findViewById(R.id.txtTitleMyAds)
        val address:AppCompatTextView=itemView.findViewById(R.id.txtAddressMyAds)
        val postedTime:AppCompatTextView=itemView.findViewById(R.id.txtTimePostedMyAds)
        val price:AppCompatTextView=itemView.findViewById(R.id.txtPriceMyAds)
        val Active:AppCompatTextView=itemView.findViewById(R.id.txtActivieStatusMyAds)
        val Sold:AppCompatTextView=itemView.findViewById(R.id.txtSoldStatusMyAds)
        val threeDot:AppCompatImageView=itemView.findViewById(R.id.imgOptionOverflowMyAds)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.rv_my_ads_list,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        setColorBlue(holder)
        holder.img.alpha=1f
        holder.threeDot.background=ActivityCompat.getDrawable(context,R.drawable.option_overflow)

//        Glide.with(context).load(list[holder.absoluteAdapterPosition].images[0].images).into(holder.img)
        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.no_img_found)).into(holder.img)

        holder.title.text=list[holder.absoluteAdapterPosition].title
        holder.address.text=list[holder.absoluteAdapterPosition].location
        holder.price.text= Extentions.formatPricee(list[holder.absoluteAdapterPosition].price.toLong()?:0)
        holder.postedTime.text=Extentions.getPostAgoTime(list[holder.absoluteAdapterPosition].createdAt)
        // for nothing
//        if(list[holder.absoluteAdapterPosition].activeStatus == 0){
            holder.Sold.visibility=View.GONE
            holder.Active.visibility=View.GONE
//        }
//        // for sold
//        else if (list[holder.absoluteAdapterPosition].activeStatus == 1){
//            setColorGrey(holder)
//            holder.img.alpha=0.5f
//            holder.Sold.visibility=View.VISIBLE
//            holder.Active.visibility=View.GONE
//        }
        // for active
//        else{
//            holder.Sold.visibility=View.GONE
//            holder.Active.visibility=View.VISIBLE
//            setColorBlue(holder)
//        }
        holder.threeDot.setOnClickListener {
            Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
        }
        holder.itemView.setOnClickListener {
            click.onClick(list[holder.absoluteAdapterPosition])
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }
    private fun setColorBlue(holder:MyViewHolder){
        holder.title.setTextColor(ContextCompat.getColor(context,R.color.signUpBtnColor))
        holder.address.setTextColor(ContextCompat.getColor(context,R.color.signUpBtnColor))
        holder.postedTime.setTextColor(ContextCompat.getColor(context,R.color.signUpBtnColor))
        holder.price.background=ActivityCompat.getDrawable(context,R.drawable.et_round_blue_price)
    }
    private fun setColorGrey(holder:MyViewHolder){
        holder.Sold.visibility=View.GONE
        holder.Active.visibility=View.VISIBLE
        holder.title.setTextColor(ContextCompat.getColor(context,R.color.soldColor))
        holder.address.setTextColor(ContextCompat.getColor(context,R.color.soldColor))
        holder.postedTime.setTextColor(ContextCompat.getColor(context,R.color.soldColor))
        holder.price.background=ActivityCompat.getDrawable(context,R.drawable.et_round_sold_price)
    }
    interface Clicks{
        fun  onClick(value:Data)
    }
}