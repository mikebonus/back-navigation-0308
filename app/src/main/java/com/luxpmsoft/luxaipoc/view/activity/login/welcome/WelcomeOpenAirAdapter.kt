package com.luxpmsoft.luxaipoc.view.activity.login.welcome

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.databinding.ItemOrganizationBinding
import com.luxpmsoft.luxaipoc.model.WelcomeOpenAirItem

class WelcomeOpenAirAdapter(private val onItemClick: (intent: String) -> Unit) :
    RecyclerView.Adapter<WelcomeOpenAirViewHolder>() {

    private val items = arrayListOf<WelcomeOpenAirItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WelcomeOpenAirViewHolder {
        val binding =
            ItemOrganizationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WelcomeOpenAirViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: WelcomeOpenAirViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setData(items: List<WelcomeOpenAirItem>) {
        this.items.clear()
        this.items.addAll(items)
    }
}

class WelcomeOpenAirViewHolder(
    binding: ItemOrganizationBinding,
    private val onItemClick: (name: String) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {
    private val binding: ItemOrganizationBinding = ItemOrganizationBinding.bind(itemView)
    fun bind(item: WelcomeOpenAirItem) {
        binding.textOrganizationName.text = item.name
        // Glide.with(binding.root).load(item.image?.toImageUri()).into(binding.image)
        binding.root.setOnClickListener { onItemClick(item.name) }
    }
}
