/*
 * Copyright $today.year the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package griffon.plugins.jcouchdb

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

/**
 * Submitted by Jörn Huxhorn (@huxi) via Twitter
 * http://pastebin.com/hiJZqVym
 */
class CouchAvailability {
    static boolean isLocalhostAvailable() {
        return isHostAvailable('http://127.0.0.1:5984/')
    }

    static boolean isHostAvailable(String host) {

        boolean result = false;
        try {

            def http = new HTTPBuilder(host)

            http.request(Method.GET, ContentType.JSON) {
                uri.path = '/'

                // response handler for a success response code:
                response.success = { resp, json ->
                    // parse the JSON response object:
                    result = json.couchdb == 'Welcome' && json.version
                }

                // handler for any failure status code:
                response.failure = { resp ->
                    println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
                }
            }
        }
        catch (Throwable t) {
            // ignore
        }
        return result;
    }

    static void main(String[] args) {
        println('CouchAvailability.isLocalhostAvailable(): ' + isLocalhostAvailable())
    }
}