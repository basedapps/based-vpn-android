package io.norselabs.vpn.common_compose

import androidx.compose.animation.Animatable
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.animateBackground(getColor: () -> Color): Modifier {
  val color = getColor()
  val colorState = remember { Animatable(color) }
  LaunchedEffect(color) {
    colorState.animateTo(color)
  }
  return this.background(color = colorState.value)
}
