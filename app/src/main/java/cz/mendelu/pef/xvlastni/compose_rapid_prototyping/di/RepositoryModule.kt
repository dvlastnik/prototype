package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.di

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication.IPetsRemoteRepository
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication.PetsAPI
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication.PetsRemoteRepositoryImpl
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
    fun providePetsRemoteRepository(petsAPI: PetsAPI): IPetsRemoteRepository
        = PetsRemoteRepositoryImpl(petsAPI)
}