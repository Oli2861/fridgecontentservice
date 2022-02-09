package com.smart2fridge.inventoryservice.item

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository: CoroutineCrudRepository<Item, Int>{
    /**
     * Find all item by the provided snapshotId
     * @param snapshotId The ID of the corresponding snapshot
     * @return A flow emitting the items matching the snapshot id
     */
    suspend fun findAllBySnapshotId(snapshotId: Int): Flow<Item>

    /**
     * Find all items matching the provided snapshot id
     * @param snapshotId The ID of the corresponding snapshot
     */
    suspend fun deleteAllBySnapshotId(snapshotId: Int)

}