package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi
        = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi): Retrofit
        = Retrofit.Builder()
        .baseUrl("https://petstore.swagger.io/v2/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
}