package com.smart2fridge.inventoryservice

import com.smart2fridge.inventoryservice.MessageConstants.SNAPSHOT_CREATE_MESSAGE_CODE
import com.smart2fridge.inventoryservice.MessageConstants.SNAPSHOT_DELETE_FAIL_MESSAGE
import com.smart2fridge.inventoryservice.MessageConstants.SNAPSHOT_DELETE_MESSAGE
import com.smart2fridge.inventoryservice.MessageConstants.SNAPSHOT_GET_FAIL_MESSAGE
import com.smart2fridge.inventoryservice.MessageConstants.SNAPSHOT_GET_MESSAGE
import com.smart2fridge.inventoryservice.item.Item
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.MessageSource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest
@ActiveProfiles("test")
class FridgeContentServiceIntegrationTests {

    @Autowired
    private lateinit var fridgeContentController: FridgeContentController

    @Autowired
    private lateinit var messageSource: MessageSource

    private val usLocale = Locale.US

    @Test
    fun testSave() = runBlocking {
        val items = listOf(
            Item(description = "Apple"),
            Item(description = "Banana"),
            Item(description = "Butter"),
            Item(description = "Jelly"),
            Item(description = "Grapes")
        )

        val actualResponse = fridgeContentController.saveItems(items)

        // Test whether the expected response is returned
        Assertions.assertNotNull(actualResponse.body)
        val body = actualResponse.body!!

        Assertions.assertTrue(body.snapshotId >= 0)

        val expectedMessage = messageSource.getMessage(SNAPSHOT_CREATE_MESSAGE_CODE, null, usLocale).format(body.snapshotId)
        Assertions.assertEquals(expectedMessage, body.message)

        Assertions.assertTrue(ChronoUnit.MINUTES.between(body.captureDate, LocalDateTime.now()) < 2)

        Assertions.assertEquals(body.items, items)
    }

    @Test
    fun testSaveAndRetrieve() = runBlocking {
        val items = listOf(
            Item(description = "Apple"),
            Item(description = "Banana"),
            Item(description = "Butter"),
            Item(description = "Jelly"),
            Item(description = "Grapes")
        )

        val savedResponse = fridgeContentController.saveItems(items)

        val retrieveResponse = fridgeContentController.getItems(savedResponse.body!!.snapshotId)

        // Test whether the expected response is returned
        Assertions.assertNotNull(retrieveResponse.body)
        val body = retrieveResponse.body!!

        Assertions.assertTrue(body.snapshotId >= 0)

        val expectedMessage = messageSource.getMessage(SNAPSHOT_GET_MESSAGE, null, usLocale).format(body.snapshotId)
        Assertions.assertEquals(expectedMessage, body.message)

        Assertions.assertTrue(ChronoUnit.MINUTES.between(body.captureDate, LocalDateTime.now()) < 2)

        Assertions.assertEquals(body.items, items)

    }

    @Test
    fun testGetNotExisting() = runBlocking {
        val snapshotId = 9001
        val retrieveResponse = fridgeContentController.getItems(snapshotId)

        // Test whether the expected response is returned
        Assertions.assertNotNull(retrieveResponse.body)
        val body = retrieveResponse.body!!

        Assertions.assertTrue(body.snapshotId == snapshotId)

        val expectedMessage = messageSource.getMessage(SNAPSHOT_GET_FAIL_MESSAGE, null, usLocale).format(body.snapshotId)
        Assertions.assertEquals(expectedMessage, body.message)

        Assertions.assertFalse(ChronoUnit.MINUTES.between(body.captureDate, LocalDateTime.now()) < 2)

        Assertions.assertTrue(body.items.isEmpty())
    }


    @Test
    fun testSaveAndDelete() = runBlocking {
        val items = listOf(
            Item(description = "Apple"),
            Item(description = "Banana"),
            Item(description = "Butter"),
            Item(description = "Jelly"),
            Item(description = "Grapes")
        )

        val saveResponse = fridgeContentController.saveItems(items)

        val deleteResponse = fridgeContentController.deleteItems(saveResponse.body!!.snapshotId)

        // Test whether the expected response is returned
        Assertions.assertNotNull(deleteResponse.body)
        val body = deleteResponse.body!!

        Assertions.assertEquals(saveResponse.body!!.snapshotId, body.snapshotId)

        val expectedMessage = messageSource.getMessage(SNAPSHOT_DELETE_MESSAGE, null, usLocale).format(body.snapshotId)
        Assertions.assertEquals(expectedMessage, body.message)

        Assertions.assertTrue(ChronoUnit.MINUTES.between(body.captureDate, LocalDateTime.now()) < 2)

        Assertions.assertEquals(items, body.items)

        // check whether the items got actually deleted
        val getResponse = fridgeContentController.getItems(saveResponse.body!!.snapshotId)

        val expectedGetMessage = messageSource.getMessage(SNAPSHOT_GET_FAIL_MESSAGE, null, usLocale).format(body.snapshotId)
        Assertions.assertEquals(expectedGetMessage, getResponse.body!!.message)
        Assertions.assertTrue(getResponse.body!!.items.isEmpty())
    }

    fun testDeleteNotExisting() = runBlocking {
        val snapshotId = 9002
        val deleteResponse = fridgeContentController.deleteItems(snapshotId)

        // Test whether the expected response is returned
        Assertions.assertNotNull(deleteResponse.body)
        val body = deleteResponse.body!!

        Assertions.assertEquals(snapshotId, body.snapshotId)

        val expectedMessage = messageSource.getMessage(SNAPSHOT_DELETE_FAIL_MESSAGE, null, usLocale).format(body.snapshotId)
        Assertions.assertEquals(expectedMessage, body.message)

        Assertions.assertFalse(ChronoUnit.MINUTES.between(body.captureDate, LocalDateTime.now()) < 2)

        Assertions.assertTrue(body.items.isEmpty())
    }
}