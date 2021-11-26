package dev.xavierc.herbarium.api.repository

import dev.xavierc.herbarium.api.repository.Greenhouses.nullable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime
import java.util.*

object Plant : Table("plants") {
    val uuid: Column<UUID> = uuid("uuid").autoGenerate().primaryKey()
    val oldUuid: Column<UUID?> = uuid("old_uuid").nullable()
    val type: Column<Int> = integer("type") references PlantType.id
    val position: Column<Int> = integer("position")
    val overrideMoistureGoal: Column<Double?> = double("override_moisture_goal").nullable()
    val overrideLightExposureMinDuration: Column<Double?> = double("override_light_exposure_min_duration").nullable()
    val plantedAt: Column<DateTime> = datetime("planted_at")
}

object PlantType : Table("plant_types") {
    val id: Column<Int> = integer("id").autoIncrement().primaryKey()
    val name: Column<String> = varchar("name", length = 512)
    val moistureGoal: Column<Double> = double("moisture_goal").default(80.0)
    val lightExposureMinDuration: Column<Double> = double("light_exposure_min_duration").default(14.0)
}

class PlantRepository {

}