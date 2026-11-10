package com.cdpg.kmppoc.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cdpg.dhruva.core.strings.StringKey
import com.cdpg.dhruva.di.AppGraph
import com.cdpg.dhruva.presentation.auth.FullNameEvent
import com.cdpg.dhruva.presentation.auth.LoginEvent
import com.cdpg.dhruva.presentation.auth.OtpEvent
import com.cdpg.dhruva.presentation.splash.SplashDestination
import com.cdpg.kmppoc.ui.common.BrandMark
import com.cdpg.kmppoc.ui.common.InitiativeFooter
import com.cdpg.kmppoc.ui.common.PrimaryButton
import com.cdpg.kmppoc.ui.common.SecondaryButton
import com.cdpg.kmppoc.ui.common.asString
import com.cdpg.kmppoc.ui.common.rememberSharedViewModel
import com.cdpg.kmppoc.ui.common.text
import com.cdpg.kmppoc.ui.theme.DhruvaColors

/*
 * Native Compose screens for the auth flow. No rules, timers or strings live here: they come
 * from the shared ViewModels in :shared (presentation/auth).
 */

@Composable
private fun AuthBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DhruvaColors.BackgroundTop, DhruvaColors.Background)))
            .safeDrawingPadding()
            .imePadding(),
    ) { content() }
}

@Composable
fun SplashScreen(graph: AppGraph, onSignedOut: () -> Unit, onSignedIn: () -> Unit) {
    val viewModel = rememberSharedViewModel { graph.makeSplashViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.destination) {
        when (state.destination) {
            SplashDestination.Intro -> onSignedOut()
            SplashDestination.Main -> onSignedIn()
            null -> Unit
        }
    }

    AuthBackground {
        BrandMark(Modifier.align(Alignment.Center))
        InitiativeFooter(Modifier.align(Alignment.BottomCenter).padding(24.dp))
    }
}

@Composable
fun IntroScreen(onContinue: () -> Unit) {
    AuthBackground {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.weight(1f))
            BrandMark()
            Text(text(StringKey.INTRO_HEADLINE), style = MaterialTheme.typography.headlineLarge, color = DhruvaColors.OnSurface)
            Text(text(StringKey.INTRO_SUPPORTING_TEXT), style = MaterialTheme.typography.bodyLarge, color = DhruvaColors.Caption)
            Spacer(Modifier.weight(1f))
            PrimaryButton(text(StringKey.INTRO_GET_STARTED), onClick = onContinue)
            TextButton(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text(text(StringKey.INTRO_RETURNING_USER), color = DhruvaColors.Primary)
            }
        }
    }
}

@Composable
fun LoginScreen(graph: AppGraph, onOtpSent: (String) -> Unit) {
    val viewModel = rememberSharedViewModel { graph.makeLoginViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.OtpSent -> onOtpSent(event.phoneNumber)
            }
        }
    }

    AuthBackground {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(Modifier.height(48.dp))
            BrandMark()
            Text(text(StringKey.LOGIN_TAGLINE), style = MaterialTheme.typography.titleMedium, color = DhruvaColors.Caption)
            AuthCard {
                Text(text(StringKey.LOGIN_TITLE), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = state.phoneNumber,
                    onValueChange = viewModel::onPhoneNumberChange,
                    prefix = { Text(text(StringKey.LOGIN_COUNTRY_CODE) + "  ") },
                    placeholder = { Text(text(StringKey.LOGIN_PHONE_PLACEHOLDER)) },
                    isError = state.error != null,
                    supportingText = state.error?.let { { Text(it.asString()) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.onContinue() }),
                    modifier = Modifier.fillMaxWidth(),
                )
                PrimaryButton(
                    text(StringKey.CONTINUE_ACTION),
                    onClick = viewModel::onContinue,
                    enabled = state.isContinueEnabled,
                    isLoading = state.isSendingOtp,
                )
            }
        }
    }
}

@Composable
fun OtpScreen(
    graph: AppGraph,
    phoneNumber: String,
    onBack: () -> Unit,
    onNewUser: () -> Unit,
    onReturningUser: () -> Unit,
) {
    val viewModel = rememberSharedViewModel(key = phoneNumber) { graph.makeOtpViewModel(phoneNumber) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is OtpEvent.Verified -> if (event.isNewUser) onNewUser() else onReturningUser()
            }
        }
    }

    AuthBackground {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TextButton(onClick = onBack) { Text("‹ " + text(StringKey.BACK), color = DhruvaColors.Primary) }
            AuthCard {
                Text(text(StringKey.OTP_TITLE), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(state.codeSentText.asString(), style = MaterialTheme.typography.bodyMedium, color = DhruvaColors.Caption)
                OutlinedTextField(
                    value = state.otp,
                    onValueChange = viewModel::onOtpChange,
                    isError = state.error != null,
                    supportingText = state.error?.let { { Text(it.asString()) } },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center, letterSpacing = MaterialTheme.typography.headlineSmall.fontSize * 0.4f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.onVerify() }),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text(StringKey.OTP_NOT_RECEIVED), style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = viewModel::onResend, enabled = state.canResend) {
                        Text(state.resendText.asString())
                    }
                }
                PrimaryButton(
                    text(StringKey.CONTINUE_ACTION),
                    onClick = viewModel::onVerify,
                    enabled = state.isVerifyEnabled,
                    isLoading = state.isVerifying,
                )
            }
        }
    }
}

@Composable
fun FullNameScreen(graph: AppGraph, onSubmitted: (firstName: String) -> Unit) {
    val viewModel = rememberSharedViewModel { graph.makeFullNameViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is FullNameEvent.Submitted -> onSubmitted(event.firstName)
            }
        }
    }

    AuthBackground {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(Modifier.height(48.dp))
            Text(text(StringKey.FULL_NAME_WELCOME), style = MaterialTheme.typography.titleMedium, color = DhruvaColors.Caption)
            AuthCard {
                Text(text(StringKey.FULL_NAME_TITLE), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = state.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    placeholder = { Text(text(StringKey.FULL_NAME_PLACEHOLDER)) },
                    isError = state.error != null,
                    supportingText = state.error?.let { { Text(it.asString()) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { viewModel.onContinue() }),
                    modifier = Modifier.fillMaxWidth(),
                )
                PrimaryButton(
                    text(StringKey.CONTINUE_ACTION),
                    onClick = viewModel::onContinue,
                    enabled = state.isContinueEnabled,
                    isLoading = state.isSaving,
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(firstName: String, onFinished: () -> Unit) {
    AuthBackground {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.weight(1f))
            Text(text(StringKey.WELCOME_USER_GREETING, firstName), style = MaterialTheme.typography.headlineMedium, color = DhruvaColors.Primary)
            Text(text(StringKey.WELCOME_USER_CREATE_ADDRESS), style = MaterialTheme.typography.headlineSmall, color = DhruvaColors.OnSurface)
            Spacer(Modifier.weight(1f))
            // Both go to the dashboard for now (same as Dhruva iOS) until Create DA can start from here.
            PrimaryButton(text(StringKey.WELCOME_USER_GET_STARTED), onClick = onFinished)
            SecondaryButton(text(StringKey.WELCOME_USER_SKIP), onClick = onFinished)
        }
    }
}

@Composable
private fun AuthCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { content() }
    }
}
