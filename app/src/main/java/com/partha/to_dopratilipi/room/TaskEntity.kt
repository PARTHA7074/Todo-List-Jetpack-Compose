package com.partha.to_dopratilipi.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    val taskName: String
)