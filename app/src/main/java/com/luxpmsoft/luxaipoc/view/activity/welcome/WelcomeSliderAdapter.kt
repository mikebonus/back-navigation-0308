package com.luxpmsoft.luxaipoc.view.activity.welcome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.WelcomeSlide

class WelcomeSliderAdapter : RecyclerView.Adapter<SliderViewHolder>() {

    private val slidesList = arrayListOf<WelcomeSlide>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_welcome_slider, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        if (position == 0) {
            holder.title.isVisible = false
            holder.subTitle.isVisible = false
            holder.titleReverse.isVisible = true
            holder.subTitleReverse.isVisible = true
            holder.titleReverse.text = slidesList[position].title
            holder.subTitleReverse.text = slidesList[position].subtitle
        } else {
            holder.title.isVisible = true
            holder.subTitle.isVisible = true
            holder.titleReverse.isVisible = false
            holder.subTitleReverse.isVisible = false
        }
        holder.title.text = slidesList[position].title
        holder.subTitle.text = slidesList[position].subtitle
        holder.image.setImageResource(slidesList[position].image)
    }

    override fun getItemCount(): Int = slidesList.size

    fun setData(slide: List<WelcomeSlide>) {
        this.slidesList.clear()
        this.slidesList.addAll(slide)
    }
}

class SliderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView
    val subTitle: TextView
    val image: ImageView
    val titleReverse: TextView
    val subTitleReverse: TextView

    init {
        title = view.findViewById(R.id.text_title)
        subTitle = view.findViewById(R.id.text_subtitle)
        image = view.findViewById(R.id.image_content)
        titleReverse = view.findViewById(R.id.text_title_reverse)
        subTitleReverse = view.findViewById(R.id.text_subtitle_reverse)
    }
}
