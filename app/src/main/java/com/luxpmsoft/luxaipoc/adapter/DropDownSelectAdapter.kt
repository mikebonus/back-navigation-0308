package com.luxpmsoft.luxaipoc.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSpinner
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.Country
import com.luxpmsoft.luxaipoc.model.user.Organization

class DropDownSelectAdapter(activity: Context, dropdown: ArrayList<Organization>?, isIcon: Boolean?,
    isColorText: Boolean?) : BaseAdapter() {
    var activity: Context = activity
    var dropdown: ArrayList<Organization>? = dropdown
    var isIcon: Boolean? = isIcon
    var isColorText: Boolean? = isColorText

    interface OnCheck {
        fun onCheck(childPosition: Int)
    }

    override fun getCount(): Int {
        return dropdown!!.size
    }

    override fun getItem(position: Int): Any? {
        return dropdown?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView == null) {
            convertView =
                LayoutInflater.from(activity).inflate(R.layout.item_dropdown_select_option, parent, false)
            viewHolder = ViewHolder()
            viewHolder.tvName = convertView!!.findViewById<TextView>(R.id.tvName)
            viewHolder.icItem = convertView!!.findViewById<ImageView>(R.id.icItem)
            viewHolder.spn = convertView!!.findViewById<AppCompatSpinner>(R.id.spn)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        val model: Organization = dropdown!!.get(position)
        try {
            model.icon?.let {
                viewHolder.icItem?.visibility = View.VISIBLE
                viewHolder.icItem?.setImageDrawable(it)
            }
            viewHolder.spn?.background = activity.resources.getDrawable(R.drawable.bg_gradient_12)
//            if (isColorText == true) {
//                viewHolder.tvName?.setTextColor(activity.resources.getColor(R.color.white))
//            } else {
//                viewHolder.tvName?.setTextColor(activity.resources.getColor(R.color.black))
//            }
            viewHolder.tvName!!.setText(model.name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView
    }

    internal class ViewHolder {
        var tvName: TextView? = null
        var icItem: ImageView? = null
        var spn: AppCompatSpinner? = null
    }
}