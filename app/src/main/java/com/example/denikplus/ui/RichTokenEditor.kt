// FILE: ui/RichTokenEditor.kt
package com.example.denikplus.ui

import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.MaterialTheme

/**
 * Controller umožní vkládat tokeny do nativního EditTextu (na aktuální kurzor/selection),
 * přestože skutečný state drží Compose jako raw String.
 */
class RichTokenEditorController internal constructor() {
    internal var attached: TokenEditText? = null
    internal var commit: ((newText: String, newCursor: Int) -> Unit)? = null
    internal var pendingSelection: Int? = null

    fun insertToken(type: InlineTokenType, payload: String) {
        val et = attached ?: return
        val raw = et.text?.toString() ?: ""
        val start = et.selectionStart.coerceIn(0, raw.length)
        val end = et.selectionEnd.coerceIn(0, raw.length)

        val before = raw.substring(0, start)
        val after = raw.substring(end, raw.length)

        val token = buildToken(type, payload)

        val prefix = if (before.isNotEmpty() && !before.last().isWhitespace()) " " else ""
        val suffix = if (after.isNotEmpty() && !after.first().isWhitespace()) " " else ""

        val inserted = prefix + token + suffix
        val newText = before + inserted + after
        val cursor = (before.length + inserted.length).coerceIn(0, newText.length)

        pendingSelection = cursor
        commit?.invoke(newText, cursor)
    }
}

@Composable
fun rememberRichTokenEditorController(): RichTokenEditorController {
    return remember { RichTokenEditorController() }
}

/**
 * Jediná hlavní textová oblast s inline prvky (IMG/AUD/MAP) vykreslenými přes spans.
 * Do DB ukládáš 1 raw string s tokeny – uživatel tokeny neuvidí.
 */
@Composable
fun RichTokenEditor(
    rawText: String,
    onRawTextChange: (String) -> Unit,
    controller: RichTokenEditorController,
    modifier: Modifier = Modifier,
    onImageClick: (payload: String) -> Unit,
    onAudioClick: (payload: String) -> Unit,
    onMapClick: (payload: String) -> Unit,
) {
    val density = LocalDensity.current.density
    val view = LocalView.current

    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val bgImg = MaterialTheme.colorScheme.secondaryContainer.toArgb()
    val bgAud = MaterialTheme.colorScheme.tertiaryContainer.toArgb()
    val bgMap = MaterialTheme.colorScheme.primaryContainer.toArgb()
    val fgChip = MaterialTheme.colorScheme.onSecondaryContainer.toArgb()

    val lastApplied = remember { mutableStateOf<String?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TokenEditText(ctx).apply {
                setTextColor(textColor)
                setHintTextColor(hintColor)
                hint = "Zápis…"

                // Multiline + UX
                isSingleLine = false
                setLines(10)
                maxLines = 18
                gravity = Gravity.TOP or Gravity.START
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setHorizontallyScrolling(false)
                inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE or EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES

                // Klikatelné spany
                movementMethod = LinkMovementMethod.getInstance()
                highlightColor = 0x00000000

                // Bez defaultního pozadí (Compose okolo)
                background = null

                // Attach controller
                controller.attached = this
                controller.commit = { newText, newCursor ->
                    // Compose state update
                    onRawTextChange(newText)
                    // selection se aplikuje po recomposition v update bloku
                    controller.pendingSelection = newCursor
                }

                // TextWatcher – drží rawText v Compose a přidává spans do Editable
                val watcher = object : TextWatcher {
                    private var beforeText: String = ""
                    private var beforeStart: Int = 0
                    private var beforeCount: Int = 0
                    private var beforeAfter: Int = 0

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        if (internalChange) return
                        beforeText = s?.toString() ?: ""
                        beforeStart = start
                        beforeCount = count
                        beforeAfter = after
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        if (internalChange) return
                        val editable = s ?: return

                        // Ochrana tokenů při mazání: když zásah protne token, smaž celý token (a ne jeho část)
                        if (beforeAfter == 0 && beforeCount > 0) {
                            val delStart = beforeStart.coerceIn(0, beforeText.length)
                            val delEnd = (beforeStart + beforeCount).coerceIn(0, beforeText.length)
                            val tokensBefore = findInlineTokens(beforeText)

                            val hit = tokensBefore.filter { t ->
                                rangesIntersect(delStart, delEnd, t.start, t.endExclusive)
                            }

                            if (hit.isNotEmpty()) {
                                val expandedStart = minOf(delStart, hit.minOf { it.start })
                                val expandedEnd = maxOf(delEnd, hit.maxOf { it.endExclusive })

                                val newText = buildString {
                                    append(beforeText.substring(0, expandedStart))
                                    append(beforeText.substring(expandedEnd))
                                }

                                internalChange = true
                                setText(newText)
                                val cursor = expandedStart.coerceIn(0, newText.length)
                                setSelection(cursor)
                                internalChange = false

                                // commit do Compose
                                onRawTextChange(newText)
                                return
                            }
                        }

                        // Přidej/obnov spans (v-place)
                        applyTokenSpans(
                            editable = editable,
                            density = density,
                            bgImg = bgImg,
                            bgAud = bgAud,
                            bgMap = bgMap,
                            fg = fgChip,
                            onImageClick = onImageClick,
                            onAudioClick = onAudioClick,
                            onMapClick = onMapClick
                        )

                        // commit do Compose
                        onRawTextChange(editable.toString())
                    }
                }
                addTextChangedListener(watcher)

                // Initial apply
                internalChange = true
                setText(rawText)
                setSelection(rawText.length)
                internalChange = false
                lastApplied.value = rawText

                // spans
                text?.let { editable ->
                    applyTokenSpans(
                        editable = editable,
                        density = density,
                        bgImg = bgImg,
                        bgAud = bgAud,
                        bgMap = bgMap,
                        fg = fgChip,
                        onImageClick = onImageClick,
                        onAudioClick = onAudioClick,
                        onMapClick = onMapClick
                    )
                }
            }
        },
        update = { et ->
            controller.attached = et
            controller.commit = { newText, newCursor ->
                onRawTextChange(newText)
                controller.pendingSelection = newCursor
            }

            // Jen pokud se rawText liší – jinak neresetujeme kurzor.
            if (lastApplied.value != rawText && et.text?.toString() != rawText) {
                et.internalChange = true
                et.setText(rawText)
                et.internalChange = false
                lastApplied.value = rawText
            }

            // Apply pending selection after programmatic insert
            controller.pendingSelection?.let { cursor ->
                val safe = cursor.coerceIn(0, (et.text?.length ?: 0))
                if (safe != et.selectionStart || safe != et.selectionEnd) {
                    et.setSelection(safe)
                }
                controller.pendingSelection = null
            }

            // Ensure spans exist (např. při návratu do dialogu)
            et.text?.let { editable ->
                applyTokenSpans(
                    editable = editable,
                    density = density,
                    bgImg = bgImg,
                    bgAud = bgAud,
                    bgMap = bgMap,
                    fg = fgChip,
                    onImageClick = onImageClick,
                    onAudioClick = onAudioClick,
                    onMapClick = onMapClick
                )
            }
        }
    )

    // Fokus (když se otevře dialog)
    LaunchedEffect(view) {
        // bez explicitního requestFocus (necháme na uživateli kliknout), ale je tu místo pro případné rozšíření
    }
}

