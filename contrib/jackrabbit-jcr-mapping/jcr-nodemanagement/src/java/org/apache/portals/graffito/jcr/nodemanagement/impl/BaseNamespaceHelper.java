/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.portals.graffito.jcr.nodemanagement.impl;

/** Base class of all namespace helpers.
 *
 * @author <a href="mailto:okiessler@apache.org">Oliver Kiessler</a>
 */
public abstract class BaseNamespaceHelper {
    
    /** Default namespace if none is specified.
     */
    public static final String DEFAULT_NAMESPACE = "graffito";
    
    /** Default namespace URI if none is specified.
     */
    public static final String DEFAULT_NAMESPACE_URI = "http://incubator.apache.org/graffito";    
    
    /** Creates a new instance of BaseNamespaceHelper. */
    public BaseNamespaceHelper() {
    }
}
