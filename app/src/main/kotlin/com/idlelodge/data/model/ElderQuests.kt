package com.idlelodge.data.model

import androidx.annotation.StringRes
import com.idlelodge.R

/**
 * Isle main-quest chain. Twelve quests across four acts. Each quest has a stable id,
 * a predicate that runs against the player's current stats (dungeon runs, kills,
 * inventory, elder skill levels, armor pieces owned), and a linear "requires prior
 * quest complete" gate — so the acts unfold in order like a story chain.
 *
 * Completion is persistent: once a quest's predicate has been met, its id is added
 * to PlayerFlags.elderQuestsCompleted and stays there. Consuming quest materials
 * afterwards (smelting the ore, eating the fish) does not undo the completion.
 */
object ElderQuests {

    data class Snapshot(
        val dungeonRuns: Map<String, Int>,
        val enemyKills: Map<String, Int>,
        val inventory: Map<String, Int>,
    )

    data class Quest(
        val id: String,
        val roman: String,
        @StringRes val actLabelRes: Int,
        @StringRes val titleRes: Int,
        @StringRes val objectiveRes: Int,
        @StringRes val rewardRes: Int,
        /** Current progress toward [target]. */
        val counter: (Snapshot) -> Int,
        val target: Int,
    )

    private val ELDER_PIECES = listOf(
        "elder_helm", "elder_platebody", "elder_platelegs", "elder_boots",
        "elder_cape", "elder_shield", "elder_signet_ring", "elder_amulet",
    )
    private val COASTAL_PIECES = listOf("coastal_helm", "coastal_platebody", "coastal_platelegs", "coastal_boots")
    private val GROVE_PIECES   = listOf("grove_helm", "grove_platebody", "grove_platelegs", "grove_boots")
    private val VOLCANIC_PIECES = listOf("volcanic_helm", "volcanic_platebody", "volcanic_platelegs", "volcanic_boots")

    val CHAIN: List<Quest> = listOf(
        // Act I: The Landing
        Quest(
            id        = "act1_first_steps",
            roman     = "I.1",
            actLabelRes = R.string.code_elder_act1_label,
            titleRes     = R.string.code_elder_quest_first_steps_title,
            objectiveRes = R.string.code_elder_quest_first_steps_objective,
            rewardRes    = R.string.code_elder_quest_first_steps_reward,
            counter   = { it.dungeonRuns["beach_and_cliffs"] ?: 0 },
            target    = 8,
        ),
        Quest(
            id        = "act1_rowans_cache",
            roman     = "I.2",
            actLabelRes = R.string.code_elder_act1_label,
            titleRes     = R.string.code_elder_quest_rowans_cache_title,
            objectiveRes = R.string.code_elder_quest_rowans_cache_objective,
            rewardRes    = R.string.code_elder_quest_rowans_cache_reward,
            counter   = { it.inventory["mythrite_ore"] ?: 0 },
            target    = 500,
        ),
        Quest(
            id        = "act1_first_cooking",
            roman     = "I.3",
            actLabelRes = R.string.code_elder_act1_label,
            titleRes     = R.string.code_elder_quest_first_cooking_title,
            objectiveRes = R.string.code_elder_quest_first_cooking_objective,
            rewardRes    = R.string.code_elder_quest_first_cooking_reward,
            counter   = { it.inventory["tidepool_crab"] ?: 0 },
            target    = 250,
        ),

        // Act II: The Buried Library
        Quest(
            id        = "act2_cutting_vines",
            roman     = "II.1",
            actLabelRes = R.string.code_elder_act2_label,
            titleRes     = R.string.code_elder_quest_cutting_vines_title,
            objectiveRes = R.string.code_elder_quest_cutting_vines_objective,
            rewardRes    = R.string.code_elder_quest_cutting_vines_reward,
            counter   = { it.dungeonRuns["ancient_forest"] ?: 0 },
            target    = 15,
        ),
        Quest(
            id        = "act2_library_salvage",
            roman     = "II.2",
            actLabelRes = R.string.code_elder_act2_label,
            titleRes     = R.string.code_elder_quest_library_salvage_title,
            objectiveRes = R.string.code_elder_quest_library_salvage_objective,
            rewardRes    = R.string.code_elder_quest_library_salvage_reward,
            counter   = { it.inventory["abyssal_ore"] ?: 0 },
            target    = 800,
        ),
        Quest(
            id        = "act2_first_armor",
            roman     = "II.3",
            actLabelRes = R.string.code_elder_act2_label,
            titleRes     = R.string.code_elder_quest_first_armor_title,
            objectiveRes = R.string.code_elder_quest_first_armor_objective,
            rewardRes    = R.string.code_elder_quest_first_armor_reward,
            counter   = { COASTAL_PIECES.count { key -> (it.inventory[key] ?: 0) >= 1 } },
            target    = 4,
        ),

        // Act III: The Sealing Site
        Quest(
            id        = "act3_ascent",
            roman     = "III.1",
            actLabelRes = R.string.code_elder_act3_label,
            titleRes     = R.string.code_elder_quest_ascent_title,
            objectiveRes = R.string.code_elder_quest_ascent_objective,
            rewardRes    = R.string.code_elder_quest_ascent_reward,
            counter   = { it.dungeonRuns["volcano_peak"] ?: 0 },
            target    = 20,
        ),
        Quest(
            id        = "act3_voidsteel_study",
            roman     = "III.2",
            actLabelRes = R.string.code_elder_act3_label,
            titleRes     = R.string.code_elder_quest_voidsteel_study_title,
            objectiveRes = R.string.code_elder_quest_voidsteel_study_objective,
            rewardRes    = R.string.code_elder_quest_voidsteel_study_reward,
            counter   = { it.inventory["voidsteel_ore"] ?: 0 },
            target    = 1200,
        ),
        Quest(
            id        = "act3_grove_gear",
            roman     = "III.3",
            actLabelRes = R.string.code_elder_act3_label,
            titleRes     = R.string.code_elder_quest_grove_gear_title,
            objectiveRes = R.string.code_elder_quest_grove_gear_objective,
            rewardRes    = R.string.code_elder_quest_grove_gear_reward,
            counter   = { GROVE_PIECES.count { key -> (it.inventory[key] ?: 0) >= 1 } },
            target    = 4,
        ),

        // Act IV: The Last Elder
        Quest(
            id        = "act4_descent",
            roman     = "IV.1",
            actLabelRes = R.string.code_elder_act4_label,
            titleRes     = R.string.code_elder_quest_descent_title,
            objectiveRes = R.string.code_elder_quest_descent_objective,
            rewardRes    = R.string.code_elder_quest_descent_reward,
            counter   = { it.dungeonRuns["abyssal_depths"] ?: 0 },
            target    = 25,
        ),
        Quest(
            id        = "act4_full_set",
            roman     = "IV.2",
            actLabelRes = R.string.code_elder_act4_label,
            titleRes     = R.string.code_elder_quest_full_set_title,
            objectiveRes = R.string.code_elder_quest_full_set_objective,
            rewardRes    = R.string.code_elder_quest_full_set_reward,
            counter   = { ELDER_PIECES.count { key -> (it.inventory[key] ?: 0) >= 1 } },
            target    = 8,
        ),
        Quest(
            id        = "act4_face_last_elder",
            roman     = "IV.3",
            actLabelRes = R.string.code_elder_act4_label,
            titleRes     = R.string.code_elder_quest_face_last_elder_title,
            objectiveRes = R.string.code_elder_quest_face_last_elder_objective,
            rewardRes    = R.string.code_elder_quest_face_last_elder_reward,
            // #2058: the kill registers as enemyKills["last_elder"] via recordDailyKills,
            // but the guaranteed Ancient Signet drop (chance 1.0) landing in inventory is
            // the sturdier signal — HomeScreen already treats it as lastElderKilled.
            // Counting the max of both keeps save imports and any kill-map gap working.
            counter   = { maxOf(it.enemyKills["last_elder"] ?: 0, it.inventory["ancient_signet"] ?: 0) },
            target    = 1,
        ),
    )

