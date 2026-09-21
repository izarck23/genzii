package com.example.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import java.util.Collections

/**
 * AdMob Test Ad Component
 *
 * CRITICAL RULE: Uses Google's official sample/test ad units ONLY.
 * Never use live ad unit IDs or production publisher IDs during development or QA.
 */
object AdMobConfig {
    const val TAG = "AdMobTestAds"

    // Official Google Test Ad Units (Sample IDs provided by Google):
    // https://developers.google.com/admob/android/test-ads
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var isInitialized = false

    fun initialize(context: Context) {
        AdMobManager.initialize(context)
    }
}

@Composable
fun AdMobTestBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobConfig.TEST_BANNER_AD_UNIT_ID
) {
    val context = LocalContext.current
    val isInspection = LocalInspectionMode.current
    var adLoaded by remember { mutableStateOf(false) }
    var adFailed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        AdMobConfig.initialize(context)
    }

    if (isInspection) {
        TestAdFallbackCard(modifier = modifier, message = "AdMob Banner Preview (Test Mode)")
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_test_banner_container"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Label clarifying test status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE0E7FF))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SPONSOR / TEST AD",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3730A3),
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Official Test Ad Unit (Safe QA)",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Real Google AdView with safe test unit ID
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                if (adFailed) {
                    TestAdFallbackBanner()
                } else {
                    AndroidView(
                        modifier = Modifier.fillMaxWidth(),
                        factory = { ctx ->
                            var adView: AdView? = null
                            try {
                                adView = AdView(ctx).apply {
                                    setAdSize(AdSize.BANNER)
                                    this.adUnitId = adUnitId
                                    adListener = object : AdListener() {
                                        override fun onAdLoaded() {
                                            adLoaded = true
                                            adFailed = false
                                            Log.d(AdMobConfig.TAG, "AdMob Test Banner loaded successfully")
                                        }

                                        override fun onAdFailedToLoad(error: LoadAdError) {
                                            adFailed = true
                                            adLoaded = false
                                            Log.w(AdMobConfig.TAG, "AdMob Test Banner failed to load: ${error.message}")
                                        }
                                    }
                                    val adRequest = AdRequest.Builder().build()
                                    loadAd(adRequest)
                                }
                            } catch (t: Throwable) {
                                Log.w(AdMobConfig.TAG, "Failed creating AdView: ${t.message}")
                                adFailed = true
                            }
                            adView ?: android.view.View(ctx)
                        },
                        update = { view ->
                            // No dynamic update needed
                        },
                        onRelease = { view ->
                            try {
                                (view as? AdView)?.destroy()
                            } catch (_: Throwable) {}
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TestAdFallbackBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF6366F1),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Google AdMob Test Ad Unit: ca-app-pub-3940256099942544/6300978111",
            fontSize = 10.sp,
            color = Color(0xFF475569),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TestAdFallbackCard(modifier: Modifier = Modifier, message: String) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(50.dp), contentAlignment = Alignment.Center) {
            Text(text = message, fontSize = 11.sp, color = Color(0xFF64748B))
        }
    }
}
