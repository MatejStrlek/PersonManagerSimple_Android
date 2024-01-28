package hr.algebra.personmanager.dao

import androidx.room.TypeConverter
import java.time.LocalDate

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long): LocalDate = LocalDate.ofEpochDay(value)
    @TypeConverter
    fun toTimestamp(value: LocalDate): Long = value.toEpochDay()
}