package com.virtue.habittracker.domain
import com.virtue.habittracker.domain.model.AuthOutcome
import com.virtue.habittracker.domain.model.User
import com.virtue.habittracker.domain.repository.AuthRepository
import com.virtue.habittracker.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
class RegistrationValidationTest {
    private class FakeRepository : AuthRepository {
        var registerCalled = false
        override val authState: Flow<User?> = emptyFlow()
        override suspend fun signIn(email: String, password: String) = AuthOutcome.Failure("not called")
        override suspend fun register(email: String, password: String): AuthOutcome {
            registerCalled = true
            return AuthOutcome.Success(User("test", email, null))
        }
        override suspend fun signInWithGoogle(idToken: String) = AuthOutcome.Failure("not called")
        override suspend fun sendPasswordReset(email: String) = Result.success(Unit)
        override suspend fun signOut() = Unit
    }
    private val fake = FakeRepository()
    private val useCase = RegisterUseCase(fake)
    @Test fun rejectsInvalidEmail() = runTest {
        assertTrue(useCase("not-an-email", "abc12345") is AuthOutcome.Failure)
        assertFalse(fake.registerCalled)
    }
    @Test fun rejectsPasswordWithoutNumber() = runTest {
        assertTrue(useCase("a@example.com", "abcdefgh") is AuthOutcome.Failure)
        assertFalse(fake.registerCalled)
    }
    @Test fun acceptsPasswordMatchingCurrentRules() = runTest {
        assertTrue(useCase("a@example.com", "abc12345") is AuthOutcome.Success)
        assertTrue(fake.registerCalled)
    }
}
