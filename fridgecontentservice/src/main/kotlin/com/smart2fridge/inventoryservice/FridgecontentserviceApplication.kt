package com.smart2fridge.inventoryservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.support.ResourceBundleMessageSource

@SpringBootApplication
class FridgecontentserviceApplication

fun main(args: Array<String>) {
    runApplication<FridgecontentserviceApplication>(*args)

}

