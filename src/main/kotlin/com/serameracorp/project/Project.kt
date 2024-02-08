package com.serameracorp.project

data class Project(
    val id: Int,
    val name: String,
    val pattern: String,
    val user: String,
    val app_user_id: Int,
    val imgUrl: String,
    val patternId: Int
)