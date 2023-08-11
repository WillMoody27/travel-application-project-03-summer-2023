package edu.msudenver.cs3013.project03.api

data class OverpassResponse(
    val elements: List<OverpassElement>
)
data class OverpassElement(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val tags: OverpassTags
)

data class OverpassTags(
    val name: String,
    val amenity: String,
    val phone: String,
    val website: String,
)