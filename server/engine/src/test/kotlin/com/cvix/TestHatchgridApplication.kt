package com.cvix

import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<LoomifyApplication>().with(TestcontainersConfiguration::class).run(*args)
}
