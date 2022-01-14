package me.dannly.sanic.utils

import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchListResponse

object Search {
    private val HTTP_TRANSPORT: HttpTransport = NetHttpTransport()
    private val JSON_FACTORY: JacksonFactory = JacksonFactory()
    private val youtube: YouTube = YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY) { }.setApplicationName("Sanic").build()
    
    fun search(query: String, maxResults: Long): List<com.google.api.services.youtube.model.SearchResult> {
        try {
            val search: YouTube.Search.List = youtube.search().list(listOf("id,snippet"))
            search.key = "AIzaSyD6ckT_I9kvQV2WQgnTTMzS0FlF4W2xB9g"
            search.q = query
            search.type = listOf("video")
            search.fields = "items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)"
            search.maxResults = maxResults
            val searchResponse: SearchListResponse = search.execute()
            return searchResponse.items
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
}