package co.uk.basedapps.vpn.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

  @Provides
  @Singleton
  fun provideSharedPreferences(
    @ApplicationContext context: Context,
  ): SharedPreferences =
    context.getSharedPreferences("co.uk.basedapps.vpn", Context.MODE_PRIVATE)

  @Provides
  @Singleton
  fun provideGson() = Gson()
}
