package com.cdpg.kmppoc.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cdpg.dhruva.core.strings.StringKey
import com.cdpg.kmppoc.ui.theme.DhruvaColors

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DhruvaColors.Primary),
        modifier = modifier.fillMaxWidth().height(52.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(label, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SecondaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        modifier = modifier.fillMaxWidth().height(52.dp),
    ) {
        Text(label, color = DhruvaColors.Primary, fontWeight = FontWeight.Medium)
    }
}

/** "An initiative by / Department of Posts…" footer from both splash screens. */
@Composable
fun InitiativeFooter(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text(StringKey.SplashInitiativeBy),
            style = MaterialTheme.typography.labelMedium,
            color = DhruvaColors.Caption,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text(StringKey.SplashMinistry),
            style = MaterialTheme.typography.bodySmall,
            color = DhruvaColors.OnSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    Text(
        text(StringKey.AppName),
        modifier = modifier,
        color = DhruvaColors.Primary,
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
    )
}
