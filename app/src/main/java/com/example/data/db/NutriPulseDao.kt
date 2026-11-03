package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import kotlinx.coroutines.flow.Flow

@Dao
interface NutriPulseDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileDirect(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    // Meal Records
    @Query("SELECT * FROM meal_records ORDER BY id DESC")
    fun getAllMeals(): Flow<List<MealRecordEntity>>

    @Query("SELECT * FROM meal_records WHERE dateString = :date ORDER BY id DESC")
    fun getMealsForDate(date: String): Flow<List<MealRecordEntity>>

    @Query("SELECT * FROM meal_records WHERE dateString = :date ORDER BY id DESC")
    suspend fun getMealsForDateDirect(date: String): List<MealRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealRecordEntity)

    @Query("DELETE FROM meal_records WHERE id = :id")
    suspend fun deleteMeal(id: Long)

    @Query("DELETE FROM meal_records")
    suspend fun clearAllMeals()

    // Hydration
    @Query("SELECT * FROM hydration_records WHERE dateString = :date ORDER BY id DESC")
    fun getHydrationForDate(date: String): Flow<List<HydrationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHydration(hydration: HydrationEntity)

    @Query("DELETE FROM hydration_records WHERE dateString = :date")
    suspend fun resetHydrationForDate(date: String)
}
