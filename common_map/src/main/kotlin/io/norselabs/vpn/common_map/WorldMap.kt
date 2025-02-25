package io.norselabs.vpn.common_map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.tan
import kotlinx.coroutines.launch

@Composable
fun WorldMap(
  lat: Double,
  long: Double,
  mapColor: Color,
  dotColor: Color,
  modifier: Modifier = Modifier,
  dotSize: Dp = 6.dp,
  mapScale: Float = 1.5f,
) {
  val coordinates = remember { mutableStateOf(0f to 0f) }

  LaunchedEffect(lat, long) {
    coordinates.value = getCoordinates(latitude = lat, longitude = long)
  }

  val radiusPx = with(LocalDensity.current) { dotSize.toPx() }

  val imgSize = remember { mutableStateOf(Size.Zero) }
  val viewSize = remember { mutableStateOf(Size.Zero) }
  val scale = remember { mutableFloatStateOf(1f) }

  val pointOffset = remember {
    derivedStateOf {
      if (coordinates.value.first == 0f || coordinates.value.second == 0f) {
        Offset.Zero
      } else {
        // space from screen start to unscaled map start
        val leftSpace = (viewSize.value.width - imgSize.value.width) / 2
        val topSpace = (viewSize.value.height - imgSize.value.height) / 2

        // coordinate offset
        val xOffset = imgSize.value.width * coordinates.value.first
        val yOffset = imgSize.value.height * coordinates.value.second
        Offset(
          x = leftSpace + xOffset,
          y = topSpace + yOffset,
        )
      }
    }
  }

  val transX = rememberSaveable(saver = screenAnimationSaver()) { Animatable(0f) }
  val transY = rememberSaveable(saver = screenAnimationSaver()) { Animatable(0f) }

  LaunchedEffect(pointOffset.value) {
    if (pointOffset.value == Offset.Zero) return@LaunchedEffect
    val point = pointOffset.value
    val image = imgSize.value
    val view = viewSize.value
    val scaleFactor = scale.floatValue

    // delta from screen center to point
    val pointDx = (view.width / 2 - point.x) * scaleFactor
    val pointDy = (view.height / 2 - point.y) * scaleFactor

    // max delta to avoid moving out of map bounds
    val maxDx = (image.width * scaleFactor - view.width) / 2
    val maxDy = (image.height * scaleFactor - view.height) / 2

    val dx = min(abs(pointDx), maxDx) * pointDx.sign
    val dy = min(abs(pointDy), maxDy) * pointDy.sign

    launch { transX.animateTo(targetValue = dx, animationSpec = tween(500)) }
    launch { transY.animateTo(targetValue = dy, animationSpec = tween(500)) }
  }

  Image(
    colorFilter = ColorFilter.tint(mapColor),
    painter = painterResource(R.drawable.img_map),
    contentDescription = null,
    contentScale = object : ContentScale {
      override fun computeScaleFactor(srcSize: Size, dstSize: Size): ScaleFactor {
        // scale to fit map in the box
        val initialWidthScale = dstSize.height / srcSize.height
        val initialHeightScale = dstSize.width / srcSize.width
        val initialScale = min(initialWidthScale, initialHeightScale)
        // scale to fix the min dimension
        val additionalWidthScale = dstSize.width / srcSize.width / initialScale
        val additionalHeightScale = dstSize.height / srcSize.height / initialScale
        val additionalScale = max(additionalWidthScale, additionalHeightScale)
        scale.floatValue = additionalScale * mapScale

        viewSize.value = dstSize
        imgSize.value = Size(srcSize.width * initialScale, srcSize.height * initialScale)
        return ScaleFactor(initialScale, initialScale)
      }
    },
    modifier = modifier
      .clipToBounds() // Prevent the view from drawing outside its bounding box
      .graphicsLayer {
        scaleX = scale.floatValue
        scaleY = scale.floatValue
        translationX = transX.value
        translationY = transY.value
      }
      .drawWithContent {
        drawContent()
        val offset = pointOffset.value
        if (offset != Offset.Zero) {
          drawCircle(
            color = dotColor.copy(alpha = 0.2f),
            radius = radiusPx * 3 / scale.floatValue,
            center = offset,
          )
          drawCircle(
            color = dotColor,
            radius = radiusPx / scale.floatValue,
            center = offset,
          )
        }
      },
  )
}

private fun getCoordinates(latitude: Double, longitude: Double): Pair<Float, Float> {

  val mapWidthPx = 1068
  val mapHeightPx = 704
  val zeroX = 0.4981273408
  val zeroY = 0.6960227273
  val latMin = -58.55
  val latMax = 83.62
  val lonMin = -180.0
  val lonMax = 180.0

  val y = when {
    latitude > 0 -> {
      (mapHeightPx * zeroY) * (1 - (normalizeLatitude(latitude) / normalizeLatitude(latMax)))
    }

    latitude < 0 -> {
      (mapHeightPx * zeroY) + (
        (mapHeightPx - (mapHeightPx * zeroY)) * (normalizeLatitude(latitude) / normalizeLatitude(latMin))
        )
    }

    else -> 0.0
  }

  val x = when {
    longitude > 0 -> (mapWidthPx * zeroX) + ((mapWidthPx - (mapWidthPx * zeroX)) * (longitude / lonMax))
    longitude < 0 -> (mapWidthPx * zeroX) - ((mapWidthPx * zeroX) * (longitude / lonMin))
    else -> 0.0
  }

  return (x.toFloat() / mapWidthPx) to (y.toFloat() / mapHeightPx)
}

private fun normalizeLatitude(lat: Double): Double {
  return ln(tan(Math.PI / 4 + (lat * Math.PI / 180) / 2))
}

private fun screenAnimationSaver(): Saver<Animatable<Float, *>, Float> {
  return Saver(
    save = { it.value },
    restore = { Animatable(it) },
  )
}
