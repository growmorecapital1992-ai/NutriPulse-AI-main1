package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String,
    val age: Int,
    val occupation: String,
    val cityRegion: String,
    val dietaryCategory: String, // Vegetarian, Eggitarian, Non-Vegetarian
    val proteinPreferences: String, // Comma separated list
    val healthConditions: String, // General Wellness, Diabetes, Hypertension, etc.
    val weightGoal: String, // Weight Loss, Muscle Gain, Maintain Weight, Metabolic Control
    val calorieTarget: Int,
    val proteinTarget: Int,
    val waterTargetMl: Int,
    val currentWaterMl: Int,
    val streakDays: Int = 1,
    val lastActiveDate: String = ""
)

@Entity(tableName = "meal_records")
data class MealRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val portion: String,
    val weightGrams: Int,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val fiber: Int,
    val sodium: Int,
    val giCategory: String, // LOW, MEDIUM, HIGH
    val clinicalFeedback: String,
    val dateString: String, // YYYY-MM-DD
    val timestamp: String, // HH:mm
    val imageUri: String? = null
)

@Entity(tableName = "hydration_records")
data class HydrationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val dateString: String, // YYYY-MM-DD
    val timestamp: String
)
