package com.wabadaba.dziennik.ui.timetable

import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.wabadaba.dziennik.R
import com.wabadaba.dziennik.ui.*
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractSectionableItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import org.joda.time.LocalDate
import org.joda.time.LocalTime

class LessonItem(header: LessonHeaderItem, timetableLesson: TimetableLesson)
    : AbstractSectionableItem<LessonItem.ViewHolder, LessonHeaderItem>(header) {

    val lesson = timetableLesson.lesson
    val event = timetableLesson.event

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LessonItem

        if (lesson != other.lesson) return false

        return true
    }

    override fun hashCode(): Int {
        return lesson.hashCode()
    }

    override fun getLayoutRes(): Int = R.layout.item_lesson

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<out IFlexible<*>>) = ViewHolder(view, adapter)

    override fun bindViewHolder(adapter: FlexibleAdapter<out IFlexible<*>>?, holder: ViewHolder, position: Int, payloads: MutableList<Any?>?) {

        val context = holder.itemView.context

        holder.number.text = lesson.lessonNumber.toString()
        holder.title.text = lesson.subject?.name

        holder.subtitle.text = lesson.teacher?.fullName()

        holder.classroom.text = lesson.entry?.classroom?.symbol

        if (lesson.canceled) {

            holder.title.disabled()
            holder.subtitle.disabled()
            holder.number.disabled()
            holder.classroom.disabled()

            holder.badgeIcon.secondary().setImageResource(R.drawable.ic_cancel_black_24dp)
            holder.badgeTitle.secondary().text = context.getString(R.string.canceled)

        } else {

            holder.title.primary()
            holder.subtitle.secondary()
            holder.number.primary()
            holder.classroom.secondary()

            when {
                event != null -> {
                    holder.badgeIcon.secondary().setImageResource(R.drawable.ic_event_black_24dp)
                    holder.badgeTitle.secondary().text = event.category?.name
                }
                lesson.substitution -> {
                    holder.badgeIcon.secondary().setImageResource(R.drawable.ic_swap_horiz_black_24dp)
                    holder.badgeTitle.secondary().text = context.getString(R.string.substitution)
                }
                else -> {
                    holder.badgeIcon.gone()
                    holder.badgeTitle.gone()
                }

            }
        }

        if (LocalDate.now() == lesson.date && LocalTime.now() in lesson.hourFrom..lesson.hourTo) {
            holder.title.setTypeface(holder.title.typeface, Typeface.BOLD)
        } else {
            holder.title.setTypeface(null, Typeface.NORMAL)
        }
    }

    class ViewHolder(view: View, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(view, adapter) {
        val number: TextView = view.findViewById(R.id.item_lesson_number)
        val title: TextView = view.findViewById(R.id.item_lesson_title)
        val subtitle: TextView = view.findViewById(R.id.item_lesson_subtitle)
        val classroom: TextView = view.findViewById(R.id.item_lesson_classroom)
        val badgeIcon: ImageView = view.findViewById(R.id.item_lesson_badge_icon)
        val badgeTitle: TextView = view.findViewById(R.id.item_lesson_badge_title)
    }
}