package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.di

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication.BoredAPI
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication.BoredRemoteRepositoryImpl
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication.IBoredRemoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideBoredRemoteRepository(boredAPI: BoredAPI): IBoredRemoteRepository
        = BoredRemoteRepositoryImpl(boredAPI)
}