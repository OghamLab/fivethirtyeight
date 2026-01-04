package com.ola.fivethirtyeight.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.ola.fivethirtyeight.R
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.utils.toRelativeTime
import com.ola.fivethirtyeight.viewmodel.SharedViewModel

@Composable
fun FeedCardDispatcher(
    item: FeedItem,
    onArticleClick: (FeedItem) -> Unit
) {

    if (item.imageUrl.isNotEmpty() && item.description.isNotEmpty() && !item.imageUrl.contains("default")) {
        FeedCardLargeImageDesc(item,) {
            onArticleClick(item)
        }
    } else if ((item.imageUrl.isEmpty() || item.imageUrl.contains("default") || item.imageUrl.contains(
            "null"
        )) && item.description.isNotEmpty()
    ) {
        FeedCardDesc(item) {
            onArticleClick(item)
        }

    } else if (item.imageUrl.isNotEmpty() && item.description.isEmpty() && !item.imageUrl.contains("default")) {
        FeedCardSmallImage(item) {
            onArticleClick(item)
        }

    } else if ((item.imageUrl.isEmpty() || item.imageUrl.contains("default") || item.imageUrl.contains(
            "null"
        )) && item.description.isEmpty()
    ) {
        FeedCardEmptyBoth(item) {
            onArticleClick(item)
        }

    } else if (item.imageUrl.isEmpty() || item.imageUrl.contains("default") || item.imageUrl.contains(
            "null"
        )
    ) {
        FeedEmptyImageOnly(item) {
            onArticleClick(item)
        }

    } else {
        FeedDefaultNoImageDec(item) {
            onArticleClick(item)
        }

    }


}

@Composable
fun FeedCardDispatcherSaved(
    item: FeedItem,
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    FeedCard(
        item = item,
        viewModel = viewModel,
        onClick = { onArticleClick(item) }
    )
}


@Composable
private fun FeedDefaultNoImageDec(
    it: FeedItem,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val animatedElevation by animateDpAsState(
        targetValue = if (isVisible) 8.dp else 0.dp, animationSpec = tween(durationMillis = 500),
        label = "",

        )

    LaunchedEffect(Unit) {
        isVisible = true
    }


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 2.dp, end = 2.dp, bottom = 16.dp)
            .clickable { onClick },
        tonalElevation = animatedElevation,
        color = MaterialTheme.colorScheme.tertiary,

        shadowElevation = 5.dp,
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, Color.LightGray)


    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )


        {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp)
            ) {

                Text(
                    text = it.title,
                    modifier = Modifier.padding(
                        top = 12.dp,
                        bottom = 4.dp,
                        start = 16.dp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,

                    )

                Text(
                    text = it.publishedAt,
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                        start = 16.dp
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    style = typography.titleSmall
                )

            }

        }

    }
}

@Composable
private fun FeedEmptyImageOnly(
    it: FeedItem,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val animatedElevation by animateDpAsState(
        targetValue = if (isVisible) 8.dp else 0.dp, animationSpec = tween(durationMillis = 500),
        label = "",

        )

    LaunchedEffect(Unit) {
        isVisible = true
    }


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()


            .padding(start = 2.dp, end = 2.dp, bottom = 16.dp)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.tertiary,
        tonalElevation = animatedElevation,
        shadowElevation = 5.dp,
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, Color.LightGray),


        ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )


        {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp)
            ) {

                Text(
                    text = it.title,
                    modifier = Modifier.padding(
                        top = 12.dp,
                        bottom = 4.dp,
                        start = 16.dp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = typography.titleMedium.copy(lineHeight = 24.sp),
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = it.description,
                    modifier = Modifier.padding(
                        bottom = 16.dp,
                        start = 16.dp,
                        top = 8.dp,
                        end = 16.dp
                    ),
                    color = MaterialTheme.colorScheme.onTertiary,
                    style = typography.bodyMedium.copy(lineHeight = 24.sp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )



                Text(
                    text = it.publishedAt.toRelativeTime(),
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                        start = 16.dp
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    style = typography.titleSmall
                )

            }
        }
    }
}

