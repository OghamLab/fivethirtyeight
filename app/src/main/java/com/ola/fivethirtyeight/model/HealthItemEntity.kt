package com.ola.fivethirtyeight.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_items")
data class HealthItemEntity(
    val title: String, val description: String, val content: String, val author: String, val publishedAt: String, val imageUrl: String, @PrimaryKey val link: String,
    val savedDate: String, val timeInMil: Long, val isSavedForLater: Boolean = false)


