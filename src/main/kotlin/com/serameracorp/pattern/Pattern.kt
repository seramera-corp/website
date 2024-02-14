package com.serameracorp.pattern
data class Pattern(
    val id: Int,
    val name: String,
    val publisher: String,
    val img_url: String,
    val patternFabric: MutableList<PatternFabric> = mutableListOf(),
    val clothingType: MutableList<ClothingType> = mutableListOf(),
)