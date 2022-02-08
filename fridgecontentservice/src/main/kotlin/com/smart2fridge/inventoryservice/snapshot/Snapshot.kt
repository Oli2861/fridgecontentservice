package com.smart2fridge.inventoryservice.snapshot

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class Snapshot(
    @Id
    var id: Int? = null,
    var captureDate: LocalDateTime = LocalDateTime.now()
)