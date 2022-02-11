package com.smart2fridge.inventoryservice.item

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Item in the fridge.
 * @param id The id (primary key) of the item.
 * @param snapshotId The id of the associated snapshot.
 * @param description Description of the item e.g. Apple.
 * @param amount Amount of items (if there are currently 2 apples in the fridge 2).
 */
@Table
data class Item(
    @Id
    var id: Int? = null,
    var snapshotId: Int? = null,
    val description: String,
    var amount: Int = 0
)
