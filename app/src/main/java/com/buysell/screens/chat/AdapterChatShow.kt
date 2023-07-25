package com.buysell.screens.chat

import SharedPref
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.buysell.R
import com.buysell.base.Constant
import com.buysell.screens.chatuser.PojoChatLastMsgDetailModel
import com.buysell.screens.chatuser.PojoChatLastMsgDetailModelTwo
import com.buysell.screens.chatuser.PojoChatOfferDataModel
import com.buysell.utils.Extentions
import com.buysell.utils.Extentions.compareSecondsToCurrentTime
import kotlin.collections.ArrayList
import kotlin.math.log

class AdapterChatShow(
    val context: Context, val list: ArrayList<PojoChatLastMsgDetailModelTwo>,
    val click: AdapterChatShow.Click
) : RecyclerView.Adapter<AdapterChatShow.MyViewHolder>() {

    val loginId = SharedPref(context).getInt(Constant.ID_kEY)

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDayStatus: AppCompatTextView = view.findViewById(R.id.txtDayStatus)
        val msgLayout: ConstraintLayout = view.findViewById(R.id.clMsgChat)
        val msg: AppCompatTextView = view.findViewById(R.id.txtMsgChat)
        val msgTime: AppCompatTextView = view.findViewById(R.id.txtMsgTimeChat)

        val offerLayout: ConstraintLayout = view.findViewById(R.id.clCardOffer)
        val offerTitle: AppCompatTextView = view.findViewById(R.id.txtTitleOffer)
        val offerMsg: AppCompatTextView = view.findViewById(R.id.txtMsgOffer)
        val offerDoneBtn: AppCompatTextView = view.findViewById(R.id.btnDoneOffer)
        val offerLastPriceBtn: AppCompatTextView = view.findViewById(R.id.btnLastPriceOffer)
        val offerTime: AppCompatTextView = view.findViewById(R.id.txtOfferTimeChat)

        val offerAcceptedLayout: ConstraintLayout = view.findViewById(R.id.clDoneCardOffer)
        val offerAcceptedTitle: AppCompatTextView = view.findViewById(R.id.txtDoneTitleOffer)
        val offerAcceptedTime: AppCompatTextView = view.findViewById(R.id.txtDoneOfferTimeChat)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view: View? = null
        if (viewType == 0) {
            // right Side
            view = LayoutInflater.from(context).inflate(R.layout.chat_right_side, parent, false)
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.chat_left_side, parent, false)
        }

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (list[holder.absoluteAdapterPosition].senderID == loginId.toString()) {
            holder.msgLayout.background =
                ContextCompat.getDrawable(context, R.drawable.round_msg_drawable)
        } else {
            holder.msgLayout.background =
                ContextCompat.getDrawable(context, R.drawable.round_left_msg_drawable)
        }
        if(position==0){
            holder.txtDayStatus.text= compareSecondsToCurrentTime(list[holder.absoluteAdapterPosition].messageTime)
            holder.txtDayStatus.visibility=View.VISIBLE
            Log.i("asfdjbasdfjnbaskfdnb","inside 0 postition ${compareSecondsToCurrentTime(list[holder.absoluteAdapterPosition].messageTime)}")
        }
        else{
            Log.i("asfdjbasdfjnbaskfdnb","else 0+")
            val now= compareSecondsToCurrentTime(list[holder.absoluteAdapterPosition].messageTime)
            val yesterday= compareSecondsToCurrentTime(list[holder.absoluteAdapterPosition-1].messageTime)
            if(now==yesterday){
                Log.i("asfdjbasdfjnbaskfdnb","else equal now and yesterday")
                holder.txtDayStatus.visibility=View.GONE
            }else{
                Log.i("asfdjbasdfjnbaskfdnb","else not equal now and yesterday")
                holder.txtDayStatus.visibility=View.VISIBLE
                holder.txtDayStatus.text=now
            }
        }
        if (list[holder.absoluteAdapterPosition].messageType.toInt() == 1) {
            holder.msgLayout.visibility = View.VISIBLE
            holder.offerLayout.visibility = View.GONE
            holder.offerAcceptedLayout.visibility = View.GONE
            holder.msg.text = list[holder.absoluteAdapterPosition].message
            holder.msgTime.text =
                Extentions.convertSecond(list[holder.absoluteAdapterPosition].messageTime)
        } else {
                holder.msgLayout.visibility = View.GONE
                var price = list[holder.absoluteAdapterPosition].offerData.price
                if (list[holder.absoluteAdapterPosition].offerData.offerStatus != 2 && list[holder.absoluteAdapterPosition].offerData.ownerOfferStatus != 2) {
                holder.offerLayout.visibility = View.VISIBLE
                holder.offerAcceptedLayout.visibility = View.GONE
                holder.offerTime.text =
                Extentions.convertSecond(list[holder.absoluteAdapterPosition].messageTime)
                if (price.isEmpty())
                    price = "0"

                    holder.offerMsg.text = Extentions.formatPricee(price.toLong())
            }
            else
            {
                holder.offerAcceptedLayout.visibility = View.VISIBLE
                holder.offerLayout.visibility = View.GONE
                holder.offerAcceptedTime.text = Extentions.convertSecond(list[holder.absoluteAdapterPosition].messageTime)
            }
            if(list[holder.absoluteAdapterPosition].offerData.ownerId == loginId.toString()){
                val offerStatus = list[holder.absoluteAdapterPosition].offerData.ownerOfferStatus
                if (offerStatus == 0) {
                    holder.offerDoneBtn.visibility = View.INVISIBLE
                    holder.offerLastPriceBtn.visibility = View.INVISIBLE
                    if(list[holder.absoluteAdapterPosition].offerData.sendToId!=loginId.toString()) {
                        holder.offerTitle.text = "Your offer"
                    }
                    else{
                        holder.offerTitle.text = "Buyer offer"
                    }
                }
                else if (offerStatus == 1) {
                    holder.offerTitle.text = "Buyer offer"
                    holder.offerDoneBtn.visibility = View.VISIBLE
                    holder.offerLastPriceBtn.visibility = View.VISIBLE
                }
                else {
                    if(list[holder.absoluteAdapterPosition].offerData.sendToId!=loginId.toString()) {
                        holder.offerAcceptedTitle.text =  "Buyer Offer - ${Extentions.formatPricee(price.toLong())}"
                    }else{
                        holder.offerAcceptedTitle.text =  "Your Offer - ${Extentions.formatPricee(price.toLong())}"
                    }
                    holder.offerAcceptedLayout.visibility = View.VISIBLE
                }
            }
            else{
                val offerStatus = list[holder.absoluteAdapterPosition].offerData.offerStatus
                if (offerStatus == 0) {
                    if(list[holder.absoluteAdapterPosition].offerData.sendToId!=loginId.toString()) {
                        holder.offerTitle.text = "Your offer"
                    }
                    else{
                        holder.offerTitle.text = "Seller offer"
                    }
                    holder.offerDoneBtn.visibility = View.INVISIBLE
                    holder.offerLastPriceBtn.visibility = View.INVISIBLE
                }
                else if (offerStatus == 1) {
                    holder.offerTitle.text = "Seller offer"
                    holder.offerDoneBtn.visibility = View.VISIBLE
                    holder.offerLastPriceBtn.visibility = View.VISIBLE
                } else {
                    if(list[holder.absoluteAdapterPosition].offerData.sendToId!=loginId.toString()) {
                        holder.offerAcceptedTitle.text =  "Seller Offer - ${Extentions.formatPricee(price.toLong())}"
                    }else{
                        holder.offerAcceptedTitle.text =  "your Offer - ${Extentions.formatPricee(price.toLong())}"
                    }
                    holder.offerAcceptedLayout.visibility = View.VISIBLE
                }
            }
        }
        holder.offerDoneBtn.setOnClickListener {
            click.onClick(2, list[holder.absoluteAdapterPosition].msgId,list[holder.absoluteAdapterPosition].offerData)
        }
        holder.offerLastPriceBtn.setOnClickListener {
            click.onClick(1, list[holder.absoluteAdapterPosition].msgId,list[holder.absoluteAdapterPosition].offerData)
        }
        holder.itemView.setOnClickListener {
            click.onView(holder.itemView)
        }


    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        val loginUid = SharedPref(context).getInt(Constant.ID_kEY)
        var tempPosition = 0
        if (loginUid.toString() == list[position].senderID) {
            tempPosition = 0
        } else {
            tempPosition = 1
        }
        return tempPosition
    }

    interface Click {
//        fun onClick(value: Int, price: String,id:String,ownerId:String,sendBy:Boolean)
        fun onClick(value: Int, id:String,offerData: PojoChatOfferDataModel)
        fun onView(view: View)
    }

}