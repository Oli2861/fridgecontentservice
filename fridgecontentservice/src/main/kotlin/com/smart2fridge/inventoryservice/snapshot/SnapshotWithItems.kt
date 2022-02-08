package com.smart2fridge.inventoryservice.snapshot

import com.smart2fridge.inventoryservice.item.Item
import java.time.LocalDateTime

data class SnapshotWithItems(
    val id: Int?,
    val captureDate: LocalDateTime,
    val items: List<Item>
){
    constructor(snapshot: Snapshot, items: List<Item>): this(snapshot.id, snapshot.captureDate, items)
}
