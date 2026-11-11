package com.virtue.habittracker.presentation.auth

import com.virtue.habittracker.domain.model.AuthOutcome
import com.virtue.habittracker.domain.model.User
import com.virtue.habittracker.domain.repository.AuthRepository
import com.virtue.habittracker.domain.repository.AuthSessionCoordinator
import com.virtue.habittracker.domain.usecase.GoogleSignInUseCase
import com.virtue.habittracker.domain.usecase.RegisterUseCase
import com.virtue.habittracker.domain.usecase.SendPasswordResetUseCase
import com.virtue.habittracker.domain.usecase.SignInUseCase
import com.virtue.habittracker.domain.usecase.SignOutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private class FakeAuthRepository : AuthRepository {
        var returnOutcome: AuthOutcome = AuthOutcome.Success(User("1", "test@test.com", "Test"))
        var resetResult: Result<Unit> = Result.success(Unit)
        var signedOut = false

        override val authState: Flow<User?> = emptyFlow()
        override suspend fun signIn(email: String, password: String): AuthOutcome = returnOutcome
        override suspend fun register(name: String, email: String, password: String): AuthOutcome = returnOutcome
        override suspend fun signInWithGoogle(idToken: String): AuthOutcome = returnOutcome
        override suspend fun sendPasswordReset(email: String): Result<Unit> = resetResult
        override suspend fun signOut() { signedOut = true }
    }

    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var sessionCoordinator: AuthSessionCoordinator
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        sessionCoordinator = AuthSessionCoordinator { }
        viewModel = AuthViewModel(
            signInUseCase = SignInUseCase(fakeRepository, sessionCoordinator),
            registerUseCase = RegisterUseCase(fakeRepository, sessionCoordinator),
            googleSignInUseCase = GoogleSignInUseCase(fakeRepository, sessionCoordinator),
            sendPasswordResetUseCase = SendPasswordResetUseCase(fakeRepository),
            signOutUseCase = SignOutUseCase(fakeRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun passwordMismatchOnRegister_setsErrorMessageWithoutCallingUseCase() = runTest {
        viewModel.onEmailChanged("test@test.com")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("different123")

        viewModel.register()

        assertEquals("Passwords do not match.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun successfulSignIn_emitsAuthenticatedEventAndClearsPasswords() = runTest {
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("validPassword1")

        var receivedEvent: AuthEvent? = null
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { receivedEvent = it }
        }

        viewModel.signIn()
        testScheduler.advanceUntilIdle()

        assertEquals(AuthEvent.Authenticated, receivedEvent)
        assertEquals("", viewModel.uiState.value.password)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        job.cancel()
    }

    @Test
    fun failedSignIn_updatesErrorMessage() = runTest {
        fakeRepository.returnOutcome = AuthOutcome.Failure("Invalid credentials.")
        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("wrongPass1")

        viewModel.signIn()
        testScheduler.advanceUntilIdle()

        assertEquals("Invalid credentials.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun sendPasswordReset_success_updatesInfoMessage() = runTest {
        viewModel.onEmailChanged("user@example.com")
        viewModel.sendPasswordReset()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.infoMessage?.contains("reset email") == true)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun sendPasswordReset_failure_updatesErrorMessage() = runTest {
        fakeRepository.resetResult = Result.failure(RuntimeException("User not found"))
        viewModel.onEmailChanged("unknown@example.com")
        viewModel.sendPasswordReset()
        testScheduler.advanceUntilIdle()

        assertEquals("User not found", viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.infoMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun googleCredentialProvider_canBeFakedForScreenTesting() = runTest {
        val fakeProvider = GoogleCredentialProvider { "mock_google_id_token_123" }
        val token = fakeProvider.getIdToken()
        assertEquals("mock_google_id_token_123", token)

        var receivedEvent: AuthEvent? = null
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { receivedEvent = it }
        }

        viewModel.signInWithGoogle(token)
        testScheduler.advanceUntilIdle()

        assertEquals(AuthEvent.Authenticated, receivedEvent)
        job.cancel()
    }
}
