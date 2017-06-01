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
package griffon.plugins.jcouchdb

import griffon.core.RunnableWithArgs
import griffon.core.GriffonApplication
import griffon.core.test.GriffonUnitRule
import griffon.inject.BindTo
import org.jcouchdb.db.Database
import org.jcouchdb.document.ViewResult
import org.junit.Rule
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject

@Unroll
@IgnoreIf({ !CouchAvailability.localhostAvailable })
class JcouchdbSpec extends Specification {
    static {
        System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', 'trace')
    }

    @Rule
    public final GriffonUnitRule griffon = new GriffonUnitRule()

    @Inject
    private DatabaseHandler databaseHandler

    @Inject
    private GriffonApplication application

    void 'Open and close default database'() {
        given:
        List eventNames = [
            'JcouchdbConnectStart', 'ConfigureJcouchdbJSONParser', 'JcouchdbConnectEnd',
            'JcouchdbDisconnectStart', 'JcouchdbDisconnectEnd'
        ]
        List events = []
        eventNames.each { name ->
            application.eventRouter.addEventListener(name, { Object... args ->
                events << [name: name, args: args]
            } as RunnableWithArgs)
        }

        when:
        databaseHandler.withJcouchdb { String databaseName, Database database ->
            true
        }
        databaseHandler.closeJcouchdb()
        // second call should be a NOOP
        databaseHandler.closeJcouchdb()

        then:
        events.size() == 5
        events.name == eventNames
    }

    void 'Connect to default database'() {
        expect:
        databaseHandler.withJcouchdb { String databaseName, Database database ->
            databaseName == 'default' && database
        }
    }

    void 'Bootstrap init is called'() {
        given:
        assert !bootstrap.initWitness

        when:
        databaseHandler.withJcouchdb { String databaseName, Database database -> }

        then:
        bootstrap.initWitness
        !bootstrap.destroyWitness
    }

    void 'Bootstrap destroy is called'() {
        given:
        assert !bootstrap.initWitness
        assert !bootstrap.destroyWitness

        when:
        databaseHandler.withJcouchdb { String databaseName, Database database -> }
        databaseHandler.closeJcouchdb()

        then:
        bootstrap.initWitness
        bootstrap.destroyWitness
    }

    void 'Can connect to #name database'() {
        expect:
        databaseHandler.withJcouchdb(name) { String databaseName, Database database ->
            databaseName == name && database
        }

        where:
        name       | _
        'default'  | _
        'internal' | _
        'people'   | _
    }

    void 'Bogus database name (#name) results in error'() {
        when:
        databaseHandler.withJcouchdb(name) { String databaseName, Database database ->
            true
        }

        then:
        thrown(IllegalArgumentException)

        where:
        name    | _
        null    | _
        ''      | _
        'bogus' | _
    }

    void 'Execute statements on people database'() {
        when:
        List peopleIn = databaseHandler.withJcouchdb('people') { String databaseName, Database database ->
            [[id: '1', name: 'Danno', lastname: 'Ferrin'],
             [id: '2', name: 'Andres', lastname: 'Almiray'],
             [id: '3', name: 'James', lastname: 'Williams'],
             [id: '4', name: 'Guillaume', lastname: 'Laforge'],
             [id: '5', name: 'Jim', lastname: 'Shingler'],
             [id: '6', name: 'Alexander', lastname: 'Klein'],
             [id: '7', name: 'Rene', lastname: 'Groeschke']].each { data ->
                database.createDocument(data)
                new Person(data)
            }
        }

        List peopleOut = databaseHandler.withJcouchdb('people') { String databaseName, Database database ->
            ViewResult<Map> result = database.listDocuments(null, null)
            result.rows.collect([]) { row ->
                Map json = database.getDocument(Map, row.id)
                new Person(id: json.id, name: json.name, lastname: json.lastname)
            }
        }

        then:
        peopleIn == peopleOut
    }

    @BindTo(JcouchdbBootstrap)
    private TestJcouchdbBootstrap bootstrap = new TestJcouchdbBootstrap()
}
