package com.smart2fridge.inventoryservice

import com.smart2fridge.inventoryservice.item.Item
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("v1")
class FridgeContentController(
    @Autowired val fridgeContentService: FridgeContentService
) {


    @PostMapping("/item/bulk")
    suspend fun saveItems(
        @RequestBody items: List<Item>
    ): ResponseEntity<URI> {
        val snapshotId = fridgeContentService.saveItems(items)
        val location = URI("/v1/item/bulk/$snapshotId")
        return ResponseEntity.created(location).build()
    }

    /**
     * Get all items for the userId matching the snapshotId
     */
    @GetMapping("/item/bulk/{snapshotId}")
    suspend fun getItems(
        @PathVariable snapshotId: Int
    ) = fridgeContentService.getItemsWithContext(snapshotId)

    /**
     * Delete all items of snapshot
     */
    @DeleteMapping("/item/bulk/{snapshotId}")
    suspend fun deleteItems(
        @PathVariable snapshotId: Int
    ) =  fridgeContentService.deleteItems(snapshotId)


}