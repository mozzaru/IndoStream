package com.anichin

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class Anichin : MainAPI() {
    override var mainUrl = "https://anichin.club"
    override var name = "Anichin"
    override val lang = "id" // Menambahkan bahasa Indonesia
    override val hasMainPage = true
    override val hasSearch = true

    // Menggunakan metode search() sesuai dengan dokumentasi CloudStream3
    override suspend fun search(query: String): List<SearchResponse> {
        return try {
            val response = app.get("$mainUrl/search?q=$query")
            val document = response.document
            document.select("div.donghua-item").map { element ->
                val title = element.select("h3.title").text().trim()
                val link = fixUrl(element.select("a").attr("href"))
                val imageUrl = fixUrl(element.select("img").attr("src"))

                AnimeSearchResponse(
                    title = title,
                    url = link,
                    imageUrl = imageUrl,
                    description = "Deskripsi tidak tersedia",
                    genres = emptyList(),
                    releaseDate = null
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Menggunakan metode getMainPage() untuk halaman utama
    override suspend fun getMainPage(): HomePageResponse {
        val items = mutableListOf<HomePageList>()
        try {
            val response = app.get(mainUrl)
            val document = response.document
            document.select("div.donghua-item").forEach { element ->
                val title = element.select("h3.title").text().trim()
                val link = fixUrl(element.select("a").attr("href"))
                val imageUrl = fixUrl(element.select("img").attr("src"))

                items.add(
                    HomePageList(
                        name = title,
                        url = link,
                        imageUrl = imageUrl,
                        type = HomePageList.Type.Anime
                    )
                )
            }
        } catch (e: Exception) {
            // Menangani kesalahan jika terjadi
        }
        return HomePageResponse(items)
    }

    // Menggunakan metode load() untuk memuat detail media
    override suspend fun load(url: String): LoadResponse {
        return try {
            val response = app.get(url)
            val document = response.document

            val title = document.select("h1.title").text().trim()
            val description = document.select("div.description").text().trim()
            val genres = document.select("div.genres a").map { it.text().trim() }

            val episodes = document.select("div.episode-item").map { epElement ->
                val epTitle = epElement.select("h3.episode-title").text().trim()
                val epLink = fixUrl(epElement.select("a").attr("href"))
                Episode(epTitle, epLink)
            }

            LoadResponse(
                title = title,
                description = description,
                genres = genres,
                episodes = episodes
            )
        } catch (e: Exception) {
            LoadResponse("Unknown", "Deskripsi tidak tersedia", emptyList(), emptyList())
        }
    }

    // Menggunakan metode loadLinks() untuk memuat tautan episode
    override suspend fun loadLinks(url: String): List<Stream> {
        return try {
            val response = app.get(url)
            val document = response.document

            document.select("div.server-item").mapNotNull { serverElement ->
                val serverName = serverElement.select("h3.server-name").text().trim()
                val videoSrc = serverElement.select("video").attr("src")
                if (videoSrc.isNotEmpty()) {
                    Stream(videoSrc, serverName, "Video")
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Fungsi pembantu untuk memperbaiki URL relatif
    private fun fixUrl(url: String): String {
        return if (url.startsWith("http")) url else "$mainUrl$url"
    }
}
