package dev.xavierc.herbarium.api.repository

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime
import java.util.*

/**
 * Table containing all the data read by the sensors
 */
object SensorDatas : Table("sensors_datas") {
    val uuid: Column<UUID> = uuid("uuid") references Greenhouses.uuid
    val type: Column<Char> = char("type").check {
        it.inList(listOf('M', 'L', 'T'))
    }
    val value: Column<Double> = double("value")
    val timestamp: Column<DateTime> = datetime("timestamp")
    val plantUuid: Column<UUID?> = (uuid("plant_uuid") references Plant.uuid).nullable()
}

/**
 * Table containing all the orders given to the actuators.
 */
object ActuactorsOrder : Table("actuators_order") {
    val uuid: Column<UUID> = uuid("uuid") references Greenhouses.uuid
    val type: Column<Char> = char("type").check {
        it.inList(listOf('V', 'L', 'P'))
    }
    val status: Column<Boolean> = bool("status")
    val timestamp: Column<DateTime> = datetime("timestamp")
    val plantUuid: Column<UUID?> = (uuid("plant_uuid") references Plant.uuid).nullable()
}

class DataRepository {

}