package com.kreait.bots.agile

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}