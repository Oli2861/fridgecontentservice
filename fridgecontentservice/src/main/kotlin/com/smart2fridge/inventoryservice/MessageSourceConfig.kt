package com.smart2fridge.inventoryservice

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource

@Configuration
class MessageSourceConfig {
    @Bean
    fun messageSource(): ResourceBundleMessageSource {
        val messageSource = ResourceBundleMessageSource()
        //returns code instead of throwing an error when no message to the corresponding message code is found
        messageSource.setUseCodeAsDefaultMessage(true)
        //name of the message files which gets always appended with underscore and the language abbreviation e.g. messages_en_US.properties
        messageSource.setBasenames("messages")
        return messageSource
    }
}