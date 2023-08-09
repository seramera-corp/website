package com.serameracorp.plugins

import io.ktor.server.application.*
import io.ktor.server.thymeleaf.*
import org.thymeleaf.templateresolver.FileTemplateResolver

fun Application.configureTemplating() {
  install(Thymeleaf) {
    setTemplateResolver(
      FileTemplateResolver().apply {
        cacheManager = null
        prefix = "src/main/resources/templates/"
        suffix = ".html"
        characterEncoding = "utf-8"
      })
  }
}
