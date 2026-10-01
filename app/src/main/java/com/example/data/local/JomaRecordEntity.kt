package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "joma_records")
data class JomaRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val memberName: String,
    val amount: Double = 100.0,
    val dateString: String, // e.g. "2026-10-01"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
