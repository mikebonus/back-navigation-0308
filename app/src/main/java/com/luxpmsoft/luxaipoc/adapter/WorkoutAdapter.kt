package com.luxpmsoft.luxaipoc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.workout.ExerciseSessionData
import com.luxpmsoft.luxaipoc.model.workout.Workout
import com.luxpmsoft.luxaipoc.utils.MyUtils
import com.luxpmsoft.luxaipoc.utils.PrefManager

class WorkoutAdapter(
    activity: Activity,
    resource: Int,
    workout: ArrayList<ExerciseSessionData>,
    onListener: OnListener
) :
    RecyclerView.Adapter<WorkoutAdapter.RecyclerViewHolder>() {
    val onListener: OnListener = onListener
    var activity: Activity? = activity
    var resource = resource
    val workout: ArrayList<ExerciseSessionData> = workout
    var prefManager: PrefManager? = PrefManager(activity)
    interface ItemClickListener {
        fun onClick(view: View?, position: Int, isLongClick: Boolean)
    }

    interface OnListener {
        fun onWorkoutListener(model: ExerciseSessionData, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView: View = layoutInflater.inflate(resource, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val model = workout.get(position)
        try {

            model.workoutCategoryName?.let {
                holder.tvName.text = it
            }

            model.createdAt?.let {
                holder.tvTime.text = MyUtils.convertDateTime(it)
            }

            holder.tvKcal.text = "0 ".plus(activity?.resources?.getString(R.string.str_kcal))
            holder.tvReps.text = "0 ".plus(activity?.resources?.getString(R.string.str_reps))
            model.calories?.let {
                holder.tvKcal.text = it.toString().plus(" "+activity?.resources?.getString(R.string.str_kcal))
            }

            model.reps?.let {
                holder.tvReps.text = it.toString().plus(" "+activity?.resources?.getString(R.string.str_reps))
            }

            holder.setItemClickListener(object : ItemClickListener {
                override fun onClick(view: View?, position: Int, isLongClick: Boolean) {
                    onListener.onWorkoutListener(model, position)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var tvName: TextView
        var tvKcal: TextView
        var tvReps: TextView
        var tvTime: TextView
        var iv_image: ImageView
        private var itemClickListener: ItemClickListener? = null
        fun setItemClickListener(itemClickListener: ItemClickListener?) {
            this.itemClickListener = itemClickListener
        }

        override fun onClick(v: View) {
            try {
                itemClickListener!!.onClick(v, adapterPosition, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        init {
            tvName = itemView.findViewById(R.id.tvName)
            tvKcal = itemView.findViewById(R.id.tvKcal)
            tvTime = itemView.findViewById(R.id.tvTime)
            tvReps = itemView.findViewById(R.id.tvReps)
            iv_image = itemView.findViewById(R.id.iv_image)
            itemView.setOnClickListener(this)
        }
    }

    override fun getItemCount(): Int {
        return workout.size
    }
}