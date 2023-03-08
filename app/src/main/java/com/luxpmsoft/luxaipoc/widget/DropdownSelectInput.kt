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
import com.luxpmsoft.luxaipoc.databinding.ItemDropdownInputBinding
import com.luxpmsoft.luxaipoc.model.user.Organization
import kotlinx.android.synthetic.main.item_custom_dropdown_select.view.*

class DropdownSelectInput(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var mOpenInitiated = false
    private var binding: ItemDropdownInputBinding
    var dropdownAdapter: DropDownSelectAdapter? = null
    var list: ArrayList<Organization>? = null
    var name : String? = null
    var idSelect : String? = null
    private var listener: OnListener? =null

    init {
        binding = ItemDropdownInputBinding.inflate(LayoutInflater.from(context), this, true)
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

        if (arr.getBoolean(R.styleable.EdittextLabl_textColorTitle, false)) {
            binding.icItem.visibility = View.VISIBLE
        } else {
            binding.icItem.visibility = View.GONE
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
                idSelect = list?.get(position)?.referenceID
                if (arr.getBoolean(R.styleable.EdittextLabl_textColorTitle, false)) {
                    binding.icItem.setImageDrawable(list?.get(position)?.icon)
                }
                if (listener != null) {
                    listener?.onChoose(this@DropdownSelectInput, list?.get(position)?.organizationId)
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

    fun getID() : String {
        return idSelect.toString()
    }

    fun setSelection(value : String) {
        for (i in 0 until list!!.size) {
            if (list!!.get(i).name.toString().equals(value)) {
                spn.setSelection(i)
            }
        }
    }

    fun setSelection(pos : Int) {
        binding.spn.setSelection(pos)
    }

    fun setData(dropdownList: ArrayList<Organization>?, isIcon: Boolean?) {
        if (list != null) {
            list?.removeAll(list!!)
        } else {
            list = ArrayList()
        }
        list?.addAll(dropdownList!!)
        dropdownAdapter = DropDownSelectAdapter(context!!, dropdownList!!, isIcon, true)
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