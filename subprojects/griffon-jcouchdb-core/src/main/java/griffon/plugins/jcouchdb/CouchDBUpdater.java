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
package griffon.plugins.jcouchdb;

import org.jcouchdb.db.Database;
import org.jcouchdb.document.DesignDocument;

import java.io.IOException;
import java.util.List;

/**
 * @author Andres Almiray
 */
public interface CouchDBUpdater {
    void setDatabase(Database database);

    void setCreateDatabase(boolean createDatabase);

    List<DesignDocument> updateDesignDocuments() throws IOException;
}
