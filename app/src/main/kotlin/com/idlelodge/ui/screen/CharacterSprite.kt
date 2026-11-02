package com.idlelodge.ui.screen

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay

private const val FRAME_W = 64
private const val FRAME_H = 36
private const val EYE_FRAMES = 18

// Rites are a gameplay choice only: every rite shares the same (human) sprite set,
// so no pointed ears or fantasy skin tones. The apron is drawn over every body.
private const val SPRITE_BASE = "sprites/characters/generic"
private const val SPRITE_BODY = "human"
internal const val APRON_PATH = "sprites/characters/regalia/apron.png"
private val SKIN_TONES = 1..4

internal fun loadLayer(context: Context, path: String): ImageBitmap? =
    try { context.assets.open(path).use { BitmapFactory.decodeStream(it) }?.asImageBitmap() }
    catch (_: Exception) { null }

/** Clamps tones saved by older versions (fantasy races used 5..9) to the shared range. */
internal fun spriteSkinTone(skinTone: Int): Int = skinTone.coerceIn(SKIN_TONES)

internal fun bodyAssetPath(skinTone: Int) = "$SPRITE_BASE/body/${SPRITE_BODY}_skin${spriteSkinTone(skinTone)}.png"

internal fun hairAssetPath(style: Int, color: String, skinTone: Int): String? =
    if (style == 0) null
    else "$SPRITE_BASE/hair/standard/Hair${style.toString().padStart(2, '0')}_${color}_skin${spriteSkinTone(skinTone)}.png"

internal fun beardAssetPath(style: Int, color: String): String? =
    if (style == 0) null
    else "$SPRITE_BASE/beard/generic/Beard${style.toString().padStart(2, '0')}_$color.png"

internal fun eyesAssetPath(style: Int) = "$SPRITE_BASE/eyes/eyes${style.toString().padStart(2, '0')}.png"

internal fun actionAssetPath(skinTone: Int) = "$SPRITE_BASE/action/standard/action_generic_skin${spriteSkinTone(skinTone)}.png"

/** Asset paths for one static character frame, in draw order. */
internal data class CharacterLayerPaths(
    val body: String,
    val apron: String,
    val eyes: String,
    val head: String?,
    val beard: String?,
    val action: String,
) {
    companion object {
        const val FRAME_W = 64
        const val FRAME_H = 36
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun characterLayerPaths(
    race: String,
    skinTone: Int,
    hairStyle: Int,
    hairColor: String,
    eyeStyle: Int,
    beardStyle: Int,
    beardColor: String,
): CharacterLayerPaths = CharacterLayerPaths(
    body   = bodyAssetPath(skinTone),
    apron  = APRON_PATH,
    eyes   = eyesAssetPath(eyeStyle),
    head   = hairAssetPath(hairStyle, hairColor, skinTone),
    beard  = beardAssetPath(beardStyle, beardColor),
    action = actionAssetPath(skinTone),
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun CharacterSprite(
    race:        String,
    skinTone:    Int,
    hairStyle:   Int,
    hairColor:   String,
    eyeStyle:    Int,
    beardStyle:  Int,
    beardColor:  String,
    modifier:    Modifier = Modifier,
) {
    val context = LocalContext.current

    val bodyPath   = remember(skinTone)                       { bodyAssetPath(skinTone) }
    val eyesPath   = remember(eyeStyle)                       { eyesAssetPath(eyeStyle) }
    val headPath   = remember(hairStyle, hairColor, skinTone) { hairAssetPath(hairStyle, hairColor, skinTone) }
    val beardPath  = remember(beardStyle, beardColor)         { beardAssetPath(beardStyle, beardColor) }
    val actionPath = remember(skinTone)                       { actionAssetPath(skinTone) }

    val bodyBmp   = remember(bodyPath)   { loadLayer(context, bodyPath) }
    val apronBmp  = remember             { loadLayer(context, APRON_PATH) }
    val eyesBmp   = remember(eyesPath)   { loadLayer(context, eyesPath) }
    val headBmp   = remember(headPath)   { headPath?.let  { loadLayer(context, it) } }
    val beardBmp  = remember(beardPath)  { beardPath?.let { loadLayer(context, it) } }
    val actionBmp = remember(actionPath) { loadLayer(context, actionPath) }

    var eyeFrame by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000L + (Math.random() * 2000).toLong())
            for (f in 1 until EYE_FRAMES) { eyeFrame = f; delay(80L) }
            eyeFrame = 0
        }
    }

    Canvas(modifier = modifier) {
        val dst     = IntSize(size.width.toInt(), size.height.toInt())
        val fullSrc = IntSize(FRAME_W, FRAME_H)
        val zero    = IntOffset.Zero

        fun draw(bmp: ImageBitmap, srcOff: IntOffset = zero) =
            drawImage(bmp, srcOffset = srcOff, srcSize = fullSrc, dstOffset = zero, dstSize = dst, filterQuality = FilterQuality.None)

        bodyBmp?.let  { draw(it) }
        apronBmp?.let { draw(it) }

        eyesBmp?.let  { draw(it, srcOff = IntOffset(eyeFrame * FRAME_W, 0)) }
        headBmp?.let  { draw(it) }
        beardBmp?.let { draw(it) }

        actionBmp?.let { draw(it) }
    }
}

/** Valid skin tone range; identical for every rite. */
@Suppress("UNUSED_PARAMETER")
fun skinToneRange(race: String): IntRange = SKIN_TONES

val HAIR_COLORS  = ('a'..'k').map { it.toString() }
val BEARD_COLORS = HAIR_COLORS
