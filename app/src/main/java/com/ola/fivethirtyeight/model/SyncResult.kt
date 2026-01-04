package com.ola.fivethirtyeight.model

data class SyncResult<Domain>(
    val newItems: List<Domain>,
    val fetchedCount: Int
)
