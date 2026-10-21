package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagnostic_sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val categoryId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isCompleted: Boolean,
    val confidence: Double,
    val summary: String,
    val technicianRecommended: Boolean,
    // JSON serialized fields
    val problemInputJson: String,
    val possibleCausesJson: String,
    val safeTestJson: String?,
    val repairPlanJson: String?,
    val safetyWarningsJson: String,
    val messagesJson: String,
    val costComparisonJson: String
)
