package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdMobManager
 * Centralized manager for AdMob Interstitial, Rewarded, and Banner Ads using Google Test Ad IDs.
 *
 * Directives:
 * - Natural, non-disruptive transition points only for interstitials.
 * - Cooldown enforcement (minimum 120s between interstitial showings).
 * - Never interrupt AI conversations, OCR scanning, file selection, media playback, or active workflows.
 * - Rewarded ads for optional user rewards (e.g., bonus AI credits).
 * - Graceful fallback: onAdDismissed is always invoked so the user journey is never blocked.
 */
object AdMobManager {
    private const val TAG = "AdMobManager"

    // Test Ad Unit IDs provided by Google:
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShowTime = 0L
    private const val INTERSTITIAL_COOLDOWN_MS = 90_000L // 90 seconds minimum cooldown

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            val testDeviceIds = listOf(AdRequest.DEVICE_ID_EMULATOR)
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)

            MobileAds.initialize(context.applicationContext) { status ->
                Log.d(TAG, "AdMob MobileAds initialized: $status")
            }
            isInitialized = true
        } catch (t: Throwable) {
            Log.w(TAG, "AdMob initialization encountered warning: ${t.message}")
        }
    }

    /**
     * Preloads an interstitial ad if not already cached.
     */
    fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                TEST_INTERSTITIAL_AD_UNIT_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isInterstitialLoading = false
                        Log.d(TAG, "AdMob Interstitial Ad preloaded successfully")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isInterstitialLoading = false
                        Log.w(TAG, "AdMob Interstitial failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (t: Throwable) {
            isInterstitialLoading = false
            Log.w(TAG, "Preload interstitial exception: ${t.message}")
        }
    }

    /**
     * Shows an interstitial ad at a natural transition point if cooldown has elapsed.
     * Always calls [onDismissed] so user workflow is never blocked.
     */
    fun showInterstitial(
        activity: Activity,
        forceIgnoreCooldown: Boolean = false,
        onDismissed: () -> Unit
    ) {
        val now = SystemClock.elapsedRealtime()
        val timeSinceLast = now - lastInterstitialShowTime

        if (!forceIgnoreCooldown && lastInterstitialShowTime > 0 && timeSinceLast < INTERSTITIAL_COOLDOWN_MS) {
            Log.d(TAG, "Skipping interstitial ad due to cooldown (${(INTERSTITIAL_COOLDOWN_MS - timeSinceLast) / 1000}s remaining)")
            onDismissed()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "No interstitial ad cached. Proceeding directly with workflow.")
            preloadInterstitial(activity)
            onDismissed()
            return
        }

        try {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed by user.")
                    interstitialAd = null
                    lastInterstitialShowTime = SystemClock.elapsedRealtime()
                    preloadInterstitial(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
                    interstitialAd = null
                    preloadInterstitial(activity)
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed full screen.")
                    interstitialAd = null
                }
            }

            ad.show(activity)
        } catch (t: Throwable) {
            Log.w(TAG, "Error displaying interstitial: ${t.message}")
            interstitialAd = null
            onDismissed()
        }
    }

    /**
     * Preloads a rewarded ad if not already cached.
     */
    fun preloadRewarded(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        if (rewardedAd != null) {
            onComplete?.invoke(true)
            return
        }
        if (isRewardedLoading) {
            onComplete?.invoke(false)
            return
        }
        isRewardedLoading = true

        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                TEST_REWARDED_AD_UNIT_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isRewardedLoading = false
                        Log.d(TAG, "AdMob Rewarded Ad preloaded successfully")
                        onComplete?.invoke(true)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        isRewardedLoading = false
                        Log.w(TAG, "AdMob Rewarded Ad failed to load: ${loadAdError.message}")
                        onComplete?.invoke(false)
                    }
                }
            )
        } catch (t: Throwable) {
            isRewardedLoading = false
            Log.w(TAG, "Preload rewarded exception: ${t.message}")
            onComplete?.invoke(false)
        }
    }

    /**
     * Shows a rewarded ad for optional user rewards (e.g. bonus AI credits).
     */
    fun showRewarded(
        activity: Activity,
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        val ad = rewardedAd
        if (ad == null) {
            Log.d(TAG, "Rewarded ad not cached. Requesting ad and attempting load...")
            preloadRewarded(activity) { loaded ->
                if (loaded && rewardedAd != null) {
                    showRewarded(activity, onRewardEarned, onDismissed, onError)
                } else {
                    onError?.invoke("Rewarded ad not available at this time. Please try again in a few moments.")
                    onDismissed()
                }
            }
            return
        }

        var rewardGranted = false

        try {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed.")
                    rewardedAd = null
                    preloadRewarded(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Rewarded ad failed to show: ${adError.message}")
                    rewardedAd = null
                    preloadRewarded(activity)
                    onError?.invoke("Ad presentation failed: ${adError.message}")
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad showed successfully.")
                    rewardedAd = null
                }
            }

            ad.show(activity) { rewardItem ->
                rewardGranted = true
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned(rewardItem.amount, rewardItem.type)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Error displaying rewarded ad: ${t.message}")
            rewardedAd = null
            onError?.invoke(t.localizedMessage ?: "Failed to show rewarded ad")
            onDismissed()
        }
    }

    fun isRewardedAdReady(): Boolean = rewardedAd != null
}
