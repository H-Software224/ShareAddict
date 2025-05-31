package com.example.ch13_activity.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AppRule)

    @Query("SELECT * FROM app_rules")
    suspend fun getAllRules(): List<AppRule>

    @Delete
    suspend fun deleteRule(rule: AppRule)
}
