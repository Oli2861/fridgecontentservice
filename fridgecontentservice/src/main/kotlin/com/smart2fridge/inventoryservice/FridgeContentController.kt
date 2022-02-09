package com.smart2fridge.inventoryservice

import com.smart2fridge.inventoryservice.item.Item
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("v1")
class FridgeContentController(
    @Autowired val fridgeContentService: FridgeContentService
) {
    private val defaultLocale: Locale = Locale.US

    @PostMapping("/item/bulk")
    suspend fun saveItems(
        @RequestBody items: List<Item>,
        @RequestHeader(value = "Accept-Language", required = false) locale: Locale = defaultLocale
    ): ResponseEntity<Response> {
        val response = fridgeContentService.saveItems(items, locale)
        return ResponseEntity.ok().body(response)
    }

    /**
     * Get all items for the userId matching the snapshotId
     */
    @GetMapping("/item/bulk/{snapshotId}")
    suspend fun getItems(
        @PathVariable snapshotId: Int,
        @RequestHeader(value = "Accept-Language", required = false) locale: Locale = defaultLocale
    ): ResponseEntity<Response> {
        val response = fridgeContentService.getItemsWithContext(snapshotId, locale)
        return ResponseEntity.ok().body(response)
    }

    /**
     * Delete all items of snapshot
     */
    @DeleteMapping("/item/bulk/{snapshotId}")
    suspend fun deleteItems(
        @PathVariable snapshotId: Int,
        @RequestHeader(value = "Accept-Language", required = false) locale: Locale = defaultLocale
    ): ResponseEntity<Response> {
        val response = fridgeContentService.deleteItems(snapshotId, locale)
        return ResponseEntity.ok().body(response)
    }

}