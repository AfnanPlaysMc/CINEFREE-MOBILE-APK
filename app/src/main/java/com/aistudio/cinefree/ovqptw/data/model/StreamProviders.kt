package com.aistudio.cinefree.ovqptw.data.model

data class StreamServer(
    val id: String,
    val name: String,
    val badge: String,
    val description: String,
    val embedUrl: String
)

object StreamProviders {
    fun buildServers(mediaId: String, mediaType: String = "movie", season: Int = 1, episode: Int = 1): List<StreamServer> {
        val isMovie = mediaType == "movie"
        return listOf(
            StreamServer(
                id = "vidlink",
                name = "Server 1: VidLink Fast HD (Primary)",
                badge = "Recommended • Fast Stream",
                description = "High-speed clean streaming node with auto-playback and subtitle support.",
                embedUrl = if (isMovie) "https://vidlink.pro/movie/$mediaId?primaryColor=e50914" else "https://vidlink.pro/tv/$mediaId/$season/$episode?primaryColor=e50914"
            ),
            StreamServer(
                id = "vidsrc_sh",
                name = "Server 2: VidSrc SH (Direct)",
                badge = "Direct Mirror • 1080p",
                description = "High-availability direct stream player.",
                embedUrl = if (isMovie) "https://vidsrc.sh/embed/movie?tmdb=$mediaId" else "https://vidsrc.sh/embed/tv?tmdb=$mediaId&sea=$season&epi=$episode"
            ),
            StreamServer(
                id = "autoembed",
                name = "Server 3: AutoEmbed Cloud",
                badge = "Ultra Stable",
                description = "Multi-source failover node with fast global buffering.",
                embedUrl = if (isMovie) "https://player.autoembed.cc/embed/movie/$mediaId" else "https://player.autoembed.cc/embed/tv/$mediaId/$season/$episode"
            ),
            StreamServer(
                id = "twoembed",
                name = "Server 4: 2Embed HD",
                badge = "Fast Mirror",
                description = "Secondary high-bandwidth node with global film and TV coverage.",
                embedUrl = if (isMovie) "https://www.2embed.cc/embed/$mediaId" else "https://www.2embed.cc/embedtv/$mediaId&s=$season&e=$episode"
            ),
            StreamServer(
                id = "multiembed",
                name = "Server 5: MultiEmbed HD",
                badge = "Multi-CDN",
                description = "Aggregator node with multiple streaming links.",
                embedUrl = if (isMovie) "https://multiembed.mov/?video_id=$mediaId&tmdb=1" else "https://multiembed.mov/?video_id=$mediaId&tmdb=1&s=$season&e=$episode"
            ),
            StreamServer(
                id = "vidsrc_icu",
                name = "Server 6: VidSrc Pro",
                badge = "Multi-Audio",
                description = "Alternative VidSrc network with multiple audio tracks.",
                embedUrl = if (isMovie) "https://vidsrc.icu/embed/movie/$mediaId" else "https://vidsrc.icu/embed/tv/$mediaId/$season/$episode"
            )
        )
    }

    fun getDownloadUrl(mediaId: String, mediaType: String = "movie", season: Int = 1, episode: Int = 1): String {
        val isMovie = mediaType == "movie"
        return if (isMovie) {
            "https://multiembed.mov/?video_id=$mediaId&tmdb=1&download=1"
        } else {
            "https://multiembed.mov/?video_id=$mediaId&tmdb=1&s=$season&e=$episode&download=1"
        }
    }
}
