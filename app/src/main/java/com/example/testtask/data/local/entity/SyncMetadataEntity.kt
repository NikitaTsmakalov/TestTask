package com.example.testtask.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val key: String,
    val lastSuccessSyncAtMs: Long?,
    val lastFailureSyncAtMs: Long?,
    val lastFailureReason: String?,
    val lastSource: String?,
)

