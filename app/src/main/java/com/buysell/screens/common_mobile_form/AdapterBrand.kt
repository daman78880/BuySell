package com.buysell.screens.common_mobile_form

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.buysell.R

class AdapterBrand(val context: Context, val list: List<DataBrands>, val click:Clicks):RecyclerView.Adapter<AdapterBrand.MyViewHolder>() {
class MyViewHolder(view: View):RecyclerView.ViewHolder(view)
{
    val name:AppCompatTextView=itemView.findViewById(R.id.txtNameBrandNameShow)
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.rv_name_show_list,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.name.text=list[holder.absoluteAdapterPosition].brand
        holder.itemView.setOnClickListener {
            click.onClick(list[holder.absoluteAdapterPosition])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface Clicks{
        fun  onClick(data: DataBrands)
    }

}