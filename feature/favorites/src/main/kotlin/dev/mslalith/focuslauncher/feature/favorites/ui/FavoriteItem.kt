package dev.mslalith.focuslauncher.feature.favorites.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.mslalith.focuslauncher.core.model.app.AppWithColor
import dev.mslalith.focuslauncher.core.ui.remember.rememberAppColor
import dev.mslalith.focuslauncher.feature.favorites.model.FavoritesContextMode

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FavoriteItem(
    modifier: Modifier = Modifier,
    appWithColor: AppWithColor,
    isInContextualMode: () -> Boolean,
    isAppAboutToReorder: () -> Boolean,
    changeFavoritesContextMode: (FavoritesContextMode) -> Unit,
    onClick: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onSurface

    val appIconBasedColor = rememberAppColor(graphicsColor = appWithColor.color)

    val animatedColor by animateColorAsState(
        label = "Animated color",
        targetValue = if (isAppAboutToReorder()) contentColor else appIconBasedColor,
        animationSpec = tween(durationMillis = 2300)
    )

    fun backgroundColor(): Color = animatedColor.copy(alpha = if (isAppAboutToReorder()) 0.8f else 0.23f)

    fun textColor(): Color = if (isAppAboutToReorder()) backgroundColor else animatedColor

    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.small)
            .background(color = backgroundColor())
            .border(
                width = 0.4.dp,
                color = animatedColor.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.small
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    if (isInContextualMode()) return@combinedClickable
                    changeFavoritesContextMode(FavoritesContextMode.Open)
                }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = appWithColor.app.displayName,
            color = textColor(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
