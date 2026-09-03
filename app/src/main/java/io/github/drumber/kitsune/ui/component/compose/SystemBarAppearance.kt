package io.github.drumber.kitsune.ui.component.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun StatusBarIconAppearance(
    useDarkIcons: Boolean,
    defaultUseDarkIcons: Boolean
) {
    val view = LocalView.current
    val activity = view.context.findActivity() ?: return
    val controller = WindowCompat.getInsetsController(activity.window, view)
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentUseDarkIcons by rememberUpdatedState(useDarkIcons)

    SideEffect {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            controller.isAppearanceLightStatusBars = useDarkIcons
        }
    }
    DisposableEffect(lifecycleOwner, controller, defaultUseDarkIcons) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    controller.isAppearanceLightStatusBars = currentUseDarkIcons
                }

                Lifecycle.Event.ON_PAUSE -> {
                    controller.isAppearanceLightStatusBars = defaultUseDarkIcons
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.isAppearanceLightStatusBars = defaultUseDarkIcons
        }
    }
}

@Composable
fun NavigationBarIconAppearance(
    useDarkIcons: Boolean,
    defaultUseDarkIcons: Boolean
) {
    val view = LocalView.current
    val activity = view.context.findActivity() ?: return
    val controller = WindowCompat.getInsetsController(activity.window, view)
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentUseDarkIcons by rememberUpdatedState(useDarkIcons)

    SideEffect {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            controller.isAppearanceLightNavigationBars = useDarkIcons
        }
    }
    DisposableEffect(lifecycleOwner, controller, defaultUseDarkIcons) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    controller.isAppearanceLightNavigationBars = currentUseDarkIcons
                }

                Lifecycle.Event.ON_PAUSE -> {
                    controller.isAppearanceLightNavigationBars = defaultUseDarkIcons
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.isAppearanceLightNavigationBars = defaultUseDarkIcons
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
