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

import griffon.core.GriffonApplication;
import griffon.core.env.Metadata;
import griffon.plugins.jcouchdb.DatabaseCallback;
import griffon.plugins.jcouchdb.DatabaseFactory;
import griffon.plugins.jcouchdb.DatabaseHandler;
import griffon.plugins.jcouchdb.DatabaseStorage;
import griffon.plugins.monitor.MBeanManager;
import org.codehaus.griffon.runtime.core.addon.AbstractGriffonAddon;
import org.codehaus.griffon.runtime.jmx.DatabaseStorageMonitor;
import org.jcouchdb.db.Database;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static griffon.util.ConfigUtils.getConfigValueAsBoolean;

/**
 * @author Andres Almiray
 */
@Named("jcouchdb")
public class JcouchdbAddon extends AbstractGriffonAddon {
    @Inject
    private DatabaseHandler databaseHandler;

    @Inject
    private DatabaseFactory databaseFactory;

    @Inject
    private DatabaseStorage databaseStorage;

    @Inject
    private MBeanManager mbeanManager;

    @Inject
    private Metadata metadata;

    @Override
    public void init(@Nonnull GriffonApplication application) {
        mbeanManager.registerMBean(new DatabaseStorageMonitor(metadata, databaseStorage));
    }

    public void onStartupStart(@Nonnull GriffonApplication application) {
        for (String databaseName : databaseFactory.getDatabaseNames()) {
            Map<String, Object> config = databaseFactory.getConfigurationFor(databaseName);
            if (getConfigValueAsBoolean(config, "connect_on_startup", false)) {
                databaseHandler.withJcouchdb(new DatabaseCallback<Object>() {
                    @Override
                    public Object handle(@Nonnull String databaseName, @Nonnull Database database) {
                        return null;
                    }
                });
            }
        }
    }

    public void onShutdownStart(@Nonnull GriffonApplication application) {
        for (String databaseName : databaseFactory.getDatabaseNames()) {
            databaseHandler.closeJcouchdb(databaseName);
        }
    }
}
