package dev.mslalith.focuslauncher.core.ui.effects

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SystemBroadcastReceiver(
    systemAction: String,
    configure: IntentFilter.() -> Unit = {},
    onSystemEvent: (intent: Intent?) -> Unit
) {
    val context = LocalContext.current
    val currentOnSystemEvent by rememberUpdatedState(newValue = onSystemEvent)

    DisposableEffect(key1 = context, key2 = systemAction) {
        val intentFilter = IntentFilter(systemAction)
        intentFilter.configure()

        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action ?: return
                if (action == systemAction) currentOnSystemEvent(intent)
            }
        }

        context.registerReceiver(broadcast, intentFilter)
        onDispose { context.unregisterReceiver(broadcast) }
    }
}

@Composable
fun SystemBroadcastReceiver(
    systemActions: ImmutableList<String>,
    configure: IntentFilter.() -> Unit = {},
    onSystemEvent: (intent: Intent?) -> Unit
) {
    val context = LocalContext.current
    val currentOnSystemEvent by rememberUpdatedState(newValue = onSystemEvent)

    DisposableEffect(key1 = context, key2 = systemActions) {
        val intentFilter = IntentFilter().apply {
            systemActions.forEach { addAction(it) }
        }
        intentFilter.configure()

        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action ?: return
                if (systemActions.contains(action)) currentOnSystemEvent(intent)
            }
        }

        context.registerReceiver(broadcast, intentFilter)
        onDispose { context.unregisterReceiver(broadcast) }
    }
}
