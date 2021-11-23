/**
* Herbarium API
* No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
*
* The version of the OpenAPI document: 1.0.0
* 
*
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package dev.xavierc.herbarium.api.apis

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import dev.xavierc.herbarium.api.Paths
import io.ktor.locations.*
import io.ktor.routing.*
import dev.xavierc.herbarium.api.infrastructure.ApiPrincipal
import dev.xavierc.herbarium.api.models.ActuatorState
import dev.xavierc.herbarium.api.models.InlineObject1

@KtorExperimentalLocationsAPI
fun Route.ActuatorsApi() {
    val gson = Gson()
    val empty = mutableMapOf<String, Any?>()

    authenticate("oauth") {
    post<Paths.postActuatorState> {
        val principal = call.authentication.principal<OAuthAccessTokenResponse>()!!
        
        call.respond(HttpStatusCode.NotImplemented)
    }
    }

    authenticate("apiKey") {
    put<Paths.putData> {
        val principal = call.authentication.principal<ApiPrincipal>()!!
        
        call.respond(HttpStatusCode.NotImplemented)
    }
    }

}