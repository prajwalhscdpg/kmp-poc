import SwiftUI
import Shared

// Native SwiftUI screens for the auth flow. Rules, timers and text come from the shared
// ViewModels (shared/presentation/auth); these views only draw state and forward actions.

struct SplashScreen: View {
    let graph: AppGraph
    let onFinished: (SplashDestination) -> Void

    var body: some View {
        WithViewModel(make: { graph.makeSplashViewModel() }, state: { $0.stateForSwift }) { _, state in
            ZStack {
                DhruvaColor.gradient.ignoresSafeArea()
                BrandMark()
                VStack {
                    Spacer()
                    InitiativeFooter().padding(24)
                }
            }
            .onChange(of: state.destination) { _, destination in
                if let destination { onFinished(destination) }
            }
        }
    }
}

struct IntroScreen: View {
    let onContinue: () -> Void

    var body: some View {
        ZStack {
            DhruvaColor.gradient.ignoresSafeArea()
            VStack(alignment: .leading, spacing: 16) {
                Spacer()
                BrandMark()
                Text(L10n.text(.introHeadline)).font(.largeTitle).foregroundStyle(DhruvaColor.onSurface)
                Text(L10n.text(.introSupportingText)).foregroundStyle(DhruvaColor.caption)
                Spacer()
                PrimaryButton(title: L10n.text(.introGetStarted), action: onContinue)
                Button(L10n.text(.introReturningUser), action: onContinue)
                    .frame(maxWidth: .infinity)
                    .foregroundStyle(DhruvaColor.primary)
            }
            .padding(24)
        }
    }
}

struct LoginScreen: View {
    let graph: AppGraph
    let onOtpSent: (String) -> Void

    var body: some View {
        WithViewModel(make: { graph.makeLoginViewModel() }, state: { $0.stateForSwift }) { vm, state in
            ZStack {
                DhruvaColor.gradient.ignoresSafeArea()
                ScrollView {
                    VStack(spacing: 24) {
                        BrandMark().padding(.top, 48)
                        Text(L10n.text(.loginTagline)).font(.headline).foregroundStyle(DhruvaColor.caption)
                        AuthCard {
                            Text(L10n.text(.loginTitle)).font(.title2.weight(.semibold))
                            HStack(spacing: 8) {
                                Text(L10n.text(.loginCountryCode)).foregroundStyle(DhruvaColor.onSurface)
                                SharedTextField(
                                    placeholder: L10n.text(.loginPhonePlaceholder),
                                    value: state.phoneNumber,
                                    read: { vm.stateForSwift.value.phoneNumber },
                                    write: { vm.onPhoneNumberChange(input: $0) },
                                    keyboard: .numberPad,
                                    contentType: .telephoneNumber,
                                    hasError: state.error != nil,
                                    onSubmit: { vm.onContinue() }
                                )
                            }
                            ErrorLine(text: state.error)
                            PrimaryButton(
                                title: L10n.text(.continueAction),
                                isEnabled: state.isContinueEnabled,
                                isLoading: state.isSendingOtp
                            ) { vm.onContinue() }
                        }
                    }
                    .padding(24)
                }
            }
            .onKmpEvent(vm.eventsForSwift) { event in
                if let sent = event as? LoginEventOtpSent { onOtpSent(sent.phoneNumber) }
            }
        }
    }
}

struct OtpScreen: View {
    let graph: AppGraph
    let phoneNumber: String
    let onVerified: (_ isNewUser: Bool) -> Void

    var body: some View {
        WithViewModel(make: { graph.makeOtpViewModel(phoneNumber: phoneNumber) }, state: { $0.stateForSwift }) { vm, state in
            ZStack {
                DhruvaColor.gradient.ignoresSafeArea()
                ScrollView {
                    AuthCard {
                        Text(L10n.text(.otpTitle)).font(.title2.weight(.semibold))
                        Text(L10n.resolve(state.codeSentText)).foregroundStyle(DhruvaColor.caption)
                        SharedTextField(
                            placeholder: "••••••",
                            value: state.otp,
                            read: { vm.stateForSwift.value.otp },
                            write: { vm.onOtpChange(input: $0) },
                            keyboard: .numberPad,
                            // iOS offers the code from the SMS above the keyboard.
                            contentType: .oneTimeCode,
                            hasError: state.error != nil,
                            onSubmit: { vm.onVerify() }
                        )
                        .font(.title2.monospacedDigit())
                        ErrorLine(text: state.error)
                        HStack {
                            Text(L10n.text(.otpNotReceived))
                            Button(L10n.resolve(state.resendText)) { vm.onResend() }
                                .disabled(!state.canResend)
                                .foregroundStyle(state.canResend ? DhruvaColor.primary : DhruvaColor.caption)
                        }
                        .font(.subheadline)
                        PrimaryButton(
                            title: L10n.text(.continueAction),
                            isEnabled: state.isVerifyEnabled,
                            isLoading: state.isVerifying
                        ) { vm.onVerify() }
                    }
                    .padding(24)
                }
            }
            .onKmpEvent(vm.eventsForSwift) { event in
                if let verified = event as? OtpEventVerified { onVerified(verified.isNewUser) }
            }
        }
    }
}

struct FullNameScreen: View {
    let graph: AppGraph
    let onSubmitted: (_ firstName: String) -> Void

    var body: some View {
        WithViewModel(make: { graph.makeFullNameViewModel() }, state: { $0.stateForSwift }) { vm, state in
            ZStack {
                DhruvaColor.gradient.ignoresSafeArea()
                ScrollView {
                    VStack(alignment: .leading, spacing: 24) {
                        Text(L10n.text(.fullNameWelcome)).font(.headline).foregroundStyle(DhruvaColor.caption)
                            .padding(.top, 48)
                        AuthCard {
                            Text(L10n.text(.fullNameTitle)).font(.title2.weight(.semibold))
                            SharedTextField(
                                placeholder: L10n.text(.fullNamePlaceholder),
                                value: state.fullName,
                                read: { vm.stateForSwift.value.fullName },
                                write: { vm.onFullNameChange(input: $0) },
                                contentType: .name,
                                hasError: state.error != nil,
                                onSubmit: { vm.onContinue() }
                            )
                            ErrorLine(text: state.error)
                            PrimaryButton(
                                title: L10n.text(.continueAction),
                                isEnabled: state.isContinueEnabled,
                                isLoading: state.isSaving
                            ) { vm.onContinue() }
                        }
                    }
                    .padding(24)
                }
            }
            .onKmpEvent(vm.eventsForSwift) { event in
                if let submitted = event as? FullNameEventSubmitted { onSubmitted(submitted.firstName) }
            }
        }
    }
}

struct WelcomeScreen: View {
    let firstName: String
    let onFinished: () -> Void

    var body: some View {
        ZStack {
            DhruvaColor.gradient.ignoresSafeArea()
            VStack(alignment: .leading, spacing: 16) {
                Spacer()
                Text(L10n.format(.welcomeUserGreeting, firstName)).font(.title).foregroundStyle(DhruvaColor.primary)
                Text(L10n.text(.welcomeUserCreateAddress)).font(.title2).foregroundStyle(DhruvaColor.onSurface)
                Spacer()
                // Both go to the dashboard for now (same as Dhruva iOS) until Create DA can start from here.
                PrimaryButton(title: L10n.text(.welcomeUserGetStarted), action: onFinished)
                SecondaryButton(title: L10n.text(.welcomeUserSkip), action: onFinished)
            }
            .padding(24)
        }
    }
}
