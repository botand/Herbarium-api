package dev.xavierc.herbarium.api.repositories

import dev.xavierc.herbarium.api.models.*
import dev.xavierc.herbarium.api.utils.exceptions.NotFoundException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.security.InvalidParameterException
import java.util.*

object Plants : Table("plants") {
    val uuid: Column<UUID> = uuid("uuid").autoGenerate().primaryKey()
    val greenhouseUuid: Column<UUID> =
        uuid("greenhouse_uuid").references(Greenhouses.uuid, onDelete = ReferenceOption.CASCADE)
    val type: Column<Int> = integer("type").references(PlantTypes.id).default(1)
    val position: Column<Int> = integer("position").check { it greaterEq 0 }
    val overrideMoistureGoal: Column<Double?> =
        double("override_moisture_goal").check { it.greaterEq(0) and it.lessEq(100.0) }.nullable()
    val overrideLightExposureMinDuration: Column<Double?> =
        double("override_light_exposure_min_duration").check { it.greaterEq(0) and it.lessEq(24) }.nullable()
    val plantedAt: Column<DateTime> = datetime("planted_at")

    /* Indicate if the plant was removed from the greenhouse */
    val removed: Column<Boolean> = bool("removed").default(false)
    val removedAt: Column<DateTime?> = datetime("removed_at").nullable()
}

object PlantTypes : Table("plant_types") {
    val id: Column<Int> = integer("id").autoIncrement().primaryKey()
    val name: Column<String> = varchar("name", length = 512)
    val moistureGoal: Column<Double> = double("moisture_goal").default(80.0)
    val lightExposureMinDuration: Column<Double> = double("light_exposure_min_duration").default(14.0)
    val germinationTime: Column<Int> = integer("germination_time").default(0)
    val growingTime: Column<Int> = integer("growing_time").default(0)
}

class PlantRepository(private val dataRepository: DataRepository) {
    /**
     * Check if the plant UUID is referenced in the database
     * @param uuid UUID to validate
     * @return true if the plant exists
     */
    fun exists(uuid: UUID): Boolean {
        var count = 0

        transaction {
            count = Plants
                .slice(Plants.uuid)
                .select {
                    Plants.uuid eq uuid
                }.count()
        }

        return count == 1
    }

    /**
     * Validate that each plant UUID exists in the database
     * @param uuids all the UUID to validate
     * @return list of the UUIDs that doesn't exist
     */
    fun exists(uuids: List<UUID>, greenhouseUuid: UUID? = null): List<UUID> {
        val unknown = mutableListOf<UUID>()

        transaction {
            var query = Plants
                .slice(Plants.uuid)
                .select {
                    Plants.uuid.inList(uuids)
                }

            if (greenhouseUuid != null) {
                query = query.andWhere { Plants.greenhouseUuid.eq(greenhouseUuid) }
            }

            val exists = query.map { it[Plants.uuid] }

            uuids.forEach {
                if (!exists.contains(it)) {
                    unknown.add(it)
                }
            }
        }

        return unknown
    }

    /**
     * Validate if a specific [position] in the greenhouse is free
     * @param greenhouseUuid Greenhouse to check
     * @param position
     * @return true if the position is free
     */
    fun positionFree(greenhouseUuid: UUID, position: Int): Boolean {
        var count = 0
        transaction {
            count = Plants.slice(Plants.uuid).select {
                Plants.greenhouseUuid.eq(greenhouseUuid) and Plants.position.eq(position) and Plants.removed.eq(false)
            }.count()
        }

        return count == 0
    }

    /**
     * Register a new plant
     * @param greenhouseUuid UUID of the greenhouse on which is the plant
     * @param position where the plant is hold
     * @param plantedAt when the plant was planted
     * @return the UUID of the new plant.
     */
    fun addPlant(greenhouseUuid: UUID, position: Int, plantedAt: DateTime): UUID {
        lateinit var plantUuid: UUID

        if (!positionFree(greenhouseUuid, position)) {
            throw InvalidParameterException(ErrorCode.Code.PLANT_POSITION_ALREADY_OCCUPIED.value)
        }

        transaction {
            plantUuid = Plants.insert {
                it[Plants.greenhouseUuid] = greenhouseUuid
                it[Plants.position] = position
                it[Plants.plantedAt] = plantedAt
            } get Plants.uuid
        }

        return plantUuid
    }

