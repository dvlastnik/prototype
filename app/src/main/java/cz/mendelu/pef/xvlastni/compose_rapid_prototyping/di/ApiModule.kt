package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.di

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication.BoredAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun provideBoredApi(retrofit: Retrofit): BoredAPI
        = retrofit.create(BoredAPI::class.java)
}