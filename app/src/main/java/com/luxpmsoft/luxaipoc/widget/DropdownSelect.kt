package com.luxpmsoft.luxaipoc.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.DropDownSelectAdapter
import com.luxpmsoft.luxaipoc.databinding.ItemDropdownSelectBinding
import com.luxpmsoft.luxaipoc.model.user.Organization
import kotlinx.android.synthetic.main.item_custom_dropdown_select.view.*

class DropdownSelect(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var mOpenInitiated = false
    private var binding: ItemDropdownSelectBinding
    var dropdownAdapter: DropDownSelectAdapter? = null
    var list: ArrayList<Organization>? = null
    var name : String? = null
    private var listener: OnListener? =null

    init {
        binding = ItemDropdownSelectBinding.inflate(LayoutInflater.from(context), this, true)
        setupAttributes(attrs)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupAttributes(attrs: AttributeSet?) {
        val arr = context.theme.obtainStyledAttributes(attrs, R.styleable.EdittextLabl, 0, 0)
        binding.tvName.setOnClickListener {
            if (!arr.getBoolean(R.styleable.EdittextLabl_isPerformClick, false)) {
                mOpenInitiated = true
                binding.spn.performClick()
            }
        }

        if (arr.getBoolean(R.styleable.EdittextLabl_icSite, false)) {
            binding.icSite.visibility = VISIBLE
        } else {
            binding.icSite.visibility = GONE
        }

        if (arr.getBoolean(R.styleable.EdittextLabl_textColorTitle, false)) {
            binding.flDropdown.background = resources.getDrawable(R.drawable.bg_search1)
        } else {
            binding.flDropdown.background = resources.getDrawable(R.drawable.bg_stroke_12_purple)
        }

        if (arr.getString(R.styleable.EdittextLabl_textTitle)!!.contains("site")) {
            binding.icDropdown.visibility = VISIBLE
            binding.icDropdown.setImageDrawable(resources.getDrawable(R.drawable.ic_site))
        } else if (arr.getString(R.styleable.EdittextLabl_textTitle)!!.contains("repo")) {
            binding.icDropdown.visibility = VISIBLE
            binding.icDropdown.setImageDrawable(resources.getDrawable(R.drawable.ic_repositories))
        } else if(arr.getString(R.styleable.EdittextLabl_textTitle)!!.contains("project")) {
            binding.icDropdown.visibility = VISIBLE
            binding.icDropdown.setImageDrawable(resources.getDrawable(R.drawable.ic_workspaces))
        } else {
            binding.icDropdown.visibility = GONE
        }

        spn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                binding.tvName.text = list?.get(position)?.name
                name = list?.get(position)?.name
                if (listener != null) {
                    listener?.onChoose(this@DropdownSelect, list?.get(position)?.organizationId)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (mOpenInitiated && hasWindowFocus) {
            mOpenInitiated = false
        }
    }

    fun getNames() : String {
        return name.toString()
    }

    fun setSelection(value : String?) {
        for (i in 0 until list!!.size) {
            if (list!!.get(i).organizationId.toString().equals(value)) {
                spn.setSelection(i)
            }
        }
    }

    fun setSelection(pos : Int) {
        binding.spn.setSelection(pos)
    }

    fun setData(dropdownList: ArrayList<Organization>?) {
        if (list != null) {
            list?.removeAll(list!!)
        } else {
            list = ArrayList()
        }
        list?.addAll(dropdownList!!)
        dropdownAdapter = DropDownSelectAdapter(context!!, dropdownList!!, false, false)
        spn.adapter = dropdownAdapter
        dropdownAdapter!!.notifyDataSetChanged()
    }

    fun notifyData() {
        dropdownAdapter!!.notifyDataSetChanged()
    }

    fun setOnListener(listener: OnListener) {
        this.listener = listener
    }

    interface OnListener {
        fun onChoose(view: View, id: String?)
    }
}