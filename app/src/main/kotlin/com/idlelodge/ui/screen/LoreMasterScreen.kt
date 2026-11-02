package com.idlelodge.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.idlelodge.R
import com.idlelodge.ui.viewmodel.ElderQuestsViewModel

private data class LoreFragment(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    /** Quest id gating this fragment; null = always shown. */
    val unlockedByQuest: String?,
    @StringRes val unlockHintRes: Int,
)

private val LORE: List<LoreFragment> = listOf(
    LoreFragment(
        titleRes        = R.string.code_lore_prologue_title,
        bodyRes         = R.string.code_lore_prologue_body,
        unlockedByQuest = null,
        unlockHintRes   = R.string.code_lore_prologue_hint,
    ),

    // Act I fragments
    LoreFragment(
        titleRes        = R.string.code_lore_1a_title,
        bodyRes         = R.string.code_lore_1a_body,
        unlockedByQuest = "act1_first_steps",
        unlockHintRes   = R.string.code_lore_1a_hint,
    ),
    LoreFragment(
        titleRes        = R.string.code_lore_1b_title,
        bodyRes         = R.string.code_lore_1b_body,
        unlockedByQuest = "act1_rowans_cache",
        unlockHintRes   = R.string.code_lore_1b_hint,
    ),
    LoreFragment(
        titleRes        = R.string.code_lore_1c_title,
        bodyRes         = R.string.code_lore_1c_body,
        unlockedByQuest = "act1_first_cooking",
        unlockHintRes   = R.string.code_lore_1c_hint,
    ),

    // Act II fragments
    LoreFragment(
        titleRes        = R.string.code_lore_2a_title,
        bodyRes         = R.string.code_lore_2a_body,
        unlockedByQuest = "act2_cutting_vines",
        unlockHintRes   = R.string.code_lore_2a_hint,
    ),
    LoreFragment(
        titleRes        = R.string.code_lore_2b_title,
        bodyRes         = R.string.code_lore_2b_body,
        unlockedByQuest = "act2_library_salvage",
        unlockHintRes   = R.string.code_lore_2b_hint,
    ),
    LoreFragment(
        titleRes        = R.string.code_lore_2c_title,
        bodyRes         = R.string.code_lore_2c_body,
        unlockedByQuest = "act2_first_armor",
        unlockHintRes   = R.string.code_lore_2c_hint,
    ),

    // Act III fragments
    LoreFragment(
        titleRes        = R.string.code_lore_3a_title,
        bodyRes         = R.string.code_lore_3a_body,
        unlockedByQuest = "act3_ascent",
        unlockHintRes   = R.string.code_lore_3a_hint,
    ),
    LoreFragment(
        titleRes        = R.string.code_lore_3b_title,
        bodyRes         = R.string.code_lore_3b_body,
        unlockedByQuest = "act3_voidsteel_study",
        unlockHintRes   = R.string.code_lore_3b_hint,
    ),
    LoreFragment(
        titleRes        = R.string.code_lore_3c_title,
        bodyRes         = R.string.code_lore_3c_body,
        unlockedByQuest = "act3_grove_gear",
        unlockHintRes   = R.string.code_lore_3c_hint,
    ),

    // Act IV fragments
    LoreFragment(
        titleRes        = R.string.code_lore_4a_title,
        bodyRes         = R.string.code_lore_4a_body,
        unlockedByQuest = "act4_descent",
        unlockHintRes   = R.string.code_lore_4a_hint,
    ),
    LoreFragment(
        titleRes        = R.string.code_lore_4b_title,
        bodyRes         = R.string.code_lore_4b_body,
        unlockedByQuest = "act4_full_set",
        unlockHintRes   = R.string.code_lore_4b_hint,
    ),
    LoreFragment(
        titleRes        = R.string.code_lore_4c_title,
        bodyRes         = R.string.code_lore_4c_body,
        unlockedByQuest = "act4_face_last_elder",
        unlockHintRes   = R.string.code_lore_4c_hint,
    ),
)

/** Lore Master: the isle's world-building. Fragments drip out as the quest chain
 *  advances. Reads like a story, from Rowan's arrival on the isle to the truth of
 *  what the Last Elder was. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoreMasterScreen(
    onBack: () -> Unit,
    questsVm: ElderQuestsViewModel = hiltViewModel(),
) {
    val questsState by questsVm.state.collectAsState()
    val completed = questsState.completedIds

    val discovered = LORE.count { it.unlockedByQuest == null || it.unlockedByQuest in completed }
    val total = LORE.size

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.elder_isle_lore_master_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item {
                Surface(
                    shape    = RoundedCornerShape(16.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text       = stringResource(R.string.code_lore_fragments_title),
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.weight(1f),
                            )
                            Text(
                                text  = "$discovered / $total",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text  = stringResource(R.string.code_lore_fragments_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            items(LORE, key = { it.titleRes }) { fragment ->
                val unlocked = fragment.unlockedByQuest == null || fragment.unlockedByQuest in completed
                LoreFragmentCard(fragment, unlocked)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun LoreFragmentCard(fragment: LoreFragment, unlocked: Boolean) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape    = CircleShape,
                    color    = if (unlocked) MaterialTheme.colorScheme.primaryContainer
                               else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector        = if (unlocked) Icons.Filled.Check else Icons.Filled.Lock,
                            contentDescription = null,
                            tint               = if (unlocked) MaterialTheme.colorScheme.primary
                                                 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text       = stringResource(fragment.titleRes),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = if (unlocked) MaterialTheme.colorScheme.onSurface
                                 else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            if (unlocked) {
                Text(
                    text  = stringResource(fragment.bodyRes),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Text(
                    text  = stringResource(fragment.unlockHintRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
