package com.example.bot.spring.echo

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSetter
import org.bson.types.ObjectId
import org.springframework.data.annotation.*
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

abstract class BaseDocument {

    @Id
    @Field("_id")
    var id: ObjectId? = null

    @get:JsonGetter("id")
    @set:JsonSetter("id")
    var idHexString: String?
        get() = if (id != null) id!!.toHexString() else null
        set(value) { id = value?.let { ObjectId(it) } }

    @CreatedBy
    var createdBy: String? = null

    @LastModifiedBy
    var lastModifiedBy: String? = null

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    var createdDate: LocalDateTime? = null

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    var lastModifiedDate: LocalDateTime? = null

    @get:Transient
    @get:JsonIgnore
    val persisted: Boolean get() = id != null

    override fun toString(): String {
        return "${super.toString()} id = $id"
    }
}
