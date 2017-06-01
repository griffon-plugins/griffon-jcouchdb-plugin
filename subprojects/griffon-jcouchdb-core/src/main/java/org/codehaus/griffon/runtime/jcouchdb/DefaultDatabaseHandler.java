/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.jcouchdb;

import griffon.plugins.jcouchdb.DatabaseCallback;
import griffon.plugins.jcouchdb.DatabaseFactory;
import griffon.plugins.jcouchdb.DatabaseHandler;
import griffon.plugins.jcouchdb.DatabaseStorage;
import org.jcouchdb.db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultDatabaseHandler implements DatabaseHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDatabaseHandler.class);
    private static final String ERROR_DATASBASE_BLANK = "Argument 'databaseName' must not be blank";
    private static final String ERROR_CONNECTION_SOURCE_NULL = "Argument 'database' must not be null";
    private static final String ERROR_CALLBACK_NULL = "Argument 'callback' must not be null";

    private final DatabaseFactory databaseFactory;
    private final DatabaseStorage databaseStorage;

    @Inject
    public DefaultDatabaseHandler(@Nonnull DatabaseFactory databaseFactory, @Nonnull DatabaseStorage databaseStorage) {
        this.databaseFactory = requireNonNull(databaseFactory, "Argument 'databaseFactory' must not be null");
        this.databaseStorage = requireNonNull(databaseStorage, "Argument 'databaseStorage' must not be null");
    }

    @Nullable
    @Override
    public <R> R withJcouchdb(@Nonnull DatabaseCallback<R> callback) {
        return withJcouchdb(DefaultDatabaseFactory.KEY_DEFAULT, callback);
    }

    @Nullable
    @Override
    public <R> R withJcouchdb(@Nonnull String databaseName, @Nonnull DatabaseCallback<R> callback) {
        requireNonBlank(databaseName, ERROR_DATASBASE_BLANK);
        requireNonNull(callback, ERROR_CALLBACK_NULL);

        Database database = getDatabase(databaseName);
        return doWithDatabase(databaseName, database, callback);
    }

    @Nullable
    @SuppressWarnings("ThrowFromFinallyBlock")
    static <R> R doWithDatabase(@Nonnull String databaseName, @Nonnull Database database, @Nonnull DatabaseCallback<R> callback) {
        requireNonBlank(databaseName, ERROR_DATASBASE_BLANK);
        requireNonNull(database, ERROR_CONNECTION_SOURCE_NULL);
        requireNonNull(callback, ERROR_CALLBACK_NULL);

        LOG.debug("Executing statements on database '{}'", databaseName);
        return callback.handle(databaseName, database);
    }

    @Override
    public void closeJcouchdb() {
        closeJcouchdb(DefaultDatabaseFactory.KEY_DEFAULT);
    }

    @Override
    public void closeJcouchdb(@Nonnull String databaseName) {
        Database database = databaseStorage.get(databaseName);
        if (database != null) {
            databaseFactory.destroy(databaseName, database);
            databaseStorage.remove(databaseName);
        }
    }

    @Nonnull
    private Database getDatabase(@Nonnull String databaseName) {
        Database database = databaseStorage.get(databaseName);
        if (database == null) {
            database = databaseFactory.create(databaseName);
            databaseStorage.set(databaseName, database);
        }
        return database;
    }
}
