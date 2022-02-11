package com.smart2fridge.inventoryservice

import com.smart2fridge.inventoryservice.MessageConstants.SNAPSHOT_CREATE_MESSAGE_CODE
import com.smart2fridge.inventoryservice.item.Item
import com.smart2fridge.inventoryservice.item.ItemRepository
import com.smart2fridge.inventoryservice.snapshot.Snapshot
import com.smart2fridge.inventoryservice.snapshot.SnapshotRepository
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.springframework.context.MessageSource
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class FridgeContentServiceTests {
    private val itemRepositoryMock: ItemRepository = Mockito.mock(ItemRepository::class.java)
    private val snapshotRepositoryMock: SnapshotRepository = Mockito.mock(SnapshotRepository::class.java)
    private val messageSourceMock: MessageSource = Mockito.mock(MessageSource::class.java)

    private val subject = FridgeContentService(itemRepositoryMock, snapshotRepositoryMock, messageSourceMock)
    private val usLocale = Locale.US

    @Test
    fun testSave() = runBlocking {
        val items: List<Item> = listOf(
            Item(description = "Apple"),
            Item(description = "Banana"),
            Item(description = "Butter"),
            Item(description = "Jelly"),
            Item(description = "Grapes")
        )
        val snapshotId = 1
        // Mock behavior
        Mockito.`when`(snapshotRepositoryMock.save(Mockito.any(Snapshot::class.java))).thenReturn(Snapshot(snapshotId))

        /*
         Prepare items as the function is supposed to do in order to only return values by mockito, when the items are transformed as expected:
         - Ids are supposed to be null
         - SnapshotId has to be correct
         - Amount is supposed to hold the amount of items with a similar description
         - No multiple descriptions --> distinctBy
         */
        var itemsModified = items
        itemsModified.forEach { item ->
            item.id = null
            item.snapshotId = snapshotId
            item.amount = itemsModified.count { it.description == item.description }
        }
        itemsModified = itemsModified.distinctBy { it.description }

        // set ids for the item objects returned by the itemRepository
        val itemsSaved = itemsModified
        for ((index, item) in itemsModified.withIndex()) {
            item.id = index
        }

        Mockito.`when`(itemRepositoryMock.saveAll(itemsModified)).thenReturn(itemsSaved.asFlow())
        val message = "Snapshot created; URI: /v1/item/bulk/%s"
        Mockito.`when`(messageSourceMock.getMessage(SNAPSHOT_CREATE_MESSAGE_CODE, null, usLocale)).thenReturn(message)

        val actual = subject.saveItems(items, usLocale)

        // Check expected interactions
        // Snapshot saved
        Mockito.verify(snapshotRepositoryMock, times(1)).save(any(Snapshot::class.java))
        // Items saved
        Mockito.verify(itemRepositoryMock, times(1)).saveAll(itemsModified)
        // Message retrieved
        Mockito.verify(messageSourceMock, times(1)).getMessage(SNAPSHOT_CREATE_MESSAGE_CODE, null, usLocale)

        // Expected message
        val expectedMessage = message.format(snapshotId)
        Assertions.assertEquals(expectedMessage, actual.message)
        // Correct snapshot id
        Assertions.assertEquals(snapshotId, actual.snapshotId)
        // Time not older than 2 minutes
        Assertions.assertTrue(ChronoUnit.MINUTES.between(actual.captureDate, LocalDateTime.now()) < 2)
        // Correct items returned
        Assertions.assertEquals(itemsSaved, actual.items)
    }

    @Test
    fun testGetItemsWithContext() = runBlocking {
        val snapshotId = 1
        val snapshot = Snapshot(snapshotId)
        val items: List<Item> = listOf(
            Item(description = "Apple"),
            Item(description = "Banana"),
            Item(description = "Butter"),
            Item(description = "Jelly"),
            Item(description = "Grapes")
        )

        // Define mock behavior
        Mockito.`when`(itemRepositoryMock.findAllBySnapshotId(snapshotId)).thenReturn(items.asFlow())
        Mockito.`when`(snapshotRepositoryMock.findById(snapshotId)).thenReturn(snapshot)
        val message = "Snapshot with snapshotId=%d"
        Mockito.`when`(messageSourceMock.getMessage(MessageConstants.SNAPSHOT_GET_MESSAGE, null, usLocale))
            .thenReturn(message)

        val actual = subject.getItemsWithContext(snapshotId, usLocale)

        // Check expected interactions
        Mockito.verify(itemRepositoryMock, times(1)).findAllBySnapshotId(snapshotId)
        Mockito.verify(snapshotRepositoryMock, times(1)).findById(snapshotId)
        Mockito.verify(messageSourceMock, times(1)).getMessage(MessageConstants.SNAPSHOT_GET_MESSAGE, null, usLocale)

        // Expected message
        val expectedMessage = message.format(snapshotId)
        Assertions.assertEquals(expectedMessage, actual.message)
        Assertions.assertEquals(snapshotId, actual.snapshotId)
        Assertions.assertEquals(snapshot.captureDate, actual.captureDate)
        Assertions.assertEquals(items, actual.items)
    }

    @Test
    fun testGetNotExisting() = runBlocking {
        val snapshotId = 1
        val snapshot = Snapshot(snapshotId)

        // Define mock behavior
        Mockito.`when`(itemRepositoryMock.findAllBySnapshotId(snapshotId)).thenReturn(null)
        val message = "No snapshot with snapshotId=%d"
        Mockito.`when`(messageSourceMock.getMessage(MessageConstants.SNAPSHOT_GET_FAIL_MESSAGE, null, usLocale))
            .thenReturn(message)

        val actual = subject.getItemsWithContext(snapshotId, usLocale)

        // Check expected interactions
        Mockito.verify(snapshotRepositoryMock, times(1)).findById(snapshotId)
        Mockito.verify(messageSourceMock, times(1))
            .getMessage(MessageConstants.SNAPSHOT_GET_FAIL_MESSAGE, null, usLocale)

        // Expected message
        val expectedMessage = message.format(snapshotId)
        Assertions.assertEquals(expectedMessage, actual.message)
        Assertions.assertEquals(snapshotId, actual.snapshotId)
        Assertions.assertFalse(ChronoUnit.MINUTES.between(actual.captureDate, LocalDateTime.now()) < 2)
        Assertions.assertTrue(actual.items.isEmpty())
    }

    @Test
    fun testDeleteItems() = runBlocking {
        val snapshotId = 1
        val snapshot = Snapshot(snapshotId)
        val items: List<Item> = listOf(
            Item(description = "Apple"),
            Item(description = "Banana"),
            Item(description = "Butter"),
            Item(description = "Jelly"),
            Item(description = "Grapes")
        )

        // Define mock behavior
        Mockito.`when`(itemRepositoryMock.findAllBySnapshotId(snapshotId)).thenReturn(items.asFlow())
        Mockito.`when`(snapshotRepositoryMock.findById(snapshotId)).thenReturn(snapshot)
        val message = "Deleted snapshot with snapshotId=%d"
        Mockito.`when`(messageSourceMock.getMessage(MessageConstants.SNAPSHOT_DELETE_MESSAGE, null, usLocale))
            .thenReturn(message)

        val actual = subject.deleteItems(snapshotId, usLocale)

        // Check expected interactions: Correct items retrieved, delete functions called
        Mockito.verify(itemRepositoryMock, times(1)).findAllBySnapshotId(snapshotId)
        Mockito.verify(snapshotRepositoryMock, times(1)).findById(snapshotId)
        Mockito.verify(itemRepositoryMock, times(1)).deleteAllBySnapshotId(snapshotId)
        Mockito.verify(snapshotRepositoryMock, times(1)).deleteById(snapshotId)

        // Expected message
        val expectedMessage = message.format(snapshotId)
        Assertions.assertEquals(expectedMessage, actual.message)
        Assertions.assertEquals(snapshotId, actual.snapshotId)
        Assertions.assertEquals(snapshot.captureDate, actual.captureDate)
        Assertions.assertEquals(items, actual.items)
    }

    @Test
    fun testDeleteNotExisting() = runBlocking {
        val snapshotId = 1

        // Define mock behavior
        Mockito.`when`(snapshotRepositoryMock.findById(snapshotId)).thenReturn(null)
        val message = "Could not delete snapshot with snapshotId=%d"
        Mockito.`when`(messageSourceMock.getMessage(MessageConstants.SNAPSHOT_DELETE_FAIL_MESSAGE, null, usLocale))
            .thenReturn(message)

        val actual = subject.deleteItems(snapshotId, usLocale)

        // Check expected interactions
        Mockito.verify(snapshotRepositoryMock, times(1)).findById(snapshotId)
        Mockito.verify(messageSourceMock, times(1))
            .getMessage(MessageConstants.SNAPSHOT_DELETE_FAIL_MESSAGE, null, usLocale)
        Mockito.verifyNoMoreInteractions(snapshotRepositoryMock)
        Mockito.verifyNoInteractions(itemRepositoryMock)

        // Expected message
        val expectedMessage = message.format(snapshotId)
        Assertions.assertEquals(expectedMessage, actual.message)
        Assertions.assertEquals(snapshotId, actual.snapshotId)
        Assertions.assertFalse(ChronoUnit.MINUTES.between(actual.captureDate, LocalDateTime.now()) < 2)
        Assertions.assertTrue(actual.items.isEmpty())
    }

}