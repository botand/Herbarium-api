package dev.xavierc.herbarium.api.repositories

import dev.xavierc.herbarium.api.applicationDatabaseConfiguration
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    private val appConfig = applicationDatabaseConfiguration()

    private val username = appConfig.property("username").getString()
    private val password = appConfig.property("password").getString()
    private val url = appConfig.property("url").getString()
    private val port = appConfig.property("port").getString()
    private val dbName = appConfig.property("db_name").getString()

    fun init() {
        // Initialize DB connection
        Database.connect(
            "jdbc:postgresql://$url:$port/$dbName",
            driver = "org.postgresql.Driver",
            user = username,
            password = password
        )

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                SensorsData,
                ActuatorsState,
                PlantTypes,
                Plants,
                Users,
                Greenhouses
            )

            if(PlantTypes.select { PlantTypes.id.eq(1) and PlantTypes.name.eq("default") }.count() == 0) {
                PlantTypes.insert {
                    it[name] = "default"
                }
            }
        }
    }
}
