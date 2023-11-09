package co.uk.basedapps.vpn.di

import android.content.SharedPreferences
import co.uk.basedapps.vpn.common.provider.AppDetailsProvider
import co.uk.basedapps.vpn.network.Api
import co.uk.basedapps.vpn.network.HeadersInterceptor
import co.uk.basedapps.vpn.network.repository.BasedRepository
import co.uk.basedapps.vpn.network.repository.BasedRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

  @Provides
  @Singleton
  fun provideHeadersInterceptor(
    prefs: SharedPreferences,
  ): HeadersInterceptor = HeadersInterceptor(prefs)

  @Provides
  @Singleton
  fun provideOkHttp(
    headersInterceptor: HeadersInterceptor,
  ): OkHttpClient {
    val interceptor = HttpLoggingInterceptor()
    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
    return OkHttpClient.Builder()
      .connectTimeout(60, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .addInterceptor(headersInterceptor)
      .addInterceptor(interceptor)
      .build()
  }

  @Provides
  @Singleton
  fun provideRetrofit(
    client: OkHttpClient,
    provider: AppDetailsProvider,
  ): Retrofit =
    Retrofit.Builder()
      .baseUrl(provider.getBaseUrl())
      .addConverterFactory(GsonConverterFactory.create())
      .client(client)
      .build()

  @Provides
  @Singleton
  fun provideApi(retrofit: Retrofit): Api =
    retrofit.create(Api::class.java)

  @Provides
  @Singleton
  fun provideRepository(
    api: Api,
    client: OkHttpClient,
  ): BasedRepository = BasedRepositoryImpl(api, client)
}
