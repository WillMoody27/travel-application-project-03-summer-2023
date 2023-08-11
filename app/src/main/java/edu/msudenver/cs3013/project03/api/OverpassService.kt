package edu.msudenver.cs3013.project03.api

import retrofit2.http.GET
import retrofit2.http.Query


// TODO-Requirement: Implement Overpass API service interface
interface OverpassService {
    @GET("api/interpreter")
    suspend fun getLocations(
        @Query("data") data: String
    ): OverpassResponse
}