package dev.xavierc.herbarium.api.repository

import dev.xavierc.herbarium.api.models.ApiErrorResponse
import dev.xavierc.herbarium.api.models.Plant
import dev.xavierc.herbarium.api.models.PlantType
import dev.xavierc.herbarium.api.models.SensorData
import dev.xavierc.herbarium.api.utils.exceptions.NotFoundException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.security.InvalidParameterException
import java.util.*

object Plants : Table("plants") {
    val uuid: Column<UUID> = uuid("uuid").autoGenerate().primaryKey()
    val oldUuid: Column<UUID?> = uuid("old_uuid").nullable()
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
}

class PlantRepository(private val dataRepository: DataRepository) {
    /**
     * Check if the plant UUID is referenced in the database
     */
    fun exists(uuid: UUID): Boolean {
        var count = 0

        transaction {
            count = Plants
                .slice(Plants.uuid)
                .select {
                    Plants.uuid.eq(uuid) or Plants.oldUuid.eq(uuid)
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
                    Plants.uuid.inList(uuids) or Plants.oldUuid.inList(uuids)
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
            throw InvalidParameterException(ApiErrorResponse.Code.PLANT_POSITION_ALREADY_OCCUPIED.value)
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
     * Remove a plant from a greenhouse.
     * @param plantUuid UUID of the plant to remove
     * @throws InvalidParameterException if the plant has already been removed.
     */
    fun removePlant(plantUuid: UUID) {
        transaction {
            // Check the plant isn't already removed
            if (Plants.select { (Plants.uuid.eq(plantUuid) or Plants.oldUuid.eq(plantUuid)) and Plants.removed.eq(true) }
                    .count() > 0) {
                throw InvalidParameterException("Plant already removed.")
            }

            Plants.update({ Plants.uuid.eq(plantUuid) or Plants.oldUuid.eq(plantUuid) }) {
                it[removed] = true
                it[removedAt] = DateTime.now()
            }
        }
    }
}

fun mapPlantTypes(row: ResultRow): PlantType {
    return PlantType(
        row[PlantTypes.id],
        row[PlantTypes.name],
        row[PlantTypes.moistureGoal],
        row[PlantTypes.lightExposureMinDuration]
    )
}