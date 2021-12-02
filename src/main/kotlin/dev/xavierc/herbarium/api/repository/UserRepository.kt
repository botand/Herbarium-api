package dev.xavierc.herbarium.api.repository

import dev.xavierc.herbarium.api.models.ErrorCode
import dev.xavierc.herbarium.api.models.User
import dev.xavierc.herbarium.api.utils.exceptions.NotFoundException
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.security.InvalidParameterException
import java.util.*

/**
 * Table containing all the data read by the sensors
 */
object Users : Table("users") {
    val uuid: Column<String> = varchar("uuid", 128).primaryKey()
    val displayName: Column<String> = varchar("displayName", 256)
    val email: Column<String> = varchar("email", 512)
    val language: Column<String> = varchar("language", 2).default("en")
    val joined_on: Column<DateTime> = datetime("joined_on")
}

class UserRepository {
    /**
     * Check if the user UUID is referenced in the database
     * @param uuid UUID to validate
     * @return true if the user exists
     */
    fun exists(uuid: String): Boolean {
        var count = 0

        transaction {
            count = Users
                .slice(Users.uuid)
                .select {
                    Users.uuid eq uuid
                }.count()
        }

        return count == 1
    }

    /**
     * Retrieve the user corresponding to the [uuid]
     * @param uuid of the user
     * @throws NotFoundException if the user doesn't exist in the database
     */
    fun getUser(uuid: String): User {
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

    /**
     * Insert a new user. This will also validate if the user uuid already exists in the database.
     * @param uuid UID of the user.
     * @param displayName known name of the user.
     * @param email email of the account for the user.
     * @param language short code (2 letters) of the language used by the user.
     * @throws InvalidParameterException if the user already exists
     */
    fun insertUser(uuid: String, displayName: String, email: String, language: String? = null) {
        // Check if the user exists
        if (exists(uuid)) {
            throw InvalidParameterException(ErrorCode.Code.USER_ALREADY_EXISTS.toString())
        }

        transaction {
            Users.insert {
                it[Users.uuid] = uuid
                it[Users.email] = email
                it[Users.displayName] = displayName
                if(language != null) {
                    it[Users.language] = language
                }
                it[Users.joined_on] = DateTime.now()
            }
        }
    }
}