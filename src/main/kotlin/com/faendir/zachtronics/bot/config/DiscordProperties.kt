package com.faendir.zachtronics.bot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jda")
class DiscordProperties {
    lateinit var token: String
}