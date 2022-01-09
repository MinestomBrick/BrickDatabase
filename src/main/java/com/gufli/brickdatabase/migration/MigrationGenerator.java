package com.gufli.brickdatabase.migration;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.dbmigration.DbMigration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MigrationGenerator {

    private final DbMigration dbMigration;
    private final String dataSourceName;

    private final List<Class<?>> classes = new ArrayList<>();

    public MigrationGenerator(String dataSourceName, Path path, Platform... platforms) {
        this.dataSourceName = dataSourceName;

        this.dbMigration = DbMigration.create();
        this.dbMigration.setPathToResources(path.toString());
        this.dbMigration.setMigrationPath("migrations");

        for ( Platform platform : platforms ) {
            this.dbMigration.addPlatform(platform);
        }
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public void generate() throws IOException {
        // create mock db with same name as used in the app
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUrl("jdbc:h2:mem:migrationdb;");
        dataSourceConfig.setUsername("dbuser");
        dataSourceConfig.setPassword("");

        // use same datasource name
        DatabaseConfig config = new DatabaseConfig();
        config.setDataSourceConfig(dataSourceConfig);
        config.setName(dataSourceName);
        classes.forEach(config::addClass);


        Database database = DatabaseFactory.create(config);
        dbMigration.setServer(database);

        dbMigration.generateMigration();
    }

}
