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

import griffon.core.Configuration;
import griffon.core.addon.GriffonAddon;
import griffon.core.injection.Module;
import griffon.plugins.jcouchdb.CouchDBUpdater;
import griffon.plugins.jcouchdb.DatabaseFactory;
import griffon.plugins.jcouchdb.DatabaseHandler;
import griffon.plugins.jcouchdb.DatabaseStorage;
import org.codehaus.griffon.runtime.core.injection.AbstractModule;
import org.codehaus.griffon.runtime.util.ResourceBundleProvider;
import org.kordamp.jipsy.ServiceProviderFor;

import javax.inject.Named;
import java.util.ResourceBundle;

import static griffon.util.AnnotationUtils.named;

/**
 * @author Andres Almiray
 */
@Named("jcouchdb")
@ServiceProviderFor(Module.class)
public class JcouchdbModule extends AbstractModule {
    @Override
    protected void doConfigure() {
        // tag::bindings[]
        bind(ResourceBundle.class)
            .withClassifier(named("jcouchdb"))
            .toProvider(new ResourceBundleProvider("Jcouchdb"))
            .asSingleton();

        bind(Configuration.class)
            .withClassifier(named("jcouchdb"))
            .to(DefaultJcouchdbConfiguration.class)
            .asSingleton();

        bind(DatabaseStorage.class)
            .to(DefaultDatabaseStorage.class)
            .asSingleton();

        bind(DatabaseFactory.class)
            .to(DefaultDatabaseFactory.class)
            .asSingleton();

        bind(DatabaseHandler.class)
            .to(DefaultDatabaseHandler.class)
            .asSingleton();

        bind(CouchDBUpdater.class)
            .to(DefaultCouchDBUpdater.class)
            .asSingleton();

        bind(GriffonAddon.class)
            .to(JcouchdbAddon.class)
            .asSingleton();
        // end::bindings[]
    }
}
