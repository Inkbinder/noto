package uk.co.inkbinder.noto.feature.tags

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TagColorInputTest {
    @Test
    fun normalizeColorHexInput_acceptsSixDigitHexWithOrWithoutHash() {
        assertEquals("#123ABC", normalizeColorHexInput("123abc"))
        assertEquals("#D86A6A", normalizeColorHexInput("#d86a6a"))
    }

    @Test
    fun normalizeColorHexInput_rejectsInvalidHexValues() {
        assertNull(normalizeColorHexInput("#12345"))
        assertNull(normalizeColorHexInput("#12GG56"))
        assertNull(normalizeColorHexInput(""))
    }
}
