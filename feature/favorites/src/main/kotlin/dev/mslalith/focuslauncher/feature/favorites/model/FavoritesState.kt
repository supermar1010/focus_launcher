package dev.mslalith.focuslauncher.feature.favorites.model

import dev.mslalith.focuslauncher.core.model.AppWithIcon

internal data class FavoritesState(
    val favoritesList: List<AppWithIcon>,
    val favoritesContextualMode: FavoritesContextMode
)
