package io.add.calendar.domain

import androidx.lifecycle.LiveData
import io.add.calendar.utils.Event

interface IAppStartChecker {
    val navigateToAppSetup: LiveData<Event<Unit>>
    var isFirstLaunched: Boolean
    fun gotoAppSetup()
}