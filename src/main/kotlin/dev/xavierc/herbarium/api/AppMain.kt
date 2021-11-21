package dev.xavierc.herbarium.api

import com.codahale.metrics.Slf4jReporter
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.metrics.dropwizard.*
import java.util.concurrent.TimeUnit
import io.ktor.routing.*
import io.ktor.util.*
import com.typesafe.config.ConfigFactory
import io.ktor.auth.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.config.HoconApplicationConfig
import dev.xavierc.herbarium.api.infrastructure.*
import dev.xavierc.herbarium.api.apis.ActuatorsApi
import dev.xavierc.herbarium.api.apis.GreenhouseApi
import dev.xavierc.herbarium.api.apis.PlantApi
import dev.xavierc.herbarium.api.apis.SensorsApi
import dev.xavierc.herbarium.api.repository.DatabaseFactory


internal val settings = HoconApplicationConfig(ConfigFactory.defaultApplication(HTTP::class.java.classLoader))

object HTTP {
    val client = HttpClient(Apache)
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Application.main() {
    install(DefaultHeaders)
    install(DropwizardMetrics) {
        val reporter = Slf4jReporter.forRegistry(registry)
            .outputTo(log)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
        reporter.start(10, TimeUnit.SECONDS)
    }
    install(ContentNegotiation) {
        register(ContentType.Application.Json, GsonConverter())
    }
    install(AutoHeadResponse) // see https://ktor.io/docs/autoheadresponse.html
    install(Compression, applicationCompressionConfiguration()) // see https://ktor.io/docs/compression.html
    install(CORS, applicationCORSConfiguration()) // see https://ktor.io/docs/cors.html
    install(HSTS, applicationHstsConfiguration()) // see https://ktor.io/docs/hsts.html
    install(Locations) // see https://ktor.io/docs/features-locations.html
    install(Authentication) {
        // "Implement API key auth (apiKey) for parameter name 'X-API-Key'."
        apiKeyAuth("apiKey") {
            validate { apikeyCredential: ApiKeyCredential ->
                when {
                    apikeyCredential.value == "keyboardcat" -> ApiPrincipal(apikeyCredential)
                    else -> null
                }
            }
        }
        oauth("oauth") {
            client = HttpClient(Apache)
            providerLookup = { ApplicationAuthProviders["oauth"] }
            urlProvider = { _ ->
                // TODO: define a callback url here.
                "/"
            }
        }
    }

    DatabaseFactory.init()

    install(Routing) {
        ActuatorsApi()
        GreenhouseApi()
        PlantApi()
        SensorsApi()
    }

}
