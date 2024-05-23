package co.sentinel.vpn.based.di

import android.content.Context
import co.sentinel.vpn.v2ray.repo.V2RayRepository
import co.sentinel.vpn.v2ray.repo.V2RayRepositoryImpl
import co.sentinel.vpn.v2ray.store.V2RayUserPreferenceStore
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
  fun provideWireguardUserPreferenceStore(@ApplicationContext context: Context): V2RayUserPreferenceStore =
    V2RayUserPreferenceStore(context)

  @Provides
  @Singleton
  fun provideV2RayRepository(
    @ApplicationContext context: Context,
    userPreferenceStore: V2RayUserPreferenceStore,
  ): V2RayRepository = V2RayRepositoryImpl(context, userPreferenceStore)
}