    /**
     * Display progress for a quest row (issue #2039).
     *
     * Mainland quests accumulate via QuestRepository.addProgress and never regress.
     * Isle counters read live inventory, so a validated quest would visually drop to
     * 0/500 after selling or smelting its objective even though its id stays in
     * elderQuestsCompleted. A validated quest therefore always displays [target];
     * only the next open quest shows the live counter.
     */
    fun displayProgress(quest: Quest, snapshot: Snapshot, completed: Set<String>): Int =
        if (quest.id in completed) quest.target
        else quest.counter(snapshot).coerceAtMost(quest.target)

    /**
     * Walks the chain in order and marks quests complete only if every prior quest is
     * already complete or completes in the same pass. Prevents late-game predicates
     * (owning the full Elder set, killing the Last Elder) from marking their quests
     * done before Act I is ever finished — especially on save imports where the
     * inventory already satisfies many predicates.
     */
    fun newlyCompleted(alreadyCompleted: Set<String>, snapshot: Snapshot): Set<String> {
        val newly = mutableSetOf<String>()
        for (q in CHAIN) {
            val done = q.id in alreadyCompleted || q.id in newly
            if (done) continue
            // Not done yet — only allowed to complete if predicate met AND this is the
            // very next open quest (all prior quests already complete).
            if (q.counter(snapshot) >= q.target) {
                newly += q.id
                // Continue scanning: subsequent quests may also complete in-order.
            } else {
                // First unmet quest in the chain blocks any later completion.
                break
            }
        }
        return newly
    }

    /**
     * #2058: pure title-qualification check, shared by the live grant and the
     * retroactive repair. True when the final quest is (or just became) complete OR
     * the kill is visible in the snapshot (kills map or guaranteed signet drop).
     * The ViewModel grants `isle_champion` whenever this is true and the title is
     * still missing; Set addition keeps the repair idempotent.
     */
    fun qualifiesForIsleChampion(completedAfter: Set<String>, snapshot: Snapshot): Boolean {
        if ("act4_face_last_elder" in completedAfter) return true
        val quest = CHAIN.firstOrNull { it.id == "act4_face_last_elder" } ?: return false
        return quest.counter(snapshot) >= quest.target
    }

    /**
     * Linear-chain unlock: quest N is only available once every prior quest is complete.
     * Returns the set of quest ids currently unlocked (visible/actionable) — including all
     * completed ones and the very next open quest.
     */
    fun unlockedIds(completed: Set<String>): Set<String> {
        val open = mutableSetOf<String>()
        for (q in CHAIN) {
            open += q.id
            if (q.id !in completed) break
        }
        return open
    }
}
