package com.idlelodge.ui.viewmodel

import com.idlelodge.util.withAppLocale

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idlelodge.R
import com.idlelodge.data.json.SkillingDungeonData
import com.idlelodge.data.model.EquipSlot
import com.idlelodge.util.toolEfficiency
import com.idlelodge.data.model.PlayerFlags
import com.idlelodge.data.model.QueuedAction
import com.idlelodge.data.model.Skills
import com.idlelodge.simulator.SkillSimulator
import com.idlelodge.repository.BoostRepository
import com.idlelodge.repository.GameDataRepository
import com.idlelodge.repository.PlayerRepository
import com.idlelodge.repository.QueuedSessionStarter
import com.idlelodge.repository.SessionRepository
import com.idlelodge.repository.TownRepository
import com.idlelodge.simulator.SkillingDungeonSimulator
import com.idlelodge.util.GameStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class SkillingDungeonUiItem(
    val key: String,
    val dungeon: SkillingDungeonData,
    val notesFound: Int,
    val isAccessible: Boolean,
    val lockReason: String?,
)

data class ExpeditionsUiState(
    val isLoading: Boolean = true,
    val skillLevels: Map<String, Int> = emptyMap(),
    val dungeonsBySkill: Map<String, List<SkillingDungeonUiItem>> = emptyMap(),
    val snackbarMessage: String? = null,
    val isQueueFull: Boolean = false,
)

