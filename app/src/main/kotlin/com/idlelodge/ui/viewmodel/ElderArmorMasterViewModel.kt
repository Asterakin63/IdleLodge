package com.idlelodge.ui.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idlelodge.R
import com.idlelodge.data.model.PlayerFlags
import com.idlelodge.repository.PlayerRepository
import com.idlelodge.util.GameStrings
import com.idlelodge.util.withAppLocale
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

/** One Elder BIS piece with its craft recipe, material availability, and embedded sigil. */
data class ElderPieceRow(
    val key: String,
    val displayName: String,
    val slot: String,
    val materials: List<MaterialSlot>,
    val crafted: Boolean,
    val progress: Float,
    val canCraft: Boolean,
    /** Sigil stone item key currently embedded in this piece, or null if the socket is empty. */
    val embeddedSigil: String?,
) {
    data class MaterialSlot(val itemKey: String, val displayName: String, val required: Int, val owned: Int) {
        val met: Boolean get() = owned >= required
    }
}

/** A sigil stone type the player can embed. Effect text is display-only for now; wiring
 *  the actual gameplay bonuses is a follow-up slice. */
data class SigilStone(
    val itemKey: String,
    val displayName: String,
    val effect: String,
)

data class ElderArmorMasterState(
    val rows: List<ElderPieceRow> = emptyList(),
    /** Sigil stones the player owns, with per-stone counts pulled from inventory. */
    val sigilStones: List<SigilStoneRow> = emptyList(),
    val snackbarMessage: String? = null,
)

data class SigilStoneRow(val stone: SigilStone, val owned: Int)

