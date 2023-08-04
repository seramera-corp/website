package com.serameracorp

import com.serameracorp.plugins.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import java.io.File

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureTemplating()
    configureSecurity()
    configureRouting()
    routing {
        staticFiles("static", File("src/main/resources/static"))
    }
}
