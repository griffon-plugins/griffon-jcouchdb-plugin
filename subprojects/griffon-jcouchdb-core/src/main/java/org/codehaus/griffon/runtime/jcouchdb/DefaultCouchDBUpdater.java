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

import griffon.plugins.jcouchdb.CouchDBUpdater;
import org.jcouchdb.document.DesignDocument;
import org.jcouchdb.util.AbstractCouchDBUpdater;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Andres Almiray
 */
public class DefaultCouchDBUpdater extends AbstractCouchDBUpdater implements CouchDBUpdater {
    @Override
    protected List<DesignDocument> readDesignDocuments() throws IOException {
        return Collections.emptyList();
    }
}