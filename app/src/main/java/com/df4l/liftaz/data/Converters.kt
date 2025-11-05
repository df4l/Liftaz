package com.df4l.liftaz.data

import androidx.room.TypeConverter
import java.util.Date

object Converters {
    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return if (dateLong == null) null else Date(dateLong)
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return if (date == null) null else date.getTime()
    }

    @TypeConverter
    fun fromList(value: List<Int>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toList(value: String?): List<Int>? {
        return value?.split(",")?.map { it.toInt() }
    }
}