package com.cdpg.kmppoc.ui.addresses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cdpg.dhruva.core.strings.StringKey
import com.cdpg.dhruva.core.strings.UiText
import com.cdpg.dhruva.di.AppGraph
import com.cdpg.dhruva.presentation.createaddress.CreateAddressEvent
import com.cdpg.kmppoc.sync.OutboxWorker
import com.cdpg.kmppoc.ui.common.PrimaryButton
import com.cdpg.kmppoc.ui.common.asString
import com.cdpg.kmppoc.ui.common.text
import com.cdpg.kmppoc.ui.theme.DhruvaColors

/**
 * POC stand-in for the Create DA wizard: nickname, type and prefix on the default pin.
 * It drives the real shared CreateAddressViewModel (validation, DIGIPIN, offline-capable
 * submit through the outbox); the map steps are the next slice.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAddressSheet(graph: AppGraph, onDismiss: () -> Unit, onFinished: (UiText?) -> Unit) {
    // A fresh ViewModel each time the sheet opens, cleared when it closes.
    val viewModel = remember { graph.makeCreateAddressViewModel() }
    DisposableEffect(viewModel) { onDispose { viewModel.clear() } }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                // The quick form skips the map and details steps.
                CreateAddressEvent.GoToConfirm -> viewModel.onConfirm()
                is CreateAddressEvent.Finished -> {
                    // Queued offline: let WorkManager deliver it once the network is back.
                    if (event.message != null) OutboxWorker.enqueueNow(context)
                    onFinished(event.message)
                }
                CreateAddressEvent.GoToDetails, CreateAddressEvent.GoToNickname -> Unit
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text(StringKey.CreateDaHeadline), style = MaterialTheme.typography.titleLarge)
            Text(text(StringKey.CreateDaPocNote), style = MaterialTheme.typography.bodySmall, color = DhruvaColors.Caption)

            Text(
                text(StringKey.CreateDaDigipinLabel) + ": " + state.digipinDisplay.asString(),
                style = MaterialTheme.typography.titleSmall,
                color = DhruvaColors.Primary,
            )

            Text(text(StringKey.CreateDaTypeLabel), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.typeOptions.forEach { type ->
                    FilterChip(
                        selected = state.type == type,
                        onClick = { viewModel.onTypeSelected(type) },
                        label = { Text(text(type.label)) },
                    )
                }
            }

            OutlinedTextField(
                value = state.nickname,
                onValueChange = viewModel::onNicknameChange,
                label = { Text(text(StringKey.CreateDaNicknameLabel)) },
                isError = state.nicknameError != null,
                supportingText = state.nicknameError?.let { { Text(it.asString()) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.prefix,
                onValueChange = viewModel::onPrefixChange,
                label = { Text(text(StringKey.CreateDaPrefixLabel)) },
                suffix = { Text(state.suffix) },
                isError = state.prefixError != null,
                supportingText = state.prefixError?.let { { Text(it.asString()) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(state.previewHandle.asString(), style = MaterialTheme.typography.bodyMedium, color = DhruvaColors.Caption)

            state.submitError?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }

            PrimaryButton(
                text(StringKey.CreateDaSave),
                onClick = viewModel::onNicknameProceed,
                isLoading = state.isSubmitting,
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}
