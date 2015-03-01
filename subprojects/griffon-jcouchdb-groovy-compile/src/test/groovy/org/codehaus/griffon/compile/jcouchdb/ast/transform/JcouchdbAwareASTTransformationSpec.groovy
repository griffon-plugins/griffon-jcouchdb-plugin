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
package org.codehaus.griffon.compile.jcouchdb.ast.transform

import griffon.plugins.jcouchdb.DatabaseHandler
import spock.lang.Specification

import java.lang.reflect.Method

/**
 * @author Andres Almiray
 */
class JcouchdbAwareASTTransformationSpec extends Specification {
    def 'DatabaseAwareASTTransformation is applied to a bean via @DatabaseAware'() {
        given:
        GroovyShell shell = new GroovyShell()

        when:
        def bean = shell.evaluate('''import griffon.transform.JcouchdbAware
        @JcouchdbAware
        class Bean { }
        new Bean()
        ''')

        then:
        bean instanceof DatabaseHandler
        DatabaseHandler.methods.every { Method target ->
            bean.class.declaredMethods.find { Method candidate ->
                candidate.name == target.name &&
                    candidate.returnType == target.returnType &&
                    candidate.parameterTypes == target.parameterTypes &&
                    candidate.exceptionTypes == target.exceptionTypes
            }
        }
    }

    def 'DatabaseAwareASTTransformation is not applied to a DatabaseHandler subclass via @DatabaseAware'() {
        given:
        GroovyShell shell = new GroovyShell()

        when:
        def bean = shell.evaluate('''
        import griffon.plugins.jcouchdb.DatabaseCallback
        import griffon.plugins.jcouchdb.DatabaseCallback
        import griffon.plugins.jcouchdb.DatabaseHandler
        import griffon.transform.JcouchdbAware

        import javax.annotation.Nonnull
        @JcouchdbAware
        class DatabaseHandlerBean implements DatabaseHandler {
            @Override
            public <R> R withJcouchdb(@Nonnull DatabaseCallback<R> callback)  {
                return null
            }
            @Override
            public <R> R withJcouchdb(@Nonnull String databaseName, @Nonnull DatabaseCallback<R> callback) {
                 return null
            }
            @Override
            void closeJcouchdb(){}
            @Override
            void closeJcouchdb(@Nonnull String databaseName){}
        }
        new DatabaseHandlerBean()
        ''')

        then:
        bean instanceof DatabaseHandler
        DatabaseHandler.methods.every { Method target ->
            bean.class.declaredMethods.find { Method candidate ->
                candidate.name == target.name &&
                    candidate.returnType == target.returnType &&
                    candidate.parameterTypes == target.parameterTypes &&
                    candidate.exceptionTypes == target.exceptionTypes
            }
        }
    }
}
