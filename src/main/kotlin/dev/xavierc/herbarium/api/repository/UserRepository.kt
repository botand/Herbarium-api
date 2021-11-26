package dev.xavierc.herbarium.api.repository

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime
import java.util.*

/**
 * Table containing all the data read by the sensors
 */
object Users : Table("users") {
    val uuid: Column<UUID> = uuid("uuid").autoGenerate().primaryKey()
    val firstName: Column<String> = varchar("firstname", 256)
    val email: Column<String> = varchar("email", 512)
    val language: Column<String> = varchar("language", 2)
    val joined_on: Column<DateTime> = datetime("joined_on")
}

class UserRepository {

}