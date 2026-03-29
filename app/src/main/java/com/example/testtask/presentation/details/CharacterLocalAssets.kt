package com.example.testtask.presentation.details

import androidx.annotation.DrawableRes
import com.example.testtask.R
import com.example.testtask.domain.model.CharacterDetails

@DrawableRes
fun CharacterDetails.localHeroDrawableRes(): Int? {
    when (id) {
        2 -> return R.drawable.c3po
        10 -> return R.drawable.obi_wan_kenobi
        11 -> return R.drawable.anakin_skywalker
        22 -> return R.drawable.boba_fett
        27 -> return R.drawable.ackbar
    }
    val n = name.lowercase()
    return when {
        n.contains("obi-wan") || n.contains("obi wan") -> R.drawable.obi_wan_kenobi
        n.contains("c-3po") || n.contains("c3po") || n.contains("threepio") -> R.drawable.c3po
        n.contains("anakin") -> R.drawable.anakin_skywalker
        n.contains("boba fett") || (n.contains("boba") && n.contains("fett")) -> R.drawable.boba_fett
        n.contains("ackbar") -> R.drawable.ackbar
        else -> null
    }
}
