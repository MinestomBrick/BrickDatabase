package com.gufli.brickdatabase;

import com.gufli.brickdatabase.converters.ColorConverter;
import com.gufli.brickdatabase.converters.ItemStackConverter;
import com.gufli.brickdatabase.converters.NBTCompoundConverter;
import com.gufli.brickdatabase.converters.PosConverter;
import com.gufli.brickdatabase.migration.MigrationGenerator;
import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseContextTests {

    private static DatabaseContext databaseContext;
    private static Set<Class<?>> classes = new HashSet<>();

    private static File tempdir;

    @BeforeAll
    public static void init() {
        classes.add(TestBean.class);
        classes.add(ColorConverter.class);
        classes.add(ItemStackConverter.class);
        classes.add(NBTCompoundConverter.class);
        classes.add(PosConverter.class);
        
        databaseContext = new DatabaseContext("TestDatabase") {
            @Override
            protected void buildConfig(DatabaseConfig config) {
                classes.forEach(config::addClass);
            }
        };

        tempdir = new File("tmp");
        tempdir.mkdirs();
    }

    @AfterAll
    public static void finish() {
        databaseContext.shutdown();
        deleteDirectory(tempdir);
    }

    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    @Test
    @Order(1)
    public void generateMigrationFiles() throws IOException {
        MigrationGenerator generator = new MigrationGenerator("TestDatabase",
                tempdir.toPath(), Platform.H2);
        classes.forEach(generator::addClass);
        generator.generate();

        File[] files = new File(tempdir, "migrations/model").listFiles();
        assertNotNull(files);
        assertEquals(1, files.length);
    }

    @Test
    @Order(2)
    public void initializeDatabase() throws SQLException {
        databaseContext.init("jdbc:h2:mem:migrationdb;", "dbuser", "",
                "filesystem:" + tempdir.toPath().resolve("migrations"));

        ResultSet rs = databaseContext.dataSourcePool().getConnection()
                .prepareStatement("SELECT COUNT(*) FROM db_migration").executeQuery();

        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
    }

}
