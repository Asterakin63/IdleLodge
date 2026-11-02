package com.idlelodge.ui.screen

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.idlelodge.R
import com.idlelodge.util.GameStrings
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Replication for upstream #1888: the Vigilance (Slayer) shop showed English
 * `ATK`/`STR`/`DEF` and `Defense 40` under a non-English in-app language,
 * even though all translations exist. Checked here against French.
 *
 * `ShopRow` hardcoded `"ATK"/"STR"/"DEF"` and built requirements with
 * `"${skill.replaceFirstChar { it.uppercase() }}"` (locale-blind English).
 */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE, sdk = [34])
class SlayerShopLabelsTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    @Config(manifest = Config.NONE, sdk = [34], qualifiers = "fr")
    fun `stat format resources render French abbreviations`() {
        // The resources the fix relies on (same pattern as CarnivalScreen).
        val frContext: Context = ApplicationProvider.getApplicationContext()
        assertEquals("RIG +5", frContext.getString(R.string.carnival_stat_atk, 5))
        assertEquals("FOR +5", frContext.getString(R.string.carnival_stat_str, 5))
        assertEquals("TEM +5", frContext.getString(R.string.carnival_stat_def, 5))
    }

    @Test
    @Config(manifest = Config.NONE, sdk = [34], qualifiers = "fr")
    fun `skillName resolves requirement skills in French`() {
        val frContext: Context = ApplicationProvider.getApplicationContext()
        assertEquals("Tempérance", GameStrings.skillName(frContext, "defense"))
        assertEquals("Rigueur", GameStrings.skillName(frContext, "attack"))
        assertEquals("Force", GameStrings.skillName(frContext, "strength"))
    }

    @Test
    @Config(manifest = Config.NONE, sdk = [34], qualifiers = "fr")
    fun `formatSlayerRequirements localizes skill names in French`() {
        val frContext: Context = ApplicationProvider.getApplicationContext()
        val text = formatSlayerRequirements(
            mapOf("defense" to 40, "attack" to 10),
        ) { GameStrings.skillName(frContext, it) }
        // Buggy logic yields "Defense 40, Attack 10".
        assertEquals("Tempérance 40, Rigueur 10", text)
    }

    @Test
    @Config(manifest = Config.NONE, sdk = [34], qualifiers = "fr")
    fun `slayer_requires wrapper is French`() {
        val frContext: Context = ApplicationProvider.getApplicationContext()
        assertEquals(
            "Nécessite : Tempérance 40",
            frContext.getString(R.string.slayer_requires, "Tempérance 40"),
        )
    }
}
