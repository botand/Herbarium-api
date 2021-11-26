package dev.xavierc.herbarium.api.models

import org.joda.time.DateTime
import java.util.UUID

/**
 * Representation of a User.
 *
 * @param uuid Unique identifier of the user
 * @param displayName Name of the user
 * @param email email address of the user
 * @param language short language code (e.g: "fr") used by the user
 * @param joined_on when the user account was created
 */
data class User(
    val uuid: UUID,
    val displayName: String,
    val email: String,
    val language: String,
    val joined_on: DateTime
)
