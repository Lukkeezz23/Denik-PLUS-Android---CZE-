// FILE: ui/TokenSpans.kt
package com.example.denikplus.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ClickableSpan
import android.text.style.ReplacementSpan
import android.view.View
import kotlin.math.max

/**
 * ReplacementSpan, který vizuálně nahradí text tokenu chipem/miniaturou.
 * Token text fyzicky v Editable zůstává (kvůli uložení do DB), ale uživatel ho neuvidí.
 */
internal class TokenReplacementSpan(
    private val kind: InlineTokenType,
    private val bgColor: Int,
    private val fgColor: Int,
    private val density: Float,
) : ReplacementSpan() {

    private fun dp(dp: Float): Float = dp * density

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        // Všichni mají stabilní výšku (a šířku podle typu)
        val height = dp(28f)
        val width = when (kind) {
            InlineTokenType.IMG -> height // čtverec
            InlineTokenType.AUD -> max(dp(72f), dp(72f)) // tenký dlouhý chip
            InlineTokenType.MAP -> dp(30f) // malý čtverec/ikona
        }

        // Vertikální zarovnání: přizpůsobíme font metrics tak, aby token seděl na baseline.
        fm?.let {
            val fontH = it.descent - it.ascent
            val targetH = height.toInt()
            if (targetH > fontH) {
                val diff = targetH - fontH
                it.ascent -= diff / 2
                it.descent += diff - diff / 2
                it.top = it.ascent
                it.bottom = it.descent
            }
        }

        return width.toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val height = dp(28f)
        val width = when (kind) {
            InlineTokenType.IMG -> height
            InlineTokenType.AUD -> dp(72f)
            InlineTokenType.MAP -> dp(30f)
        }

        val centerY = (top + bottom) / 2f
        val rectTop = centerY - height / 2f
        val rectBottom = centerY + height / 2f
        val rect = RectF(x, rectTop, x + width, rectBottom)

        val oldColor = paint.color
        val oldStyle = paint.style
        val oldStroke = paint.strokeWidth
        val oldTextAlign = paint.textAlign
        val oldTextSize = paint.textSize
        val oldAA = paint.isAntiAlias

        paint.isAntiAlias = true

        // Background
        paint.style = Paint.Style.FILL
        paint.color = bgColor
        val radius = dp(10f)
        canvas.drawRoundRect(rect, radius, radius, paint)

        // Foreground glyph / hint
        paint.color = fgColor
        paint.textAlign = Paint.Align.CENTER

        when (kind) {
            InlineTokenType.IMG -> {
                // „miniatura“ placeholder: čtverec + text IMG
                paint.textSize = dp(11f)
                val textY = centerY + (paint.textSize * 0.35f)
                canvas.drawText("IMG", rect.centerX(), textY, paint)
            }

            InlineTokenType.AUD -> {
                // tenká linka uvnitř chipu
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = dp(2f)
                val lineY = rect.centerY()
                val pad = dp(10f)
                canvas.drawLine(rect.left + pad, lineY, rect.right - pad, lineY, paint)

                // malý nápis „AUD“
                paint.style = Paint.Style.FILL
                paint.textSize = dp(10f)
                val textY = rect.centerY() - dp(6f)
                canvas.drawText("AUD", rect.centerX(), textY, paint)
            }

            InlineTokenType.MAP -> {
                paint.textSize = dp(16f)
                val textY = centerY + (paint.textSize * 0.35f)
                canvas.drawText("★", rect.centerX(), textY, paint)
            }
        }

        paint.color = oldColor
        paint.style = oldStyle
        paint.strokeWidth = oldStroke
        paint.textAlign = oldTextAlign
        paint.textSize = oldTextSize
        paint.isAntiAlias = oldAA
    }
}

/**
 * Klikací span – volá callback podle tokenu.
 */
internal class TokenClickableSpan(
    private val onClick: () -> Unit
) : ClickableSpan() {
    override fun onClick(widget: View) = onClick()
    override fun updateDrawState(ds: android.text.TextPaint) {
        // nechceme podtrhávání ani změny barvy
        ds.isUnderlineText = false
    }
}
