package com.smart2fridge.inventoryservice

import com.smart2fridge.inventoryservice.item.Item
import java.time.LocalDateTime

data class Response(
    val message: String,
    val snapshotId: Int,
    val captureDate: LocalDateTime,
    val items: List<Item>
)
