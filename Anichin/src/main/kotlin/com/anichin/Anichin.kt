package com.anichin

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.nicehttp.NiceResponse
import org.jsoup.nodes.Element
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class Anichin : MainAPI() {
    override var mainUrl = "https://anichin.club"
    override var name = "Anichin"

    override val hasMainPage = true
    override val hasSearch = true

    override suspend fun fetchPopular(page: Int): List<Media> {
        return try {
            val response = app.get("$mainUrl/popular?page=$page")
            val document = response.document

            document.select("div.donghua-item").map { element ->
                val title = element.select("h3.title").text().trim()
                val link = fixUrl(element.select("a").attr("href"))
                val imageUrl = fixUrl(element.select("img").attr("src"))

                Media(
                    title = title,
                    link = link,
                    imageUrl = imageUrl,
                    type = MediaType.Anime
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchGenres(): List<String> {
        return try {
            val response = app.get("$mainUrl/genres")
            val document = response.document
            document.select("div.genre-item a").map { it.text().trim() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchLatestUpdates(page: Int): List<Media> {
        return try {
            val response = app.get("$mainUrl/latest?page=$page")
            val document = response.document
            document.select("div.donghua-item").map { element ->
                val title = element.select("h3.title").text().trim()
                val link = fixUrl(element.select("a").attr("href"))
                val imageUrl = fixUrl(element.select("img").attr("src"))

                Media(title, link, imageUrl, MediaType.Anime)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchCompletedDonghua(page: Int): List<Media> {
        return try {
            val response = app.get("$mainUrl/completed?page=$page")
            val document = response.document
            document.select("div.donghua-item").map { element ->
                val title = element.select("h3.title").text().trim()
                val link = fixUrl(element.select("a").attr("href"))
                val imageUrl = fixUrl(element.select("img").attr("src"))

                Media(title, link, imageUrl, MediaType.Anime)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun fetchSearch(query: String): List<Media> {
        return try {
            val response = app.get("$mainUrl/search?q=$query")
            val document = response.document
            document.select("div.donghua-item").map { element ->
                val title = element.select("h3.title").text().trim()
                val link = fixUrl(element.select("a").attr("href"))
                val imageUrl = fixUrl(element.select("img").attr("src"))

                Media(title, link, imageUrl, MediaType.Anime)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun fetchMediaDetails(link: String): MediaDetails {
        return try {
            val response = app.get(link)
            val document = response.document

            val title = document.selectFirst("h1.title")?.text()?.trim() ?: "Unknown"
            val description = document.selectFirst("div.description")?.text()?.trim() ?: "No description available."
            val genres = document.select("div.genres a").map { it.text().trim() }

            // Ambil status ongoing/completed dari halaman
            val statusText = document.selectFirst("div.status")?.text()?.lowercase() ?: ""
            val showStatus = when {
                "ongoing" in statusText -> ShowStatus.Ongoing
                "completed" in statusText -> ShowStatus.Completed
                else -> ShowStatus.Unknown
            }

            // Ambil episodes dan kelompokkan dengan DubStatus.Subbed (contoh)
            val episodes = document.select("div.episode-item").map { epElement ->
                val epTitle = epElement.selectFirst("h3.episode-title")?.text()?.trim() ?: "Episode"
                val epLink = fixUrl(epElement.selectFirst("a")?.attr("href") ?: "")
                Episode(epTitle, epLink)
            }

            MediaDetails(
                title = title,
                description = description,
                genres = genres,
                episodes = mapOf(DubStatus.Subbed to episodes),
                showStatus = showStatus
            )
        } catch (e: Exception) {
            MediaDetails(
                title = "Unknown",
                description = "No description available.",
                genres = emptyList(),
                episodes = emptyMap(),
                showStatus = ShowStatus.Unknown
            )
        }
    }

    override suspend fun fetchEpisode(link: String): List<Stream> {
        return try {
            val response = app.get(link)
            val document = response.document

            val streams = document.select("div.server-item").mapNotNull { serverElement ->
                val serverName = serverElement.selectFirst("h3.server-name")?.text()?.trim() ?: "Unknown"
                val videoSrc = serverElement.selectFirst("video")?.attr("src") ?: return@mapNotNull null

                Stream(videoSrc, serverName, "Video")
            }

            if (streams.isEmpty()) {
                listOf(Stream("", "No stream available", "Video"))
            } else streams
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Fungsi pembantu fixUrl untuk menambahkan base url jika link relatif
    private fun fixUrl(url: String): String {
        return if (url.startsWith("http")) url else "$mainUrl$url"
    }
}
