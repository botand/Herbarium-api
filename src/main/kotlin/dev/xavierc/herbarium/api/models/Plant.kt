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

import dev.xavierc.herbarium.api.models.ActuatorState
import dev.xavierc.herbarium.api.models.PlantType
import dev.xavierc.herbarium.api.models.SensorData

/**
 * Representation of a plant in a greenhouse.
 * @param uuid Universal unique identifier
 * @param position On which tile of the greenhouse the plant is.
 * @param type 
 * @param plantedAt When the plant was planted.
 * @param moistureLastReading 
 * @param lightLastReading 
 * @param lastUuid Universal unique identifier
 * @param valveStatus 
 * @param lightStripStatus 
 */
data class Plant(
    /* Universal unique identifier */
    val uuid: java.util.UUID,
    /* On which tile of the greenhouse the plant is. */
    val position: kotlin.Int,
    val type: PlantType,
    /* When the plant was planted. */
    val plantedAt: java.time.OffsetDateTime,
    val moistureLastReading: SensorData,
    val lightLastReading: SensorData,
    /* Universal unique identifier */
    val lastUuid: java.util.UUID? = null,
    val valveStatus: ActuatorState? = null,
    val lightStripStatus: ActuatorState? = null
) 
