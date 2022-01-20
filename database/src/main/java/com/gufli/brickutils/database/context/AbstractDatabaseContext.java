package com.gufli.brickutils.database.context;

import com.google.gson.JsonObject;
import com.gufli.brickutils.database.BaseModel;
import io.ebean.DB;
import io.ebean.DatabaseFactory;
import io.ebean.Transaction;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.datasource.DataSourceFactory;
import io.ebean.datasource.DataSourcePool;
import io.ebean.migration.MigrationConfig;
import io.ebean.migration.MigrationRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractDatabaseContext implements DatabaseContext {

    private final String dataSourceName;
    private DataSourcePool pool;

    public AbstractDatabaseContext(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public final <T> T withContextClassLoader(Callable<T> callable) throws Exception {
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        try {
            return callable.call();
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
    }

    public final void init(JsonObject config) throws SQLException {
        init(config.get("dsn").getAsString(), config.get("username").getAsString(), config.get("password").getAsString());
    }

    public final void init(String dsn, String username, String password) throws SQLException {
        init(dsn, username, password, "migrations");
    }

    public final void init(String dsn, String username, String password, String migrationsPath) throws SQLException {
        if ( pool != null ) {
            throw new IllegalStateException("This context has already been initialized.");
        }

        initInternal(dsn, username, password, migrationsPath);
    }

    private void initInternal(String dsn, String username, String password, String migrationsPath) throws SQLException {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUrl(dsn);
        dataSourceConfig.setUsername(username);
        dataSourceConfig.setPassword(password);

        String driver;
        if (dsn.startsWith("jdbc:h2")) {
            driver = "org.h2.Driver";
        } else if (dsn.startsWith("jdbc:mysql")) {
            driver = "com.mysql.cj.jdbc.Driver";
        } else {
            throw new IllegalArgumentException("Invalid dsn, driver not available");
        }

        try {
            Class.forName(driver);
            dataSourceConfig.setDriver(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        pool = DataSourceFactory.create(dataSourceName, dataSourceConfig);
        migrate(pool, migrationsPath);
        connect(pool);
    }

    private void migrate(DataSourcePool pool, String migrationsPath) throws SQLException {
        MigrationConfig config = new MigrationConfig();

        Connection conn = pool.getConnection();
        String platform = conn.getMetaData().getDatabaseProductName().toLowerCase();
        config.setMigrationPath(migrationsPath + "/" + platform);

        MigrationRunner runner = new MigrationRunner(config);
        runner.run(conn);
    }

    private void connect(DataSourcePool pool) {
        DatabaseConfig config = new DatabaseConfig();
        config.setDataSource(pool);
        config.setRegister(true);
        config.setDefaultServer(false);
        config.setName(dataSourceName);
        buildConfig(config);
        DatabaseFactory.create(config);
    }

    public final void shutdown() {
        if (pool != null) {
            pool.shutdown();
        }
    }

    public Connection getConnection() throws SQLException {
        return pool.getConnection();
    }

    /**
     * Override this to add the required bean classes and converters to this database
     */
    protected abstract void buildConfig(DatabaseConfig config);

    // UTILS

    public final CompletableFuture<Void> saveAsync(BaseModel... models) {
        return saveAsync(Arrays.asList(models));
    }

    public final CompletableFuture<Void> saveAsync(Collection<? extends BaseModel> models) {
        return CompletableFuture.runAsync(() -> save(models))
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void save(Collection<? extends BaseModel> models) {
        try (Transaction transaction = DB.byName(dataSourceName).beginTransaction()) {
            for (BaseModel m : models) {
                m.save();
            }

            transaction.commit();
        }
    }

    public final CompletableFuture<Void> deleteAsync(BaseModel... models) {
        return deleteAsync(Arrays.asList(models));
    }

    public final CompletableFuture<Void> deleteAsync(Collection<? extends BaseModel> models) {
        return CompletableFuture.runAsync(() -> delete(models))
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void delete(Collection<? extends BaseModel> models) {
        try (Transaction transaction = DB.byName(dataSourceName).beginTransaction()) {
            for (BaseModel m : models) {
                m.delete();
            }

            transaction.commit();
        }
    }

}
