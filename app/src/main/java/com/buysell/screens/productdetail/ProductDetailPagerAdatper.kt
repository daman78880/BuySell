package com.buysell.screens.productdetail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.buysell.R
import java.util.*


class ProductDetailPagerAdatper(val context: Context, val imageList: List<com.buysell.screens.productdetail.Image>): PagerAdapter() {

    override fun getCount(): Int {
        return imageList.size
    }
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as ConstraintLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val mLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView: View =
            mLayoutInflater.inflate(R.layout.image_show_layout, container, false)
        val imageView: AppCompatImageView =
            itemView.findViewById<View>(R.id.imgImageShowLayout) as AppCompatImageView
//        Glide.with(context).load(list[position]).into(imageView)
        Glide.with(context).load(ContextCompat.getDrawable(context,R.drawable.no_img_found)).into(imageView)
        Objects.requireNonNull(container).addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }
}