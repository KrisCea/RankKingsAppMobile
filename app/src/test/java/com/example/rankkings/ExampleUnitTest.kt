package com.example.rankkings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailValidatorTest {

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    @Test
    fun email_correcto_retorna_true() {
        val result = isValidEmail("usuario@test.com")
        assertTrue(result)
    }

    @Test
    fun email_incorrecto_retorna_false() {
        val result = isValidEmail("usuariotest")
        assertFalse(result)
    }
}
