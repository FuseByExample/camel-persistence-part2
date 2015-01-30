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
package org.jboss.fuse.examples.persistence.part2.dao.impl;

import org.jboss.fuse.examples.persistence.part2.model.Incident;
import org.jboss.fuse.examples.persistence.part2.dao.IncidentDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class IncidentDAOImpl implements IncidentDAO {

    public static Logger LOG = LoggerFactory.getLogger(IncidentDAOImpl.class);

    private EntityManager entityManager;

    private static final String FIND_INCIDENT_BY_REFERENCE = "select i from Incident as i where i.incidentRef = :ref";
    private static final String FIND_INCIDENT = "select i from Incident as i";

    @Override
    public Incident getIncident(long id) {
        return this.entityManager.find(Incident.class, id);
    }

    @Override
    public List<Incident> findIncidents() {
        Query q = this.entityManager.createQuery(FIND_INCIDENT, Incident.class);
        return q.getResultList();
    }

    @Override
    public List<Incident> findIncidentsByRef(String ref) {
        Query q = this.entityManager.createQuery(FIND_INCIDENT_BY_REFERENCE, Incident.class);
        q.setParameter("ref", ref);
        return q.getResultList();
    }

    @Override
    public void saveIncident(Incident incident) {
        LOG.info("Incident to be saved: {}", incident.toString());
        this.entityManager.persist(incident);
    }

    @Override
    public void removeIncident(long id) {
        Incident toRemove = this.entityManager.find(Incident.class, id);
        this.entityManager.remove(toRemove);
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
