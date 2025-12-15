
package com.example.rankkings.data.preferences
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "welcome_prefs")

class WelcomePreferences(private val context: Context) {

    private fun keyForUser(userId: Int) =
        booleanPreferencesKey("welcome_shown_user_$userId")

    suspend fun hasSeenWelcome(userId: Int): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[keyForUser(userId)] ?: false
    }

    suspend fun setWelcomeSeen(userId: Int) {
        context.dataStore.edit { prefs ->
            prefs[keyForUser(userId)] = true
        }
    }
}
