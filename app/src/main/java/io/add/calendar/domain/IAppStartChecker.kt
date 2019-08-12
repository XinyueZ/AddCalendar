package io.add.calendar.domain

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.add.calendar.utils.Event

interface IAppStartChecker {
    val navigateToAppSetup: LiveData<Event<Unit>>
    var isFirstLaunched: Boolean
    fun gotoAppSetup()
}

const val IS_FIRST_LAUNCHED = "io.add.calendar.domain.AppStartChecker.IS_FIRST_LAUNCHED"

class AppStartChecker(context: Context) : IAppStartChecker {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(IS_FIRST_LAUNCHED, Context.MODE_PRIVATE)

    private val _navigateToAppSetup = MutableLiveData<Event<Unit>>()
    override val navigateToAppSetup: LiveData<Event<Unit>> = _navigateToAppSetup

    override fun gotoAppSetup() {
        _navigateToAppSetup.value = Event(Unit)
    }

    override var isFirstLaunched: Boolean
        get() = preferences.getBoolean(IS_FIRST_LAUNCHED, true)
        set(value) {
            preferences.edit {
                putBoolean(IS_FIRST_LAUNCHED, value)
            }
        }
}
