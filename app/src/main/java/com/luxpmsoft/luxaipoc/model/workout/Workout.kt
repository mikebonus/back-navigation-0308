package com.luxpmsoft.luxaipoc.model.workout

import java.io.Serializable

class Workout: Serializable {
    val categoryID: Int? = null
    val uid: String? = null
    val workoutCategoryName: String? = null
    val createdAt: String? = null
    val updatedAt: String? = null
    val exerciseSessionData: Array<ExerciseSessionData>? = null
    val total: ExerciseSessionData? = null
    val weeklyAverage: ExerciseSessionData? = null
}