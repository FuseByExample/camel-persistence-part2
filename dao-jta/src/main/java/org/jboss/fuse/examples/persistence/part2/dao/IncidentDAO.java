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

import java.util.List;

import org.jboss.fuse.examples.persistence.part2.model.Incident;

public interface IncidentDAO {

    public Incident getIncident(long id);

    public List<Incident> findIncidents();

    public List<Incident> findIncidentsByRef(String ref);

    public void saveIncident(Incident incident);

    public void removeIncident(long id);

}
