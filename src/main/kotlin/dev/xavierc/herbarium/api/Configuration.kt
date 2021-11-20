package dev.xavierc.herbarium.api

// Use this file to hold package-level internal functions that return receiver object passed to the `install` method.
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.util.*
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Application block for [CORS] configuration.
 *
 * This file may be excluded in .openapi-generator-ignore,
 * and application specific configuration can be applied in this function.
 *
 * See http://ktor.io/features/cors.html
 */
internal fun ApplicationCORSConfiguration(): CORS.Configuration.() -> Unit {
    return {
        // method(HttpMethod.Options)
        // header(HttpHeaders.XForwardedProto)
        // anyHost()
        // host("my-host")
        // host("my-host:80")
        // host("my-host", subDomains = listOf("www"))
        // host("my-host", schemes = listOf("http", "https"))
        // allowCredentials = true
        // maxAge = Duration.ofDays(1)
    }
}

/**
 * Application block for [HSTS] configuration.
 *
 * This file may be excluded in .openapi-generator-ignore,
 * and application specific configuration can be applied in this function.
 *
 * See http://ktor.io/features/hsts.html
 */
internal fun ApplicationHstsConfiguration(): HSTS.Configuration.() -> Unit {
    return {
        maxAgeInSeconds = TimeUnit.DAYS.toSeconds(365)
        includeSubDomains = true
        preload = false

        // You may also apply any custom directives supported by specific user-agent. For example:
        // customDirectives.put("redirectHttpToHttps", "false")
    }
}

/**
 * Application block for [Compression] configuration.
 *
 * This file may be excluded in .openapi-generator-ignore,
 * and application specific configuration can be applied in this function.
 *
 * See http://ktor.io/features/compression.html
 */
internal fun ApplicationCompressionConfiguration(): Compression.Configuration.() -> Unit {
    return {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }
}

// Defines authentication mechanisms used throughout the application.
val ApplicationAuthProviders: Map<String, OAuthServerSettings> = listOf<OAuthServerSettings>(
        OAuthServerSettings.OAuth2ServerSettings(
            name = "oauth",
            authorizeUrl = "https://accounts.google.com/o/oauth2/v2/auth",
            accessTokenUrl = "",
            requestMethod = HttpMethod.Get,
            clientId = settings.property("auth.oauth.oauth.clientId").getString(),
            clientSecret = settings.property("auth.oauth.oauth.clientSecret").getString(),
            defaultScopes = listOf("read")
        )
//        OAuthServerSettings.OAuth2ServerSettings(
//                name = "facebook",
//                authorizeUrl = "https://graph.facebook.com/oauth/authorize",
//                accessTokenUrl = "https://graph.facebook.com/oauth/access_token",
//                requestMethod = HttpMethod.Post,
//
//                clientId = settings.property("auth.oauth.facebook.clientId").getString(),
//                clientSecret = settings.property("auth.oauth.facebook.clientSecret").getString(),
//                defaultScopes = listOf("public_profile")
//        )
).associateBy { it.name }
