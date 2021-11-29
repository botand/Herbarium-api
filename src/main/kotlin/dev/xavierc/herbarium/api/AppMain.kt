package dev.xavierc.herbarium.api

import com.codahale.metrics.Slf4jReporter
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.locations.*
import io.ktor.metrics.dropwizard.*
import java.util.concurrent.TimeUnit
import io.ktor.routing.*
import io.ktor.util.*
import com.typesafe.config.ConfigFactory
import dev.xavierc.herbarium.api.apis.*
import io.ktor.auth.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.config.HoconApplicationConfig
import dev.xavierc.herbarium.api.infrastructure.*
import dev.xavierc.herbarium.api.repository.*
import dev.xavierc.herbarium.api.utils.DateTimeSerializer
import org.joda.time.DateTime
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import java.util.*


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
//        reporter.start(10, TimeUnit.SECONDS)
    }
    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(DateTime::class.java, DateTimeSerializer())
        }
    }
    install(DataConversion) {
        convert<UUID> {
            decode { values, _ ->
                values.singleOrNull()?.let { UUID.fromString(it) }
            }
            encode {
                when (it) {
                    null -> listOf()
                    is UUID -> listOf(it.toString())
                    else -> throw DataConversionException("Cannot convert to UUID")
                }
            }
        }
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
                when (apikeyCredential.value) {
                    "keyboardcat" -> ApiPrincipal(apikeyCredential)
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

    val di = DI {
        bind<DataRepository>() with singleton { DataRepository() }
        bind<PlantRepository>() with singleton { PlantRepository(DataRepository()) }
        bind<GreenhouseRepository>() with singleton {
            GreenhouseRepository(
                DataRepository(),
                PlantRepository(DataRepository())
            )
        }
        bind<UserRepository>() with singleton { UserRepository() }
    }

    install(Routing) {
        ActuatorsApi()
        GreenhouseApi(di)
        PlantApi(di)
        HealthApi()
    }

}
