package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.model.FruitSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)

    var soundEnabled: Boolean = true
    var musicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) {
                startBackgroundMusic()
            } else {
                stopBackgroundMusic()
            }
        }
    var hapticEnabled: Boolean = true

    private val sampleRate = 44100
    private var musicJob: Job? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun playPcmTone(
        durationMs: Int,
        frequencyStart: Double,
        frequencyEnd: Double = frequencyStart,
        waveType: String = "sine",
        volume: Double = 0.8
    ) {
        if (!soundEnabled) return
        scope.launch {
            try {
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / numSamples
                    val currentFreq = frequencyStart + (frequencyEnd - frequencyStart) * t
                    val angle = 2.0 * PI * currentFreq * (i.toDouble() / sampleRate)

                    // Natural decay envelope with faster crisp transient for clink sounds
                    val decayRate = when (waveType) {
                        "clink" -> 6.5
                        "pop" -> 8.0
                        "marimba" -> 4.5
                        "kalimba" -> 5.0
                        else -> 3.5
                    }
                    val envelope = exp(-decayRate * t) * (1.0 - exp(-30.0 * t))

                    val sampleVal = when (waveType) {
                        "sine" -> sin(angle)
                        "triangle" -> (2.0 / PI) * kotlin.math.asin(sin(angle).coerceIn(-1.0, 1.0))
                        "bell" -> 0.6 * sin(angle) + 0.3 * sin(2.0 * angle) + 0.1 * sin(3.0 * angle)
                        "clink" -> 0.52 * sin(angle) + 0.28 * sin(2.42 * angle) + 0.14 * sin(4.68 * angle) + 0.06 * sin(7.15 * angle)
                        "marimba" -> 0.70 * sin(angle) + 0.20 * sin(3.0 * angle) + 0.10 * sin(5.0 * angle)
                        "kalimba" -> 0.65 * sin(angle) + 0.25 * sin(2.75 * angle) + 0.10 * sin(5.4 * angle)
                        "pop" -> sin(angle) * (1.0 - t * 0.7)
                        "crunch" -> (sin(angle) * 0.7 + (Math.random() - 0.5) * 0.5)
                        else -> sin(angle)
                    }

                    buffer[i] = (sampleVal * envelope * volume * Short.MAX_VALUE).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                // Auto release after sound finishes
                scope.launch {
                    delay(durationMs.toLong() + 100)
                    try {
                        audioTrack.stop()
                        audioTrack.release()
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }

    // ==========================================
    // 🎵 PROCEDURAL BACKGROUND MUSIC ENGINE (BGM)
    // ==========================================

    fun startBackgroundMusic() {
        if (!musicEnabled || musicJob?.isActive == true) return

        musicJob = scope.launch {
            // Melodic tropical kalimba / marimba chord pattern (C major pentatonic breezy groove)
            // Sequence of (frequency, durationMs, waveType, volume, waitMs)
            val melodyTrack = listOf(
                // Bar 1 (C Major)
                Triple(523.25, 180L, 0.22), // C5
                Triple(659.25, 180L, 0.20), // E5
                Triple(783.99, 220L, 0.25), // G5
                Triple(659.25, 180L, 0.18), // E5
                Triple(587.33, 160L, 0.20), // D5
                Triple(523.25, 260L, 0.22), // C5

                // Bar 2 (A Minor)
                Triple(440.00, 180L, 0.22), // A4
                Triple(523.25, 180L, 0.20), // C5
                Triple(659.25, 240L, 0.24), // E5
                Triple(523.25, 180L, 0.18), // C5
                Triple(493.88, 160L, 0.20), // B4
                Triple(440.00, 260L, 0.22), // A4

                // Bar 3 (F Major)
                Triple(349.23, 180L, 0.22), // F4
                Triple(440.00, 180L, 0.20), // A4
                Triple(523.25, 240L, 0.24), // C5
                Triple(587.33, 180L, 0.22), // D5
                Triple(659.25, 200L, 0.22), // E5
                Triple(698.46, 260L, 0.24), // F5

                // Bar 4 (G Major turnaround)
                Triple(392.00, 180L, 0.22), // G4
                Triple(493.88, 180L, 0.20), // B4
                Triple(587.33, 240L, 0.24), // D5
                Triple(659.25, 180L, 0.20), // E5
                Triple(587.33, 180L, 0.18), // D5
                Triple(392.00, 300L, 0.22)  // G4
            )

            val bassNotes = listOf(
                130.81, // C3
                110.00, // A2
                87.31,  // F2
                98.00   // G2
            )

            var noteIndex = 0
            var barCounter = 0

            while (isActive && musicEnabled) {
                val (freq, stepDuration, vol) = melodyTrack[noteIndex]

                // Trigger gentle warm kalimba note
                if (soundEnabled && musicEnabled) {
                    playPcmTone(
                        durationMs = 260,
                        frequencyStart = freq,
                        waveType = "kalimba",
                        volume = vol
                    )

                    // On bar starts, trigger warm bass note
                    if (noteIndex % 6 == 0) {
                        val bassFreq = bassNotes[(barCounter % bassNotes.size)]
                        playPcmTone(
                            durationMs = 450,
                            frequencyStart = bassFreq,
                            waveType = "sine",
                            volume = 0.18
                        )
                        barCounter++
                    }
                }

                noteIndex = (noteIndex + 1) % melodyTrack.size
                delay(stepDuration + 70L)
            }
        }
    }

    fun stopBackgroundMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    // ==========================================
    // 🔊 JUICY & SATISFYING UI SOUND EFFECTS
    // ==========================================

    /**
     * Joyful energetic start game fanfare chord when tapping PLAY
     */
    fun playPlayGame() {
        scope.launch {
            val chord = listOf(
                Pair(523.25, 90L),  // C5
                Pair(659.25, 90L),  // E5
                Pair(783.99, 100L), // G5
                Pair(1046.50, 260L) // C6
            )
            for ((f, d) in chord) {
                playPcmTone(durationMs = 240, frequencyStart = f, waveType = "bell", volume = 0.85)
                delay(d)
            }
        }
        vibrate(35, 120)
    }

    /**
     * Crisp, bubbly wooden pop sound for UI navigation buttons
     */
    fun playButtonClick() {
        playPcmTone(durationMs = 70, frequencyStart = 580.0, frequencyEnd = 880.0, waveType = "pop", volume = 0.70)
        vibrate(18, 70)
    }

    /**
     * Unique satisfying tones for specific menu tabs
     */
    fun playMenuTabSound(tabName: String) {
        scope.launch {
            when (tabName) {
                "levels" -> {
                    playPcmTone(durationMs = 120, frequencyStart = 440.0, frequencyEnd = 660.0, waveType = "triangle", volume = 0.75)
                    delay(60)
                    playPcmTone(durationMs = 180, frequencyStart = 880.0, waveType = "bell", volume = 0.80)
                }
                "daily" -> {
                    playPcmTone(durationMs = 100, frequencyStart = 700.0, frequencyEnd = 1100.0, waveType = "pop", volume = 0.80)
                    delay(50)
                    playPcmTone(durationMs = 160, frequencyStart = 1100.0, frequencyEnd = 1400.0, waveType = "bell", volume = 0.75)
                }
                "challenge" -> {
                    playPcmTone(durationMs = 140, frequencyStart = 320.0, frequencyEnd = 640.0, waveType = "triangle", volume = 0.85)
                    delay(60)
                    playPcmTone(durationMs = 200, frequencyStart = 960.0, waveType = "sine", volume = 0.80)
                }
                "shop" -> {
                    // Shimmering cash register / coin sparkle
                    playPcmTone(durationMs = 90, frequencyStart = 987.77, waveType = "bell", volume = 0.85)
                    delay(70)
                    playPcmTone(durationMs = 180, frequencyStart = 1480.0, waveType = "clink", volume = 0.90)
                }
                "achievements" -> {
                    // Regal golden fanfare
                    playPcmTone(durationMs = 110, frequencyStart = 587.33, waveType = "bell", volume = 0.80)
                    delay(70)
                    playPcmTone(durationMs = 220, frequencyStart = 880.0, waveType = "bell", volume = 0.90)
                }
                else -> playButtonClick()
            }
        }
        vibrate(22, 85)
    }

    /**
     * Playful squeak/pop sound when tapping the floating fruit hero on the Home Screen
     */
    fun playFruitEasterEggTap() {
        scope.launch {
            playPcmTone(durationMs = 90, frequencyStart = 450.0, frequencyEnd = 920.0, waveType = "pop", volume = 0.85)
            delay(50)
            playPcmTone(durationMs = 140, frequencyStart = 920.0, frequencyEnd = 1250.0, waveType = "triangle", volume = 0.75)
        }
        vibrate(28, 110)
    }

    /**
     * Switch toggle acoustic blip
     */
    fun playToggleSound(enabled: Boolean) {
        if (enabled) {
            playPcmTone(durationMs = 65, frequencyStart = 550.0, frequencyEnd = 850.0, waveType = "pop", volume = 0.65)
            vibrate(15, 60)
        } else {
            playPcmTone(durationMs = 65, frequencyStart = 750.0, frequencyEnd = 450.0, waveType = "pop", volume = 0.55)
            vibrate(15, 45)
        }
    }

    // ==========================================
    // 🍇 GAMEPLAY SOUND EFFECTS
    // ==========================================

    fun playFruitPickup() {
        playPcmTone(durationMs = 120, frequencyStart = 480.0, frequencyEnd = 720.0, waveType = "sine", volume = 0.7)
        vibrate(30, 80)
    }

    /**
     * Plays a satisfying layered 'clink' and fruit impact sound when fruit reaches the bottom or stack.
     * Pitch and tactile resonance adjust dynamically based on the fruit's size.
     */
    fun playFruitDrop(fruitSize: FruitSize = FruitSize.MEDIUM, isAtBottom: Boolean = false) {
        val pitchMultiplier = when (fruitSize) {
            FruitSize.SMALL -> 1.28
            FruitSize.MEDIUM -> 1.00
            FruitSize.LARGE -> 0.80
        }

        val hapticDuration = when (fruitSize) {
            FruitSize.SMALL -> 22L
            FruitSize.MEDIUM -> 34L
            FruitSize.LARGE -> 48L
        }

        val hapticAmp = when (fruitSize) {
            FruitSize.SMALL -> 90
            FruitSize.MEDIUM -> 135
            FruitSize.LARGE -> 180
        }

        scope.launch {
            // Layer 1: Satisfying Glass Clink transient sound (sharp acoustic glass tap)
            val clinkBaseFreq = 1450.0 * pitchMultiplier
            val clinkVol = if (isAtBottom) 0.95 else 0.75
            playPcmTone(
                durationMs = 70,
                frequencyStart = clinkBaseFreq,
                frequencyEnd = clinkBaseFreq * 0.97,
                waveType = "clink",
                volume = clinkVol
            )

            // Layer 2: Juicy fruit body compression tone
            val bodyStartFreq = 410.0 * pitchMultiplier
            val bodyEndFreq = 165.0 * pitchMultiplier
            playPcmTone(
                durationMs = 105,
                frequencyStart = bodyStartFreq,
                frequencyEnd = bodyEndFreq,
                waveType = "triangle",
                volume = 0.85
            )

            // Layer 3: Deep resonant bottom container impact thud
            val thudStartFreq = 155.0 * pitchMultiplier
            val thudEndFreq = 65.0 * pitchMultiplier
            val thudVol = if (isAtBottom) 0.95 else 0.70
            playPcmTone(
                durationMs = 135,
                frequencyStart = thudStartFreq,
                frequencyEnd = thudEndFreq,
                waveType = "sine",
                volume = thudVol
            )
        }
        vibrate(hapticDuration, hapticAmp)
    }

    fun playFruitBounce() {
        scope.launch {
            playPcmTone(durationMs = 90, frequencyStart = 260.0, frequencyEnd = 180.0, waveType = "triangle", volume = 0.5)
            playPcmTone(durationMs = 50, frequencyStart = 750.0, frequencyEnd = 600.0, waveType = "sine", volume = 0.25)
        }
        vibrate(15, 60)
    }

    fun playBottleCompleted() {
        // Glorious bell chime sequence
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            for (freq in notes) {
                playPcmTone(durationMs = 280, frequencyStart = freq, frequencyEnd = freq, waveType = "bell", volume = 0.9)
                delay(70)
            }
        }
        vibrate(60, 200)
    }

    fun playLevelWin() {
        scope.launch {
            val fanfare = listOf(440.0, 554.37, 659.25, 880.0, 1108.73, 1318.51)
            for (f in fanfare) {
                playPcmTone(durationMs = 320, frequencyStart = f, frequencyEnd = f, waveType = "bell", volume = 0.95)
                delay(90)
            }
        }
        vibrate(100, 255)
    }

    fun playCoinCollect() {
        scope.launch {
            playPcmTone(durationMs = 120, frequencyStart = 987.77, frequencyEnd = 1318.51, waveType = "bell", volume = 0.8)
            delay(80)
            playPcmTone(durationMs = 200, frequencyStart = 1318.51, frequencyEnd = 1760.0, waveType = "bell", volume = 0.9)
        }
        vibrate(25, 100)
    }

    fun playGemCollect() {
        scope.launch {
            playPcmTone(durationMs = 100, frequencyStart = 1200.0, waveType = "clink", volume = 0.85)
            delay(60)
            playPcmTone(durationMs = 180, frequencyStart = 1760.0, waveType = "bell", volume = 0.90)
        }
        vibrate(25, 105)
    }

    fun playIceCrack() {
        scope.launch {
            playPcmTone(durationMs = 120, frequencyStart = 1600.0, frequencyEnd = 2400.0, waveType = "clink", volume = 0.9)
            delay(40)
            playPcmTone(durationMs = 180, frequencyStart = 2200.0, frequencyEnd = 1200.0, waveType = "crunch", volume = 0.85)
        }
        vibrate(40, 160)
    }

    fun playTimerTick() {
        playPcmTone(durationMs = 60, frequencyStart = 880.0, frequencyEnd = 440.0, waveType = "pop", volume = 0.6)
        vibrate(15, 60)
    }

    fun playHammerSmash() {
        playPcmTone(durationMs = 250, frequencyStart = 350.0, frequencyEnd = 80.0, waveType = "crunch", volume = 1.0)
        vibrate(80, 220)
    }

    fun playBombExplosion() {
        playPcmTone(durationMs = 350, frequencyStart = 200.0, frequencyEnd = 50.0, waveType = "crunch", volume = 1.0)
        vibrate(120, 255)
    }

    fun playShuffle() {
        playPcmTone(durationMs = 220, frequencyStart = 300.0, frequencyEnd = 900.0, waveType = "triangle", volume = 0.7)
        vibrate(40, 90)
    }

    fun playUndo() {
        playPcmTone(durationMs = 140, frequencyStart = 650.0, frequencyEnd = 350.0, waveType = "sine", volume = 0.6)
        vibrate(20, 70)
    }

    private fun vibrate(durationMs: Long, amplitude: Int = 100) {
        if (!hapticEnabled) return
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(durationMs)
                    }
                }
            }
        } catch (_: Exception) {}
    }
}

