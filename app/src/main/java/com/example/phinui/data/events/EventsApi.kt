package com.example.phinui.data.events

import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET

interface EventsApi {
    @GET("csuci-calendar-of-events.rss")
    suspend fun getEvents(): RssFeed
}

object RetrofitInstance {
    val api: EventsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://25livepub.collegenet.com/calendars/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
            .create(EventsApi::class.java)
    }
}

