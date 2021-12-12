package dev.xavierc.herbarium.api.repositories

import dev.xavierc.herbarium.api.models.ErrorCode
import dev.xavierc.herbarium.api.models.Greenhouse
import dev.xavierc.herbarium.api.models.Plant
import dev.xavierc.herbarium.api.models.SensorData
import dev.xavierc.herbarium.api.utils.exceptions.NotFoundException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*

/**
 * Representation of a greenhouse
 */
object Greenhouses : Table("greenhouses") {
    val uuid: Column<UUID> = uuid("uuid").primaryKey()
    val name: Column<String> = varchar("name", 256)
    val userUuid: Column<String> = varchar("user_uuid", 128).references(Users.uuid, onDelete = ReferenceOption.CASCADE)
    val createdAt: Column<DateTime> = datetime("created_at")
}

class GreenhouseRepository(private val dataRepository: DataRepository, private val plantRepository: PlantRepository) {

    /**
     * Check if the greenhouse UUID is referenced in the database
     * @param uuid UUID to validate
     * @return true if the greenhouse exists
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
     * Check if the user UUID is linked to the specified greenhouse
     * @param uuid of the greenhouse
     * @param userUuid of the user to validate
     * @return true if the greenhouse is linked to the user
     */
    fun isUserLinked(uuid: UUID, userUuid: String): Boolean {
        var count = 0

        transaction {
            count = Greenhouses
                .slice(Greenhouses.uuid)
                .select {
                    Greenhouses.uuid.eq(uuid) and Greenhouses.userUuid.eq(userUuid)
                }.count()
        }

        return count == 1
    }

    /**
     * Retrieve the greenhouse corresponding to the [uuid]
     * @param uuid of the greenhouse
     * @throws NotFoundException when the greenhouse doesn't exist in the database
     * @return Greenhouse which is linked to the specified uuid
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

            val plants: List<Plant> = plantRepository.getPlantsByGreenhouse(uuid)

            greenhouse = mapToGreenhouse(info, tankLevel, lastDataTimestamp, plants)
        }

        return greenhouse
    }

    /**
     * Retrieve the greenhouses linked to the user
     * @param userUuid of the greenhouse
     * @return list of the greenhouses linked to the user
     */
    fun getGreenhousesByUserUid(userUuid: String, showRemovedPlant: Boolean = false): List<Greenhouse> {
        var greenhouses = listOf<Greenhouse>()

        transaction {
            // Get basic info of the greenhouse
            greenhouses = Greenhouses
                .select {
                    Greenhouses.userUuid eq userUuid
                }.map {
                    val tankLevel = dataRepository.getLastSensorData(
                        greenhouseUuid = it[Greenhouses.uuid],
                        type = SensorData.Type.T
                    )

                    val lastDataTimestamp =
                        dataRepository.getLastSensorData(greenhouseUuid = it[Greenhouses.uuid])?.timestamp

                    val plants: List<Plant> =
                        plantRepository.getPlantsByGreenhouse(it[Greenhouses.uuid], showRemovedPlant)

                    return@map mapToGreenhouse(it, tankLevel, lastDataTimestamp, plants)
                }
        }

        return greenhouses
    }

    /**
     * Create a new greenhouse
     * @param userUuid of the user linked to the greenhouse
     * @param name name for the greenhouse
     * @return UUID of the new greenhouse
     */
    fun addGreenhouse(userUuid: String, greenhouseUuid: UUID, name: String): UUID {
        lateinit var uuid: UUID;

        transaction {
            if(exist(greenhouseUuid)) {
                throw IllegalArgumentException(ErrorCode.Code.GREENHOUSE_ALREADY_EXISTS.toString())
            }

            uuid = Greenhouses.insert {
                it[Greenhouses.uuid] = greenhouseUuid
                it[Greenhouses.name] = name
                it[Greenhouses.userUuid] = userUuid
            } get Greenhouses.uuid
        }

        return uuid
    }

    /**
     * Update a specified greenhouse
     * @param uuid of the greenhouse to update
     * @param name new name for the greenhouse
     * @throws NotFoundException if the greenhouse doesn't exist
     */
    fun updateGreenhouseDetails(uuid: UUID, name: String) {
        // Check if the greenhouse exists
        if (!exist(uuid)) {
            throw NotFoundException(ErrorCode.Code.NOT_FOUND.toString())
        }

        transaction {
            Greenhouses.update(where = { Greenhouses.uuid eq uuid }) {
                it[Greenhouses.name] = name
            }
        }
    }

    /**
     * Delete a specified greenhouse
     * @param uuid of the greenhouse to update
     * @throws NotFoundException if the greenhouse doesn't exist
     */
    fun deleteGreenhouse(uuid: UUID) {
        // Check if the greenhouse exists
        if (!exist(uuid)) {
            throw NotFoundException(ErrorCode.Code.NOT_FOUND.toString())
        }

        transaction {
            Greenhouses.deleteWhere { Greenhouses.uuid eq uuid }
        }
    }
}

fun mapToGreenhouse(
    row: ResultRow,
    tankLevel: SensorData?,
    lastDataTimestamp: DateTime?,
    plants: List<Plant>
): Greenhouse {
    return Greenhouse(
        row[Greenhouses.uuid],
        row[Greenhouses.name],
        plants,
        tankLevel,
        lastDataTimestamp ?: row[Greenhouses.createdAt],
        row[Greenhouses.createdAt]
    )
}