/*
 * Copyright 2014-2015 the original author or authors.
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

import griffon.core.Configuration;
import griffon.core.GriffonApplication;
import griffon.core.injection.Injector;
import griffon.exceptions.GriffonException;
import griffon.plugins.jcouchdb.CouchDBUpdater;
import griffon.plugins.jcouchdb.DatabaseFactory;
import griffon.plugins.jcouchdb.JcouchdbBootstrap;
import griffon.util.GriffonNameUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.codehaus.griffon.runtime.core.storage.AbstractObjectFactory;
import org.jcouchdb.db.Database;
import org.svenson.JSON;
import org.svenson.JSONConfig;
import org.svenson.JSONParser;
import org.svenson.converter.DefaultTypeConverterRepository;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static griffon.util.ConfigUtils.getConfigValueAsInt;
import static griffon.util.ConfigUtils.getConfigValueAsString;
import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultDatabaseFactory extends AbstractObjectFactory<Database> implements DatabaseFactory {
    private static final String ERROR_DATASOURCE_BLANK = "Argument 'databaseName' must not be blank";

    private final Set<String> databaseNames = new LinkedHashSet<>();

    @Inject
    private Injector injector;

    @Inject
    private CouchDBUpdater couchDBUpdater;

    @Inject
    public DefaultDatabaseFactory(@Nonnull @Named("jcouchdb") Configuration configuration, @Nonnull GriffonApplication application) {
        super(configuration, application);
        databaseNames.add(KEY_DEFAULT);

        if (configuration.containsKey(getPluralKey())) {
            Map<String, Object> jcouchdbs = (Map<String, Object>) configuration.get(getPluralKey());
            databaseNames.addAll(jcouchdbs.keySet());
        }
    }

    @Nonnull
    @Override
    public Set<String> getDatabaseNames() {
        return databaseNames;
    }

    @Nonnull
    @Override
    public Map<String, Object> getConfigurationFor(@Nonnull String databaseName) {
        requireNonBlank(databaseName, ERROR_DATASOURCE_BLANK);
        return narrowConfig(databaseName);
    }

    @Nonnull
    @Override
    protected String getSingleKey() {
        return "database";
    }

    @Nonnull
    @Override
    protected String getPluralKey() {
        return "databases";
    }

    @Nonnull
    @Override
    public Database create(@Nonnull String name) {
        requireNonBlank(name, ERROR_DATASOURCE_BLANK);
        Map<String, Object> config = narrowConfig(name);

        event("JcouchdbConnectStart", asList(name, config));

        Database database = createDatabase(config, name);

        for (Object o : injector.getInstances(JcouchdbBootstrap.class)) {
            ((JcouchdbBootstrap) o).init(name, database);
        }

        event("JcouchdbConnectEnd", asList(name, config, database));

        return database;
    }

    @Override
    public void destroy(@Nonnull String name, @Nonnull Database instance) {
        requireNonBlank(name, ERROR_DATASOURCE_BLANK);
        requireNonNull(instance, "Argument 'instance' must not be null");
        Map<String, Object> config = narrowConfig(name);

        event("JcouchdbDisconnectStart", asList(name, config, instance));

        for (Object o : injector.getInstances(JcouchdbBootstrap.class)) {
            ((JcouchdbBootstrap) o).destroy(name, instance);
        }

        event("JcouchdbDisconnectEnd", asList(name, config));
    }

    @Nonnull
    private Database createDatabase(@Nonnull Map<String, Object> config, @Nonnull String name) {
        String host = getConfigValueAsString(config, "host", "localhost");
        int port = getConfigValueAsInt(config, "port", 5984);
        String datastore = getConfigValueAsString(config, "datastore");
        String username = getConfigValueAsString(config, "username", "");
        String password = getConfigValueAsString(config, "password", "");
        String realm = getConfigValueAsString(config, "password", "");
        String scheme = getConfigValueAsString(config, "password", "");

        requireNonBlank(host, "Configuration value for 'host' in database." + name + " must not be blank");
        requireNonBlank(datastore, "Configuration value for 'datastore' in database." + name + " must not be blank");

        Database db = new Database(host, port, datastore);

        // check to see if there are any user credentials and set them
        if (!GriffonNameUtils.isBlank(username)) {
            Credentials credentials = new UsernamePasswordCredentials(username, password);
            AuthScope authScope = new AuthScope(host, port);

            // set the realm and scheme if they are set
            if (!GriffonNameUtils.isBlank(realm) || !GriffonNameUtils.isBlank(scheme)) {
                authScope = new AuthScope(host, port, realm, scheme);
            }

            db.getServer().setCredentials(authScope, credentials);
        }

        DefaultTypeConverterRepository typeConverterRepository = new DefaultTypeConverterRepository();
        JsonDateConverter dateConverter = new JsonDateConverter();
        typeConverterRepository.addTypeConverter(dateConverter);

        JSON generator = new JSON();
        generator.setIgnoredProperties(Arrays.asList("metaClass"));
        generator.setTypeConverterRepository(typeConverterRepository);
        generator.registerTypeConversion(java.util.Date.class, dateConverter);
        generator.registerTypeConversion(java.sql.Date.class, dateConverter);
        generator.registerTypeConversion(java.sql.Timestamp.class, dateConverter);

        JSONParser parser = new JSONParser();
        parser.setTypeConverterRepository(typeConverterRepository);
        parser.registerTypeConversion(java.util.Date.class, dateConverter);
        parser.registerTypeConversion(java.sql.Date.class, dateConverter);
        parser.registerTypeConversion(java.sql.Timestamp.class, dateConverter);
        event("ConfigureJcouchdbJSONParser", asList(name, config, parser));

        db.setJsonConfig(new JSONConfig(generator, parser));

        // setup views
        try {
            couchDBUpdater.setDatabase(db);
            couchDBUpdater.updateDesignDocuments();
        } catch (IOException e) {
            throw new GriffonException(e);
        }

        return db;
    }
}
