package com.morsify.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morsify.R
import com.morsify.data.MorseCode
import com.morsify.data.OutputMode
import com.morsify.data.TimingProfile
import com.morsify.service.Transmitter
import com.morsify.ui.MorsifyViewModel
import com.morsify.ui.theme.MFHairline
import com.morsify.ui.theme.MFHairlineStrong
import com.morsify.ui.theme.MFMorseStyle
import com.morsify.ui.theme.MFMuted
import com.morsify.ui.theme.MFOnPrimary
import com.morsify.ui.theme.MFPrimary
import com.morsify.ui.theme.MFSlate
import com.morsify.ui.theme.MFSurface
import com.morsify.ui.theme.MFSurfaceDark
import com.morsify.ui.theme.MFTintLavender
import com.morsify.ui.theme.MFError

@Composable
fun DashboardScreen(
    state: MorsifyViewModel.State,
    onTextChange: (String) -> Unit,
    onSpeedChange: (Int) -> Unit,
    onModeChange: (OutputMode) -> Unit,
    onTransmit: () -> Unit,
    onStop: () -> Unit,
    onToggleAuto: () -> Unit,
    onToggleLanguage: () -> Unit,
    onRequestCamera: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val isTransmitting = state.progress != null
    val isOn = state.progress?.isOn == true
    val scroll = rememberScrollState()
    val view = androidx.compose.ui.platform.LocalView.current

    LaunchedEffect(isTransmitting) {
        view.keepScreenOn = isTransmitting
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            TopBar(onOpenSettings = onOpenSettings)

            Spacer(Modifier.height(20.dp))

            InputCard(
                text = state.text,
                onTextChange = onTextChange,
                morseReadable = state.encoded.readable,
                encodedTokens = state.encoded.tokens,
                isTransmitting = isTransmitting,
                currentCharIndex = state.progress?.charIndex ?: -1,
                isOn = isOn
            )

            Spacer(Modifier.height(20.dp))

            SpeedCard(
                speedProgress = state.speedProgress,
                timing = state.timing,
                onSpeedChange = onSpeedChange
            )

            Spacer(Modifier.height(20.dp))

            OutputModeCard(
                current = state.outputMode,
                hasFlash = state.hasFlash,
                onModeChange = onModeChange,
                onRequestCamera = onRequestCamera
            )

            Spacer(Modifier.height(20.dp))

            // Auto Loop Card
            AutoLoopCard(
                isAuto = state.isAuto,
                onToggleAuto = onToggleAuto
            )

            Spacer(Modifier.height(20.dp))

            TransmitButton(
                isTransmitting = isTransmitting,
                outputMode = state.outputMode,
                hasFlash = state.hasFlash,
                hasMessage = state.text.isNotBlank(),
                onTransmit = onTransmit,
                onStop = onStop
            )

            if (isTransmitting) {
                Spacer(Modifier.height(16.dp))
                ProgressCard(
                    progress = state.progress!!,
                    timing = state.timing
                )
            }

            Spacer(Modifier.height(24.dp))

            LearnMorseSection()

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.labelMedium,
                color = MFMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TopBar(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.dashboard_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.settings_cd),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun InputCard(
    text: String,
    onTextChange: (String) -> Unit,
    morseReadable: String,
    encodedTokens: List<MorseCode.SymbolSequence>,
    isTransmitting: Boolean,
    currentCharIndex: Int,
    isOn: Boolean
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.input_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (text.isNotEmpty()) {
                    IconButton(
                        onClick = { onTextChange("") },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.action_clear),
                            tint = MFMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.input_hint),
                        color = MFMuted,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MFPrimary,
                    unfocusedIndicatorColor = MFHairline,
                    cursorColor = MFPrimary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )

            Text(
                text = stringResource(R.string.char_count, text.length),
                style = MaterialTheme.typography.labelSmall,
                color = MFMuted,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(Modifier.height(20.dp))
            HorizontalHairline()
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.morse_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (morseReadable.isNotBlank() && morseReadable != stringResource(R.string.morse_placeholder)) {
                        IconButton(
                            onClick = {
                                val clip = ClipData.newPlainText("morse", morseReadable)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.morse_copied),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = stringResource(R.string.cd_copy_morse),
                                tint = MFPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                PulseDot(isOn = isOn && isTransmitting)
            }

            Spacer(Modifier.height(8.dp))

            if (morseReadable.isBlank()) {
                Text(
                    text = stringResource(R.string.morse_placeholder),
                    style = MFMorseStyle.copy(color = MFMuted)
                )
            } else {
                MorseReadable(
                    readable = morseReadable,
                    tokens = encodedTokens,
                    isTransmitting = isTransmitting,
                    currentCharIndex = currentCharIndex
                )
            }
        }
    }
}

@Composable
private fun HorizontalHairline() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MFHairline)
    )
}