private fun rangesIntersect(aStart: Int, aEnd: Int, bStart: Int, bEnd: Int): Boolean {
    // [aStart, aEnd) intersects [bStart, bEnd)
    return aStart < bEnd && bStart < aEnd
}

private fun applyTokenSpans(
    editable: Editable,
    density: Float,
    bgImg: Int,
    bgAud: Int,
    bgMap: Int,
    fg: Int,
    onImageClick: (payload: String) -> Unit,
    onAudioClick: (payload: String) -> Unit,
    onMapClick: (payload: String) -> Unit
) {
    // Remove old spans of our types
    editable.getSpans(0, editable.length, TokenReplacementSpan::class.java).forEach { editable.removeSpan(it) }
    editable.getSpans(0, editable.length, TokenClickableSpan::class.java).forEach { editable.removeSpan(it) }

    val raw = editable.toString()
    val tokens = findInlineTokens(raw)
    for (t in tokens) {
        val bg = when (t.type) {
            InlineTokenType.IMG -> bgImg
            InlineTokenType.AUD -> bgAud
            InlineTokenType.MAP -> bgMap
        }

        val rep = TokenReplacementSpan(
            kind = t.type,
            bgColor = bg,
            fgColor = fg,
            density = density
        )

        val click = TokenClickableSpan {
            when (t.type) {
                InlineTokenType.IMG -> onImageClick(t.payload)
                InlineTokenType.AUD -> onAudioClick(t.payload)
                InlineTokenType.MAP -> onMapClick(t.payload)
            }
        }

        // SPAN_EXCLUSIVE_EXCLUSIVE aby se token choval stabilně vůči vkládání textu okolo
        editable.setSpan(rep, t.start, t.endExclusive, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        editable.setSpan(click, t.start, t.endExclusive, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

internal class TokenEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    internal var internalChange: Boolean = false

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (!internalChange) {
            val raw = text?.toString().orEmpty()
            if (raw.isNotEmpty()) {
                val tokens = findInlineTokens(raw)

                // Cursor (bez selection)
                if (selStart == selEnd) {
                    val hit = tokens.firstOrNull { selStart > it.start && selStart < it.endExclusive }
                    if (hit != null) {
                        // Když je kurzor "uvnitř" tokenu, přeskoč na nejbližší okraj
                        val to = if (selStart - hit.start < hit.endExclusive - selStart) hit.start else hit.endExclusive
                        internalChange = true
                        try {
                            setSelection(to.coerceIn(0, raw.length))
                        } finally {
                            internalChange = false
                        }
                        return
                    }
                }
            }
        }
        super.onSelectionChanged(selStart, selEnd)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Backspace: pokud stojíme hned za tokenem a uživatel maže, TextWatcher to stejně ošetří,
        // ale tohle zlepšuje UX (necháme default a spoléháme na watcher).
        return super.onKeyDown(keyCode, event)
    }
}
