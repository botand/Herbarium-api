package dev.xavierc.herbarium.api.repository

import dev.xavierc.herbarium.api.models.User
import dev.xavierc.herbarium.api.utils.exceptions.NotFoundException
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*

/**
 * Table containing all the data read by the sensors
 */
object Users : Table("users") {
    val uuid: Column<UUID> = uuid("uuid").autoGenerate().primaryKey()
    val displayName: Column<String> = varchar("displayName", 256)
    val email: Column<String> = varchar("email", 512)
    val language: Column<String> = varchar("language", 2)
    val joined_on: Column<DateTime> = datetime("joined_on")
}

class UserRepository {
    fun getUser(uuid: UUID): User {
        lateinit var user: User

        transaction {
            val userInfo =
                Users.select { Users.uuid eq uuid }.singleOrNull() ?: throw NotFoundException("User $uuid not found")

            user = User(
                userInfo[Users.uuid],
                userInfo[Users.displayName],
                userInfo[Users.email],
                userInfo[Users.language],
                userInfo[Users.joined_on]
            )
        }

        return user
    }
}