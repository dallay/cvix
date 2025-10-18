package com.loomify

import com.loomify.engine.TestcontainersConfiguration
import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<LoomifyApplication>().with(TestcontainersConfiguration::class).run(*args)
}
