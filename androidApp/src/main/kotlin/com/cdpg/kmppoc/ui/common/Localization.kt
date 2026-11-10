package com.cdpg.kmppoc.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.cdpg.dhruva.core.strings.Localizer
import com.cdpg.dhruva.core.strings.StringKey
import com.cdpg.dhruva.core.strings.UiText

/*
 * All text comes from :shared. The root re-composes everything when the language changes
 * (see DhruvaApp), so these helpers can read the current language directly.
 */

val LocalLocalizer = staticCompositionLocalOf<Localizer> { error("Localizer not provided") }

@Composable
@ReadOnlyComposable
fun text(key: StringKey): String = LocalLocalizer.current.text(key)

@Composable
@ReadOnlyComposable
fun text(key: StringKey, vararg args: Any): String = LocalLocalizer.current.format(key, args.toList())

@Composable
@ReadOnlyComposable
fun UiText.asString(): String = LocalLocalizer.current.resolve(this)
