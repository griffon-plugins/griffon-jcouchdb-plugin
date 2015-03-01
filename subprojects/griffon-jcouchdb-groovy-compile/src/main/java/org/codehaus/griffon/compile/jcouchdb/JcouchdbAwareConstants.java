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
package org.codehaus.griffon.compile.jcouchdb;

import org.codehaus.griffon.compile.core.BaseConstants;
import org.codehaus.griffon.compile.core.MethodDescriptor;

import static org.codehaus.griffon.compile.core.MethodDescriptor.annotatedMethod;
import static org.codehaus.griffon.compile.core.MethodDescriptor.annotatedType;
import static org.codehaus.griffon.compile.core.MethodDescriptor.annotations;
import static org.codehaus.griffon.compile.core.MethodDescriptor.args;
import static org.codehaus.griffon.compile.core.MethodDescriptor.method;
import static org.codehaus.griffon.compile.core.MethodDescriptor.type;
import static org.codehaus.griffon.compile.core.MethodDescriptor.typeParams;
import static org.codehaus.griffon.compile.core.MethodDescriptor.types;

/**
 * @author Andres Almiray
 */
public interface JcouchdbAwareConstants extends BaseConstants {
    String DATABASE_TYPE = "java.sql.Database";
    String DATABASE_HANDLER_TYPE = "griffon.plugins.jcouchdb.DatabaseHandler";
    String DATABASE_CALLBACK_TYPE = "griffon.plugins.jcouchdb.DatabaseCallback";
    String DATABASE_HANDLER_PROPERTY = "databaseHandler";
    String DATABASE_HANDLER_FIELD_NAME = "this$" + DATABASE_HANDLER_PROPERTY;

    String METHOD_WITH_DATABASE = "withJcouchdb";
    String METHOD_CLOSE_DATABASE = "closeJcouchdb";
    String DATABASE_NAME = "databaseName";
    String CALLBACK = "callback";

    MethodDescriptor[] METHODS = new MethodDescriptor[]{
        method(
            type(VOID),
            METHOD_CLOSE_DATABASE
        ),
        method(
            type(VOID),
            METHOD_CLOSE_DATABASE,
            args(annotatedType(types(type(JAVAX_ANNOTATION_NONNULL)), JAVA_LANG_STRING))
        ),

        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            type(R),
            typeParams(R),
            METHOD_WITH_DATABASE,
            args(annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), DATABASE_CALLBACK_TYPE, R))
        ),
        annotatedMethod(
            types(type(JAVAX_ANNOTATION_NONNULL)),
            type(R),
            typeParams(R),
            METHOD_WITH_DATABASE,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), DATABASE_CALLBACK_TYPE, R))
        )
    };
}
