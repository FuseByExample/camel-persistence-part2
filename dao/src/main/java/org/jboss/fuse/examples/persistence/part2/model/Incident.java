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
package org.jboss.fuse.examples.persistence.part2.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(separator = ",")
@Entity
@Access(AccessType.FIELD)
@Table(name = "T_INCIDENT")
public class Incident implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INCIDENT_ID")
    private long incidentId;

    @Basic
    @Column(name = "INCIDENT_REF", length = 55)
    @DataField(pos = 1)
    private String incidentRef;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "INCIDENT_DATE")
    @DataField(pos = 2, pattern = "dd-mm-yyyy")
    private Date incidentDate;

    @Basic
    @DataField(pos = 3)
    @Column(name = "GIVEN_NAME", length = 35)
    private String givenName;

    @Basic
    @DataField(pos = 4)
    @Column(name = "FAMILY_NAME", length = 35)
    private String familyName;

    @Basic
    @Column(name = "SUMMARY", length = 35)
    @DataField(pos = 5)
    private String summary;

    @Basic
    @Column(name = "DETAILS", length = 255)
    @DataField(pos = 6)
    private String details;

    @Basic
    @Column(name = "EMAIL", length = 60)
    @DataField(pos = 7)
    private String email;

    @Basic
    @Column(name = "PHONE", length = 35)
    @DataField(pos = 8)
    private String phone;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATION_DATE")
    private Date creationDate;

    @Basic
    @Column(name = "CREATION_USER")
    private String creationUser;

    public long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(long incidentId) {
        this.incidentId = incidentId;
    }

    public String getIncidentRef() {
        return incidentRef;
    }

    public void setIncidentRef(String incidentRef) {
        this.incidentRef = incidentRef;
    }

    public Date getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(Date incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreationUser() {
        return creationUser;
    }

    public void setCreationUser(String creationUser) {
        this.creationUser = creationUser;
    }

    @Override
    public String toString() {
        return "Incident{" +
                "incidentId=" + incidentId +
                ", incidentRef='" + incidentRef + '\'' +
                ", incidentDate=" + incidentDate +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", summary='" + summary + '\'' +
                ", details='" + details + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", creationDate=" + creationDate +
                ", creationUser='" + creationUser + '\'' +
                '}';
    }

}
