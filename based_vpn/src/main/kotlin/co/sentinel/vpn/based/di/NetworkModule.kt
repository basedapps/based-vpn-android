package co.sentinel.vpn.based.di

import android.content.SharedPreferences
import co.sentinel.vpn.based.app_config.AppConfig
import co.sentinel.vpn.based.network.Api
import co.sentinel.vpn.based.network.ConnectApi
import co.sentinel.vpn.based.network.HeadersInterceptor
import co.sentinel.vpn.based.network.repository.AppRepository
import co.sentinel.vpn.based.network.repository.AppRepositoryImpl
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
    config: AppConfig,
    prefs: SharedPreferences,
  ): HeadersInterceptor = HeadersInterceptor(config, prefs)

  @Provides
  @Singleton
  fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply {
      setLevel(HttpLoggingInterceptor.Level.BODY)
    }
  }

  @Provides
  @Singleton
  fun provideOkHttp(
    headersInterceptor: HeadersInterceptor,
    loggingInterceptor: HttpLoggingInterceptor,
  ): OkHttpClient {
    return OkHttpClient.Builder()
      .addInterceptor(headersInterceptor)
      .addInterceptor(loggingInterceptor)
      .build()
  }

  @Provides
  @Singleton
  fun provideRetrofit(
    client: OkHttpClient,
    config: AppConfig,
  ): Retrofit = Retrofit.Builder()
    .baseUrl(config.getBaseUrl())
    .addConverterFactory(GsonConverterFactory.create())
    .client(client)
    .build()

  @Provides
  @Singleton
  fun provideApi(retrofit: Retrofit): Api = retrofit.create(Api::class.java)

  @Provides
  @Singleton
  fun provideConnectApi(
    config: AppConfig,
    headersInterceptor: HeadersInterceptor,
    loggingInterceptor: HttpLoggingInterceptor,
  ): ConnectApi {

    val client = OkHttpClient.Builder()
      .connectTimeout(60, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .addInterceptor(headersInterceptor)
      .addInterceptor(loggingInterceptor)
      .build()

    val retrofit = Retrofit.Builder()
      .baseUrl(config.getBaseUrl())
      .addConverterFactory(GsonConverterFactory.create())
      .client(client)
      .build()

    return retrofit.create(ConnectApi::class.java)
  }

  @Provides
  @Singleton
  fun provideRepository(
    api: Api,
    connectApi: ConnectApi,
    client: OkHttpClient,
  ): AppRepository = AppRepositoryImpl(api, connectApi, client)
}
