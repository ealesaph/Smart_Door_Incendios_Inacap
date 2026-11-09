package com.example.smartdoorapp

import java.security.MessageDigest
import java.security.SecureRandom

object HashUtils {

    /**
     * Genera un Salt aleatorio criptográficamente seguro de 16 bytes en formato Hexadecimal.
     */
    fun generarSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return bytesToHex(saltBytes)
    }

    /**
     * Genera un Hash SHA-256 combinando la contraseña dada con el Salt hexadecimal.
     */
    fun hashPassword(password: String, saltHex: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.reset()
        digest.update(hexToBytes(saltHex))
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashBytes)
    }

    /**
     * Verifica si una contraseña coincide con el Salt y Hash esperado.
     */
    fun verificarPassword(passwordIngresada: String, saltHex: String, hashEsperado: String): Boolean {
        if (passwordIngresada.isEmpty() || saltHex.isEmpty() || hashEsperado.isEmpty()) return false
        val hashCalculado = hashPassword(passwordIngresada, saltHex)
        return hashCalculado.equals(hashEsperado, ignoreCase = true)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
