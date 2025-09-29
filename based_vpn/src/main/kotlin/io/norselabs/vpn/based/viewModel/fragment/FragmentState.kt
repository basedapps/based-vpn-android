package io.norselabs.vpn.based.viewModel.fragment

import com.v2ray.ang.dto.FragmentConfig
import io.norselabs.vpn.common.state.ViewStateHolder
import io.norselabs.vpn.v2ray.repo.V2RayRepository
import javax.inject.Inject

class FragmentScreenStateHolder
@Inject constructor(
  v2Ray: V2RayRepository,
) : ViewStateHolder<FragmentScreenState, FragmentScreenEffect>(
  FragmentScreenState(
    config = v2Ray.getFragmentConfig(),
  ),
)

data class FragmentScreenState(
  val config: FragmentConfig,
)

sealed interface FragmentScreenEffect {
  data object GoBack : FragmentScreenEffect
}
