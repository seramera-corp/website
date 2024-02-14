package com.serameracorp.pattern
data class PatternParams(
    val name: String?,
    val publisher: String?,
    val publishedIn: String?,
    val difficulty: String?,
    val patternFabric: MutableList<Pair<String?, String?>> = mutableListOf(),
    val clothingType: MutableList<String?> = mutableListOf(),
)