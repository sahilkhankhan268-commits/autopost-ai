package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private const val TAG = "AdMobManager"

    // Set to false for production release (uses real AdMob IDs)
    const val USE_TEST_ADS = false

    // Real Google AdMob Production IDs
    const val PROD_APP_ID = "ca-app-pub-1779845563126619~7495513457"
    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-1779845563126619/8265292342"
    const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-1779845563126619/3535709842"
    const val PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-1779845563126619/2573981961"

    // Official Google Test Ad Unit IDs
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // Active Ad Unit IDs based on test mode flag
    val BANNER_AD_UNIT_ID: String
        get() = if (USE_TEST_ADS) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID

    val INTERSTITIAL_AD_UNIT_ID: String
        get() = if (USE_TEST_ADS) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID

    val REWARDED_AD_UNIT_ID: String
        get() = if (USE_TEST_ADS) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID

    // Interstitial pacing parameters
    private var levelsCompletedCount = 0
    private const val LEVELS_BETWEEN_INTERSTITIALS = 2
    private var lastInterstitialTime = 0L
    private const val MIN_INTERSTITIAL_COOLDOWN_MS = 45_000L // 45 seconds

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        Log.d(TAG, "Initializing MobileAds SDK (Test Mode = $USE_TEST_ADS)...")
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob MobileAds initialized successfully: $status")
                isInitialized = true
                loadInterstitial(context)
                loadRewarded(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob initialize error: ${e.message}", e)
        }
    }

    // ==========================================
    // 1. INTERSTITIAL ADS
    // ==========================================

    fun loadInterstitial(context: Context, useFallbackTestUnit: Boolean = false) {
        if (interstitialAd != null) {
            Log.d(TAG, "Interstitial Ad already available in cache.")
            return
        }
        if (isInterstitialLoading) {
            Log.d(TAG, "Interstitial Ad is currently loading...")
            return
        }
        isInterstitialLoading = true
        val adUnit = if (useFallbackTestUnit || USE_TEST_ADS) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID
        Log.d(TAG, "Requesting Interstitial Ad from unit ID: $adUnit (fallback=$useFallbackTestUnit)")

        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                adUnit,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isInterstitialLoading = false
                        Log.i(TAG, "✅ Interstitial Ad loaded successfully ($adUnit).")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isInterstitialLoading = false
                        Log.w(TAG, "⚠️ Interstitial Ad failed to load ($adUnit): Code ${loadAdError.code}, Message: ${loadAdError.message}")
                        
                        // If production ad unit failed (e.g. Code 3 Publisher data not found), automatically fallback to Google test unit
                        if (!useFallbackTestUnit && !USE_TEST_ADS) {
                            Log.i(TAG, "🔄 Falling back to Google test interstitial ad unit...")
                            loadInterstitial(context, useFallbackTestUnit = true)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            interstitialAd = null
            isInterstitialLoading = false
            Log.e(TAG, "Interstitial Ad load exception: ${e.message}", e)
        }
    }

    /**
     * Checks pacing policy and shows interstitial at natural breaks (e.g. after level complete).
     */
    fun onLevelCompleted(activity: Activity, onAdClosedOrSkipped: () -> Unit) {
        levelsCompletedCount++
        val now = System.currentTimeMillis()
        val cooldownPassed = (now - lastInterstitialTime) >= MIN_INTERSTITIAL_COOLDOWN_MS
        val isMilestoneLevel = (levelsCompletedCount % LEVELS_BETWEEN_INTERSTITIALS == 0)

        Log.d(TAG, "onLevelCompleted: levelCount=$levelsCompletedCount, isMilestone=$isMilestoneLevel, cooldownPassed=$cooldownPassed, adReady=${interstitialAd != null}")

        if (isMilestoneLevel && cooldownPassed && interstitialAd != null) {
            showInterstitial(activity, onAdClosedOrSkipped)
        } else {
            // If ad not loaded or not time yet, continue seamlessly
            if (interstitialAd == null) {
                loadInterstitial(activity)
            }
            onAdClosedOrSkipped()
        }
    }

    fun showInterstitial(activity: Activity, onAdClosed: () -> Unit) {
        val currentAd = interstitialAd
        if (currentAd != null) {
            Log.d(TAG, "Presenting Interstitial Ad...")
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.i(TAG, "Interstitial Ad dismissed by user.")
                    interstitialAd = null
                    lastInterstitialTime = System.currentTimeMillis()
                    loadInterstitial(activity)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "❌ Interstitial Ad failed to show: Code ${adError.code}, Message: ${adError.message}")
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAdClosed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.i(TAG, "Interstitial Ad full-screen content presented.")
                }
            }
            currentAd.show(activity)
        } else {
            Log.d(TAG, "Interstitial Ad not ready to show. Loading new ad...")
            loadInterstitial(activity)
            onAdClosed()
        }
    }

    // ==========================================
    // 2. REWARDED ADS
    // ==========================================

    fun loadRewarded(context: Context, useFallbackTestUnit: Boolean = false) {
        if (rewardedAd != null) {
            Log.d(TAG, "Rewarded Ad already available in cache.")
            return
        }
        if (isRewardedLoading) {
            Log.d(TAG, "Rewarded Ad is currently loading...")
            return
        }
        isRewardedLoading = true
        val adUnit = if (useFallbackTestUnit || USE_TEST_ADS) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID
        Log.d(TAG, "Requesting Rewarded Ad from unit ID: $adUnit (fallback=$useFallbackTestUnit)")

        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                adUnit,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isRewardedLoading = false
                        Log.i(TAG, "✅ Rewarded Ad loaded successfully ($adUnit).")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        isRewardedLoading = false
                        Log.w(TAG, "⚠️ Rewarded Ad failed to load ($adUnit): Code ${loadAdError.code}, Message: ${loadAdError.message}")
                        
                        // If production ad unit failed (e.g. Code 3 Publisher data not found), automatically fallback to Google test unit
                        if (!useFallbackTestUnit && !USE_TEST_ADS) {
                            Log.i(TAG, "🔄 Falling back to Google test rewarded ad unit...")
                            loadRewarded(context, useFallbackTestUnit = true)
                        }
                    }
                }
            )
        } catch (e: Exception) {
            rewardedAd = null
            isRewardedLoading = false
            Log.e(TAG, "Rewarded Ad load exception: ${e.message}", e)
        }
    }

    fun isRewardedAdReady(): Boolean = rewardedAd != null

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (rewardAmount: Int, rewardType: String) -> Unit,
        onAdUnavailable: () -> Unit = {}
    ) {
        val currentAd = rewardedAd
        if (currentAd != null) {
            var rewardGranted = false
            Log.d(TAG, "Presenting Rewarded Ad...")

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.i(TAG, "Rewarded Ad dismissed. Granted = $rewardGranted")
                    rewardedAd = null
                    loadRewarded(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "❌ Rewarded Ad failed to show: Code ${adError.code}, Message: ${adError.message}")
                    rewardedAd = null
                    loadRewarded(activity)
                    onAdUnavailable()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.i(TAG, "Rewarded Ad full-screen content presented.")
                }
            }

            currentAd.show(activity) { rewardItem ->
                rewardGranted = true
                Log.i(TAG, "🎉 User completed rewarded ad: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned(rewardItem.amount, rewardItem.type)
            }
        } else {
            Log.w(TAG, "Rewarded ad is not ready yet. Triggering reload.")
            loadRewarded(activity)
            onAdUnavailable()
        }
    }
}

/**
 * Clean, safe Jetpack Compose Banner Ad View.
 * Displays banner ads without blocking UI controls.
 */
@Composable
fun AdMobBannerView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdManager.BANNER_AD_UNIT_ID
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            try {
                AdView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.i("AdMobManager", "✅ Banner Ad loaded successfully ($adUnitId).")
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.w("AdMobManager", "⚠️ Banner Ad failed to load ($adUnitId): Code ${adError.code}, Message: ${adError.message}")
                            if (adUnitId != AdManager.TEST_BANNER_AD_UNIT_ID) {
                                post {
                                    try {
                                        this@apply.adUnitId = AdManager.TEST_BANNER_AD_UNIT_ID
                                        loadAd(AdRequest.Builder().build())
                                    } catch (_: Exception) {}
                                }
                            }
                        }

                        override fun onAdOpened() {
                            Log.d("AdMobManager", "Banner Ad clicked/opened.")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            } catch (e: Exception) {
                Log.e("AdMobManager", "Banner creation error: ${e.message}", e)
                FrameLayout(context)
            }
        }
    )
}