@Composable
private fun FeedCardEmptyBoth(
    it: FeedItem,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val animatedElevation by animateDpAsState(
        targetValue = if (isVisible) 8.dp else 0.dp, animationSpec = tween(durationMillis = 500),
        label = "",

        )

    LaunchedEffect(Unit) {
        isVisible = true
    }


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()


            .padding(start = 2.dp, end = 2.dp, bottom = 16.dp)
            .clickable(onClick = onClick),
        tonalElevation = animatedElevation,
        color = MaterialTheme.colorScheme.tertiary,
        shadowElevation = 5.dp,
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    )

    {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )


        {


            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp)
            ) {

                Text(
                    text = it.title,
                    modifier = Modifier.padding(
                        top = 4.dp,
                        bottom = 0.dp,
                        start = 16.dp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = typography.titleMedium.copy(lineHeight = 24.sp),
                    fontWeight = FontWeight.ExtraBold
                )


                Text(
                    text = it.publishedAt.toRelativeTime(),
                    modifier = Modifier.padding(
                        top = 12.dp,
                        bottom = 8.dp,
                        start = 16.dp
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    style = typography.titleSmall
                )

            }
        }
    }
}

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
private fun FeedCardSmallImage(
    it: FeedItem,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val animatedElevation by animateDpAsState(
        targetValue = if (isVisible) 8.dp else 0.dp, animationSpec = tween(durationMillis = 500),
        label = "",

        )

    LaunchedEffect(Unit) {
        isVisible = true
    }



    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 4.dp, end = 4.dp, bottom = 16.dp)

            .clickable(onClick = onClick),
        tonalElevation = animatedElevation,
        color = MaterialTheme.colorScheme.tertiary,

        shadowElevation = 5.dp,
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, Color.LightGray)


    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()

                .wrapContentHeight()
        )


        {

            GlideImage(
                modifier = Modifier.size(
                    width = 150.dp,
                    height = 120.dp
                ),

                model = it.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                requestBuilderTransform = { requestBuilder ->
                    requestBuilder
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.picsart)
                        .transform(CenterCrop())
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()


            ) {


                Text(
                    modifier = Modifier.padding(
                        top = 12.dp,
                        bottom = 4.dp,
                        start = 16.dp
                    ),
                    text = it.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,

                    )

                Text(
                    text = it.publishedAt.toRelativeTime(),
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                        start = 16.dp
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    style = typography.titleSmall
                )

            }
        }

    }
}


@Composable
private fun FeedCardDesc(
    it: FeedItem,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val animatedElevation by animateDpAsState(
        targetValue = if (isVisible) 8.dp else 0.dp, animationSpec = tween(durationMillis = 500),
        label = "",

        )

    LaunchedEffect(Unit) {
        isVisible = true
    }


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 4.dp, end = 4.dp, bottom = 16.dp)

            .clickable(onClick = onClick),
        tonalElevation = animatedElevation,
        color = MaterialTheme.colorScheme.tertiary,

        shadowElevation = 5.dp,
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, Color.LightGray)


    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight()


        )

        {

            Spacer(Modifier.height(12.dp))

            Text(
                text = it.title,
                modifier = Modifier.padding(
                    bottom = 8.dp,
                    start = 16.dp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                style = typography.titleLarge.copy(lineHeight = 24.sp),
                fontWeight = FontWeight.Bold

            )
            Text(
                text = it.description,
                modifier = Modifier.padding(
                    bottom = 16.dp,
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp

                ),
                color = MaterialTheme.colorScheme.onTertiary,
                style = typography.bodyMedium.copy(lineHeight = 24.sp),

                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )


            Text(
                text = it.publishedAt.toRelativeTime(),
                modifier = Modifier.padding(
                    bottom = 24.dp,
                    start = 16.dp
                ),
                color = MaterialTheme.colorScheme.secondary,
                style = typography.titleSmall,

                )


        }
    }
}

@Composable
@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
private fun FeedCardLargeImageDesc(
    it: FeedItem,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val animatedElevation by animateDpAsState(
        targetValue = if (isVisible) 8.dp else 0.dp, animationSpec = tween(durationMillis = 500),
        label = "",

        )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 4.dp, end = 2.dp, bottom = 16.dp)


            .clickable(
                onClick = onClick
            ),
        color = MaterialTheme.colorScheme.tertiary,

        tonalElevation = animatedElevation,


        shadowElevation = 5.dp,
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, Color.LightGray),

        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight()


        ) {

            GlideImage(
                modifier = Modifier
                    .fillMaxSize()
                    .height(300.dp),
                model = it.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                requestBuilderTransform = { requestBuilder ->
                    requestBuilder
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.picsart)
                        .transform(CenterCrop())
                }

            )

            Spacer(Modifier.height(12.dp))
            Text(
                modifier = Modifier.padding(

                    bottom = 4.dp,
                    start = 16.dp,
                    end = 16.dp

                ),
                text = it.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,

                )


            Text(
                modifier = Modifier.padding(
                    top = 8.dp,
                    bottom = 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                text = it.description,
                color = MaterialTheme.colorScheme.onTertiary,
                style = typography.bodyMedium.copy(lineHeight = 24.sp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = it.publishedAt.toRelativeTime(),
                modifier = Modifier.padding(
                    bottom = 8.dp,
                    start = 16.dp,

                    ),
                color = MaterialTheme.colorScheme.secondary,
                style = typography.titleSmall
            )

            Spacer(Modifier.height(12.dp))

        }
    }
}


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
        animationSpec = tween(durationMillis = 500),
        label = ""
    )

    LaunchedEffect(Unit) { isVisible = true }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .clickable { onClick() },
        tonalElevation = animatedElevation,
        color = MaterialTheme.colorScheme.tertiary,
        shadowElevation = 5.dp,
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 48.dp) // leave space for icons
            ) {
                // 🖼️ Optional image
                /*if (!item.imageUrl.isNullOrEmpty()) {
                    GlideImage(
                        modifier = Modifier
                            .size(width = 120.dp, height = 100.dp)
                            .clip(RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp)),
                        model = item.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        requestBuilderTransform = { rb ->
                            rb.placeholder(R.drawable.loading_animation)
                                .error(R.drawable.picsart)
                                .transform(CenterCrop())
                        }
                    )
                }*/

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
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

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.publishedAt.toRelativeTime(),
                        style = typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // 📤 Share + ❌ Delete icons anchored bottom‑end
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


