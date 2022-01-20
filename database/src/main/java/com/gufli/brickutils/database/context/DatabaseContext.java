package com.gufli.brickutils.database.context;

import com.gufli.brickutils.database.BaseModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface DatabaseContext {

    void shutdown();

    Connection getConnection() throws SQLException;

    // UTILS

    CompletableFuture<Void> saveAsync(BaseModel... models);

    CompletableFuture<Void> saveAsync(Collection<? extends BaseModel> models);

    CompletableFuture<Void> deleteAsync(BaseModel... models);

    CompletableFuture<Void> deleteAsync(Collection<? extends BaseModel> models);

}
