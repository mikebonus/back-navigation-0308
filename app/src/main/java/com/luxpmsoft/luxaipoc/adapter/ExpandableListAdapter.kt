package com.luxpmsoft.luxaipoc.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.workspaces.WorkspaceFolder
import com.luxpmsoft.luxaipoc.model.workspaces.WorkspaceFolderBoard
import java.util.ArrayList

class ExpandableListAdapter(context: Context?, listDataHeader: List<WorkspaceFolder>?,
                            listChildData: HashMap<WorkspaceFolder, List<WorkspaceFolderBoard>>?,
                            listener: OnListener): BaseExpandableListAdapter() {
    private var _context: Context? = context
    private var listener: OnListener? = listener
    private var header // header titles
            : List<WorkspaceFolder>? = listDataHeader

    // Child data in format of header title, child title
    private var child: HashMap<WorkspaceFolder, List<WorkspaceFolderBoard>>? = listChildData

    override fun getChild(groupPosition: Int, childPosititon: Int): Any? {

        // This will return the child
        return child!![header!![groupPosition]]!![childPosititon]
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildView(
        groupPosition: Int, childPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup?
    ): View? {

        // Getting child text
        var convertView = convertView
        val childText = getChild(groupPosition, childPosition) as WorkspaceFolderBoard?

        // Inflating child layout and setting textview
        if (convertView == null) {
            val infalInflater = _context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = infalInflater.inflate(R.layout.childs, parent, false)
        }

        val child_text = convertView?.findViewById<View>(R.id.child) as TextView
        val tvFiles = convertView?.findViewById<View>(R.id.tvFiles) as TextView
        val lineBoard = convertView?.findViewById<View>(R.id.lineBoard) as LinearLayout
        val lineEmpty = convertView?.findViewById<View>(R.id.lineEmpty) as LinearLayout
        val icDelete = convertView?.findViewById<View>(R.id.icDelete) as ImageView
        if (childText?.workspaceFolderBoardName != null) {
            lineBoard.visibility = View.VISIBLE
            lineEmpty.visibility = View.GONE
            child_text.text = childText?.workspaceFolderBoardName
            childText?.filegroups?.let {
                childText.total?.let {
                    if (it > 1) {
                        tvFiles.text = it.toString().plus(" files")
                    } else {
                        tvFiles.text = it.toString().plus(" file")
                    }
                }
            }
        } else {
            lineBoard.visibility = View.GONE
            lineEmpty.visibility = View.VISIBLE
        }

        icDelete.setOnClickListener {
            listener?.odDeleteBoard(childText?.workspaceFolderBoardId!!, childText.workspaceFolderBoardName!!)
        }

        return convertView
    }

    override fun getChildrenCount(groupPosition: Int): Int {

        // return children count
        return child!![header!![groupPosition]]!!.size
    }

    override fun getGroup(groupPosition: Int): Any? {

        // Get header position
        return header!![groupPosition]
    }

    override fun getGroupCount(): Int {

        // Get header size
        return header!!.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup?
    ): View? {

        // Getting header title
        var convertView = convertView
        val headerTitle = getGroup(groupPosition) as WorkspaceFolder?

        // Inflating header layout and setting text
        if (convertView == null) {
            val infalInflater = _context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = infalInflater.inflate(R.layout.header, parent, false)
        }
        val header_text = convertView?.findViewById<View>(R.id.header) as TextView
        val header1 = convertView?.findViewById<View>(R.id.header1) as TextView
        val projectOptions = convertView?.findViewById<View>(R.id.projectOptions) as LinearLayout
        header_text.text = headerTitle?.workspaceFolderName

        // If group is expanded then change the text into bold and change the
        // icon
        if (isExpanded) {
            header1.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_circle_minus, 0,
                0, 0
            )
        } else {
            // If group is not expanded then change the text back into normal
            // and change the icon
            header1.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_add_circle, 0,
               0, 0
            )
        }

        projectOptions.setOnClickListener {
            listener?.onOptions(headerTitle, projectOptions)
        }
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    interface OnListener {
        fun onOptions(workspaceFolder: WorkspaceFolder?, view: View?)
        fun odDeleteBoard(boardId: String?, boardName: String?)
    }
}