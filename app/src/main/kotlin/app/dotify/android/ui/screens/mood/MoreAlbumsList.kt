package app.dotify.android.ui.screens.mood

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.dotify.android.LocalPlayerAwareWindowInsets
import app.dotify.android.R
import app.dotify.android.ui.components.ShimmerHost
import app.dotify.android.ui.components.themed.Header
import app.dotify.android.ui.components.themed.HeaderPlaceholder
import app.dotify.android.ui.items.AlbumItem
import app.dotify.android.ui.items.AlbumItemPlaceholder
import app.dotify.compose.persist.persist
import app.dotify.core.ui.Dimensions
import app.dotify.core.ui.LocalAppearance
import app.dotify.providers.innertube.Innertube
import app.dotify.providers.innertube.models.bodies.BrowseBody
import app.dotify.providers.innertube.requests.BrowseResult
import app.dotify.providers.innertube.requests.browse
import com.valentinilk.shimmer.shimmer
import kotlinx.collections.immutable.toImmutableList

private const val DEFAULT_BROWSE_ID = "FEmusic_new_releases_albums"

@Composable
fun MoreAlbumsList(
    onAlbumClick: (browseId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorPalette) = LocalAppearance.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    var albumsPage by persist<BrowseResult>(tag = "more_albums/list")
    val data by remember {
        derivedStateOf {
            albumsPage
                ?.items
                ?.firstOrNull()
                ?.items
                ?.filterIsInstance<Innertube.AlbumItem>()
                ?.toImmutableList()
        }
    }

    LaunchedEffect(Unit) {
        if (albumsPage != null) return@LaunchedEffect

        albumsPage = Innertube
            .browse(BrowseBody(browseId = DEFAULT_BROWSE_ID))
            ?.also { it.exceptionOrNull()?.printStackTrace() }
            ?.getOrNull()
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(Dimensions.thumbnails.album + Dimensions.items.horizontalPadding),
        contentPadding = windowInsets
            .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
            .asPaddingValues(),
        modifier = modifier
            .background(colorPalette.background0)
            .fillMaxSize()
    ) {
        item(
            key = "header",
            contentType = 0,
            span = { GridItemSpan(maxLineSpan) }
        ) {
            if (albumsPage == null) HeaderPlaceholder(modifier = Modifier.shimmer())
            else Header(
                title = stringResource(R.string.new_released_albums),
                modifier = Modifier.padding(endPaddingValues)
            )
        }

        data?.let { page ->
            itemsIndexed(
                items = page,
                key = { i, item -> "item:$i,${item.key}" }
            ) { _, album ->
                BoxWithConstraints {
                    AlbumItem(
                        album = album,
                        thumbnailSize = maxWidth - Dimensions.items.horizontalPadding * 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAlbumClick(album.key)
                            },
                        alternative = true
                    )
                }
            }
        }

        if (albumsPage == null) item(
            key = "loading",
            contentType = 0,
            span = { GridItemSpan(maxLineSpan) }
        ) {
            ShimmerHost(modifier = Modifier.fillMaxWidth()) {
                repeat(16) {
                    AlbumItemPlaceholder(thumbnailSize = Dimensions.thumbnails.album)
                }
            }
        }
    }
}
