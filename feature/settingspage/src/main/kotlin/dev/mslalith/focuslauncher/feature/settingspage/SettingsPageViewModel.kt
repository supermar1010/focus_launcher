package dev.mslalith.focuslauncher.feature.settingspage

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mslalith.focuslauncher.core.common.appcoroutinedispatcher.AppCoroutineDispatcher
import dev.mslalith.focuslauncher.core.data.repository.settings.AppDrawerSettingsRepo
import dev.mslalith.focuslauncher.core.data.repository.settings.GeneralSettingsRepo
import dev.mslalith.focuslauncher.core.model.AppDrawerViewType
import dev.mslalith.focuslauncher.core.model.Constants.Defaults.Settings.AppDrawer.DEFAULT_APP_DRAWER_ICON_VIEW_TYPE
import dev.mslalith.focuslauncher.core.model.Constants.Defaults.Settings.AppDrawer.DEFAULT_APP_DRAWER_VIEW_TYPE
import dev.mslalith.focuslauncher.core.model.Constants.Defaults.Settings.AppDrawer.DEFAULT_APP_GROUP_HEADER
import dev.mslalith.focuslauncher.core.model.Constants.Defaults.Settings.AppDrawer.DEFAULT_SEARCH_BAR
import dev.mslalith.focuslauncher.core.model.Constants.Defaults.Settings.General.DEFAULT_IS_DEFAULT_LAUNCHER
import dev.mslalith.focuslauncher.core.model.Constants.Defaults.Settings.General.DEFAULT_NOTIFICATION_SHADE
import dev.mslalith.focuslauncher.core.model.Constants.Defaults.Settings.General.DEFAULT_SHOW_ICON_PACK
import dev.mslalith.focuslauncher.core.model.Constants.Defaults.Settings.General.DEFAULT_STATUS_BAR
import dev.mslalith.focuslauncher.core.model.appdrawer.AppDrawerIconViewType
import dev.mslalith.focuslauncher.core.ui.extensions.launchInIO
import dev.mslalith.focuslauncher.core.ui.extensions.withinScope
import dev.mslalith.focuslauncher.feature.settingspage.model.SettingsSheetState
import dev.mslalith.focuslauncher.feature.settingspage.model.SettingsState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
internal class SettingsPageViewModel @Inject constructor(
    private val generalSettingsRepo: GeneralSettingsRepo,
    private val appDrawerSettingsRepo: AppDrawerSettingsRepo,
    private val appCoroutineDispatcher: AppCoroutineDispatcher
) : ViewModel() {

    private val defaultSettingsState = SettingsState(
        showStatusBar = DEFAULT_STATUS_BAR,
        canDrawNotificationShade = DEFAULT_NOTIFICATION_SHADE,
        showIconPack = DEFAULT_SHOW_ICON_PACK,
        isDefaultLauncher = DEFAULT_IS_DEFAULT_LAUNCHER
    )

    private val defaultSettingsSheetState = SettingsSheetState(
        appDrawerViewType = DEFAULT_APP_DRAWER_VIEW_TYPE,
        appDrawerIconViewType = DEFAULT_APP_DRAWER_ICON_VIEW_TYPE,
        showAppGroupHeader = DEFAULT_APP_GROUP_HEADER,
        showSearchBar = DEFAULT_SEARCH_BAR
    )

    private val canShowIconPackStateFlow = appDrawerSettingsRepo.appDrawerViewTypeFlow
        .combine(flow = appDrawerSettingsRepo.appDrawerIconViewTypeFlow) { appDrawerViewType, appDrawerIconViewType ->
            appDrawerViewType == AppDrawerViewType.GRID || appDrawerIconViewType != AppDrawerIconViewType.TEXT
        }.withinScope(initialValue = DEFAULT_SHOW_ICON_PACK)

    val settingsState: StateFlow<SettingsState> = flowOf(value = defaultSettingsState)
        .combine(flow = generalSettingsRepo.statusBarVisibilityFlow) { state, showStatusBar ->
            state.copy(showStatusBar = showStatusBar)
        }.combine(flow = generalSettingsRepo.notificationShadeFlow) { state, canDrawNotificationShade ->
            state.copy(canDrawNotificationShade = canDrawNotificationShade)
        }.combine(flow = generalSettingsRepo.isDefaultLauncher) { state, isDefaultLauncher ->
            state.copy(isDefaultLauncher = isDefaultLauncher)
        }.combine(flow = canShowIconPackStateFlow) { state, showIconPack ->
            state.copy(showIconPack = showIconPack)
        }.withinScope(initialValue = defaultSettingsState)

    val settingsSheetState: StateFlow<SettingsSheetState> = flowOf(value = defaultSettingsSheetState)
        .combine(flow = appDrawerSettingsRepo.appDrawerViewTypeFlow) { state, appDrawerViewType ->
            state.copy(appDrawerViewType = appDrawerViewType)
        }.combine(flow = appDrawerSettingsRepo.appDrawerIconViewTypeFlow) { state, appDrawerIconViewType ->
            state.copy(appDrawerIconViewType = appDrawerIconViewType)
        }.combine(flow = appDrawerSettingsRepo.appGroupHeaderVisibilityFlow) { state, showAppGroupHeader ->
            state.copy(showAppGroupHeader = showAppGroupHeader)
        }.combine(flow = appDrawerSettingsRepo.searchBarVisibilityFlow) { state, showSearchBar ->
            state.copy(showSearchBar = showSearchBar)
        }.withinScope(initialValue = defaultSettingsSheetState)

    fun toggleStatusBarVisibility() {
        appCoroutineDispatcher.launchInIO {
            generalSettingsRepo.toggleStatusBarVisibility()
        }
    }

    fun toggleNotificationShade() {
        appCoroutineDispatcher.launchInIO {
            generalSettingsRepo.toggleNotificationShade()
        }
    }

    fun refreshIsDefaultLauncher(context: Context) {
        appCoroutineDispatcher.launchInIO {
            generalSettingsRepo.setIsDefaultLauncher(isDefault = context.isDefaultLauncher())
        }
    }

    fun updateAppDrawerViewType(appDrawerViewType: AppDrawerViewType) {
        appCoroutineDispatcher.launchInIO {
            updateAppDrawerIconViewTypeBasedOnAppDrawerViewType(appDrawerViewType = appDrawerViewType)
            appDrawerSettingsRepo.updateAppDrawerViewType(appDrawerViewType = appDrawerViewType)
        }
    }

    fun updateAppDrawerIconViewType(appDrawerIconViewType: AppDrawerIconViewType) {
        appCoroutineDispatcher.launchInIO {
            appDrawerSettingsRepo.updateAppDrawerIconViewType(appDrawerIconViewType = appDrawerIconViewType)
        }
    }

    fun toggleSearchBarVisibility() {
        appCoroutineDispatcher.launchInIO {
            appDrawerSettingsRepo.toggleSearchBarVisibility()
        }
    }

    fun toggleAppGroupHeaderVisibility() {
        appCoroutineDispatcher.launchInIO {
            appDrawerSettingsRepo.toggleAppGroupHeaderVisibility()
        }
    }

    private suspend fun updateAppDrawerIconViewTypeBasedOnAppDrawerViewType(appDrawerViewType: AppDrawerViewType) {
        val appDrawerIconViewType = appDrawerSettingsRepo.appDrawerIconViewTypeFlow.first()
        if (appDrawerViewType == AppDrawerViewType.GRID && appDrawerIconViewType == AppDrawerIconViewType.TEXT) updateAppDrawerIconViewType(appDrawerIconViewType = AppDrawerIconViewType.ICONS)
    }
}