@HiltViewModel
class ExpeditionsViewModel @Inject constructor(
    private val boostRepo: BoostRepository,
    @ApplicationContext private val context: Context,
    private val playerRepo: PlayerRepository,
    private val sessionRepo: SessionRepository,
    private val gameData: GameDataRepository,
    private val queuedSessionStarter: QueuedSessionStarter,
    private val townRepo: TownRepository,
    private val json: Json,
) : ViewModel() {

    private val _extra = MutableStateFlow(ExpeditionsUiState())

    val uiState: StateFlow<ExpeditionsUiState> = combine(
        playerRepo.playerFlow,
        sessionRepo.activeSessionFlow,
        _extra,
    ) { player, activeSession, extra ->
        if (player == null) return@combine extra.copy(isLoading = true)

        val levels: Map<String, Int> = json.decodeFromString(player.skillLevels)
        val flags: PlayerFlags = json.decodeFromString(player.flags)

        val equippedMap: Map<String, String?> = json.decodeFromString(player.equipped)

        val miningEff   = gameData.toolEfficiency(equippedMap[EquipSlot.PICKAXE], EquipSlot.PICKAXE, skillLevels = levels, heirloomXp = flags.heirloomXp)
        val woodEff     = gameData.toolEfficiency(equippedMap[EquipSlot.AXE], EquipSlot.AXE, skillLevels = levels, heirloomXp = flags.heirloomXp)
        val fishingEff  = gameData.toolEfficiency(equippedMap[EquipSlot.FISHING_ROD], EquipSlot.FISHING_ROD, skillLevels = levels, heirloomXp = flags.heirloomXp)

        val agilityLevel = levels[Skills.AGILITY] ?: 1

        val SKILL_ORDER = listOf(Skills.MINING, Skills.WOODCUTTING, Skills.FISHING, Skills.AGILITY, Skills.THIEVING)
        val dungeonsBySkill = SKILL_ORDER.associateWith { skill ->
            gameData.skillingDungeons.entries
                .filter { (_, d) -> d.skill == skill }
                .sortedBy { (_, d) -> d.levelRequired }
                .map { (key, dungeon) ->
                    val playerLevel = levels[skill] ?: 1
                    val notesFound = flags.skillingDungeonNotes[key] ?: 0
                    val levelOk = playerLevel >= dungeon.levelRequired
                    val prevOk = dungeon.requiresPreviousUnlock == null ||
                            flags.unlockedDungeons.contains(dungeon.requiresPreviousUnlock)
                    val isAccessible = levelOk && prevOk
                    val lockReason = when {
                        !levelOk -> context.withAppLocale().getString(
                            R.string.expedition_lock_level,
                            GameStrings.skillName(context, skill),
                            dungeon.levelRequired,
                        )
                        !prevOk  -> {
                            val prereq = gameData.skillingDungeons.entries
                                .firstOrNull { it.value.unlockDungeon == dungeon.requiresPreviousUnlock }
                            if (prereq != null)
                                context.withAppLocale().getString(
                                    R.string.expedition_lock_notes,
                                    GameStrings.skillingDungeonName(context, prereq.key, prereq.value.displayName),
                                )
                            else
                                context.withAppLocale().getString(R.string.expedition_lock_prerequisite)
                        }
                        else -> null
                    }
                    SkillingDungeonUiItem(
                        key = key,
                        dungeon = dungeon,
                        notesFound = notesFound,
                        isAccessible = isAccessible,
                        lockReason = lockReason,
                    )
                }
        }

        extra.copy(
            isLoading = false,
            skillLevels = levels,
            dungeonsBySkill = dungeonsBySkill,
            isQueueFull = flags.sessionQueue.size >= playerRepo.maxQueueSize(flags),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpeditionsUiState())

    fun startExpedition(key: String) {
        viewModelScope.launch {
            val dungeon = gameData.skillingDungeons[key] ?: return@launch
            val player = playerRepo.getOrCreatePlayer()
            val levels: Map<String, Int> = json.decodeFromString(player.skillLevels)
            val agilityLevel = levels[Skills.AGILITY] ?: 1
            val flags: PlayerFlags = json.decodeFromString(player.flags)
            val floorReductionMin = boostRepo.sessionFloorReductionMin(flags)

            if (sessionRepo.getActiveSession()?.completed == false) {
                val enqueued = playerRepo.enqueueAction(
                    QueuedAction(
                        skillName           = "expedition",
                        activityKey         = key,
                        skillDisplayName    = dungeon.displayName,
                        estimatedDurationMs = SkillSimulator.sessionDurationMs(agilityLevel, floorReductionMin, townRepo.playerSessionDurationMultiplier(flags)),
                    )
                )
                _extra.update {
                    it.copy(snackbarMessage = if (enqueued) context.withAppLocale().getString(R.string.snackbar_added_to_queue, GameStrings.skillingDungeonName(context, key, dungeon.displayName)) else context.withAppLocale().getString(R.string.snackbar_queue_full))
                }
                return@launch
            }

            val equippedMap: Map<String, String?> = json.decodeFromString(player.equipped)
            val toolEfficiency: Float = when (dungeon.skill) {
                Skills.MINING      -> gameData.toolEfficiency(equippedMap[EquipSlot.PICKAXE], EquipSlot.PICKAXE, skillLevels = levels, heirloomXp = flags.heirloomXp)
                Skills.WOODCUTTING -> gameData.toolEfficiency(equippedMap[EquipSlot.AXE], EquipSlot.AXE, skillLevels = levels, heirloomXp = flags.heirloomXp)
                Skills.FISHING     -> gameData.toolEfficiency(equippedMap[EquipSlot.FISHING_ROD], EquipSlot.FISHING_ROD, skillLevels = levels, heirloomXp = flags.heirloomXp)
                else               -> 1.0f
            }
            val xpMap: Map<String, Long> = json.decodeFromString(player.skillXp)
            val result = SkillingDungeonSimulator.simulate(
                dungeonKey      = key,
                dungeon         = dungeon,
                startXp         = xpMap[dungeon.skill] ?: 0L,
                agilityLevel    = agilityLevel,
                floorReductionMin = floorReductionMin,
                toolEfficiency  = toolEfficiency,
                chronosMultiplier = townRepo.playerSessionDurationMultiplier(flags),
            )
            sessionRepo.startSession(
                skillName        = "expedition",
                activityKey      = key,
                frames           = json.encodeToString(result.frames),
                durationMs       = result.durationMs,
                skillDisplayName = dungeon.displayName,
            )
            _extra.update { it.copy(snackbarMessage = context.withAppLocale().getString(R.string.expedition_started, GameStrings.skillingDungeonName(context, key, dungeon.displayName))) }
        }
    }

    fun clearSnackbar() {
        _extra.update { it.copy(snackbarMessage = null) }
    }
}
