@file:OptIn(org.jetbrains.compose.resources.InternalResourceApi::class)

package rrbmustudies.composeapp.generated.resources

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

internal val Res.drawable.univ_logo: DrawableResource
  get() = CommonMainDrawable0.univ_logo

private fun init_univ_logo(): DrawableResource = org.jetbrains.compose.resources.DrawableResource(
  "drawable:univ_logo",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/rrbmustudies.composeapp.generated.resources/drawable/univ_logo.png", -1, -1),
    )
)
