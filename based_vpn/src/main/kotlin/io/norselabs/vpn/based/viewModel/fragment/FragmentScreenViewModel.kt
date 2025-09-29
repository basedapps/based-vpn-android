package io.norselabs.vpn.based.viewModel.fragment

import cafe.adriel.voyager.core.model.ScreenModel
import com.v2ray.ang.dto.FragmentConfig
import com.v2ray.ang.dto.FragmentPackets
import io.norselabs.vpn.based.viewModel.fragment.FragmentScreenEffect as Effect
import io.norselabs.vpn.v2ray.repo.V2RayRepository
import javax.inject.Inject

class FragmentScreenViewModel
@Inject constructor(
  val stateHolder: FragmentScreenStateHolder,
  private val v2Ray: V2RayRepository,
) : ScreenModel {

  private val state: FragmentScreenState
    get() = stateHolder.state.value

  fun setInEnabled(isEnabled: Boolean) {
    updateConfig { copy(isEnabled = isEnabled) }
  }

  fun setLengthMin(minLength: Int) {
    updateConfig { copy(minLength = minLength) }
  }

  fun setLengthMax(maxLength: Int) {
    updateConfig { copy(maxLength = maxLength) }
  }

  fun setIntervalMin(minInterval: Int) {
    updateConfig { copy(minInterval = minInterval) }
  }

  fun setIntervalMax(maxInterval: Int) {
    updateConfig { copy(maxInterval = maxInterval) }
  }

  fun setPackets(packets: FragmentPackets) {
    updateConfig { copy(packets = packets) }
  }

  private fun updateConfig(block: FragmentConfig.() -> FragmentConfig) {
    stateHolder.updateState {
      copy(config = state.config.block())
    }
  }

  fun saveConfig() {
    v2Ray.setFragmentConfig(state.config)
    onBackClick()
  }

  fun resetDefault() {
    val defaultConfig = FragmentConfig()
    stateHolder.updateState {
      copy(config = defaultConfig)
    }
    v2Ray.setFragmentConfig(defaultConfig)
    onBackClick()
  }

  fun onBackClick() {
    stateHolder.sendEffect(Effect.GoBack)
  }
}
