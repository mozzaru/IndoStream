package com.anichin

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.nicehttp.NiceResponse
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.Media
import com.lagradost.cloudstream3.MediaType
import com.lagradost.cloudstream3.MediaDetails
import com.lagradost.cloudstream3.Stream
import com.lagradost.cloudstream3.Episode

class Anichin : MainAPI() {
    override var mainUrl = "https://anichin.club"
    override var name = "Anichin"

    private val favorites = mutableListOf<Media>() // Daftar favorit

    override suspend fun fetchPopular(page: Int): List<Media> {
        return try {
            val response = app.get("$mainUrl/popular?page=$page")
            val document = response.document

            document.select("div.donghua-item").map { element ->
                val title = element.select("h3.title").text().trim()
                val link = element.select("a").attr("href")
                val imageUrl = element.select("img").attr("src")

                Media(
                    title = title,
                    link = link,
                    imageUrl = imageUrl,
                    type = MediaType.Anime
                )
            }
        } catch (e: Exception) {
            emptyList() // Mengembalikan daftar kosong jika terjadi kesalahan
        }
    }

    // Tambahan: Mengambil daftar genre
    suspend fun fetchGenres(): List<String> {
        return try {
            val response = app.get("$mainUrl/genres")
            val document = response.document

            document.select("div.genre-item a").map { it.text().trim() }
        } catch (e: Exception) {
            emptyList() // Mengembalikan daftar kosong jika terjadi kesalahan
        }
    }

    // Tambahan: Mengambil update terbaru
    suspend fun fetchLatestUpdates(page: Int): List<Media> {
        return try {
            val response = app.get("$mainUrl/latest?page=$page")
            val document = response.document

            document.select("div.donghua-item").map { element ->
                val title = element.select("h3.title").text().trim()
                val link = element.select("a").attr("href")
                val imageUrl = element.select("img").attr("src")

                Media(
                    title = title,
                    link = link,
                    imageUrl = imageUrl,
                    type = MediaType.Anime
                )
            }
        } catch (e: Exception) {
            emptyList() // Mengembalikan daftar kosong jika terjadi kesalahan
        }
    }

    // Tambahan: Mengambil donghua yang sudah lengkap
    suspend fun fetchCompletedDonghua(page: Int): List<Media> {
        return try {
            val response = app.get("$mainUrl/completed?page=$page")
            val document = response.document

            document.select("div.donghua-item").map { element ->
                val title = element.select("h3.title").text().trim()
                val link = element.select("a").attr("href")
                val imageUrl = element.select("img").attr("src")

                Media(
                    title = title,
                    link = link,
                    imageUrl = imageUrl,
                    type = MediaType.Anime
                )
            }
        } catch (e: Exception) {
            emptyList() // Mengembalikan daftar kosong jika terjadi kesalahan
        }
    }

    override suspend fun fetchSearch(query: String): List<Media> {
        return try {
            val response = app.get("$mainUrl/search?q=$query")
            val document = response.document

            document.select("div.donghua-item").map { element ->
                val title = element.select("h3.title").text().trim()
                val link = element.select("a").attr("href")
                val imageUrl = element.select("img").attr("src")

                Media(
                    title = title,
                    link = link,
                    imageUrl = imageUrl,
                    type = MediaType.Anime
                )
            }
        } catch (e: Exception) {
            emptyList() // Mengembalikan daftar kosong jika terjadi kesalahan
        }
    }

    override suspend fun fetchMediaDetails(link: String): MediaDetails {
        return try {
            val response = app.get(link)
            val document = response.document

            val title = document.select("h1.title").text().trim()
            val description = document.select("div.description").text().trim()
            val genres = document.select("div.genres a").map { it.text().trim() }
            val episodes = document.select("div.episode-item").map { episodeElement ->
                val episodeTitle = episodeElement.select("h3.episode-title").text().trim()
                val episodeLink = episodeElement.select("a").attr("href")
                val episodeDescription = episodeElement.select("div.episode-description").text().trim() // Ambil deskripsi episode jika ada
                Episode(episodeTitle, episodeLink, episodeDescription)
            }

            MediaDetails(
                title = title,
                description = description,
                genres = genres,
                episodes = episodes
            )
        } catch (e: Exception) {
            MediaDetails(
                title = "Unknown",
                description = "No description available.",
                genres = emptyList(),
                episodes = emptyList()
            ) // Mengembalikan nilai default jika terjadi kesalahan
        }
    }

    override suspend fun fetchEpisode(link: String): List<Stream> {
        return try {
            val response = app.get(link)
            val document = response.document

            // Mengambil daftar server dari halaman episode
            val servers = document.select("div.server-item").map { serverElement ->
                val serverName = serverElement.select("h3.server-name").text().trim()
                val streamUrl = serverElement.select("video").attr("src") // Sesuaikan selector dengan elemen video
                Stream(streamUrl, serverName, "Video")
            }

            servers.ifEmpty { listOf(Stream("No stream available", "Error", "Video")) } // Mengembalikan pesan jika tidak ada stream
        } catch (e: Exception) {
            emptyList() // Mengembalikan daftar kosong jika terjadi kesalahan
        }
    }

    // Tambahan: Mengambil detail episode dengan informasi lebih lanjut
    suspend fun fetchEpisodeDetails(link: String): EpisodeDetails {
        return try {
            val response = app.get(link)
            val document = response.document

            val title = document.select("h1.episode-title").text().trim()
            val description = document.select("div.episode-description").text().trim()
            val airDate = document.select("div.air-date").text().trim() // Ambil tanggal tayang jika ada

            EpisodeDetails(
                title = title,
                description = description,
                airDate = airDate
            )
        } catch (e: Exception) {
            EpisodeDetails(
                title = "Unknown",
                description = "No description available.",
                airDate = "Unknown"
            ) // Mengembalikan nilai default jika terjadi kesalahan
        }
    }

    // Tambahan: Menambahkan media ke daftar favorit
    fun addToFavorites(media: Media) {
        if (!favorites.contains(media)) {
            favorites.add(media)
        }
    }

    // Tambahan: Mengambil daftar favorit
    fun getFavorites(): List<Media> {
        return favorites
    }
}

// Data class untuk EpisodeDetails
data class EpisodeDetails(
    val title: String,
    val description: String,
    val airDate: String
)
