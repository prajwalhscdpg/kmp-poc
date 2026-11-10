package com.cdpg.kmppoc.ui.common

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cdpg.dhruva.core.common.SharedViewModel

/** Keeps a shared ViewModel alive across configuration changes and clears it with the screen. */
class SharedViewModelHolder<VM : SharedViewModel>(val viewModel: VM) : ViewModel() {
    override fun onCleared() {
        viewModel.clear()
    }
}

/**
 * The Android side of a shared ViewModel: scoped to the current navigation entry like any
 * Jetpack ViewModel, created by the shared AppGraph factory.
 */
@Composable
inline fun <reified VM : SharedViewModel> rememberSharedViewModel(
    key: String = "",
    crossinline factory: () -> VM,
): VM {
    val holder = viewModel<SharedViewModelHolder<VM>>(
        key = "${VM::class.qualifiedName}:$key",
        factory = viewModelFactory { initializer { SharedViewModelHolder(factory()) } },
    )
    return holder.viewModel
}
