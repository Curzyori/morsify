package com.morsify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morsify.ui.MorsifyViewModel
import com.morsify.ui.screen.DashboardScreen
import com.morsify.ui.screen.SettingsDialog
import com.morsify.ui.screen.DonateSheet
import com.morsify.ui.theme.MorsifyTheme
import com.morsify.util.LocaleManager

class MainActivity : ComponentActivity() {

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result handled in screen */ }

    override fun attachBaseContext(newBase: android.content.Context) {
        val lm = LocaleManager(newBase)
        super.attachBaseContext(lm.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lm = LocaleManager(this)
        lm.applyActivity(this)

        setContent {
            val vm: MorsifyViewModel = viewModel()
            MorsifyTheme {
                val state by vm.state.collectAsState()
                var showSettings by remember { mutableStateOf(false) }
                var showDonateSheet by remember { mutableStateOf(false) }

                DashboardScreen(
                    state = state,
                    onTextChange = vm::setText,
                    onSpeedChange = vm::setSpeed,
                    onModeChange = vm::setOutputMode,
                    onTransmit = vm::start,
                    onStop = vm::stop,
                    onToggleAuto = vm::toggleAuto,
                    onToggleLanguage = {
                        vm.toggleLanguage()
                        recreate()
                    },
                    onRequestCamera = {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    onOpenSettings = { showSettings = true }
                )

                if (showSettings) {
                    SettingsDialog(
                        currentLang = vm.localeManager.language,
                        onDismiss = { showSettings = false },
                        onLanguageToggle = { _ ->
                            vm.toggleLanguage()
                            recreate()
                        },
                        onDonateClick = {
                            showSettings = false
                            showDonateSheet = true
                        }
                    )
                }

                if (showDonateSheet) {
                    DonateSheet(
                        evmAddress = state.evmAddress,
                        btcAddress = state.btcAddress,
                        onDismiss = { showDonateSheet = false }
                    )
                }
            }
        }
    }
}