    /**
     * Retrieve the greenhouse UUID of a specific plant
     * @param uuid of the plant
     * @throws NotFoundException if the plant doesn't exist
     * @return UUID of the greenhouse
     */
    fun getGreenhouseUuidForPlant(uuid: UUID): UUID {
        lateinit var greenhouseUuid: UUID

        if (!exists(uuid)) {
            throw NotFoundException(ErrorCode.Code.NOT_FOUND.toString())
        }

        transaction {
            val result = Plants.slice(Plants.greenhouseUuid).select { Plants.uuid eq uuid }.single()

            greenhouseUuid = result[Plants.greenhouseUuid]
        }

        return greenhouseUuid
    }

    /**
     * Update a plant
     * @param uuid of the plant to update
     * @param typeId new plant type of the plant
     * @param overrideMoistureGoal new value for the custom moisture goal
     * @param overrideLightExposureMinDuration new value for the custom minimum duration exposure
     * @throws NotFoundException if the plant doesn't exist
     */
    fun updatePlant(uuid: UUID, typeId: Int, overrideMoistureGoal: Double?, overrideLightExposureMinDuration: Double?) {
        // Check if the plant exists
        if (!exists(uuid)) {
            throw NotFoundException(ErrorCode.Code.NOT_FOUND.toString())
        }

        transaction {
            Plants.update(where = { Plants.uuid eq uuid }) {
                it[Plants.type] = typeId
                it[Plants.overrideMoistureGoal] = overrideMoistureGoal
                it[Plants.overrideLightExposureMinDuration] = overrideLightExposureMinDuration
            }
        }
    }

    /**
     * Remove a plant from a greenhouse.
     * @param plantUuid UUID of the plant to remove
     * @throws InvalidParameterException if the plant has already been removed.
     */
    fun removePlant(plantUuid: UUID) {
        transaction {
            // Check the plant isn't already removed
            if (Plants.select { Plants.uuid.eq(plantUuid) and Plants.removed.eq(true) }
                    .count() > 0) {
                throw InvalidParameterException("Plant already removed.")
            }

            Plants.update({ Plants.uuid eq plantUuid }) {
                it[removed] = true
                it[removedAt] = DateTime.now()
            }
        }
    }

    /**
     * Retrieve all the plants for the specified greenhouse
     * @param greenhouseUuid UUID of the greenhouse which from retrieve the plants
     * @param showRemovedPlants do we also retrieve the plants that was removed from the greenhouse
     * @return all the plants known for the greenhouse
     */
    fun getPlantsByGreenhouse(greenhouseUuid: UUID, showRemovedPlants: Boolean = false): List<Plant> {
        val plants = mutableListOf<Plant>()

        transaction {
            var query = Plants.rightJoin(PlantTypes).select { Plants.greenhouseUuid eq greenhouseUuid }

            if (!showRemovedPlants) {
                query = query.andWhere { Plants.removed eq false }
            }

            val plantsResult = query.associateBy { it[Plants.uuid] }

            val moistureLevels =
                dataRepository.getLastSensorDataBatch(plantsUuid = plantsResult.keys.toList(), type = SensorData.Type.M)
                    .associate { it.plantUuid to it.value }

            val valvesStatus =
                dataRepository.getLastActuatorStateBatch(
                    plantsUuid = plantsResult.keys.toList(),
                    type = ActuatorState.Type.V
                )
                    .associate { it.plantUuid to it.value }
            val lightStripStatus =
                dataRepository.getLastActuatorStateBatch(
                    plantsUuid = plantsResult.keys.toList(),
                    type = ActuatorState.Type.L
                )
                    .associate { it.plantUuid to it.value }

            val lightLevel =
                dataRepository.getLastSensorData(greenhouseUuid, type = SensorData.Type.L)?.value

            plantsResult.forEach { (uuid, row) ->
                plants.add(
                    mapToPlant(
                        row,
                        mapToPlantTypes(row),
                        moistureLevels[uuid],
                        lightLevel,
                        valvesStatus[uuid],
                        lightStripStatus[uuid]
                    )
                )
            }
        }

        return plants
    }
}

fun mapToPlant(
    row: ResultRow,
    type: PlantType,
    moistureLastReading: Double?,
    lightLastReading: Double?,
    valveStatus: Boolean?,
    lightStripStatus: Boolean?
): Plant {
    return Plant(
        row[Plants.uuid],
        row[Plants.position],
        type,
        row[Plants.plantedAt],
        moistureLastReading,
        lightLastReading,
        valveStatus,
        lightStripStatus,
        row[Plants.overrideMoistureGoal],
        row[Plants.overrideLightExposureMinDuration],
        row[Plants.removed]
    )
}

fun mapToPlantTypes(row: ResultRow): PlantType {
    return PlantType(
        row[PlantTypes.id],
        row[PlantTypes.name],
        row[PlantTypes.moistureGoal],
        row[PlantTypes.lightExposureMinDuration],
        row[PlantTypes.germinationTime],
        row[PlantTypes.growingTime]
    )
}