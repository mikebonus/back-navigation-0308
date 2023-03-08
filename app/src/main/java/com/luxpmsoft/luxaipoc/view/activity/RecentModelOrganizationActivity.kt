package com.luxpmsoft.luxaipoc.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ar.core.Session
import com.google.gson.Gson
import com.luxpmsoft.luxaipoc.LidarApp
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.adapter.RecentModelOrganizationAdapter
import com.luxpmsoft.luxaipoc.adapter.SortAdapter
import com.luxpmsoft.luxaipoc.adapter.WorkoutAdapter
import com.luxpmsoft.luxaipoc.api.APIInterface
import com.luxpmsoft.luxaipoc.api.Utils
import com.luxpmsoft.luxaipoc.api.openair.APIOpenAirUtils
import com.luxpmsoft.luxaipoc.api.workout.APIWorkoutUtils
import com.luxpmsoft.luxaipoc.model.ErrorModel
import com.luxpmsoft.luxaipoc.model.SortModel
import com.luxpmsoft.luxaipoc.model.recentmodel.ReconstructionResponse
import com.luxpmsoft.luxaipoc.model.recentmodel.Rows
import com.luxpmsoft.luxaipoc.model.workout.ExerciseSessionData
import com.luxpmsoft.luxaipoc.model.workout.Workout
import com.luxpmsoft.luxaipoc.model.workout.WorkoutResponse
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.Sort
import kotlinx.android.synthetic.main.activity_recent_models_organization.*
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.math.round

class RecentModelOrganizationActivity: AppCompatActivity(), RecentModelOrganizationAdapter.OnListener , WorkoutAdapter.OnListener{
    var recentAdapter: RecentModelOrganizationAdapter? = null
    var workoutAdapter: WorkoutAdapter? = null
    var adapter: SortAdapter? = null
    var modelList: ArrayList<Rows>? = ArrayList()
    var workoutList: ArrayList<ExerciseSessionData>? = ArrayList()
    var pageIndex = 0
    var pageSize = 15
    var workoutItem = 0
    var total = 0
    var isFirst = false
    var isWorkout = false
    var type = "Scenes"
    var sort = "newest"
    var sortType = "date"
    var sortList:ArrayList<SortModel> = ArrayList()
    var chooseList:ArrayList<SortModel> = ArrayList()
    private var session: Session? = null

