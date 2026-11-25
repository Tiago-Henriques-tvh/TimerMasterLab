package com.tiagohenriques.project.timermasterlab.data.models

enum class Category(val code: Int, val displayName: String) {
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
        fun fromCode(code: Int): String = entries.firstOrNull { it.code == code }?.displayName ?: ""
    }
}

enum class Type(val displayName: String) {
    TIME_TRIAL("Time Trial"),
    GROUP_RACE("Group Race");

    companion object {
        fun fromStringToType(string: String): Type? =
            entries.find { it.displayName.equals(string, ignoreCase = true) }

        fun fromTypeToString(type: Type): String? =
            entries.find { it == type }?.displayName
    }
}
