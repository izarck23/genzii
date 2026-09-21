package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthRepositoryTest {

    private lateinit var context: Context
    private lateinit var appPreferences: AppPreferences
    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        appPreferences = AppPreferences(context)
        database = AppDatabase.getInstance(context)
        authRepository = AuthRepository(
            context = context,
            appPreferences = appPreferences,
            userAccountDao = database.userAccountDao()
        )
    }

    @Test
    fun testSignInWithEmailSuccess() = runBlocking {
        val result = authRepository.signInWithEmail("test.scholar@genzii.app", "Password123!")
        assertTrue(result.isSuccess)

        val isLoggedIn = appPreferences.isLoggedInFlow.first()
        assertTrue(isLoggedIn)

        val email = appPreferences.userEmailFlow.first()
        assertEquals("test.scholar@genzii.app", email)

        val userEntity = database.userAccountDao().getUserByEmail("test.scholar@genzii.app")
        assertNotNull(userEntity)
        assertEquals("Test.scholar", userEntity?.name)
    }

    @Test
    fun testSignUpWithEmailSuccess() = runBlocking {
        val result = authRepository.signUpWithEmail("Jane Doe", "jane.doe@university.edu", "AcademicSecurePass123!")
        assertTrue(result.isSuccess)

        val isLoggedIn = appPreferences.isLoggedInFlow.first()
        assertTrue(isLoggedIn)

        val userName = appPreferences.userNameFlow.first()
        assertEquals("Jane Doe", userName)

        val userEntity = database.userAccountDao().getUserByEmail("jane.doe@university.edu")
        assertNotNull(userEntity)
        assertEquals("Jane Doe", userEntity?.name)
    }

    @Test
    fun testSignOutClearsSession() = runBlocking {
        authRepository.signInWithEmail("logout.test@genzii.app", "Password123!")
        assertTrue(appPreferences.isLoggedInFlow.first())

        val signOutResult = authRepository.signOut()
        assertTrue(signOutResult.isSuccess)
        val isLoggedIn = appPreferences.isLoggedInFlow.first()
        assertEquals(false, isLoggedIn)
    }
}
