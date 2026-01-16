package com.ola.fivethirtyeight.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestListener
import com.ola.fivethirtyeight.R
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.utils.toRelativeTime
import com.ola.fivethirtyeight.viewmodel.SharedViewModel


sealed class FeedCardLayout {
    data object LargeImageTop : FeedCardLayout()
    data object SmallImageRight : FeedCardLayout()
    data object TextOnly : FeedCardLayout()
}

fun resolveLayout(item: FeedItem): FeedCardLayout {
    val hasImage = hasValidImage(item)
    val hasDesc = !item.description.isNullOrBlank()

    return when {
        hasImage && hasDesc -> FeedCardLayout.LargeImageTop
        hasImage && !hasDesc -> FeedCardLayout.SmallImageRight
        else -> FeedCardLayout.TextOnly
    }
}


@Composable
fun FeedCardUniversal(
    item: FeedItem,
    onClick: () -> Unit
) {
    val layout = remember(item) { resolveLayout(item) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(1.dp),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,

        color = MaterialTheme.colorScheme.tertiary
    ) {
        when (layout) {
            FeedCardLayout.LargeImageTop -> FeedCardLargeImage(item)
            FeedCardLayout.SmallImageRight -> FeedCardSmallImageRight(item)
            FeedCardLayout.TextOnly -> FeedCardTextOnly(item)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun FeedCardLargeImage(
    item: FeedItem
) {
    if (FeedImageFailureCache.hasFailed(item.link)) {
        FeedCardTextOnly(item)
        return
    }

    val retryChain = remember(item.link) { item.imageRetryChain() }

    if (retryChain.isEmpty()) {
        FeedImageFailureCache.markFailed(item.link)
        FeedCardTextOnly(item)
        return
    }

    var attemptIndex by remember(item.link) { mutableStateOf(0) }

    Column {
        GlideImage(
            model = retryChain.getOrNull(attemptIndex),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            requestBuilderTransform = { rb ->
                rb.placeholder(R.drawable.loading_animation)
                    .error(R.drawable.fivessss1) // ✅ WILL NOW SHOW
                    .transform(CenterCrop())
                    .listener(object : RequestListener<Drawable> {

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return if (attemptIndex < retryChain.lastIndex) {
                                attemptIndex++        // 🔁 retry
                                true                  // ⛔ consume (no error drawable yet)
                            } else {
                                FeedImageFailureCache.markFailed(item.link)
                                false                 // ✅ allow error drawable
                            }
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return if (resource == null || !resource.isVisuallyValid()) {
                                if (attemptIndex < retryChain.lastIndex) {
                                    attemptIndex++
                                    true               // retry
                                } else {
                                    FeedImageFailureCache.markFailed(item.link)
                                    false              // show error drawable
                                }
                            } else {
                                false
                            }
                        }
                    })
            }
        )

        if (FeedImageFailureCache.hasFailed(item.link)) {
            FeedCardTextOnly(item)
        } else {
            FeedCardText(item)
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun FeedCardSmallImageRight(
    item: FeedItem
) {
    // 🔒 If this link already failed → TextOnly
    if (FeedImageFailureCache.hasFailed(item.link)) {
        FeedCardTextOnly(item)
        return
    }

    // 🔒 Cached failure → TextOnly
    if (FeedImageFailureCache.hasFailed(item.link)) {
        FeedCardTextOnly(item)
        return
    }

    val retryChain = remember(item.link) { item.imageRetryChain() }
    var attemptIndex by remember(item.link) { mutableStateOf(0) }

    // 3️⃣ HARD GUARD: no usable image URLs
    if (retryChain.isEmpty()) {
        FeedImageFailureCache.markFailed(item.link)
        FeedCardTextOnly(item)
        return
    }


    // 1️⃣ Cached failure → TextOnly
    if (FeedImageFailureCache.hasFailed(item.link)) {
        FeedCardTextOnly(item)
        return
    }
    Row(modifier = Modifier.padding(12.dp)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            FeedCardText(
                item = item,
                maxDescLines = 2,
                titleStyle = typography.titleMedium
            )
        }

        GlideImage(
            model = retryChain.getOrNull(attemptIndex),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp),
              //  .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop,
            requestBuilderTransform = { rb ->
                rb.listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (attemptIndex < retryChain.lastIndex) {
                            attemptIndex++ // 🔁 retry smaller variant
                        } else {
                            FeedImageFailureCache.markFailed(item.link)
                        }
                        return true // ⛔ we handled it
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (resource == null || !resource.isVisuallyValid()) {
                            // 🚫 NPR-style fake image
                            FeedImageFailureCache.markFailed(item.link)
                            return true
                        }
                        return false
                    }
                })
            }
        )

    }
}


object FeedImageFailureCache {
    private val failedLinks = mutableSetOf<String>()

    fun hasFailed(link: String): Boolean = failedLinks.contains(link)

    fun markFailed(link: String) {
        failedLinks.add(link)
    }
}

fun FeedItem.imageRetryChain(): List<String> =
    buildList {
        imageUrl?.let { add(it) }

        // common RSS / CDN downscale patterns
        imageUrl?.replace("original", "medium")?.let { add(it) }
        imageUrl?.replace("large", "medium")?.let { add(it) }
        imageUrl?.replace("large", "small")?.let { add(it) }
    }.distinct()






@Composable
private fun FeedCardTextOnly(
    item: FeedItem
) {
    FeedCardText(item)
}




@Composable
fun FeedCardDispatcher(
    item: FeedItem,
    onArticleClick: (FeedItem) -> Unit
) {
    FeedCardUniversal(
        item = item,
        onClick = { onArticleClick(item) }
    )
}

@Composable
private fun FeedCardText(
    item: FeedItem,
    maxDescLines: Int = 3,
     titleStyle: TextStyle = typography.titleLarge.copy(fontSize = (24.sp * LocalDensity.current.fontScale))   // ⬅️ default
) {
    Column(modifier = Modifier.padding(12.dp)) {

        Text(
            text = item.title,
            style = titleStyle,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (item.description.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.description,
                style = typography.bodyMedium,
                maxLines = maxDescLines,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = item.publishedAt.toRelativeTime(),
            color = MaterialTheme.colorScheme.secondary,
            style = typography.titleSmall,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

fun hasValidImage(item: FeedItem): Boolean =
    !item.imageUrl.isNullOrBlank() &&
            !item.imageUrl.contains("default", true) &&
            !item.imageUrl.contains("null", true) &&
            !item.imageUrl.contains("placeholder", true) &&
            !item.imageUrl.contains("tracking", true) &&
            !item.imageUrl.contains("spacer", true) &&
            !item.imageUrl.contains("pixel", true) &&
            !item.imageUrl.endsWith(".svg", true)


//Detect visually empty images at render-time (critical)/
fun Drawable.isVisuallyValid(): Boolean =
    intrinsicWidth >= 40 && intrinsicHeight >= 40


@Composable
fun FeedCardDispatcherSaved(
    item: FeedItem,
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    FeedCardUniversal(
        item = item,
        onClick = { onArticleClick(item) }
    )
}





