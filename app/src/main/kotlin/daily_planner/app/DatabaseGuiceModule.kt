package daily_planner.app

import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Provides
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import daily_planner.app.dao.TodoRepository
import io.github.oshai.KotlinLogging
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.core.statement.SqlLogger
import org.jdbi.v3.postgres.PostgresPlugin
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import javax.inject.Named
import javax.inject.Singleton
import javax.sql.DataSource

class DatabaseGuiceModule : AbstractModule() {
    @Provides
    @Singleton
    @Named("todoDataSource")
    private fun createDataSource(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://localhost:5432/todo-db"
        config.username = "postgres"
        config.password = ""
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

        return HikariDataSource(config);
    }

    @Provides
    @Singleton
    @Named("todoJdbi")
    private fun createJdbiProvider(@Named("todoDataSource") ds : DataSource): Jdbi {
        return Jdbi.create(ds)
            .installPlugin(PostgresPlugin())
            .installPlugin(SqlObjectPlugin())
            .installPlugin(KotlinPlugin())
            .installPlugin(KotlinSqlObjectPlugin())
            .setSqlLogger(SqlLogger.NOP_SQL_LOGGER)
    }

    @Provides
    @Singleton
    @Inject
    fun createTodoRepository( @Named("todoJdbi") jdbi : Jdbi): TodoRepository {
        return TodoRepository(jdbi)
    }
}