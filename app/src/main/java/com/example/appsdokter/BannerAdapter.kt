package com.example.appsdokter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class BannerAdapter(
    private val bannerList: List<Int>
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    class BannerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val image: ImageView =
            itemView.findViewById(R.id.imgBanner)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BannerViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)

        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BannerViewHolder,
        position: Int
    ) {
        holder.image.setImageResource(bannerList[position])
    }

    override fun getItemCount(): Int {
        return bannerList.size
    }
}
