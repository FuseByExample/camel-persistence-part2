/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package org.jboss.fuse.examples.persistence.part2.dao;

import org.apache.openjpa.enhance.PCRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // see: https://issues.apache.org/jira/browse/OPENJPA-2538
        PCRegistry.deRegister(context.getBundle().adapt(BundleWiring.class).getClassLoader());
    }

}
