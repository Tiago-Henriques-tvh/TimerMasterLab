package com.tiagohenriques.project.timermasterlab.navigation

import androidx.annotation.StringRes
import com.tiagohenriques.project.timermasterlab.R

enum class TimerMasterLabAppScreens(
    @get:StringRes val title: Int,
) {
    Home(title = R.string.my_home),
    GroupRace(title = R.string.group_race),
    TimeTrial(title = R.string.time_trial),
    StartList(title = R.string.start_list),
    BeginTimeTrial(title = R.string.begin_time_trial),
    SessionResults(title = R.string.session_results),
    Settings(title = R.string.settings),
    ;

    companion object {
        fun fromRoute(route: String?): TimerMasterLabAppScreens = entries.find { it.name == route } ?: Home
    }
}
