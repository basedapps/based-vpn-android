package co.uk.basedapps.vpn.ui.screens.dashboard.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings

@OptIn(MapboxExperimental::class)
@Composable
fun MapboxConfiguredMap(
  modifier: Modifier = Modifier,
  mapViewportState: MapViewportState,
) {
  MapboxMap(
    modifier = modifier,
    mapViewportState = mapViewportState,
    gesturesSettings = GesturesSettings {
      pitchEnabled = false
      scrollEnabled = false
      pinchToZoomEnabled = false
      rotateEnabled = false
      doubleTapToZoomInEnabled = false
      doubleTouchToZoomOutEnabled = false
      quickZoomEnabled = false
    },
    compass = {},
    scaleBar = {},
    attribution = {},
  )
}