    private val customHandler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_models_organization)
        MyUtils.setStatusBarTransparentFlagBlack(this)
        init()
        listener()
    }

    fun init() {
        Utils.gridLayoutManager(this, grvRecentModel, 1, GridLayoutManager.VERTICAL)
        recentAdapter = RecentModelOrganizationAdapter(this, R.layout.item_recent_model_organization, modelList!!, this)
        grvRecentModel.adapter = recentAdapter

        Utils.gridLayoutManager(this, grvWorkout, 1, GridLayoutManager.VERTICAL)
        workoutAdapter = WorkoutAdapter(this, R.layout.item_workout, workoutList!!, this)
        grvWorkout.adapter = workoutAdapter

        sortList.add(SortModel(Sort.Newest.name, true, getString(R.string.newest)))
        sortList.add(SortModel(Sort.Oldest.name, false, getString(R.string.oldest)))
        sortList.add(SortModel(Sort.Smallest.name, false, getString(R.string.smallest)))
        sortList.add(SortModel(Sort.Largest.name, false, getString(R.string.largest)))
        sortList.add(SortModel("A-Z", false, getString(R.string.az)))
        sortList.add(SortModel("Z-A", false, getString(R.string.za)))

        chooseList.add(SortModel("1", true, getString(R.string.str_scene_scan)))
//        chooseList.add(SortModel("2", false, getString(R.string.str_body_scan1)))
//        chooseList.add(SortModel("3", false, getString(R.string.workout)))
        session = MyUtils.createSession(this)
    }

    fun listener() {
        icBack.setOnClickListener {
            finish()
        }

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                customHandler.removeCallbacks(getModel)
                customHandler.postDelayed(getModel, 1200)
                s?.let {
                    if (it.isNotEmpty()) {
                        icClear.visibility = View.VISIBLE
                    } else {
                        icClear.visibility = View.INVISIBLE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        grvRecentModel.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(isFirst && modelList?.size!! < total && !recyclerView.canScrollVertically(1)) {
                    pageIndex++
                    getReconstruction(pageIndex, pageSize, type, edtSearch.text.toString(), sort, sortType)
                }
            }
        })

        grvWorkout.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(isFirst && workoutItem < total && !recyclerView.canScrollVertically(1)) {
                    pageIndex++
                    getExerciseVideo(pageSize, pageIndex)
                }
            }
        })

        icClear.setOnClickListener {
            edtSearch.setText("")
            icClear.visibility = View.INVISIBLE
        }

        lineModels.setOnClickListener {
            showPopupChoose(lineModels)
        }

        lineSort.setOnClickListener {
//            showPopupWindow(lineSort)
            showPopupMore(lineSort)
        }
    }

    private fun showPopupMore(anchor: View?) {
        PopupWindow(anchor?.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor?.context)
            contentView = inflater.inflate(R.layout.popup_sort_recent_model, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }

            val sortRecycler = contentView.findViewById<RecyclerView>(R.id.grvSort)
            Utils.gridLayoutManager(this@RecentModelOrganizationActivity, sortRecycler, 1, GridLayoutManager.VERTICAL)
            adapter = SortAdapter(this@RecentModelOrganizationActivity, R.layout.item_sort, sortList, object :
                SortAdapter.IAdapterClickListener {
                override fun onSelect(position: Int, isCheck: Boolean) {
                    try {
                        sortList.forEachIndexed { index, element ->
                            if (index == position) {
                                sortList.get(index)?.isCheck =
                                    sortList.get(index)?.isCheck != true
                            } else {
                                sortList.get(index).isCheck = false
                            }
                        }
                        val nameSort = sortList.get(position).name
                        if (nameSort == "A-Z" || nameSort == Sort.Smallest.name || nameSort == Sort.Oldest.name) {
                            sort = "oldest"
                        } else {
                            sort = "newest"
                        }
                        if (nameSort == "A-Z" || nameSort == "Z-A") {
                            sortType = "name"
                        } else if (nameSort == Sort.Smallest.name || nameSort == Sort.Largest.name){
                            sortType = "size"
                        } else {
                            sortType = "date"
                        }
                        adapter?.notifyDataSetChanged()
                        tvType.text = sortList.get(position).localizedName
                        clearList()
                        dismiss()
                    } catch (e:Exception) {
                        e.message
                    }
                }
            })
            sortRecycler.adapter = adapter
        }.also { popupWindow ->
            popupWindow.height = LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            popupWindow.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT)
            )

            // Absolute location of the anchor view
            val location = IntArray(2).apply {
                anchor?.getLocationOnScreen(this)
            }
            val size = Size(
                popupWindow.contentView.measuredWidth,
                popupWindow.contentView.measuredHeight
            )
            popupWindow.showAtLocation(
                anchor,
                Gravity.TOP or Gravity.END,
                resources.getDimensionPixelOffset(R.dimen.size_26),
                location[1] - size.height+resources.getDimensionPixelOffset(R.dimen.size_26)+popupWindow.contentView.measuredHeight
            )
        }
    }

    private fun showPopupChoose(anchor: View?) {
        PopupWindow(anchor?.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor?.context)
            contentView = inflater.inflate(R.layout.popup_sort_recent_model, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
            }

            val sortRecycler = contentView.findViewById<RecyclerView>(R.id.grvSort)
            Utils.gridLayoutManager(this@RecentModelOrganizationActivity, sortRecycler, 1, GridLayoutManager.VERTICAL)
            adapter = SortAdapter(
                this@RecentModelOrganizationActivity,
                R.layout.item_sort,
                chooseList,
                object :
                    SortAdapter.IAdapterClickListener {
                    override fun onSelect(position: Int, isCheck: Boolean) {
                        try {
                            chooseList.forEachIndexed { index, element ->
//                                chooseList.get(index)?.isCheck
                            }
                            val nameSort = chooseList.get(position).name
                            if (nameSort == "1") {
                                type = "Scenes"
                                clearList()
                                isWorkout = false
                            } else if (nameSort == "2") {
                                type = "Body Pose"
                                clearList()
                                isWorkout = false
                            } else {
                                clearWorkoutList()
                                isWorkout = true
                            }

                            adapter?.notifyDataSetChanged()
                            tvRecentModel.text = chooseList.get(position).localizedName
                            dismiss()
                        } catch (e: Exception) {
                            e.message
                        }
                    }
                })
            sortRecycler.adapter = adapter
        }.also { popupWindow ->
                popupWindow.height = LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                popupWindow.setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT)
                )

                // Absolute location of the anchor view
                val location = IntArray(2).apply {
                    anchor?.getLocationOnScreen(this)
                }
                val size = Size(
                    popupWindow.contentView.measuredWidth,
                    popupWindow.contentView.measuredHeight
                )
                popupWindow.showAtLocation(
                    anchor,
                    Gravity.TOP or Gravity.START,
                    resources.getDimensionPixelOffset(R.dimen.size_26),
                    location[1] - size.height+resources.getDimensionPixelOffset(R.dimen.size_26)+popupWindow.contentView.measuredHeight
                )
            }
    }

    override fun onResume() {
        super.onResume()
        footer.updateTabRecent((application as LidarApp).prefManager?.getTotalNotification()!!)
        footer.updateSession(session)
        if (isWorkout) {
            clearWorkoutList()
        } else {
            clearList()
        }
    }


    private val getModel: Runnable = object : Runnable {
        override fun run() {
            clearList()
        }
    }

    fun clearList() {
        modelList?.let {
            it.clear()
        }
        pageIndex = 0
        getReconstruction(pageIndex, pageSize, type, edtSearch.text.toString(), sort, sortType)
    }

    fun clearWorkoutList() {
        isFirst = false
        workoutItem = 0
        workoutList?.let {
            it.clear()
        }
        pageIndex = 1
        getExerciseVideo(pageSize, pageIndex)
    }

    private fun showPopupWindow(anchor: View?) {
        val wrapper = ContextThemeWrapper(this, R.style.PopupMenu)
        val popup = PopupMenu(wrapper, anchor!!)
        popup.setGravity(Gravity.END)
        popup.menuInflater.inflate(R.menu.type_filter, popup.menu)
        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                val i: Int = item.getItemId()
                when(i) {
                    R.id.lineObjects -> {
                        type = item.title.toString()
                        tvType.text = type
                        clearList()
                    }
                    R.id.lineScenes -> {
                        type = item.title.toString()
                        tvType.text = type
                        clearList()
                    }
                    else -> {
                        type = item.title.toString()
                        tvType.text = type
                        clearList()
                    }
                }

                return true
            }
        })

        popup.show()
    }


    @SuppressLint("NewApi")
    override fun onListener(model: String, position: Int) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (!Environment.isExternalStorageManager()) {
//                MyUtils.startSettingExternal(this)
//            } else {
//                startViewModel(position)
//            }
//        } else {
        startViewModel(position)
//        }
    }

    private fun startViewModel(position: Int) {
        //check free storage
        var fileSize = 0
        modelList?.get(position)?.fileSize?.let {
            fileSize = round(it.toDouble()/(1024*1024)).toInt()
        }
        if (!MyUtils.getFreeSize(fileSize)) {
            MyUtils.deleteFileGltf()
        }
        val row = Gson().toJson(modelList?.get(position))
        val intent = Intent(this@RecentModelOrganizationActivity, ShowObjectOrganizationActivity::class.java)
        intent.putExtra("data", row)
        intent.putExtra("organization", "organization")
        startActivity(intent)
    }

    fun getReconstruction(pageIndex: Int,pageSize: Int, filter: String, search: String?, sort: String?,sortType: String?) {
        MyUtils.showProgress(this, flProgress)
        var id = ""
        if ((application as LidarApp).prefManager?.getOrganizationRole() == "admin") {
            id = (application as LidarApp).prefManager?.getOrganizationId().toString()
        }
        APIOpenAirUtils.getReconstruction((application as LidarApp).prefManager!!.getToken(), pageIndex, pageSize,
            sort, sortType, filter, id, search,object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@RecentModelOrganizationActivity, flProgress)
                    val data = result as ReconstructionResponse
                    if (!isFirst) {
                        isFirst = true
                    }
                    data.body?.let {
                        it.count?.let {
                            total = it
                        }
                        it.rows?.let {
                            modelList?.addAll(it)
                        }
                    }

                    recentAdapter?.notifyDataSetChanged()

                    modelList?.also {
                        grvWorkout.visibility = View.GONE
                        if (it.size > 0) {
                            grvRecentModel.visibility = View.VISIBLE
                            tvEmpty.visibility = View.GONE
                        } else {
                            grvRecentModel.visibility = View.GONE
                            tvEmpty.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@RecentModelOrganizationActivity, flProgress)
                    MyUtils.toastError(this@RecentModelOrganizationActivity, error as ErrorModel)
                }
            })
    }

    fun getExerciseVideo(pageSize: Int, pageLimit: Int) {
        MyUtils.showProgress(this, flProgress)
        APIWorkoutUtils.getExerciseVideo((application as LidarApp).prefManager!!.getToken(),
            pageSize, pageLimit, object : APIInterface.onDelegate {
                override fun onSuccess(result: Any?) {
                    MyUtils.hideProgress(this@RecentModelOrganizationActivity, flProgress)
                    val data = result as WorkoutResponse
                    if (!isFirst) {
                        isFirst = true
                    }

                    data.body?.let {
                        it.totalItems?.let {
                            total = it
                        }

                        it.exercises?.let {
                            workoutItem += it.size
                            if (it.isNotEmpty()) {
                                for (exe in it) {
                                    exe.exerciseSessionData?.let {
                                        if (it.isNotEmpty()) {
                                            for (exerciseSession in it) {
                                                exerciseSession.workoutCategoryName = exe.workoutCategoryName
                                                workoutList?.add(exerciseSession)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    workoutAdapter?.notifyDataSetChanged()
                    workoutList?.also {
                        grvRecentModel.visibility = View.GONE
                        if (it.size > 0) {
                            grvWorkout.visibility = View.VISIBLE
                            tvEmpty.visibility = View.GONE
                        } else {
                            grvWorkout.visibility = View.GONE
                            tvEmpty.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onError(error: Any?) {
                    MyUtils.hideProgress(this@RecentModelOrganizationActivity, flProgress)
                    MyUtils.toastError(this@RecentModelOrganizationActivity, error as ErrorModel)
                }
            })
    }


    override fun onDestroy() {
        super.onDestroy()
        session?.let {
            session = null
        }
    }

    override fun onWorkoutListener(model: ExerciseSessionData, position: Int) {
        val row = Gson().toJson(model)
        val intent = Intent(this@RecentModelOrganizationActivity, WorkoutVideoActivity::class.java)
        intent.putExtra("data", row)
        startActivity(intent)
    }
}