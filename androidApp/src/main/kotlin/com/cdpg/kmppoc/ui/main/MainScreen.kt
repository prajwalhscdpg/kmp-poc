package com.cdpg.kmppoc.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cdpg.dhruva.core.strings.AppLanguage
import com.cdpg.dhruva.core.strings.StringKey
import com.cdpg.dhruva.di.AppGraph
import com.cdpg.kmppoc.ui.addresses.MyAddressesScreen
import com.cdpg.kmppoc.ui.common.SecondaryButton
import com.cdpg.kmppoc.ui.common.text
import com.cdpg.kmppoc.ui.theme.DhruvaColors
import kotlinx.coroutines.launch

private enum class Tab(val label: StringKey, val glyph: String) {
    Home(StringKey.TAB_HOME, "⌂"),
    MyAddresses(StringKey.TAB_MY_ADDRESSES, "◎"),
    Sharing(StringKey.TAB_SHARING, "⇄"),
    Profile(StringKey.TAB_PROFILE, "☺"),
}

/** The tab shell (Home, My DAs, Sharing, Profile) from both apps. */
@Composable
fun MainScreen(graph: AppGraph) {
    var selected by rememberSaveable { mutableStateOf(Tab.MyAddresses) }

    Scaffold(
        containerColor = DhruvaColors.Background,
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == selected,
                        onClick = { selected = tab },
                        icon = { Text(tab.glyph) },
                        label = { Text(text(tab.label)) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selected) {
                Tab.MyAddresses -> MyAddressesScreen(graph)
                Tab.Profile -> ProfileTab(graph)
                Tab.Home, Tab.Sharing -> ComingSoon(text(selected.label))
            }
        }
    }
}

@Composable
private fun ComingSoon(title: String) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(text(StringKey.PLACEHOLDER_COMING_SOON), color = DhruvaColors.Caption, textAlign = TextAlign.Center)
    }
}

/** Language switch (proves shared strings at runtime) and sign out (proves session wipe). */
@Composable
private fun ProfileTab(graph: AppGraph) {
    val language by graph.preferences.language.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text(StringKey.TAB_PROFILE), style = MaterialTheme.typography.headlineMedium)
        Text(text(StringKey.PROFILE_APP_LANGUAGE), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(AppLanguage.English to "English", AppLanguage.Hindi to "हिन्दी").forEach { (option, name) ->
                FilterChip(
                    selected = language == option,
                    onClick = { scope.launch { graph.preferences.setLanguage(option) } },
                    label = { Text(name) },
                )
            }
        }
        SecondaryButton(
            text(StringKey.SIGN_OUT),
            onClick = { scope.launch { graph.session.signOut() } },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
