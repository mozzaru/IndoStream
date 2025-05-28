package com.anichin

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class Anichin : MainAPI() {
    override var name = "Anichin"
    override var mainUrl = "https://anichin.club"
    override var lang = "id"
    override val hasMainPage = true
    override val hasSearch = true

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("$mainUrl").document
        val items = document.select("div.donghua-item").mapNotNull { element ->
            val title = element.selectFirst("h3.title")?.text()?.trim() ?: return@mapNotNull null
            val link = fixUrl(element.selectFirst("a")?.attr("href") ?: return@mapNotNull null)
            val posterUrl = fixUrl(element.selectFirst("img")?.attr("src") ?: return@mapNotNull null)

            newAnimeSearchResponse(title, link, TvType.Anime) {
                this.posterUrl = posterUrl
            }
        }

        return HomePageResponse(listOf(HomePageList("Terbaru", items)))
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("div.donghua-item").mapNotNull { element ->
            val title = element.selectFirst("h3.title")?.text()?.trim() ?: return@mapNotNull null
            val link = fixUrl(element.selectFirst("a")?.attr("href") ?: return@mapNotNull null)
            val posterUrl = fixUrl(element.selectFirst("img")?.attr("src") ?: return@mapNotNull null)

            newAnimeSearchResponse(title, link, TvType.Anime) {
                this.posterUrl = posterUrl
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title = document.selectFirst("h1.title")?.text()?.trim() ?: "Unknown Title"
        val description = document.select("div.description").text().trim()
        val poster = document.selectFirst("img")?.attr("src")?.let { fixUrl(it) }

        val episodes = document.select("div.episode-item a").mapNotNull { ep ->
            val epName = ep.text().trim()
            val epUrl = fixUrl(ep.attr("href"))
            Episode(epUrl, epName)
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            this.plot = description
            this.episodes = episodes.reversed() // reversed agar episode awal di atas
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document

        val videoUrl = document.select("video").attr("src")
        if (videoUrl.isNotEmpty()) {
            callback(
                ExtractorLink(
                    name = "Anichin",
                    source = "anichin.club",
                    url = videoUrl,
                    referer = data,
                    quality = Qualities.Unknown.value,
                    isM3u8 = videoUrl.endsWith(".m3u8")
                )
            )
            return true
        }

        return false
    }
}
