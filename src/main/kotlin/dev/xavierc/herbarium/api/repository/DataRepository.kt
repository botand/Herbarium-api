package dev.xavierc.herbarium.api.repository

import dev.xavierc.herbarium.api.models.ActuatorState
import dev.xavierc.herbarium.api.models.SensorData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.*

/**
 * Table containing all the data read by the sensors
 */
object SensorsData : Table("sensors_data") {
    val greenhouse_uuid: Column<UUID> =
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
    val greenhouse_uuid: Column<UUID> =
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

    fun getLastSensorData(
        greenhouseUuid: UUID? = null,
        plantUuid: UUID? = null,
        type: SensorData.Type? = null
    ): SensorData? {
        var sensorData: SensorData? = null

        transaction {
            var query = SensorsData.selectAll().orderBy(SensorsData.timestamp to SortOrder.ASC)
            if (greenhouseUuid != null) {
                query = query.adjustWhere { SensorsData.greenhouse_uuid eq greenhouseUuid }
            }

            if (plantUuid != null) {
                query = query.andWhere { SensorsData.greenhouse_uuid eq plantUuid }
            }

            if (type != null) {
                query = query.andWhere { SensorsData.type eq type.value }
            }

            val info = query.singleOrNull()

            if (info != null) {
                sensorData = mapToSensorData(info)
            }
        }

        return sensorData
    }

    /**
     * Insert a batch of SensorData.
     * @param greenhouseUuid UUID of the greenhouse that logged the data
     * @param sensorsData all the SensorData to insert
     */
    fun insertSensorDataBatch(greenhouseUuid: UUID, sensorsData: List<SensorData>) {
        transaction {

            SensorsData.batchInsert(sensorsData, body = {
                this[SensorsData.greenhouse_uuid] = greenhouseUuid
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
                this[ActuatorsState.greenhouse_uuid] = greenhouseUuid
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

fun mapToActuatorOrder(row: ResultRow): ActuatorState {
    return ActuatorState(
        ActuatorState.Type.valueOf(row[ActuatorsState.type].toString()),
        row[ActuatorsState.timestamp],
        row[ActuatorsState.status],
        row[ActuatorsState.plantUuid]
    )
}