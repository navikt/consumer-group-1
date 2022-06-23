package no.nav.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import java.time.Duration
import javax.sql.DataSource

class Database(private val dataSource: DataSource = DataSourceBuilder(System.getenv()).getDataSource()) {
    internal fun settFlag() {
        val query = """INSERT INTO flag_tabell(flag) VALUES (:flag) ON CONFLICT DO NOTHING;"""
        return sessionOf(dataSource).use { session ->
                session.execute(
                    queryOf(query, mapOf("flag" to "true"))
                )
            }
    }

    internal fun flag(): Boolean {
        val query = """SELECT flag FROM flag_tabell ORDER BY id DESC LIMIT 1;"""
        return sessionOf(dataSource)
            .use { session ->
                session.run(
                    queryOf(query)
                        .map { it.string("flag").toBoolean() }
                        .asSingle
                ) == true
            }
    }

    internal fun migrate() {
        DataSourceBuilder(System.getenv()).migrate()
    }

}

internal class DataSourceBuilder(env: Map<String, String>) {

    private val hikariConfig = HikariConfig().apply {
        jdbcUrl = env["DATABASE_JDBC_URL"] ?: String.format(
            "jdbc:postgresql://%s:%s/%s",
            requireNotNull(env["DATABASE_HOST"]) { "database host must be set if jdbc url is not provided" },
            requireNotNull(env["DATABASE_PORT"]) { "database port must be set if jdbc url is not provided" },
            requireNotNull(env["DATABASE_DATABASE"]) { "database name must be set if jdbc url is not provided" })
        username = requireNotNull(env["DATABASE_USERNAME"]) { "databasebrukernavn må settes" }
        password = requireNotNull(env["DATABASE_PASSWORD"]) { "databasepassord må settes" }
        maximumPoolSize = 1
        connectionTimeout = Duration.ofSeconds(30).toMillis()
        maxLifetime = Duration.ofMinutes(30).toMillis()
        initializationFailTimeout = Duration.ofMinutes(1).toMillis()
    }

    internal fun getDataSource() = HikariDataSource(hikariConfig)

    internal fun migrate() {
        logger.info("Migrerer database")
        getDataSource().use { dataSource ->
            Flyway.configure()
                .dataSource(dataSource)
                .lockRetryCount(-1)
                .load()
                .migrate()
        }
        logger.info("Migrering ferdig!")
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(DataSourceBuilder::class.java)
    }
}