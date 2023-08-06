package co.uk.basedapps.vpn.di

import android.content.Context
import co.uk.basedapps.domain_v2ray.V2RayRepository
import co.uk.basedapps.domain_v2ray.V2RayRepositoryImpl
import co.uk.basedapps.domain_v2ray.core.V2RayUserPreferenceStore
import co.uk.basedapps.domain_v2ray.core.V2RayUserPreferenceStoreImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class V2RayModule {

  @Provides
  @Singleton
  fun provideWireguardUserPreferenceStore(
    @ApplicationContext context: Context,
  ): V2RayUserPreferenceStore = V2RayUserPreferenceStoreImpl(context)

  @Provides
  @Singleton
  fun provideV2RayRepository(
    @ApplicationContext context: Context,
    userPreferenceStore: V2RayUserPreferenceStore,
  ): V2RayRepository = V2RayRepositoryImpl(context, userPreferenceStore)
}
