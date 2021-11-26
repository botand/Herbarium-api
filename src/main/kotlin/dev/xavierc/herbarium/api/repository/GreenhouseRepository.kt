package dev.xavierc.herbarium.api.repository

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime
import java.util.*

/**
 * Representation of a greenhouse
 */
object Greenhouses : Table("greenhouses") {
    val uuid: Column<UUID> = uuid("uuid").autoGenerate().primaryKey()
    val name: Column<String> = varchar("name", 256)
    val userUuid: Column<UUID> = uuid("user_uuid") references Users.uuid
    val createdAt: Column<DateTime> = datetime("created_at")
}

/**
 * Join table between the Greenhouses and Plants table
 */
object GreenhousesPlants : Table() {
    val greenhouseUuid = uuid("greenhouse_uuid") references Greenhouses.uuid
    val imageUuid = uuid("plant_uuid") references Plant.uuid
}

class GreenhouseRepository {

}