@Composable
private fun PulseDot(isOn: Boolean) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(if (isOn) MFPrimary else MFMuted)
    )
}

@Composable
private fun MorseReadable(
    readable: String,
    tokens: List<MorseCode.SymbolSequence>,
    isTransmitting: Boolean,
    currentCharIndex: Int
) {
    val words = readable.split(" / ")
    Column(modifier = Modifier.heightIn(max = 150.dp)) {
        var tokenArrayIndex = 0
        words.forEachIndexed { wordIdx, word ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                val charTokens = word.split(' ').filter { it.isNotEmpty() }
                charTokens.forEachIndexed { tokenIdx, token ->
                    val highlighted = isTransmitting && tokenArrayIndex == currentCharIndex
                    Text(
                        text = token,
                        style = MFMorseStyle.copy(
                            color = if (highlighted) MFPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                    if (tokenIdx < charTokens.size - 1) {
                        Spacer(Modifier.width(10.dp))
                    }
                    tokenArrayIndex++
                }
            }
            if (wordIdx < words.size - 1) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "/",
                    style = MFMorseStyle.copy(color = MFMuted),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                tokenArrayIndex++
            }
        }
    }
}

@Composable
private fun SpeedCard(
    speedProgress: Int,
    timing: TimingProfile,
    onSpeedChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.speed_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${timing.unitMs} ms",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(8.dp))
            Slider(
                value = speedProgress.toFloat(),
                onValueChange = { onSpeedChange(it.toInt()) },
                valueRange = 0f..100f,
                steps = 0,
                colors = SliderDefaults.colors(
                    thumbColor = MFPrimary,
                    activeTrackColor = MFPrimary,
                    inactiveTrackColor = MFHairline
                )
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.speed_slow),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.speed_normal),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (speedProgress in 55..80) MFPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.speed_fast),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OutputModeCard(
    current: OutputMode,
    hasFlash: Boolean,
    onModeChange: (OutputMode) -> Unit,
    onRequestCamera: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.output_label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeChip(
                    label = stringResource(R.string.output_flash),
                    selected = current == OutputMode.FLASH_ONLY,
                    onClick = { if (hasFlash) onModeChange(OutputMode.FLASH_ONLY) },
                    enabled = hasFlash,
                    modifier = Modifier.weight(1f)
                )
                ModeChip(
                    label = stringResource(R.string.output_sound),
                    selected = current == OutputMode.SOUND_ONLY,
                    onClick = { onModeChange(OutputMode.SOUND_ONLY) },
                    enabled = true,
                    modifier = Modifier.weight(1f)
                )
                ModeChip(
                    label = stringResource(R.string.output_both),
                    selected = current == OutputMode.BOTH,
                    onClick = { if (hasFlash) onModeChange(OutputMode.BOTH) },
                    enabled = hasFlash,
                    modifier = Modifier.weight(1f)
                )
            }
            if (!hasFlash) {
                Spacer(Modifier.height(12.dp))
                NoFlashWarning(onRequestCamera = onRequestCamera)
            }
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = when {
        !enabled -> MFSurface
        selected -> MFPrimary
        else -> Color.Transparent
    }
    val fg = when {
        !enabled -> MFMuted
        selected -> MFOnPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val border = if (selected || !enabled) Color.Transparent else MFHairlineStrong
    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
            .background(bg)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NoFlashWarning(onRequestCamera: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MFTintLavender
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.perm_camera_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.perm_camera_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRequestCamera,
                colors = ButtonDefaults.buttonColors(containerColor = MFPrimary)
            ) {
                Text(stringResource(R.string.perm_camera_grant))
            }
        }
    }
}