@HiltViewModel
class ElderArmorMasterViewModel @Inject constructor(
    private val playerRepo: PlayerRepository,
    @ApplicationContext private val context: Context,
    private val json: Json,
) : ViewModel() {

    // Display names resolve through the item_*_name / equip_slot_* resources so they follow
    // the in-app language; the keys below stay the internal identifiers.
    private val recipes = listOf(
        Recipe("elder_helm",        "head",
            listOf("starforged_bar" to 5, "starwood_log" to 3, "ancient_sigil" to 5)),
        Recipe("elder_platebody",   "body",
            listOf("starforged_bar" to 8, "starwood_log" to 4, "ancient_sigil" to 5)),
        Recipe("elder_platelegs",   "legs",
            listOf("starforged_bar" to 7, "starwood_log" to 4, "ancient_sigil" to 5)),
        Recipe("elder_boots",       "boots",
            listOf("starforged_bar" to 4, "starwood_log" to 2, "ancient_sigil" to 5)),
        Recipe("elder_cape",        "cape",
            listOf("abyssal_log"    to 20, "starforged_bar" to 2, "ancient_sigil" to 5)),
        Recipe("elder_shield",      "shield",
            listOf("starforged_bar" to 6, "abyssal_log" to 4, "ancient_sigil" to 5)),
        Recipe("elder_signet_ring", "ring",
            listOf("starforged_bar" to 3, "elder_ruby"  to 1, "ancient_sigil" to 5)),
        Recipe("elder_amulet",      "necklace",
            listOf("starforged_bar" to 3, "elder_sapphire" to 1, "ancient_sigil" to 5)),
    )

    private data class Recipe(val key: String, val slotKey: String, val materials: List<Pair<String, Int>>)

    private val recipeByKey = recipes.associateBy { it.key }

    private fun itemName(key: String): String = GameStrings.itemName(context, key)

    /** Sigil stones the player can embed (item key to effect text). Reuses the existing Elder gem items as stones. */
    private val sigilStoneEffects = listOf(
        "elder_ruby"     to R.string.code_sigil_effect_coins,
        "elder_sapphire" to R.string.code_sigil_effect_xp,
        "elder_emerald"  to R.string.code_sigil_effect_loot,
        "elder_topaz"    to R.string.code_sigil_effect_ancient_sigil,
        "elder_amethyst" to R.string.code_sigil_effect_elder_essence,
        "elder_diamond"  to R.string.code_sigil_effect_elder_bone,
    )
    private val sigilStoneEffectByKey = sigilStoneEffects.toMap()

    private fun sigilStone(key: String, @StringRes effectRes: Int): SigilStone =
        SigilStone(key, itemName(key), context.withAppLocale().getString(effectRes))

    fun sigilStoneDisplayName(key: String): String =
        if (key in sigilStoneEffectByKey) itemName(key) else key
    fun sigilStoneEffect(key: String): String =
        sigilStoneEffectByKey[key]?.let { context.withAppLocale().getString(it) } ?: ""

    private val _snack = MutableStateFlow<String?>(null)

    val state: StateFlow<ElderArmorMasterState> = combine(
        playerRepo.playerFlow.map { p ->
            val inv: Map<String, Int> = if (p == null) emptyMap()
                else try { json.decodeFromString(p.inventory) } catch (_: Exception) { emptyMap() }
            val flags: PlayerFlags = if (p == null) PlayerFlags()
                else try { json.decodeFromString(p.flags) } catch (_: Exception) { PlayerFlags() }
            Triple(inv, flags.embeddedSigils, flags.elderQuestsCompleted /*placeholder to distinguish triple*/)
        },
        _snack,
    ) { (inv, embedded, _), snack ->
        val rows = recipes.map { r ->
            val mats = r.materials.map { (key, req) ->
                ElderPieceRow.MaterialSlot(
                    itemKey     = key,
                    displayName = itemName(key),
                    required    = req,
                    owned       = inv[key] ?: 0,
                )
            }
            val totalReq = mats.sumOf { it.required }
            val totalOwned = mats.sumOf { minOf(it.owned, it.required) }
            ElderPieceRow(
                key           = r.key,
                displayName   = itemName(r.key),
                slot          = GameStrings.slotName(context.withAppLocale(), r.slotKey),
                materials     = mats,
                crafted       = (inv[r.key] ?: 0) >= 1,
                progress      = if (totalReq == 0) 0f else totalOwned.toFloat() / totalReq,
                canCraft      = mats.all { it.met } && (inv[r.key] ?: 0) == 0,
                embeddedSigil = embedded[r.key],
            )
        }
        val stoneRows = sigilStoneEffects.map { (key, effectRes) -> SigilStoneRow(sigilStone(key, effectRes), inv[key] ?: 0) }
        ElderArmorMasterState(rows = rows, sigilStones = stoneRows, snackbarMessage = snack)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ElderArmorMasterState())

    /** Immediate craft (button on the piece card). */
    fun craft(pieceKey: String) {
        viewModelScope.launch {
            val r = recipeByKey[pieceKey] ?: return@launch
            val cost = r.materials.associate { (k, q) -> k to q }
            val ok = playerRepo.consumeItems(cost)
            if (!ok) {
                _snack.value = context.withAppLocale().getString(R.string.elder_armor_missing_mats)
                return@launch
            }
            playerRepo.addItems(mapOf(pieceKey to 1))
            _snack.value = context.withAppLocale().getString(R.string.elder_armor_crafted, itemName(r.key))
        }
    }

    /**
     * Embed a sigil stone into an Elder piece. Consumes one stone from inventory, replaces
     * any previously-embedded stone (returning it to inventory).
     */
    fun embedSigil(pieceKey: String, stoneKey: String) {
        viewModelScope.launch {
            if (recipeByKey[pieceKey] == null) return@launch
            if (stoneKey !in sigilStoneEffectByKey) return@launch
            val flags = playerRepo.getFlags()
            val consumed = playerRepo.consumeItems(mapOf(stoneKey to 1))
            if (!consumed) {
                _snack.value = context.withAppLocale().getString(R.string.elder_armor_missing_stone)
                return@launch
            }
            // Refund any previously embedded stone.
            val previous = flags.embeddedSigils[pieceKey]
            if (previous != null) {
                playerRepo.addItems(mapOf(previous to 1))
            }
            val next = flags.embeddedSigils.toMutableMap().apply { put(pieceKey, stoneKey) }
            playerRepo.updateFlags(flags.copy(embeddedSigils = next))
            _snack.value = context.withAppLocale().getString(
                R.string.elder_armor_stone_embedded,
                sigilStoneDisplayName(stoneKey),
                itemName(pieceKey),
            )
        }
    }

    fun removeSigil(pieceKey: String) {
        viewModelScope.launch {
            val flags = playerRepo.getFlags()
            val previous = flags.embeddedSigils[pieceKey] ?: return@launch
            playerRepo.addItems(mapOf(previous to 1))
            val next = flags.embeddedSigils.toMutableMap().apply { remove(pieceKey) }
            playerRepo.updateFlags(flags.copy(embeddedSigils = next))
        }
    }

    fun snackbarConsumed() { _snack.value = null }
}
