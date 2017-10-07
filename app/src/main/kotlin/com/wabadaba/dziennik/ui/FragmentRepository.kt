package com.wabadaba.dziennik.ui

import android.content.Context
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import com.wabadaba.dziennik.R
import com.wabadaba.dziennik.ui.announcements.AnnouncementsFragment
import com.wabadaba.dziennik.ui.attendance.AttendancesFragment
import com.wabadaba.dziennik.ui.events.EventsFragment
import com.wabadaba.dziennik.ui.grades.GradesFragment
import com.wabadaba.dziennik.ui.timetable.TimetableFragment
import kotlin.reflect.KClass

class FragmentRepository(val context: Context) {

    var currentFragment: FragmentInfo

    val mainFragments = listOf(
            FragmentInfo("fragment_timetable", 78, TimetableFragment::class, R.string.timetable, R.drawable.ic_event_note_black_24dp),
            FragmentInfo("fragment_grades", 79, GradesFragment::class, R.string.grades, R.drawable.ic_assignment_black_24dp),
            FragmentInfo("fragment_events", 80, EventsFragment::class, R.string.agenda, R.drawable.ic_event_black_24dp),
            FragmentInfo("fragment_announcements", 82, AnnouncementsFragment::class, R.string.announcements, R.drawable.ic_announcement_black_24dp),
            FragmentInfo("fragment_attendances", 81, AttendancesFragment::class, R.string.attendances, R.drawable.ic_person_outline_black_24dp))

    val settingsFragment = FragmentInfo("fragment_settings", 81, SettingsFragment::class, R.string.settings, R.drawable.ic_settings_black_24dp)

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val defaultFragmentId = prefs.getString("defaultFragment", "fragment_timetable")
        currentFragment = mainFragments.singleOrNull { it.fragmentId == defaultFragmentId }
                ?: throw IllegalArgumentException("0 or multiple mainFragments with fragmentId $defaultFragmentId")
    }
}

data class FragmentInfo(
        val fragmentId: String,
        val drawerId: Long,
        val kClass: KClass<out Fragment>,
        val title: Int,
        val icon: Int
)