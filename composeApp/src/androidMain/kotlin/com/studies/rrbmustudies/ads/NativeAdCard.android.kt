package com.studies.rrbmustudies.ads

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
actual fun NativeAdCard(modifier: Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(Unit) {
        var disposed = false
        val loader = AdLoader.Builder(context, AdUnitIds.NATIVE)
            .forNativeAd { ad ->
                if (disposed) ad.destroy() else nativeAd = ad
            }
            .build()
        loader.loadAd(AdRequest.Builder().build())
        onDispose {
            disposed = true
            nativeAd?.destroy()
        }
    }

    val ad = nativeAd ?: return
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        factory = { ctx -> buildNativeAdView(ctx) },
        update = { view -> (view.tag as NativeAdBinder).bind(view, ad) },
    )
}

private class NativeAdBinder(
    val icon: ImageView,
    val headline: TextView,
    val body: TextView,
    val media: MediaView,
    val cta: Button,
    val badge: TextView,
) {
    fun bind(view: NativeAdView, ad: NativeAd) {
        headline.text = ad.headline
        body.text = ad.body ?: ""
        cta.text = ad.callToAction ?: "Learn more"
        val iconDrawable = ad.icon?.drawable
        icon.visibility = if (iconDrawable != null) android.view.View.VISIBLE else android.view.View.GONE
        icon.setImageDrawable(iconDrawable)

        view.iconView = icon
        view.headlineView = headline
        view.bodyView = body
        view.mediaView = media
        view.callToActionView = cta
        view.setNativeAd(ad)
    }
}

private fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

private fun buildNativeAdView(ctx: Context): NativeAdView {
    val card = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        background = GradientDrawable().apply {
            cornerRadius = ctx.dp(20).toFloat()
            setColor(Color.WHITE)
        }
        setPadding(ctx.dp(16), ctx.dp(14), ctx.dp(16), ctx.dp(14))
    }

    val badge = TextView(ctx).apply {
        text = "AD"
        setTextColor(Color.WHITE)
        textSize = 10f
        typeface = Typeface.DEFAULT_BOLD
        background = GradientDrawable().apply {
            cornerRadius = ctx.dp(6).toFloat()
            setColor(Color.parseColor("#FF9A00"))
        }
        setPadding(ctx.dp(6), ctx.dp(2), ctx.dp(6), ctx.dp(2))
    }

    val icon = ImageView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(ctx.dp(40), ctx.dp(40)).apply {
            marginEnd = ctx.dp(10)
        }
    }
    val headline = TextView(ctx).apply {
        setTextColor(Color.parseColor("#141726"))
        textSize = 15f
        typeface = Typeface.DEFAULT_BOLD
        maxLines = 2
    }
    val headerRow = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(badge)
        addView(
            icon.also {
                (it.layoutParams as LinearLayout.LayoutParams).marginStart = ctx.dp(8)
            },
        )
        addView(headline)
    }

    val body = TextView(ctx).apply {
        setTextColor(Color.parseColor("#5B6072"))
        textSize = 13f
        maxLines = 2
        setPadding(0, ctx.dp(6), 0, 0)
    }

    val media = MediaView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ctx.dp(160),
        ).apply { topMargin = ctx.dp(10) }
    }

    val cta = Button(ctx).apply {
        setTextColor(Color.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        isAllCaps = false
        background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(Color.parseColor("#FF9A00"), Color.parseColor("#FF5A00")),
        ).apply { cornerRadius = ctx.dp(999).toFloat() }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ctx.dp(44),
        ).apply { topMargin = ctx.dp(10) }
    }

    card.addView(headerRow)
    card.addView(body)
    card.addView(media)
    card.addView(cta)

    return NativeAdView(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        addView(card)
        tag = NativeAdBinder(icon, headline, body, media, cta, badge)
    }
}
