package io.norselabs.vpn.based.di

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.hilt.ScreenModelKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap
import io.norselabs.vpn.based.viewModel.cities.CitiesScreenViewModel
import io.norselabs.vpn.based.viewModel.countries.CountriesScreenViewModel
import io.norselabs.vpn.based.viewModel.dashboard.DashboardScreenViewModel
import io.norselabs.vpn.based.viewModel.settings.SettingsScreenViewModel
import io.norselabs.vpn.based.viewModel.split_tunneling.SplitTunnelingScreenViewModel

@Module
@InstallIn(ActivityComponent::class)
abstract class ViewModelsModule {

  @Binds
  @IntoMap
  @ScreenModelKey(DashboardScreenViewModel::class)
  internal abstract fun bindDashboardScreenViewModel(impl: DashboardScreenViewModel): ScreenModel

  @Binds
  @IntoMap
  @ScreenModelKey(SettingsScreenViewModel::class)
  internal abstract fun bindSettingsScreenViewModel(impl: SettingsScreenViewModel): ScreenModel

  @Binds
  @IntoMap
  @ScreenModelKey(CountriesScreenViewModel::class)
  internal abstract fun bindCountriesScreenViewModel(impl: CountriesScreenViewModel): ScreenModel

  @Binds
  @IntoMap
  @ScreenModelKey(CitiesScreenViewModel::class)
  internal abstract fun bindCitiesScreenViewModel(impl: CitiesScreenViewModel): ScreenModel

  @Binds
  @IntoMap
  @ScreenModelKey(SplitTunnelingScreenViewModel::class)
  internal abstract fun bindSplitTunnelingScreenViewModel(impl: SplitTunnelingScreenViewModel): ScreenModel
}
