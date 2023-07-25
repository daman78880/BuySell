package com.buysell.screens.browsecategoryresult

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.buysell.R
import com.buysell.utils.Extentions
import java.util.*
import kotlin.collections.ArrayList

class AdatperBrowseCategory(val context: Context, var list:kotlin.collections.ArrayList<Data>, val click:Click):RecyclerView.Adapter<AdatperBrowseCategory.MyViewHolder>(),
    Filterable {
    private lateinit var tempList:kotlin.collections.ArrayList<Data>


    init {
        tempList=list
    }
    class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val view:ConstraintLayout=itemView.findViewById(R.id.viewUserProfile)
        val img:AppCompatImageView=itemView.findViewById(R.id.imgUserProfile)
        val title:AppCompatTextView=itemView.findViewById(R.id.txtTitleUserProfile)
        val address:AppCompatTextView=itemView.findViewById(R.id.txtAddressUserProfile)
        val postedTime:AppCompatTextView=itemView.findViewById(R.id.txtTimePostedUserProfile)
        val price:AppCompatTextView=itemView.findViewById(R.id.txtPriceUserProfile)
        val likeLayout:ConstraintLayout=itemView.findViewById(R.id.cLImgLikeUserProfile)
        val likeButton:AppCompatImageView=itemView.findViewById(R.id.imgLikeUserProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.rv_user_profile_list,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.likeLayout.visibility=View.VISIBLE
//        Glide.with(context).load(list[holder.absoluteAdapterPosition].images[0].images).into(holder.img).onLoadFailed(ContextCompat.getDrawable(context,R.drawable.no_img_found))
        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.no_img_found)).into(holder.img)
        holder.title.text=tempList[holder.absoluteAdapterPosition].title
        holder.address.text=tempList[holder.absoluteAdapterPosition].location
        holder.price.text=Extentions.formatPricee(tempList[holder.absoluteAdapterPosition].price.toLong()?:0)
        val time=Extentions.getPostAgoTime(tempList[holder.absoluteAdapterPosition].createdAt)
        holder.postedTime.text=time
        if(tempList[holder.absoluteAdapterPosition].liked){
            holder.likeButton.setColorFilter(ContextCompat.getColor(context, R.color.red))
        }else{
            holder.likeButton.setColorFilter(ContextCompat.getColor(context, R.color.white))
        }
        holder.likeLayout.setOnClickListener {
            click.onLike(tempList[holder.absoluteAdapterPosition],holder.absoluteAdapterPosition)
        }
        holder.itemView.setOnClickListener {
            click.onClick(tempList[holder.absoluteAdapterPosition])
        }
    }
    override fun getItemCount(): Int {
        return tempList.size
    }
    interface Click{
        fun onClick(value:Data)
        fun onLike(value:Data,position:Int)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    tempList = list
                }
                else {
                    val resultList = ArrayList<Data>()
                    for (row in list) {
                        if (row.title.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    tempList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = tempList
                return filterResults
            }
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                tempList = results?.values as ArrayList<Data>
                notifyDataSetChanged()
            }
        }
}
}