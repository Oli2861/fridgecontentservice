package com.smart2fridge.inventoryservice.snapshot

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SnapshotRepository: CoroutineCrudRepository<Snapshot, Int>{

}