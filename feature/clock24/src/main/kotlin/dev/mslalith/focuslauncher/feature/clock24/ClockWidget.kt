package dev.mslalith.focuslauncher.feature.clock24

import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mslalith.focuslauncher.core.model.ClockAlignment
import dev.mslalith.focuslauncher.core.testing.compose.modifier.testsemantics.testSemantics
import dev.mslalith.focuslauncher.core.ui.effects.OnLifecycleEventChange
import dev.mslalith.focuslauncher.core.ui.effects.SystemBroadcastReceiver
import dev.mslalith.focuslauncher.core.ui.extensions.clickableNoRipple
import dev.mslalith.focuslauncher.core.ui.extensions.modifyIf
import dev.mslalith.focuslauncher.feature.clock24.model.Clock24State
import dev.mslalith.focuslauncher.feature.clock24.utils.TestTags

@Composable
fun ClockWidget(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp,
    verticalPadding: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    ClockWidgetInternal(
        modifier = modifier,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        onClick = onClick,
        contentColor = contentColor
    )
}

@Composable
internal fun ClockWidgetInternal(
    modifier: Modifier = Modifier,
    clock24ViewModel: Clock24ViewModel = hiltViewModel(),
    horizontalPadding: Dp,
    verticalPadding: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val clock24State by clock24ViewModel.clock24State.collectAsStateWithLifecycle()

    ClockWidget(
        modifier = modifier,
        clock24State = clock24State,
        refreshTime = clock24ViewModel::refreshTime,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        onClick = onClick,
        contentColor = contentColor
    )
}

@Composable
internal fun ClockWidget(
    modifier: Modifier = Modifier,
    clock24State: Clock24State,
    refreshTime: () -> Unit,
    horizontalPadding: Dp,
    verticalPadding: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val updatedRefreshTime by rememberUpdatedState(newValue = refreshTime)

    val horizontalBias by animateFloatAsState(
        label = "Clock Alignment",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        targetValue = when (clock24State.clockAlignment) {
            ClockAlignment.START -> -1f
            ClockAlignment.CENTER -> 0f
            ClockAlignment.END -> 1f
        }
    )

    SystemBroadcastReceiver(systemAction = Intent.ACTION_TIME_TICK) {
        updatedRefreshTime()
    }

    OnLifecycleEventChange { event ->
        if (event == Lifecycle.Event.ON_RESUME) updatedRefreshTime()
    }

    val clickModifier = Modifier.modifyIf(predicate = { onClick != null }) {
        clickableNoRipple { onClick?.invoke() }
    }

    Crossfade(
        label = "Show Clock 24 CrossFade",
        targetState = clock24State.showClock24,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
    ) { showClock24 ->
        Column(
            horizontalAlignment = BiasAlignment.Horizontal(bias = horizontalBias),
            modifier = Modifier
                .fillMaxWidth()
                .testSemantics(tag = TestTags.TAG_CLOCK_COLUMN) {
                    testBiasAlignment(biasAlignment = BiasAlignment.Horizontal(bias = horizontalBias))
                }
        ) {
            if (showClock24) {
                Clock24(
                    modifier = clickModifier,
                    currentTime = clock24State.currentTime,
                    handleColor = contentColor,
                    offsetAnimationSpec = tween(durationMillis = clock24State.clock24AnimationDuration),
                    colorAnimationSpec = tween(durationMillis = clock24State.clock24AnimationDuration)
                )
            } else {
                CurrentTime(
                    currentTime = clock24State.currentTime,
                    contentColor = contentColor,
                    modifier = Modifier
                        .then(other = clickModifier)
                        .padding(vertical = verticalPadding)
                )
            }
        }
    }
}
