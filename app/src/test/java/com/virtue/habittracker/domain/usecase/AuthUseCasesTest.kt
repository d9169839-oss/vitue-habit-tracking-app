package com.virtue.habittracker.domain.usecase

import com.virtue.habittracker.domain.model.AuthOutcome
import com.virtue.habittracker.domain.model.User
import com.virtue.habittracker.domain.repository.AuthRepository
import com.virtue.habittracker.domain.repository.AuthSessionCoordinator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUseCasesTest {

    private class FakeAuthRepository : AuthRepository {
        var returnOutcome: AuthOutcome = AuthOutcome.Success(User("u1", "test@example.com", "Test"))
        var deleteOutcome: Result<Unit> = Result.success(Unit)
        var lastEmail: String? = null
        var lastPassword: String? = null
        var lastToken: String? = null
        var signedOut: Boolean = false
        var deletedAccount: Boolean = false

        override val authState: Flow<User?> = emptyFlow()

        override suspend fun signIn(email: String, password: String): AuthOutcome {
            lastEmail = email
            lastPassword = password
            return returnOutcome
        }

        override suspend fun register(name: String, email: String, password: String): AuthOutcome {
            lastEmail = email
            lastPassword = password
            return returnOutcome
        }

        override suspend fun signInWithGoogle(idToken: String): AuthOutcome {
            lastToken = idToken
            return returnOutcome
        }

        override suspend fun sendPasswordReset(email: String): Result<Unit> = Result.success(Unit)

        override suspend fun deleteAccount(): Result<Unit> {
            deletedAccount = true
            return deleteOutcome
        }

        override suspend fun signOut() {
            signedOut = true
        }
    }

    private class RecordingSessionCoordinator : AuthSessionCoordinator {
        var bootstrappedUser: User? = null
        var beforeSignOutCalled = false
        var deleteAccountDataCalled = false
        var accountDeletedCalled = false

        override suspend fun onSessionStarted(user: User) {
            bootstrappedUser = user
        }

        override suspend fun onBeforeSignOut() {
            beforeSignOutCalled = true
        }

        override suspend fun onDeleteAccountData() {
            deleteAccountDataCalled = true
        }

        override suspend fun onAccountDeleted() {
            accountDeletedCalled = true
        }
    }

    @Test
    fun signIn_validationFailsOnBlankEmail_doesNotCallRepoOrCoordinator() = runTest {
        val repo = FakeAuthRepository()
        val coordinator = RecordingSessionCoordinator()
        val useCase = SignInUseCase(repo, coordinator)

        val outcome = useCase("   ", "password123")
        assertTrue(outcome is AuthOutcome.Failure)
        assertEquals(null, repo.lastEmail)
        assertEquals(null, coordinator.bootstrappedUser)
    }

    @Test
    fun signIn_validationFailsOnBlankPassword() = runTest {
        val repo = FakeAuthRepository()
        val coordinator = RecordingSessionCoordinator()
        val useCase = SignInUseCase(repo, coordinator)

        val outcome = useCase("valid@example.com", "   ")
        assertTrue(outcome is AuthOutcome.Failure)
        assertEquals(null, coordinator.bootstrappedUser)
    }

    @Test
    fun signIn_authFailure_doesNotTriggerSessionCoordinator() = runTest {
        val repo = FakeAuthRepository().apply {
            returnOutcome = AuthOutcome.Failure("Wrong password")
        }
        val coordinator = RecordingSessionCoordinator()
        val useCase = SignInUseCase(repo, coordinator)

        val outcome = useCase("valid@example.com", "wrongpass")
        assertTrue(outcome is AuthOutcome.Failure)
        assertEquals(null, coordinator.bootstrappedUser)
    }

    @Test
    fun signIn_success_triggersSessionCoordinator() = runTest {
        val testUser = User("u-42", "valid@example.com", "User 42")
        val repo = FakeAuthRepository().apply {
            returnOutcome = AuthOutcome.Success(testUser)
        }
        val coordinator = RecordingSessionCoordinator()
        val useCase = SignInUseCase(repo, coordinator)

        val outcome = useCase("valid@example.com", "correctpass")
        assertTrue(outcome is AuthOutcome.Success)
        assertEquals(testUser, coordinator.bootstrappedUser)
    }

    @Test
    fun register_success_triggersSessionCoordinator() = runTest {
        val testUser = User("new-id", "new@example.com", "New User")
        val repo = FakeAuthRepository().apply {
            returnOutcome = AuthOutcome.Success(testUser)
        }
        val coordinator = RecordingSessionCoordinator()
        val useCase = RegisterUseCase(repo, coordinator)

        val outcome = useCase("New User", "new@example.com", "validPass1")
        assertTrue(outcome is AuthOutcome.Success)
        assertEquals(testUser, coordinator.bootstrappedUser)
    }

    @Test
    fun googleSignIn_blankToken_doesNotCallRepo() = runTest {
        val repo = FakeAuthRepository()
        val coordinator = RecordingSessionCoordinator()
        val useCase = GoogleSignInUseCase(repo, coordinator)

        val outcome = useCase("")
        assertTrue(outcome is AuthOutcome.Failure)
        assertEquals(null, repo.lastToken)
        assertEquals(null, coordinator.bootstrappedUser)
    }

    @Test
    fun googleSignIn_success_triggersSessionCoordinator() = runTest {
        val testUser = User("g-100", "google@example.com", "Google User")
        val repo = FakeAuthRepository().apply {
            returnOutcome = AuthOutcome.Success(testUser)
        }
        val coordinator = RecordingSessionCoordinator()
        val useCase = GoogleSignInUseCase(repo, coordinator)

        val outcome = useCase("valid_token_xyz")
        assertTrue(outcome is AuthOutcome.Success)
        assertEquals("valid_token_xyz", repo.lastToken)
        assertEquals(testUser, coordinator.bootstrappedUser)
    }

    @Test
    fun signOut_flushesPendingDataBeforeSigningOut() = runTest {
        val repo = FakeAuthRepository()
        val coordinator = RecordingSessionCoordinator()
        val useCase = SignOutUseCase(repo, coordinator)

        useCase()

        assertTrue(coordinator.beforeSignOutCalled)
        assertTrue(repo.signedOut)
    }

    @Test
    fun deleteAccount_success_deletesDataAndAuthAndClearsLocal() = runTest {
        val repo = FakeAuthRepository()
        val coordinator = RecordingSessionCoordinator()
        val useCase = DeleteAccountUseCase(repo, coordinator)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertTrue(coordinator.deleteAccountDataCalled)
        assertTrue(repo.deletedAccount)
        assertTrue(coordinator.accountDeletedCalled)
    }

    @Test
    fun deleteAccount_authFailure_doesNotClearLocalState() = runTest {
        val repo = FakeAuthRepository().apply {
            deleteOutcome = Result.failure(IllegalStateException("REQUIRES_RECENT_LOGIN"))
        }
        val coordinator = RecordingSessionCoordinator()
        val useCase = DeleteAccountUseCase(repo, coordinator)

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(coordinator.deleteAccountDataCalled)
        assertTrue(repo.deletedAccount)
        assertFalse(coordinator.accountDeletedCalled)
    }
}
