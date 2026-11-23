package com.example.appsdokter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

class BerandaFragment : Fragment(R.layout.activity_beranda) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPagerBanner)
        val bannerList = listOf(
            R.drawable.ai_health_banner,
            R.drawable.ai_health_banner1,
            R.drawable.ai_health_banner2
        )
        viewPager.adapter = BannerAdapter(bannerList)
    }
}