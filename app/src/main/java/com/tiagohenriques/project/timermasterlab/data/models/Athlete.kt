package com.tiagohenriques.project.timermasterlab.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "athlete")
data class Athlete(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "age") val age: Int,
    @ColumnInfo(name = "gender") val gender: String,
    @ColumnInfo(name = "level") val level: Int,
)

enum class Level(val code: Int, val displayName: String) {
    Minor(0, "Minor"),
    Iniciado(1, "Iniciado"),
    Infantil(2, "Infantil"),
    Cadet(3, "Cadet"),
    Junior(4, "Junior"),
    Under23(5, "Under 23"),
    Senior(6, "Senior"),
    VeteranA(7, "Veteran A"),
    VeteranB(8, "Veteran B"),
    VeteranC(9, "Veteran C"),
    VeteranD(10, "Veteran D");

    companion object {
        fun fromCode(code: Int) = entries.firstOrNull { it.code == code }?.displayName ?: ""
    }
}
