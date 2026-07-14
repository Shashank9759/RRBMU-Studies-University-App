@file:OptIn(org.jetbrains.compose.resources.InternalResourceApi::class)

package com.studies.rrbmustudies.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi

private object CommonMainDrawable0 {
  public val univ_logo: DrawableResource by 
      lazy { init_univ_logo() }
}

@InternalResourceApi
internal fun _collectCommonMainDrawable0Resources(map: MutableMap<String, DrawableResource>) {
  map.put("univ_logo", CommonMainDrawable0.univ_logo)
}

public val Res.drawable.univ_logo: DrawableResource
  get() = CommonMainDrawable0.univ_logo

private fun init_univ_logo(): DrawableResource = org.jetbrains.compose.resources.DrawableResource(
  "drawable:univ_logo",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/com.studies.rrbmustudies.resources/drawable/univ_logo.png", -1, -1),
    )
)
