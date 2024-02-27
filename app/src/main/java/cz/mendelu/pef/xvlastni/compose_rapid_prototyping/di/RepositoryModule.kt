package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.di

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.database.AppDao
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.database.AppRepositoryImpl
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.database.IAppRepository
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
    fun provideAppRepository(appDao: AppDao): IAppRepository
        = AppRepositoryImpl(appDao)
}