@Composable
private fun AutoLoopCard(
    isAuto: Boolean,
    onToggleAuto: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleAuto() }
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.output_auto),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.output_auto_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isAuto,
                onCheckedChange = { onToggleAuto() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MFOnPrimary,
                    checkedTrackColor = MFPrimary,
                    uncheckedThumbColor = MFMuted,
                    uncheckedTrackColor = MFHairline
                )
            )
        }
    }
}

@Composable
private fun TransmitButton(
    isTransmitting: Boolean,
    outputMode: OutputMode,
    hasFlash: Boolean,
    hasMessage: Boolean,
    onTransmit: () -> Unit,
    onStop: () -> Unit
) {
    val canFlash = outputMode.usesFlash() && hasFlash
    val canSound = outputMode.usesSound()
    val enabled = if (isTransmitting) true else hasMessage && (canFlash || canSound)

    Button(
        onClick = { if (isTransmitting) onStop() else onTransmit() },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isTransmitting) MFError else MFPrimary,
            disabledContainerColor = MFHairline
        )
    ) {
        Icon(
            imageVector = if (isTransmitting) Icons.Filled.Stop else Icons.Filled.PlayArrow,
            contentDescription = stringResource(
                if (isTransmitting) R.string.cd_stop else R.string.cd_transmit
            ),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(
                if (isTransmitting) R.string.action_stop else R.string.action_transmit
            ),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun ProgressCard(
    progress: Transmitter.Progress,
    timing: TimingProfile
) {
    val fraction = if (progress.total == 0) 0f else progress.played.toFloat() / progress.total
    val stateLabel = when (progress.lastSymbol) {
        Transmitter.SymKind.DOT -> stringResource(R.string.symbol_dot)
        Transmitter.SymKind.DASH -> stringResource(R.string.symbol_dash)
        Transmitter.SymKind.WORD_GAP -> stringResource(R.string.symbol_word_gap)
        else -> stringResource(R.string.symbol_none)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.state_transmitting).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stateLabel,
                    style = MFMorseStyle.copy(color = MFPrimary)
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MFPrimary,
                trackColor = MFHairline
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.progress_format, progress.charIndex + 1, progress.charTotal),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LearnMorseSection() {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.learn_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.learn_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.learn_pattern),
                style = MaterialTheme.typography.bodySmall,
                color = MFSlate
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.hint_sos),
                style = MaterialTheme.typography.bodySmall,
                color = MFSlate
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.hint_reference),
                style = MaterialTheme.typography.bodySmall,
                color = MFMuted
            )

            Spacer(Modifier.height(16.dp))
            HorizontalHairline()
            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.learn_tree_title).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            val scrollStateTree = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) MFSurfaceDark else MFSurface)
                    .horizontalScroll(scrollStateTree)
                    .padding(12.dp)
            ) {
                Text(
                    text = """
             [ START ]
            /         \
         E (dit)    T (dah)
        /     \    /     \
       I       A  N       M
      / \     / \ / \    / \
     S   U   R  W D  K  G   O
    / \  /   /  / / \ / /   / \
   H  V  F   L  P J B X Z   9 0
  /  /  /      /     / /   /
 5  4  3      2     1 6 7 8
                    """.trimIndent(),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
