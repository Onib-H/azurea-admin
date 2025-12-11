package com.harold.azureaadmin.dto

import okhttp3.Cookie
import com.google.gson.annotations.SerializedName

data class CookieDto(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String,
    @SerializedName("domain") val domain: String,
    @SerializedName("path") val path: String,
    @SerializedName("expiresAt") val expiresAt: Long,
    @SerializedName("secure") val secure: Boolean,
    @SerializedName("httpOnly") val httpOnly: Boolean,
    @SerializedName("hostOnly") val hostOnly: Boolean
)

fun cookieToDto(c: Cookie) = CookieDto(
    name = c.name,
    value = c.value,
    domain = c.domain,
    path = c.path,
    expiresAt = c.expiresAt,
    secure = c.secure,
    httpOnly = c.httpOnly,
    hostOnly = c.hostOnly
)

fun dtoToCookie(dto: CookieDto): Cookie? {
    return try {
        // validate fields
        if (dto.name.isNullOrBlank() || dto.value == null) return null
        // build Cookie with Cookie.Builder and safe fallbacks
        Cookie.Builder()
            .name(dto.name)
            .value(dto.value)
            .domain(dto.domain ?: "")
            .path(dto.path ?: "/")
            .expiresAt(dto.expiresAt ?: Long.MAX_VALUE)
            .build()
    } catch (ex: Exception) {
        null
    }
}

