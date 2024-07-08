package com.games.mw.authservice.security

import jakarta.servlet.http.HttpServletRequest

object IPUtils {

    fun getClientIP(request: HttpServletRequest): String {
        // Identify the original IP address of a client
        val xfHeader = request.getHeader("X-Forwarded-For")
        // If x-forwarded-for is null or blank, return the remote address
        return if (xfHeader == null || xfHeader.isBlank()) {
            request.remoteAddr
        } else {
            val ips = xfHeader.split(",").map { it.trim() }
            val validIps = ips.filter { isValidIP(it) }
            if (validIps.isNotEmpty()) validIps[0] else request.remoteAddr
        }
    }

    private fun isValidIP(ip: String): Boolean {
        return ip.matches(Regex("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\$")) || ip.matches(Regex("^[0-9a-fA-F:]{2,39}\$"))
    }
}