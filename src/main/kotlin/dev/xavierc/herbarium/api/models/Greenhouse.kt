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
package dev.xavierc.herbarium.api.models

import dev.xavierc.herbarium.api.models.Plant
import dev.xavierc.herbarium.api.models.SensorData

/**
 * 
 * @param uuid Universal unique identifier
 * @param name Name given to the greenhouse by the user
 * @param plants Every plant actived in the greenhouse
 * @param tankLevel 
 * @param lastTimestamp Date time when the last data from this greenhouse was received.
 * @param createdAt When the greenhouse was registered.
 */
data class Greenhouse(
    /* Universal unique identifier */
    val uuid: java.util.UUID,
    /* Name given to the greenhouse by the user */
    val name: kotlin.String,
    /* Every plant actived in the greenhouse */
    val plants: kotlin.collections.List<Plant>,
    val tankLevel: SensorData,
    /* Date time when the last data from this greenhouse was received. */
    val lastTimestamp: java.time.OffsetDateTime,
    /* When the greenhouse was registered. */
    val createdAt: java.time.OffsetDateTime
) 

