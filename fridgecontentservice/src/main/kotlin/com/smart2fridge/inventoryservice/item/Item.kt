package com.smart2fridge.inventoryservice.item

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class Item(
    @Id
    var id: Int?,
    var snapshotId: Int?,
    val description: String,
    var amount: Int = 0
)
