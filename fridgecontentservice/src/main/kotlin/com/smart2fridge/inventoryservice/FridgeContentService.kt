package com.smart2fridge.inventoryservice

import com.smart2fridge.inventoryservice.item.Item
import com.smart2fridge.inventoryservice.item.ItemRepository
import com.smart2fridge.inventoryservice.snapshot.Snapshot
import com.smart2fridge.inventoryservice.snapshot.SnapshotRepository
import com.smart2fridge.inventoryservice.snapshot.SnapshotWithItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class FridgeContentService(
    @Autowired private val itemRepository: ItemRepository,
    @Autowired private val snapshotRepository: SnapshotRepository
){

    suspend fun saveItems(items: List<Item>): Int {
        val snapshotId: Int? = snapshotRepository.save(Snapshot()).id
        snapshotId?.let { id ->
            items.forEach{ item ->
                item.id = null
                item.snapshotId = id
                item.amount = items.filter { it.description == item.description }.size
            }
            val savedItems = itemRepository.saveAll(items.distinctBy { it.description }).toList()
            return id
        }
        return -1
    }

    suspend fun getItems(snapshotId: Int): Flow<Item> {
        return itemRepository.findAllBySnapshotId(snapshotId)
    }

    suspend fun getItemsWithContext(snapshotId: Int): SnapshotWithItems?{
        val items = getItems(snapshotId).toList()
        val snapshot = snapshotRepository.findById(snapshotId)
        return snapshot?.let { SnapshotWithItems(it, items) }
    }

    suspend fun deleteItems(snapshotId: Int): SnapshotWithItems? {
        val items = getItems(snapshotId).toList()

        val snapshot = snapshotRepository.findById(snapshotId)
        itemRepository.deleteAllBySnapshotId(snapshotId)
        snapshotRepository.deleteById(snapshotId)
        return snapshot?.let { SnapshotWithItems(it, items) }
    }


}