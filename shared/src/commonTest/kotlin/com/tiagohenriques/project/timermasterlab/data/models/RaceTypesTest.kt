package com.tiagohenriques.project.timermasterlab.data.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RaceTypesTest {
    @Test
    fun categoryLookupUsesExistingCodes() {
        assertEquals("Cadet", Category.fromCode(3))
        assertEquals("", Category.fromCode(-1))
    }

    @Test
    fun raceModeParsingIsCaseInsensitive() {
        assertEquals(Type.TIME_TRIAL, Type.fromStringToType("time trial"))
        assertEquals("Group Race", Type.fromTypeToString(Type.GROUP_RACE))
        assertNull(Type.fromStringToType("Unknown"))
    }
}
