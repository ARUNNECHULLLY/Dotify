package app.dotify.providers.innertube.requests

import app.dotify.providers.innertube.Innertube
import app.dotify.providers.innertube.models.BrowseResponse
import app.dotify.providers.innertube.models.ContinuationResponse
import app.dotify.providers.innertube.models.GridRenderer
import app.dotify.providers.innertube.models.MusicResponsiveListItemRenderer
import app.dotify.providers.innertube.models.MusicShelfRenderer
import app.dotify.providers.innertube.models.MusicTwoRowItemRenderer
import app.dotify.providers.innertube.models.bodies.BrowseBody
import app.dotify.providers.innertube.models.bodies.ContinuationBody
import app.dotify.providers.utils.runCatchingCancellable
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

suspend fun <T : Innertube.Item> Innertube.itemsPage(
    body: BrowseBody,
    fromListRenderer: (MusicResponsiveListItemRenderer) -> T? = { null },
    fromTwoRowRenderer: (MusicTwoRowItemRenderer) -> T? = { null }
) = runCatchingCancellable {
    val response = client.post(BROWSE) {
        setBody(body)
    }.body<BrowseResponse>()

    val sectionListRendererContent = response
        .contents
        ?.singleColumnBrowseResultsRenderer
        ?.tabs
        ?.firstOrNull()
        ?.tabRenderer
        ?.content
        ?.sectionListRenderer
        ?.contents
        ?.firstOrNull()

    itemsPageFromMusicShelRendererOrGridRenderer(
        musicShelfRenderer = sectionListRendererContent
            ?.musicShelfRenderer,
        gridRenderer = sectionListRendererContent
            ?.gridRenderer,
        fromMusicResponsiveListItemRenderer = fromListRenderer,
        fromMusicTwoRowItemRenderer = fromTwoRowRenderer
    )
}

suspend fun <T : Innertube.Item> Innertube.itemsPage(
    body: ContinuationBody,
    fromListRenderer: (MusicResponsiveListItemRenderer) -> T? = { null },
    fromTwoRowRenderer: (MusicTwoRowItemRenderer) -> T? = { null }
) = runCatchingCancellable {
    val response = client.post(BROWSE) {
        setBody(body)
    }.body<ContinuationResponse>()

    itemsPageFromMusicShelRendererOrGridRenderer(
        musicShelfRenderer = response
            .continuationContents
            ?.musicShelfContinuation,
        gridRenderer = null,
        fromMusicResponsiveListItemRenderer = fromListRenderer,
        fromMusicTwoRowItemRenderer = fromTwoRowRenderer
    )
}

private fun <T : Innertube.Item> itemsPageFromMusicShelRendererOrGridRenderer(
    musicShelfRenderer: MusicShelfRenderer?,
    gridRenderer: GridRenderer?,
    fromMusicResponsiveListItemRenderer: (MusicResponsiveListItemRenderer) -> T?,
    fromMusicTwoRowItemRenderer: (MusicTwoRowItemRenderer) -> T?
) = when {
    musicShelfRenderer != null -> Innertube.ItemsPage(
        continuation = musicShelfRenderer
            .continuations
            ?.firstOrNull()
            ?.nextContinuationData
            ?.continuation,
        items = musicShelfRenderer
            .contents
            ?.mapNotNull(MusicShelfRenderer.Content::musicResponsiveListItemRenderer)
            ?.mapNotNull(fromMusicResponsiveListItemRenderer)
    )

    gridRenderer != null -> Innertube.ItemsPage(
        continuation = null,
        items = gridRenderer
            .items
            ?.mapNotNull(GridRenderer.Item::musicTwoRowItemRenderer)
            ?.mapNotNull(fromMusicTwoRowItemRenderer)
    )

    else -> null
}
