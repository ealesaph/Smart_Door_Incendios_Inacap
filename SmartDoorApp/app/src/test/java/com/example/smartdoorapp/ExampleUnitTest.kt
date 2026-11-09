package com.example.smartdoorapp

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun passwordValidation_validPassword_returnsTrue() {
        assertTrue(ValidationUtils.isPasswordValid("Prueba123456"))
    }

    @Test
    fun passwordValidation_shortPassword_returnsFalse() {
        assertFalse(ValidationUtils.isPasswordValid("12345"))
    }

    @Test
    fun rutValidation_validRut_returnsTrue() {
        assertTrue(ValidationUtils.isRutValid("19.876.543-2"))
    }

    @Test
    fun rutValidation_emptyRut_returnsFalse() {
        assertFalse(ValidationUtils.isRutValid(""))
    }

    @Test
    fun fireAlert_smokeDetected_returnsTrue() {
        assertTrue(ValidationUtils.isFireAlert(humoDetectado = true, temperatura = 25.0))
    }

    @Test
    fun fireAlert_highTemperature_returnsTrue() {
        assertTrue(ValidationUtils.isFireAlert(humoDetectado = false, temperatura = 50.0))
    }

    @Test
    fun fireAlert_normalConditions_returnsFalse() {
        assertFalse(ValidationUtils.isFireAlert(humoDetectado = false, temperatura = 22.0))
    }

    @Test
    fun saltGeneration_returnsUniqueHexSalt() {
        val salt1 = HashUtils.generarSalt()
        val salt2 = HashUtils.generarSalt()
        assertNotNull(salt1)
        assertNotNull(salt2)
        assertEquals(32, salt1.length) // 16 bytes en Hex = 32 caracteres
        assertNotEquals(salt1, salt2)
    }

    @Test
    fun passwordHashing_correctPassword_verifiesTrue() {
        val password = "PasswordSegura123"
        val salt = HashUtils.generarSalt()
        val hash = HashUtils.hashPassword(password, salt)

        assertTrue(HashUtils.verificarPassword(password, salt, hash))
    }

    @Test
    fun passwordHashing_wrongPassword_verifiesFalse() {
        val password = "PasswordSegura123"
        val wrongPassword = "PasswordIncorrecta"
        val salt = HashUtils.generarSalt()
        val hash = HashUtils.hashPassword(password, salt)

        assertFalse(HashUtils.verificarPassword(wrongPassword, salt, hash))
    }
}
