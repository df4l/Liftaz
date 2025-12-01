package com.df4l.liftaz.data

import androidx.fragment.app.add

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.Date

object Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, JeuDeDonneesParam::class.java)
    private val jsonAdapter = moshi.adapter<List<JeuDeDonneesParam>>(listType)

    @TypeConverter
    fun fromJeuDeDonneesList(jeuxDeDonnees: List<JeuDeDonneesParam>?): String? {
        return jeuxDeDonnees?.let { jsonAdapter.toJson(it) }
    }

    @TypeConverter
    fun toJeuDeDonneesList(json: String?): List<JeuDeDonneesParam>? {
        return json?.let { jsonAdapter.fromJson(it) }
    }

    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return if (dateLong == null) null else Date(dateLong)
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return if (date == null) null else date.getTime()
    }

    @TypeConverter
    fun fromList(list: List<Int>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toList(data: String?): List<Int>? {
        if (data.isNullOrBlank()) return null
        return data.split(",").map { it.toInt() }
    }
}