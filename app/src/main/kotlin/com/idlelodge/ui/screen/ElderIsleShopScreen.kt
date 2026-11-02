package com.idlelodge.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.idlelodge.R
import com.idlelodge.ui.viewmodel.ElderIsleShopViewModel

private data class ShopEntry(
    val name: String,
    val body: String,
    val cost: Int,
    val reward: Map<String, Int>,
)

/** Isle Shop: spend Elder Essence on material bundles and sigil stones. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderIsleShopScreen(
    onBack: () -> Unit,
    vm: ElderIsleShopViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    AppBannerEffect(state.snackbarMessage, vm::snackbarConsumed)

    val boneName    = stringResource(R.string.item_elder_bone_name)
    val woodName    = stringResource(R.string.item_sacred_wood_name)
    val sigilName   = stringResource(R.string.item_ancient_sigil_name)

    @Composable
    fun stoneEntry(@StringRes nameRes: Int, @StringRes effectRes: Int, cost: Int, key: String): ShopEntry {
        val name = stringResource(nameRes)
        return ShopEntry(
            name   = name,
            body   = stringResource(R.string.code_shop_stone_body, name, stringResource(effectRes)),
            cost   = cost,
            reward = mapOf(key to 1),
        )
    }

    val catalog = listOf(
        // Bulk material bundles for players who prefer buying over farming.
        ShopEntry(
            name   = stringResource(R.string.code_shop_bundle_name, boneName),
            body   = stringResource(R.string.code_shop_bones_body, 20, boneName),
            cost   = 50,
            reward = mapOf("elder_bone" to 20),
        ),
        ShopEntry(
            name   = stringResource(R.string.code_shop_bundle_name, woodName),
            body   = stringResource(R.string.code_shop_wood_body, 5, woodName),
            cost   = 100,
            reward = mapOf("sacred_wood" to 5),
        ),
        ShopEntry(
            name   = stringResource(R.string.code_shop_sigil_premium_name, sigilName),
            body   = stringResource(R.string.code_shop_sigil_premium_body, 1, sigilName),
            cost   = 500,
            reward = mapOf("ancient_sigil" to 1),
        ),
        // Sigil stones for the Embedder. These otherwise only come from rare enemy drops,
        // so the shop is where you go to fill a specific colour you want.
        stoneEntry(R.string.item_elder_ruby_name,     R.string.code_sigil_effect_coins,         300, "elder_ruby"),
        stoneEntry(R.string.item_elder_sapphire_name, R.string.code_sigil_effect_xp,            300, "elder_sapphire"),
        stoneEntry(R.string.item_elder_emerald_name,  R.string.code_sigil_effect_loot,          300, "elder_emerald"),
        stoneEntry(R.string.item_elder_topaz_name,    R.string.code_sigil_effect_ancient_sigil, 300, "elder_topaz"),
        stoneEntry(R.string.item_elder_amethyst_name, R.string.code_sigil_effect_elder_essence, 300, "elder_amethyst"),
        stoneEntry(R.string.item_elder_diamond_name,  R.string.code_sigil_effect_elder_bone,    500, "elder_diamond"),
    )
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.elder_isle_shop_title)) },
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            item {
                Surface(
                    shape    = MaterialTheme.shapes.medium,
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Text(
                            text  = stringResource(R.string.item_elder_essence_name),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text  = "${state.essence}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            items(catalog) { entry ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text       = entry.name,
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.weight(1f),
                            )
                            Text(
                                text  = stringResource(R.string.code_shop_cost, entry.cost),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text  = entry.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(Modifier.weight(1f))
                            Button(
                                onClick = { vm.buyBundle(entry.cost, entry.reward, entry.name) },
                                enabled = state.essence >= entry.cost,
                            ) { Text(stringResource(R.string.elder_isle_shop_buy)) }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
