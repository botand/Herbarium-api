package dev.xavierc.herbarium.api.repositories

import dev.xavierc.herbarium.api.models.ActuatorState
import dev.xavierc.herbarium.api.models.SensorData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*

/**
 * Table containing all the data read by the sensors
 */
object SensorsData : Table("sensors_data") {
    val greenhouseUuid: Column<UUID> =
        uuid("greenhouse_uuid").references(Greenhouses.uuid, onDelete = ReferenceOption.CASCADE)
    val type: Column<Char> = char("type").check {
        it.inList(listOf('M', 'L', 'T'))
    }
    val value: Column<Double> = double("value")
    val timestamp: Column<DateTime> = datetime("timestamp")
    val plantUuid: Column<UUID?> =
        uuid("plant_uuid").references(Plants.uuid, onDelete = ReferenceOption.CASCADE).nullable()
}

/**
 * Table containing all the orders given to the actuators.
 */
object ActuatorsState : Table("actuators_state") {
    val greenhouseUuid: Column<UUID> =
        uuid("greenhouse_uuid").references(Greenhouses.uuid, onDelete = ReferenceOption.CASCADE)
    val type: Column<Char> = char("type").check {
        it.inList(listOf('V', 'L', 'P'))
    }
    val status: Column<Boolean> = bool("status")
    val timestamp: Column<DateTime> = datetime("timestamp")
    val plantUuid: Column<UUID?> =
        uuid("plant_uuid").references(Plants.uuid, onDelete = ReferenceOption.CASCADE).nullable()
}

class DataRepository {
    /**
     * Retrieve the last known sensor reading for a greenhouse or a plant.
     * @param greenhouseUuid filter for a greenhouse
     * @param plantUuid filter for a plant
     * @param type filter for a type of reading
     * @return last data registered or null if none is found
     */
    fun getLastSensorData(
        greenhouseUuid: UUID? = null,
        plantUuid: UUID? = null,
        type: SensorData.Type? = null
    ): SensorData? {
        var sensorData: SensorData? = null

        transaction {
            var query = SensorsData.selectAll().orderBy(SensorsData.timestamp to SortOrder.DESC)
            if (greenhouseUuid != null) {
                query = query.adjustWhere { SensorsData.greenhouseUuid eq greenhouseUuid }
            }

            if (plantUuid != null) {
                query = query.andWhere { SensorsData.plantUuid eq plantUuid }
            }

            if (type != null) {
                query = query.andWhere { SensorsData.type eq type.value }
            }

            val info = query.limit(1).singleOrNull()

            if (info != null) {
                sensorData = mapToSensorData(info)
            }
        }

        return sensorData
    }

    /**
     * Retrieve the last known sensor reading for each greenhouse or plants specified.
     * @param greenhousesUuid filter for greenhouse
     * @param plantsUuid filter for plant
     * @param type filter for a type of reading
     * @return last data registered or null if none is found
     */
    fun getLastSensorDataBatch(
        greenhousesUuid: List<UUID>? = null,
        plantsUuid: List<UUID>? = null,
        type: SensorData.Type? = null
    ): List<SensorData> {
        val sensorDatas = mutableListOf<SensorData>()

        transaction {
            var query = SensorsData.selectAll().orderBy(SensorsData.timestamp to SortOrder.DESC)
            if (greenhousesUuid != null) {
                query = query.adjustWhere { SensorsData.greenhouseUuid.inList(greenhousesUuid) }
            } else if (plantsUuid != null) {
                query = query.andWhere { SensorsData.plantUuid.inList(plantsUuid) }
            }

            if (type != null) {
                query = query.andWhere { SensorsData.type eq type.value }
            }

            if(plantsUuid != null) {
                sensorDatas.addAll(query.distinctBy { it[SensorsData.plantUuid] }.map { mapToSensorData(it) })
            } else {
                sensorDatas.addAll(query.map { mapToSensorData(it) })
            }
        }
        return sensorDatas
    }

    /**
     * Retrieve the last known actuator state for each greenhouse or plants specified.
     * @param greenhousesUuid filter for greenhouse
     * @param plantsUuid filter for plant
     * @param type filter for a type of reading
     * @return last data registered or null if none is found
     */
    fun getLastActuatorStateBatch(
        greenhousesUuid: List<UUID>? = null,
        plantsUuid: List<UUID>? = null,
        type: ActuatorState.Type? = null
    ): List<ActuatorState> {
        val actuatorsState = mutableListOf<ActuatorState>()

        transaction {
            var query = ActuatorsState.selectAll().withDistinct().orderBy(ActuatorsState.timestamp to SortOrder.DESC)
            if (greenhousesUuid != null) {
                query = query.adjustWhere { ActuatorsState.greenhouseUuid.inList(greenhousesUuid) }
            } else if (plantsUuid != null) {
                query = query.andWhere { ActuatorsState.plantUuid.inList(plantsUuid) }
            }

            if (type != null) {
                query = query.andWhere { ActuatorsState.type eq type.value }
            }

            if(plantsUuid != null) {
                actuatorsState.addAll(query.distinctBy { it[ActuatorsState.plantUuid] }.map { mapToActuatorState(it) })
            } else {
                actuatorsState.addAll(query.map { mapToActuatorState(it) })
            }
        }
        return actuatorsState
    }

    /**
     * Insert a batch of SensorData.
     * @param greenhouseUuid UUID of the greenhouse that logged the data
     * @param sensorsData all the SensorData to insert
     */
    fun insertSensorDataBatch(greenhouseUuid: UUID, sensorsData: List<SensorData>) {
        transaction {

            SensorsData.batchInsert(sensorsData, body = {
                this[SensorsData.greenhouseUuid] = greenhouseUuid
                this[SensorsData.type] = it.type.value
                this[SensorsData.value] = it.value
                this[SensorsData.plantUuid] = it.plantUuid
                this[SensorsData.timestamp] = it.timestamp
            })
        }
    }

    /**
     * Insert a batch of ActuatorState.
     * @param greenhouseUuid UUID of the greenhouse that logged the data
     * @param actuatorsState all the SensorData to insert
     */
    fun insertActuatorStateBatch(greenhouseUuid: UUID, actuatorsState: List<ActuatorState>) {
        transaction {
            ActuatorsState.batchInsert(actuatorsState, body = {
                this[ActuatorsState.greenhouseUuid] = greenhouseUuid
                this[ActuatorsState.type] = it.type.value
                this[ActuatorsState.status] = it.value
                this[ActuatorsState.plantUuid] = it.plantUuid
                this[ActuatorsState.timestamp] = it.timestamp
            })
        }
    }
}

fun mapToSensorData(row: ResultRow): SensorData {
    return SensorData(
        SensorData.Type.valueOf(row[SensorsData.type].toString()),
        row[SensorsData.timestamp],
        row[SensorsData.value],
        row[SensorsData.plantUuid]
    )
}

fun mapToActuatorState(row: ResultRow): ActuatorState {
    return ActuatorState(
        ActuatorState.Type.valueOf(row[ActuatorsState.type].toString()),
        row[ActuatorsState.timestamp],
        row[ActuatorsState.status],
        row[ActuatorsState.plantUuid]
    )
}