package uk.co.inkbinder.noto.navigation

import androidx.annotation.StringRes
import uk.co.inkbinder.noto.R

sealed class NotoDestination(
    val route: String,
    @param:StringRes val labelRes: Int? = null,
) {
    data object Home : NotoDestination("home", R.string.nav_home)
    data object Tags : NotoDestination("tags", R.string.nav_tags)
    data object Settings : NotoDestination("settings", R.string.nav_settings)
    data object DayDetail : NotoDestination("day/{date}") {
        fun routeFor(date: String): String = "day/$date"
    }
}

val topLevelDestinations = listOf(
    NotoDestination.Home,
    NotoDestination.Tags,
    NotoDestination.Settings,
)
