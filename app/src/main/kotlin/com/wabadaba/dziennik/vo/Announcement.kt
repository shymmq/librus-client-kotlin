package com.wabadaba.dziennik.vo

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.requery.Entity
import io.requery.Key
import io.requery.ManyToOne
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

@Entity
@LibrusEntity("SchoolNotices")
@JsonDeserialize(`as` = AnnouncementEntity::class)
interface Announcement : Identifiable {

    @get:Key
    override val id: String

    val startDate: LocalDate?

    val endDate: LocalDate?

    @get:JsonProperty("Subject")
    val title: String?

    val content: String?

    @get:ManyToOne
    val addedBy: Teacher?

    @get:JsonProperty("CreationDate")
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val addDate: LocalDateTime?

    @get:JsonProperty("WasRead")
    val isRead: Boolean?
}