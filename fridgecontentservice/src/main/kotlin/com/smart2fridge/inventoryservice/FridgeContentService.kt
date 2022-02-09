package com.smart2fridge.inventoryservice

import com.smart2fridge.inventoryservice.MessageConstants.SNAPSHOT_CREATE_FAIL_MESSAGE_CODE
import com.smart2fridge.inventoryservice.MessageConstants.SNAPSHOT_CREATE_MESSAGE_CODE
import com.smart2fridge.inventoryservice.MessageConstants.SNAPSHOT_DELETE_FAIL_MESSAGE
import com.smart2fridge.inventoryservice.MessageConstants.SNAPSHOT_DELETE_MESSAGE
import com.smart2fridge.inventoryservice.MessageConstants.SNAPSHOT_GET_FAIL_MESSAGE
import com.smart2fridge.inventoryservice.MessageConstants.SNAPSHOT_GET_MESSAGE
import com.smart2fridge.inventoryservice.item.Item
import com.smart2fridge.inventoryservice.item.ItemRepository
import com.smart2fridge.inventoryservice.snapshot.Snapshot
import com.smart2fridge.inventoryservice.snapshot.SnapshotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

object MessageConstants{
    const val SNAPSHOT_CREATE_MESSAGE_CODE = "snapshot.create.message"
    const val SNAPSHOT_CREATE_FAIL_MESSAGE_CODE = "snapshot.create.fail.message"
    const val SNAPSHOT_GET_MESSAGE = "snapshot.get.message"
    const val SNAPSHOT_GET_FAIL_MESSAGE = "snapshot.get.fail.message"
    const val SNAPSHOT_DELETE_MESSAGE = "snapshot.delete.message"
    const val SNAPSHOT_DELETE_FAIL_MESSAGE = "snapshot.delete.fail.message"
}

@Service
class FridgeContentService(
    @Autowired private val itemRepository: ItemRepository,
    @Autowired private val snapshotRepository: SnapshotRepository,
    @Autowired private val messageSource: MessageSource
) {


    /**
     * Saves items to the database, produces a snapshot associated to them in the process.
     * @param items The items to be saved.
     * @return Id of the corresponding snapshot.
     */
    suspend fun saveItems(items: List<Item>, locale: Locale): Response {
        val snapshot: Snapshot = snapshotRepository.save(Snapshot())
        val snapshotId = snapshot.id
        val snapshotDate = snapshot.captureDate

        snapshotId?.let { id ->
            items.forEach { item ->
                item.id = null
                item.snapshotId = id
                item.amount = items.filter { it.description == item.description }.size
            }

            val savedItems = itemRepository.saveAll(items.distinctBy { it.description }).toList()
            return Response(
                messageSource.getMessage(SNAPSHOT_CREATE_MESSAGE_CODE, null, locale).format(id),
                id,
                snapshot.captureDate,
                savedItems
            )
        }
        return Response(
            messageSource.getMessage(SNAPSHOT_CREATE_FAIL_MESSAGE_CODE, null, locale),
            -1,
            LocalDateTime.MIN,
            listOf()
        )
    }

    /**
     * Get all items matching the provided snapshotId.
     * @param snapshotId Id of the snapshot which is associated with the desired items.
     * @return Flow emitting the desired items.
     */
    suspend fun getItems(snapshotId: Int): Flow<Item> {
        return itemRepository.findAllBySnapshotId(snapshotId)
    }

    /**
     * Get all items matching the provided snapshotId as well as information about the associated snapshot (Id, date)
     * @param snapshotId Id of the snapshot which is associated with the desired items.
     * @return Object containing the snapshotId, date of the snapshot and the items.
     */
    suspend fun getItemsWithContext(snapshotId: Int, locale: Locale): Response {
        val items = getItems(snapshotId).toList()
        val snapshot = snapshotRepository.findById(snapshotId)

        return if (snapshot != null) {
            Response(
                messageSource.getMessage(SNAPSHOT_GET_MESSAGE, null, locale).format(snapshotId),
                snapshotId,
                snapshot.captureDate,
                items
            )
        } else {
            Response(
                messageSource.getMessage(SNAPSHOT_GET_FAIL_MESSAGE, null, locale).format(snapshotId),
                snapshotId,
                LocalDateTime.MIN,
                items
            )
        }
    }

    /**
     * Delete the snapshot matching the provided snapshotId and all associated items.
     * @param snapshotId Id of the snapshot to be deleted.
     * @return Object containing the snapshotId, date of the snapshot and the items which got deleted.
     */
    suspend fun deleteItems(snapshotId: Int, locale: Locale): Response {
        val items = getItems(snapshotId).toList()
        val snapshot = snapshotRepository.findById(snapshotId)

        itemRepository.deleteAllBySnapshotId(snapshotId)
        snapshotRepository.deleteById(snapshotId)

        return if (snapshot != null) {
            Response(
                messageSource.getMessage(SNAPSHOT_DELETE_MESSAGE, null, locale).format(snapshotId),
                snapshotId,
                snapshot.captureDate,
                items
            )
        } else {
            Response(
                messageSource.getMessage(SNAPSHOT_DELETE_FAIL_MESSAGE, null, locale)
                    .format(snapshotId),
                snapshotId,
                LocalDateTime.MIN,
                items
            )
        }
    }

}

data class Response(
    val message: String,
    val snapshotId: Int,
    val captureDate: LocalDateTime,
    val items: List<Item>
)