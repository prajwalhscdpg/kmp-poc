package com.cdpg.kmppoc.ui.addresses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cdpg.dhruva.core.strings.StringKey
import com.cdpg.dhruva.di.AppGraph
import com.cdpg.dhruva.domain.address.AddressType
import com.cdpg.dhruva.domain.address.DigitalAddress
import com.cdpg.kmppoc.ui.common.LocalLocalizer
import com.cdpg.kmppoc.ui.common.PrimaryButton
import com.cdpg.kmppoc.ui.common.asString
import com.cdpg.kmppoc.ui.common.rememberSharedViewModel
import com.cdpg.kmppoc.ui.common.text
import com.cdpg.kmppoc.ui.theme.DhruvaColors
import kotlinx.coroutines.launch

/** My Digital Addresses: offline-first list, search, pull to refresh, offline banner. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAddressesScreen(graph: AppGraph) {
    val viewModel = rememberSharedViewModel { graph.makeMyAddressesViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val localizer = LocalLocalizer.current

    LaunchedEffect(viewModel) { viewModel.onAppear() }

    Scaffold(
        containerColor = DhruvaColors.Background,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreate = true },
                containerColor = DhruvaColors.Primary,
                contentColor = Color.White,
            ) { Text("+  " + text(StringKey.MyDaCreate)) }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text(
                text(StringKey.MyDaTitle),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
            )
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text(text(StringKey.MyDaSearchPlaceholder)) },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
            if (state.canSimulateOffline) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text(StringKey.DevSimulateOffline), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Switch(checked = state.isSimulatingOffline, onCheckedChange = viewModel::onSimulateOfflineChange)
                }
            }
            if (state.isOffline) {
                Text(
                    text(StringKey.MyDaOfflineBanner),
                    color = DhruvaColors.Warning,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(DhruvaColors.WarningContainer, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                )
            }

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                val error = state.error
                val noResults = state.noResults
                when {
                    state.isLoading -> CenteredBox { CircularProgressIndicator(color = DhruvaColors.Primary) }
                    error != null -> CenteredBox {
                        Text(error.asString(), textAlign = TextAlign.Center)
                        PrimaryButton(text(StringKey.Retry), onClick = viewModel::onRetry)
                    }
                    state.isEmpty -> CenteredBox {
                        Text(text(StringKey.MyDaEmptyTitle), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                        Text(text(StringKey.MyDaEmptyDescription), color = DhruvaColors.Caption, textAlign = TextAlign.Center)
                    }
                    noResults != null -> CenteredBox { Text(noResults.asString(), color = DhruvaColors.Caption) }
                    else -> LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.addresses, key = { it.id }) { address -> AddressRow(address) }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateAddressSheet(
            graph = graph,
            onDismiss = { showCreate = false },
            onFinished = { message ->
                showCreate = false
                if (message != null) scope.launch { snackbar.showSnackbar(localizer.resolve(message)) }
            },
        )
    }
}

@Composable
private fun AddressRow(address: DigitalAddress) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).background(DhruvaColors.PrimaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (address.type == AddressType.Home) "⌂" else "◎", color = DhruvaColors.Primary)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(address.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(address.handle, style = MaterialTheme.typography.bodyMedium, color = DhruvaColors.Caption)
                Text(text(address.type.label), style = MaterialTheme.typography.labelSmall, color = DhruvaColors.Caption)
                if (address.isPendingSync) {
                    Text(
                        "⟳ " + text(StringKey.MyDaPendingSync),
                        style = MaterialTheme.typography.labelSmall,
                        color = DhruvaColors.Warning,
                    )
                }
            }
            Text(text(StringKey.MyDaView), color = DhruvaColors.Primary)
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    // Scrollable so pull-to-refresh also works on the empty and error states.
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(32.dp)) {
        item {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) { content() }
        }
    }
}
