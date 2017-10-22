package com.wabadaba.dziennik.ui.grades

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wabadaba.dziennik.MainApplication
import com.wabadaba.dziennik.R
import com.wabadaba.dziennik.di.ViewModelFactory
import com.wabadaba.dziennik.ui.DetailsDialogBuilder
import com.wabadaba.dziennik.ui.fullName
import com.wabadaba.dziennik.vo.Grade
import com.wabadaba.dziennik.vo.Subject
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible
import kotlinx.android.synthetic.main.fragment_grades.*
import mu.KotlinLogging
import javax.inject.Inject


class GradesFragment : Fragment() {

    @Inject lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: GradesViewModel

    private val logger = KotlinLogging.logger { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainApplication.mainComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.fragment_grades, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GradesViewModel::class.java)

        viewModel.grades.observe(this, Observer { grades ->
            if (grades != null && grades.isNotEmpty()) {
                fragment_grades_recyclerview.visibility = View.VISIBLE
                fragment_grades_message.visibility = View.GONE
                displayGrades(grades)
            } else {
                fragment_grades_recyclerview.visibility = View.GONE
                fragment_grades_message.visibility = View.VISIBLE
                fragment_grades_message.text = getString(R.string.no_grades)
            }
        })

    }

    private fun displayGrades(grades: List<Grade>) {
        logger.info { "Displaying ${grades.size} grades" }

        val items = mutableListOf<IFlexible<*>>()

        val subjectGradeMap = mutableMapOf<Subject, MutableList<Grade>>()
        for (grade in grades) {
            if (grade.subject != null && !subjectGradeMap.contains(grade.subject)) {
                subjectGradeMap.put(grade.subject!!, mutableListOf())
            }
            subjectGradeMap[grade.subject]?.add(grade)
        }

        subjectGradeMap.entries
                .forEach { entry: MutableMap.MutableEntry<Subject, MutableList<Grade>> ->
                    val header = GradeHeaderItem(entry.key)
                    entry.value.sortedBy(Grade::date)
                            .map { GradeItem(it, header) }
                            .forEach(header::addSubItem)
                    items.add(header)
                }

        val adapter = FlexibleAdapter<IFlexible<*>>(items)
        adapter.setDisplayHeadersAtStartUp(true)
        adapter.collapseAll()

        adapter.mItemClickListener = FlexibleAdapter.OnItemClickListener { position ->
            val item = adapter.getItem(position)
            if (item is GradeItem) {
                showDialog(item.grade)
                false
            } else {
                true
            }
        }
        fragment_grades_recyclerview.itemAnimator = null
        fragment_grades_recyclerview.layoutManager = LinearLayoutManager(activity)
        fragment_grades_recyclerview.adapter = adapter
    }

    private fun showDialog(grade: Grade) {
        val ddb = DetailsDialogBuilder(activity)
                .withTitle(getString(R.string.grade_details))

        if (grade.grade != null)
            ddb.addField(getString(R.string.grade), grade.grade)

        if (grade.category?.name != null)
            ddb.addField(getString(R.string.category), grade.category?.name)

        if (grade.category?.weight != null)
            ddb.addField(getString(R.string.weight), grade.category?.weight?.toString())

        if (grade.subject?.name != null)
            ddb.addField(getString(R.string.subject), grade.subject?.name)

        if (grade.date != null)
            ddb.addField(getString(R.string.date), grade.date?.toString(getString(R.string.date_format_full)))

        if (grade.addedBy?.fullName() != null)
            ddb.addField(getString(R.string.added_by), grade.addedBy?.fullName())

        for (comment in grade.comments) {
            ddb.addField(getString(R.string.comment), comment.text)
        }
        ddb.build().show()
    }
}