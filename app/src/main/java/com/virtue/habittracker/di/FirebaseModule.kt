package com.virtue.habittracker.di
import android.content.Context
import androidx.room.Room
import com.virtue.habittracker.data.local.HabitDao
import com.virtue.habittracker.data.local.HabitDatabase
import com.virtue.habittracker.data.local.ProgramDao
import com.virtue.habittracker.domain.service.ProgramPersonalizationEngine
import com.virtue.habittracker.data.repository.RoomProgramRepository
import com.virtue.habittracker.domain.repository.ProgramRepository
import com.virtue.habittracker.domain.repository.PremiumEntitlementProvider
import com.virtue.habittracker.data.billing.PremiumBillingManager
import com.virtue.habittracker.data.repository.RoomHabitRepository
import com.virtue.habittracker.domain.repository.HabitRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideHabitDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): HabitDatabase =
        Room.databaseBuilder(context, HabitDatabase::class.java, "vitue_habits.db")
            .addMigrations(HabitDatabase.MIGRATION_1_2, HabitDatabase.MIGRATION_2_3, HabitDatabase.MIGRATION_3_4)
            .build()
    @Provides
    fun provideHabitDao(database: HabitDatabase): HabitDao = database.habitDao()

    @Provides
    fun provideProgramDao(database: HabitDatabase): ProgramDao = database.programDao()

    @Provides @Singleton
    fun provideProgramPersonalizationEngine(): ProgramPersonalizationEngine = ProgramPersonalizationEngine()
    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // Keep a single Firestore client for the lifetime of the application.
    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindings {
    @Binds @Singleton
    abstract fun bindAuthRepository(implementation: FirebaseAuthRepository): AuthRepository
    @Binds @Singleton
    abstract fun bindHabitRepository(implementation: RoomHabitRepository): HabitRepository

    @Binds @Singleton
    abstract fun bindProgramRepository(implementation: RoomProgramRepository): ProgramRepository

    @Binds @Singleton
    abstract fun bindPremiumEntitlementProvider(implementation: PremiumBillingManager): PremiumEntitlementProvider
}
