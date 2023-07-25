package com.buysell.screens.editprofile

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.buysell.R
import com.buysell.screens.browsecategoryresult.Data
import com.buysell.utils.Extentions.TAG
import java.util.*
import kotlin.collections.ArrayList

class AdapterCountryNameCode(val context: Context,var list:ArrayList<PojoCountrys>,val click:Click):RecyclerView.Adapter<AdapterCountryNameCode.MyViewHolder>(),
    Filterable {
    private lateinit var tempList:kotlin.collections.ArrayList<PojoCountrys>
    init {
        tempList=list
    }
class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
    val countryName:AppCompatTextView=itemView.findViewById(R.id.txtCountryNameCodeDialog)
    val countryCode:AppCompatTextView=itemView.findViewById(R.id.txtCountryCodeDialog)
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.rv_namelist_country_code_dialog,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.countryName.text=tempList[holder.absoluteAdapterPosition].name
        holder.countryCode.text=tempList[holder.absoluteAdapterPosition].phone_code
        holder.itemView.setOnClickListener {
            click.onClick(holder.countryName.text.toString(),holder.countryCode.text.toString())
        }
    }

    override fun getItemCount(): Int {
        return tempList.size
    }
    interface Click{
        fun onClick(name:String,code:String)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    tempList = list
                }
                else {
                    val resultList = ArrayList<PojoCountrys>()
                    for (row in list) {
                        if (row.name.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))||
                            row.phone_code.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))
                        ) {
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
                tempList = results?.values as ArrayList<PojoCountrys>
                notifyDataSetChanged()
            }
        }
    }
}