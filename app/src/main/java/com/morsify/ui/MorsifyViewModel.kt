package com.morsify.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.morsify.data.MorseCode
import com.morsify.data.OutputMode
import com.morsify.data.TimingProfile
import com.morsify.service.Transmitter
import com.morsify.util.LocaleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MorsifyViewModel(app: Application) : AndroidViewModel(app) {

    val localeManager = LocaleManager(app)

    private val transmitter = Transmitter(app)
    
    // Independent ViewModel coroutine scope
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    private var loopJob: kotlinx.coroutines.Job? = null

    private val _state = MutableStateFlow(
        State(
            text = "SOS",
            encoded = MorseCode.encode("SOS"),
            timing = TimingProfile.NORMAL,
            outputMode = OutputMode.FLASH_ONLY,
            speedProgress = 67,
            hasFlash = transmitter.hasFlash,
            isAuto = false
        )
    )
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadDonateConfig()
    }

    private fun loadDonateConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            var evm = "0x54e18F0345a099D9FE6dd0576bb1699733c44735"
            var btc = "bc1q7g5whvwjvrh7mtuap2tu7qh3tyyhvls36cp7fs"
            var fetched = false

            // 1. Try remote config
            try {
                val url = URL("https://raw.githubusercontent.com/Curzyori/morsify/main/config/donate.json")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.requestMethod = "GET"
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(text)
                    evm = json.optString("evm", evm)
                    btc = json.optString("btc", btc)
                    fetched = true
                }
            } catch (e: Exception) {
                // Fail silently, fallback to local asset
            }

            // 2. Try local asset fallback if remote fetch failed
            if (!fetched) {
                try {
                    val text = getApplication<Application>().assets.open("config/donate.json")
                        .bufferedReader().use { it.readText() }
                    val json = JSONObject(text)
                    evm = json.optString("evm", evm)
                    btc = json.optString("btc", btc)
                } catch (e: Exception) {
                    // Fail silently, fallback to hardcoded defaults
                }
            }

            _state.update {
                it.copy(evmAddress = evm, btcAddress = btc)
            }
        }
    }

    fun setText(text: String) {
        val encoded = MorseCode.encode(text)
        _state.update { it.copy(text = text, encoded = encoded) }
    }

    fun setTiming(t: TimingProfile) {
        _state.update { it.copy(timing = t) }
    }

    fun setSpeed(progress: Int) {
        val t = TimingProfile.fromProgress(progress)
        _state.update { it.copy(timing = t, speedProgress = progress) }
    }

    fun setOutputMode(mode: OutputMode) {
        _state.update { it.copy(outputMode = mode) }
    }

    fun toggleAuto() {
        _state.update { it.copy(isAuto = !it.isAuto) }
    }

    fun start() {
        // Cancel any pending loop first
        loopJob?.cancel()
        
        val s = _state.value
        transmitter.transmit(
            encoded = s.encoded,
            timing = s.timing,
            mode = s.outputMode,
            onProgress = { p -> _state.update { it.copy(progress = p) } },
            onComplete = {
                handleTransmissionComplete()
            }
        )
    }

    private fun handleTransmissionComplete() {
        val s = _state.value
        if (s.isAuto) {
            // Loop mode: wait 2 seconds and transmit again
            loopJob = viewModelScope.launch {
                delay(2000)
                start()
            }
        } else {
            stop()
        }
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
        transmitter.stop()
        _state.update { it.copy(progress = null) }
    }

    fun toggleLanguage() {
        localeManager.toggle()
        _state.update { it.copy(languageTick = it.languageTick + 1) }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
        loopJob?.cancel()
        transmitter.shutdown()
    }

    data class State(
        val text: String,
        val encoded: MorseCode.Encoded,
        val timing: TimingProfile,
        val outputMode: OutputMode,
        val speedProgress: Int,
        val hasFlash: Boolean,
        val progress: Transmitter.Progress? = null,
        val languageTick: Int = 0,
        val isAuto: Boolean = false,
        val evmAddress: String = "0x54e18F0345a099D9FE6dd0576bb1699733c44735",
        val btcAddress: String = "bc1q7g5whvwjvrh7mtuap2tu7qh3tyyhvls36cp7fs"
    )
}
