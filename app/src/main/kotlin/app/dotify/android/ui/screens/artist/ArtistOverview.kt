package app.dotify.android.ui.screens.artist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.dotify.android.LocalPlayerAwareWindowInsets
import app.dotify.android.LocalPlayerServiceBinder
import app.dotify.android.R
import app.dotify.android.ui.components.LocalMenuState
import app.dotify.android.ui.components.ShimmerHost
import app.dotify.android.ui.components.themed.Attribution
import app.dotify.android.ui.components.themed.FloatingActionsContainerWithScrollToTop
import app.dotify.android.ui.components.themed.LayoutWithAdaptiveThumbnail
import app.dotify.android.ui.components.themed.NonQueuedMediaItemMenu
import app.dotify.android.ui.components.themed.SecondaryTextButton
import app.dotify.android.ui.components.themed.TextPlaceholder
import app.dotify.android.ui.items.AlbumItem
import app.dotify.android.ui.items.AlbumItemPlaceholder
import app.dotify.android.ui.items.SongItem
import app.dotify.android.ui.items.SongItemPlaceholder
import app.dotify.android.utils.asMediaItem
import app.dotify.android.utils.forcePlay
import app.dotify.android.utils.medium
import app.dotify.android.utils.playingSong
import app.dotify.android.utils.secondary
import app.dotify.android.utils.semiBold
import app.dotify.core.ui.Dimensions
import app.dotify.core.ui.LocalAppearance
import app.dotify.core.ui.utils.isLandscape
import app.dotify.providers.innertube.Innertube
import app.dotify.providers.innertube.models.NavigationEndpoint

private val sectionTextModifier = Modifier
    .padding(horizontal = 16.dp)
    .padding(top = 24.dp, bottom = 8.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistOverview(
    youtubeArtistPage: Innertube.ArtistPage?,
    onViewAllSongsClick: () -> Unit,
    onViewAllAlbumsClick: () -> Unit,
    onViewAllSinglesClick: () -> Unit,
    onAlbumClick: (String) -> Unit,
    thumbnailContent: @Composable () -> Unit,
    headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    modifier: Modifier = Modifier
) = LayoutWithAdaptiveThumbnail(
    thumbnailContent = thumbnailContent,
    modifier = modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val scrollState = rememberScrollState()

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    windowInsets
                        .only(WindowInsetsSides.Vertical)
                        .asPaddingValues()
                )
        ) {
            Box(modifier = Modifier.padding(endPaddingValues)) {
                headerContent {
                    youtubeArtistPage?.shuffleEndpoint?.let { endpoint ->
                        SecondaryTextButton(
                            text = stringResource(R.string.shuffle),
                            onClick = {
                                binder?.stopRadio()
                                binder?.playRadio(endpoint)
                            }
                        )
                    }
                    youtubeArtistPage?.subscribersCountText?.let { subscribers ->
                        BasicText(
                            text = stringResource(R.string.format_subscribers, subscribers),
                            style = typography.xxs.medium
                        )
                    }
                }
            }

            if (!isLandscape) thumbnailContent()

            youtubeArtistPage?.let { artist ->
                artist.songs?.let { songs ->
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(endPaddingValues)
                    ) {
                        BasicText(
                            text = stringResource(R.string.songs),
                            style = typography.m.semiBold,
                            modifier = sectionTextModifier
                        )

                        artist.songsEndpoint?.let {
                            BasicText(
                                text = stringResource(R.string.view_all),
                                style = typography.xs.secondary,
                                modifier = sectionTextModifier.clickable(onClick = onViewAllSongsClick)
                            )
                        }
                    }

                    val (currentMediaId, playing) = playingSong(binder)

                    songs.forEach { song ->
                        SongItem(
                            song = song,
                            thumbnailSize = Dimensions.thumbnails.song,
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {
                                            NonQueuedMediaItemMenu(
                                                onDismiss = menuState::hide,
                                                mediaItem = song.asMediaItem
                                            )
                                        }
                                    },
                                    onClick = {
                                        val mediaItem = song.asMediaItem
                                        binder?.stopRadio()
                                        binder?.player?.forcePlay(mediaItem)
                                        binder?.setupRadio(
                                            NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                        )
                                    }
                                )
                                .padding(endPaddingValues),
                            isPlaying = playing && currentMediaId == song.key
                        )
                    }
                }

                artist.albums?.let { albums ->
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(endPaddingValues)
                    ) {
                        BasicText(
                            text = stringResource(R.string.albums),
                            style = typography.m.semiBold,
                            modifier = sectionTextModifier
                        )

                        artist.albumsEndpoint?.let {
                            BasicText(
                                text = stringResource(R.string.view_all),
                                style = typography.xs.secondary,
                                modifier = sectionTextModifier.clickable(onClick = onViewAllAlbumsClick)
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = endPaddingValues,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = albums,
                            key = Innertube.AlbumItem::key
                        ) { album ->
                            AlbumItem(
                                album = album,
                                thumbnailSize = Dimensions.thumbnails.album,
                                alternative = true,
                                modifier = Modifier.clickable {
                                    onAlbumClick(album.key)
                                }
                            )
                        }
                    }
                }

                artist.singles?.let { singles ->
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(endPaddingValues)
                    ) {
                        BasicText(
                            text = stringResource(R.string.singles),
                            style = typography.m.semiBold,
                            modifier = sectionTextModifier
                        )

                        artist.singlesEndpoint?.let {
                            BasicText(
                                text = stringResource(R.string.view_all),
                                style = typography.xs.secondary,
                                modifier = sectionTextModifier.clickable(onClick = onViewAllSinglesClick)
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = endPaddingValues,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = singles,
                            key = Innertube.AlbumItem::key
                        ) { album ->
                            AlbumItem(
                                album = album,
                                thumbnailSize = Dimensions.thumbnails.album,
                                alternative = true,
                                modifier = Modifier.clickable(onClick = { onAlbumClick(album.key) })
                            )
                        }
                    }
                }

                artist.description?.let { description ->
                    Attribution(
                        text = description,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    )
                }

                Unit
            } ?: ArtistOverviewBodyPlaceholder()
        }

        youtubeArtistPage?.radioEndpoint?.let { endpoint ->
            FloatingActionsContainerWithScrollToTop(
                scrollState = scrollState,
                icon = R.drawable.radio,
                onClick = {
                    binder?.stopRadio()
                    binder?.playRadio(endpoint)
                }
            )
        }
    }
}

@Composable
fun ArtistOverviewBodyPlaceholder(modifier: Modifier = Modifier) = ShimmerHost(
    modifier = modifier
) {
    TextPlaceholder(modifier = sectionTextModifier)

    repeat(5) {
        SongItemPlaceholder(thumbnailSize = Dimensions.thumbnails.song)
    }

    repeat(2) {
        TextPlaceholder(modifier = sectionTextModifier)

        Row {
            repeat(2) {
                AlbumItemPlaceholder(
                    thumbnailSize = Dimensions.thumbnails.album,
                    alternative = true
                )
            }
        }
    }
}
