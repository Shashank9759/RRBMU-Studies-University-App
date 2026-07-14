@file:OptIn(org.jetbrains.compose.resources.InternalResourceApi::class)

package com.studies.rrbmustudies.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.InternalResourceApi

private object CommonMainFont0 {
  public val fraunces: FontResource by 
      lazy { init_fraunces() }

  public val inter: FontResource by 
      lazy { init_inter() }

  public val sora: FontResource by 
      lazy { init_sora() }
}

@InternalResourceApi
internal fun _collectCommonMainFont0Resources(map: MutableMap<String, FontResource>) {
  map.put("fraunces", CommonMainFont0.fraunces)
  map.put("inter", CommonMainFont0.inter)
  map.put("sora", CommonMainFont0.sora)
}

public val Res.font.fraunces: FontResource
  get() = CommonMainFont0.fraunces

private fun init_fraunces(): FontResource = org.jetbrains.compose.resources.FontResource(
  "font:fraunces",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/com.studies.rrbmustudies.resources/font/fraunces.ttf", -1, -1),
    )
)

public val Res.font.inter: FontResource
  get() = CommonMainFont0.inter

private fun init_inter(): FontResource = org.jetbrains.compose.resources.FontResource(
  "font:inter",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/com.studies.rrbmustudies.resources/font/inter.ttf", -1, -1),
    )
)

public val Res.font.sora: FontResource
  get() = CommonMainFont0.sora

private fun init_sora(): FontResource = org.jetbrains.compose.resources.FontResource(
  "font:sora",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/com.studies.rrbmustudies.resources/font/sora.ttf", -1, -1),
    )
)
