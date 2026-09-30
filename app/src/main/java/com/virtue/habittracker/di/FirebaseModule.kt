package com.virtue.habittracker.di
import com.google.firebase.auth.FirebaseAuth
import com.virtue.habittracker.data.repository.FirebaseAuthRepository
import com.virtue.habittracker.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object FirebaseProviders {
    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindings {
    @Binds @Singleton
    abstract fun bindAuthRepository(implementation: FirebaseAuthRepository): AuthRepository
}
