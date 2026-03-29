package com.example.testtask.data.remote

import com.example.testtask.data.remote.dto.FilmDto
import com.example.testtask.data.remote.dto.PeopleResponseDto
import com.example.testtask.data.remote.dto.PlanetsResponseDto
import com.example.testtask.data.remote.dto.PersonDto
import com.example.testtask.data.remote.dto.PlanetDto
import com.example.testtask.data.remote.dto.SpeciesDto
import com.example.testtask.data.remote.dto.StarshipDto
import com.example.testtask.data.remote.dto.StarshipsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface SwapiApi {
    @GET("people/")
    suspend fun getPeoplePage(
        @Query("page") page: Int,
        @Query("search") search: String? = null,
    ): PeopleResponseDto

    @GET("people/{id}/")
    suspend fun getPerson(@Path("id") id: Int): PersonDto

    @GET("planets/{id}/")
    suspend fun getPlanet(@Path("id") id: Int): PlanetDto

    @GET("planets/")
    suspend fun getPlanetsPage(
        @Query("page") page: Int,
        @Query("search") search: String? = null,
    ): PlanetsResponseDto

    @GET("starships/")
    suspend fun getStarshipsPage(
        @Query("page") page: Int,
        @Query("search") search: String? = null,
    ): StarshipsResponseDto

    @GET("starships/{id}/")
    suspend fun getStarship(@Path("id") id: Int): StarshipDto

    @GET("films/{id}/")
    suspend fun getFilm(@Path("id") id: Int): FilmDto

    @GET
    suspend fun getSpeciesByUrl(@Url url: String): SpeciesDto
}
