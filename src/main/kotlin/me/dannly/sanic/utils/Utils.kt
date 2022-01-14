package me.dannly.sanic.utils

import java.net.HttpURLConnection
import java.net.URL

object Utils {

    fun isURLValid(url: String?): Boolean {
        try {
            val ur = URL(url)
            val httpURLConnection = ur.openConnection() as HttpURLConnection
            if (httpURLConnection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) return false
        } catch (e: Exception) {
            return false
        }
        return true
    }

}