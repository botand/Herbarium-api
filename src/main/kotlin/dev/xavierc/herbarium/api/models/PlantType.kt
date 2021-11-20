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


/**
 * 
 * @param id Unique identifier of the type.
 * @param name Name of the plant.
 * @param moistureGoal Percentage of moisture needed for the plant to perfectly grow.
 * @param lightExposureMinDuration Number of hour needed for the plant to perfectly grow.
 */
data class PlantType(
    /* Unique identifier of the type. */
    val id: kotlin.Int,
    /* Name of the plant. */
    val name: kotlin.String,
    /* Percentage of moisture needed for the plant to perfectly grow. */
    val moistureGoal: java.math.BigDecimal,
    /* Number of hour needed for the plant to perfectly grow. */
    val lightExposureMinDuration: java.math.BigDecimal
) 

