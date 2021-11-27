package dev.xavierc.herbarium.api.repository

import dev.xavierc.herbarium.api.models.Greenhouse
import dev.xavierc.herbarium.api.models.Plant
import dev.xavierc.herbarium.api.models.SensorData
import dev.xavierc.herbarium.api.utils.exceptions.NotFoundException
import org.jetbrains.exposed.dao.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*

/**
 * Representation of a greenhouse
 */
object Greenhouses : Table("greenhouses") {
    val uuid: Column<UUID> = uuid("uuid").autoGenerate().primaryKey()
    val name: Column<String> = varchar("name", 256)
    val userUuid: Column<UUID> = uuid("user_uuid").references(Users.uuid, onDelete = ReferenceOption.CASCADE)
    val createdAt: Column<DateTime> = datetime("created_at")
}

class GreenhouseRepository(private val dataRepository: DataRepository) {

    /**
     * Check if the greenhouse UUID is referenced in the database
     */
    fun exist(uuid: UUID): Boolean {
        var count = 0

        transaction {
            count = Greenhouses
                .slice(Greenhouses.uuid)
                .select {
                    Greenhouses.uuid eq uuid
                }.count()
        }

        return count == 1
    }

    /**
     * Retrieve the greenhouse corresponding to the [uuid]
     * @param uuid of the greenhouse
     * @throws NotFoundException when the greenhouse doesn't exist in the database
     */
    fun getGreenhouse(uuid: UUID): Greenhouse {
        lateinit var greenhouse: Greenhouse

        transaction {
            // Get basic info of the greenhouse
            val info = Greenhouses
                .select {
                    Greenhouses.uuid eq uuid
                }
                .singleOrNull() ?: throw NotFoundException("Greenhouse $uuid not found")

            val tankLevel = dataRepository.getLastSensorData(greenhouseUuid = uuid, type = SensorData.Type.T)

            val lastDataTimestamp = dataRepository.getLastSensorData(greenhouseUuid = uuid)?.timestamp

//            val plants: List<Plant> =

            greenhouse = Greenhouse(
                uuid, info[Greenhouses.name], listOf(), tankLevel,
                lastDataTimestamp, info[Greenhouses.createdAt]
            )
        }

        return greenhouse
    }
}