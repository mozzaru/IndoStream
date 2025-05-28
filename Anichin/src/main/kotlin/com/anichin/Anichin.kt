package com.anichin

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.nicehttp.NiceResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class Anichin : MainAPI() {
    override var mainUrl = "https://anichin.club"
    override var name = "Anichin"
    override val hasMainPage = true
    override var lang = "id"
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie, TvType.OVA)

    override val mainPage = mainPageOf(
        "$mainUrl/page/" to "Terbaru"
    )

    private suspend fun request(url: String): NiceResponse {
        return app.get(url)
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = request("${request.data}$page").document
        val home = doc.select("article").map { it.toSearchResult() }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): AnimeSearchResponse {
        val href = fixUrl(this.select("a").attr("href"))
        val title = this.select("h2").text()
        val poster = fixUrl(this.select("img").attr("src"))
        return newAnimeSearchResponse(title, href, TvType.Anime) {
            posterUrl = poster
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = request("$mainUrl/?s=$query").document
        return doc.select("article").map { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = request(url).document
        val title = doc.selectFirst("h1")?.text()?.replace("Subtitle Indonesia", "")?.trim() ?: ""
        val poster = doc.selectFirst("img.alignnone")?.attr("src")
        val description = doc.selectFirst(".entry-content > p")?.text()
        val episodes = doc.select("ul.daftar li").map {
            val link = it.select("a").attr("href")
            val name = it.text()
            val epNum = Regex("Episode\\s?(\\d+)").find(name)?.groupValues?.getOrNull(1)?.toIntOrNull()
            Episode(link, episode = epNum)
        }.reversed()

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            this.plot = description
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = request(data).document
        doc.select("iframe").map { iframe ->
            val iframeUrl = iframe.attr("src")
            loadExtractor(iframeUrl, data, subtitleCallback, callback)
        }
        return true
    }
}
