package com.luxpmsoft.luxaipoc.view.activity.registration.policy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.databinding.ItemPolicySectionBinding
import com.luxpmsoft.luxaipoc.model.PolicyModel

class PolicyAdapter() : RecyclerView.Adapter<PolicyViewHolder>() {

    private val items = arrayListOf<PolicyModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PolicyViewHolder {
        val binding =
            ItemPolicySectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PolicyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PolicyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setData(items: List<PolicyModel>) {
        this.items.clear()
        this.items.addAll(items)
    }
}

class PolicyViewHolder(
    binding: ItemPolicySectionBinding
) : RecyclerView.ViewHolder(binding.root) {
    private val binding: ItemPolicySectionBinding = ItemPolicySectionBinding.bind(itemView)

    fun bind(item: PolicyModel) {
        binding.textSection.text = item.section
        binding.textContent.text = item.content
    }
}
