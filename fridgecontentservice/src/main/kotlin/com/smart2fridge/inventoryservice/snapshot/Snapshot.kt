package com.smart2fridge.inventoryservice.snapshot

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

/**
 * A snapshot represents the contents of the fridge at a specific point in time.
 * @param id The id (primary key) of the snapshot.
 * @param captureDate Date the snapshot was stored.
 */
data class Snapshot(
    @Id
    var id: Int? = null,
    var captureDate: LocalDateTime = LocalDateTime.now()
)