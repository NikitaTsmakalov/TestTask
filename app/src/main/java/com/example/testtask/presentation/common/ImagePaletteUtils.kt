package com.example.testtask.presentation.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun darkenedAverageColorFrom(
    imageBitmap: ImageBitmap,
    lightnessReduction: Float = 0.1f,
): Color = withContext(Dispatchers.Default) {
    val src = imageBitmap.asAndroidBitmap()
    val bmp = src.copy(Bitmap.Config.ARGB_8888, false)
    try {
        val w = bmp.width
        val h = bmp.height
        if (w == 0 || h == 0) return@withContext Color.Transparent

        val pixels = IntArray(w * h)
        bmp.getPixels(pixels, 0, w, 0, 0, w, h)

        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        for (pixel in pixels) {
            rSum += android.graphics.Color.red(pixel)
            gSum += android.graphics.Color.green(pixel)
            bSum += android.graphics.Color.blue(pixel)
        }
        val n = pixels.size
        val average = Color(
            (rSum / n).toInt(),
            (gSum / n).toInt(),
            (bSum / n).toInt(),
        )

        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(average.toArgb(), hsl)
        val newL = (hsl[2] - lightnessReduction).coerceIn(0f, 1f)
        Color(ColorUtils.HSLToColor(floatArrayOf(hsl[0], hsl[1], newL)))
    } finally {
        bmp.recycle()
    }
}

@Composable
fun rememberDarkenedAverageColor(
    imageBitmap: ImageBitmap,
    lightnessReduction: Float = 0.1f,
    initialWhileLoading: Color = Color.Transparent,
): State<Color> {
    return produceState(initialValue = initialWhileLoading, imageBitmap, lightnessReduction) {
        value = darkenedAverageColorFrom(imageBitmap, lightnessReduction)
    }
}

@Composable
fun rememberDarkenedAverageColorFromDrawable(
    @DrawableRes resId: Int,
    lightnessReduction: Float = 0.1f,
    initialWhileLoading: Color = Color.Transparent,
): State<Color> {
    val context = LocalContext.current
    val imageBitmap = remember(resId) {
        val opts = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        BitmapFactory.decodeResource(context.resources, resId, opts)?.asImageBitmap()
    }
    return if (imageBitmap != null) {
        rememberDarkenedAverageColor(imageBitmap, lightnessReduction, initialWhileLoading)
    } else {
        remember { mutableStateOf(initialWhileLoading) }
    }
}
