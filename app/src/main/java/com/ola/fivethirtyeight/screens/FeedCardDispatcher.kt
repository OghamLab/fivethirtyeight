package com.ola.fivethirtyeight.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.ola.fivethirtyeight.R
import com.ola.fivethirtyeight.model.FeedCluster
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.utils.toRelativeTime
import com.ola.fivethirtyeight.viewmodel.SharedViewModel


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FeedCard(
    item: FeedItem,
    viewModel: SharedViewModel,
    context: Context = LocalContext.current,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    val animatedElevation by animateDpAsState(
        targetValue = if (isVisible) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 350),
        label = ""
    )

    LaunchedEffect(Unit) { isVisible = true }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 6.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        tonalElevation = animatedElevation,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        color = MaterialTheme.colorScheme.tertiary
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 56.dp) // space for icons
            ) {

                // If you want to re-enable image support later, this is ready.
                /*
                if (!item.imageUrl.isNullOrEmpty()) {
                    GlideImage(
                        modifier = Modifier
                            .size(width = 120.dp, height = 100.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
                        model = item.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        requestBuilderTransform = { rb ->
                            rb.placeholder(R.drawable.loading_animation)
                                .error(R.drawable.picsart)
                                .transform(CenterCrop())
                        }
                    )
                }
                */

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {

                    Text(
                        text = item.title,
                        style = typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!item.description.isNullOrEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = item.description,
                            style = typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = item.publishedAt.toRelativeTime(),
                        style = typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Action icons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            ) {

                IconButton(onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, item.link)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }

                IconButton(onClick = {
                    viewModel.toggleSave(item.link, false)
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove from saved")
                }
            }
        }
    }
}


sealed class FeedCardLayout {
    data object LargeImageWithDesc : FeedCardLayout()
    data object SmallImageOnly : FeedCardLayout()
    data object DescriptionOnly : FeedCardLayout()
    data object Empty : FeedCardLayout()
}


fun resolveLayout(item: FeedItem): FeedCardLayout {
    val hasImage = !item.imageUrl.isNullOrEmpty() &&
            !item.imageUrl.contains("default") &&
            !item.imageUrl.contains("null")

    val hasDesc = !item.description.isNullOrEmpty()

    return when {
        hasImage && hasDesc -> FeedCardLayout.LargeImageWithDesc
        hasImage && !hasDesc -> FeedCardLayout.SmallImageOnly
        !hasImage && hasDesc -> FeedCardLayout.DescriptionOnly
        else -> FeedCardLayout.Empty
    }
}


@Composable
fun FeedClusterCard(
    cluster: FeedCluster,
    onArticleClick: (FeedItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {

        // HERO CARD (main article)
        FeedCardUniversal(
            item = cluster.main,
            isSaved = false,
            onClick = { onArticleClick(cluster.main) }
        )

        // VARIANTS HEADER
        if (cluster.variants.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (expanded) "Hide other sources" else "More sources",
                    style = typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
        }

        // VARIANT LIST
        AnimatedVisibility(expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            ) {
                cluster.variants.forEach { variant ->
                    VariantRow(
                        item = variant,
                        onClick = { onArticleClick(variant) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VariantRow(
    item: FeedItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {

        // Source logo (optional future upgrade)
        /* AsyncImage(
            model = item.sourceLogo,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
        ) */

        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = item.title,
                style = typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = item.publishedAt.toRelativeTime(),
                style = typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FeedCardUniversal(
    item: FeedItem,
    isSaved: Boolean = false,
    viewModel: SharedViewModel? = null,
    onClick: () -> Unit
) {
    val layout = remember(item) { resolveLayout(item) }

    var isVisible by remember { mutableStateOf(false) }
    val animatedElevation by animateDpAsState(
        targetValue = if (isVisible) 8.dp else 0.dp,
        animationSpec = tween(350),
        label = ""
    )
    LaunchedEffect(Unit) { isVisible = true }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 6.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        tonalElevation = animatedElevation,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        color = MaterialTheme.colorScheme.tertiary
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            // IMAGE SECTION
            when (layout) {
                FeedCardLayout.LargeImageWithDesc,
                FeedCardLayout.SmallImageOnly -> {
                    GlideImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (layout is FeedCardLayout.LargeImageWithDesc) 260.dp else 120.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                        model = item.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        requestBuilderTransform = { rb ->
                            rb.placeholder(R.drawable.loading_animation)
                                .error(R.drawable.picsart)
                                .transform(CenterCrop())
                        }
                    )
                }

                else -> Unit
            }

            // TEXT SECTION
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = item.title,
                    style = typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (layout is FeedCardLayout.LargeImageWithDesc ||
                    layout is FeedCardLayout.DescriptionOnly
                ) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = item.description,
                        style = typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.publishedAt.toRelativeTime(),
                    style = typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // ACTIONS (only for saved items)
            if (isSaved && viewModel != null) {
                val context = LocalContext.current

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, item.link)
                                type = "text/plain"
                            }
                            context.startActivity(
                                Intent.createChooser(sendIntent, "Share via")
                            )
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    IconButton(
                        onClick = {
                            viewModel.toggleSave(item.link, false)
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }

        }
    }
}


/*@Composable
fun FeedCardDispatcher(item: FeedItem, onArticleClick: (FeedItem) -> Unit) {
    FeedCardUniversal(
        item = item,
        isSaved = false,
        onClick = { onArticleClick(item) }
    )
}*/


/*@Composable
fun FeedCardDispatcher(
    clusters: List<FeedCluster>,
    onArticleClick: (FeedItem) -> Unit
) {
    LazyColumn {
        items(clusters) { cluster ->
            FeedClusterCard(cluster, onArticleClick)
        }
    }
}*/
@Composable
fun FeedCardDispatcher(
    item: FeedItem,
    onArticleClick: (FeedItem) -> Unit
) {
    FeedCardUniversal(
        item = item,
        isSaved = false,
        onClick = { onArticleClick(item) }
    )
}


@Composable
fun FeedClusterDispatcher(
    clusters: List<FeedCluster>,
    onArticleClick: (FeedItem) -> Unit
) {
    LazyColumn {
        items(clusters) { cluster ->
            FeedClusterCard(cluster, onArticleClick)
        }
    }
}


@Composable
fun FeedCardDispatcherSaved(
    item: FeedItem,
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    FeedCardUniversal(
        item = item,
        isSaved = true,
        viewModel = viewModel,
        onClick = { onArticleClick(item) }
    )
}





/*@Composable
fun FeedCardDispatcherSaved(
    item: FeedItem,
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    FeedCardUniversal(
        item = item,
        isSaved = true,
        viewModel = viewModel,
        onClick = { onArticleClick(item) }
    )
}*/


