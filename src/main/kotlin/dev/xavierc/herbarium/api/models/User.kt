package dev.xavierc.herbarium.api.models

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import java.util.UUID

/**
 * Representation of a User.
 *
 * @param uuid Unique identifier of the user
 * @param displayName Name of the user
 * @param email email address of the user
 * @param language short language code (e.g: "fr") used by the user
 * @param joinedOn when the user account was created
 */
data class User(
    val uuid: String,
    @SerializedName(value = "display_name")
    val displayName: String,
    val email: String,
    /* Short code of the language used by the user */
    val language: String,
    /* When the user created his account */
    @SerializedName(value = "joined_on")
    val joinedOn: DateTime
)
