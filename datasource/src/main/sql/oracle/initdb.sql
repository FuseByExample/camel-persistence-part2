--
-- Copyright 2005-2015 Red Hat, Inc.
--
-- Red Hat licenses this file to you under the Apache License, version
-- 2.0 (the "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--    http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
-- implied.  See the License for the specific language governing
-- permissions and limitations under the License.
--

-- Initial setup of Oracle database

drop table t_incident;
drop sequence t_incident_incident_id_seq;

create table t_incident (
  incident_id integer not null primary key,
  incident_ref varchar2(55),
  incident_date timestamp,
  given_name varchar2(35),
  family_name varchar2(35),
  summary varchar2(35),
  details varchar2(255),
  email varchar2(60),
  phone varchar2(35),
  creation_date timestamp,
  creation_user varchar2(255)
);

create sequence t_incident_incident_id_seq;

insert into t_incident (incident_id, incident_ref, incident_date, given_name, family_name, summary, details, email, phone)
values (t_incident_incident_id_seq.nextval, '001', to_timestamp('2015-01-23 00:00:00', 'yyyy-mm-dd hh24:mi:ss'), 'Charles', 'Moulliard', 'incident webinar-001', 'This is a report incident for webinar-001', 'cmoulliard@fusesource.com', '+111 10 20 300');
insert into t_incident (incident_id, incident_ref, incident_date, given_name, family_name, summary, details, email, phone)
values (t_incident_incident_id_seq.nextval, '002', to_timestamp('2015-01-24 00:00:00', 'yyyy-mm-dd hh24:mi:ss'), 'Charles', 'Moulliard', 'incident webinar-002', 'This is a report incident for webinar-002', 'cmoulliard@fusesource.com', '+111 10 20 300');
insert into t_incident (incident_id, incident_ref, incident_date, given_name, family_name, summary, details, email, phone)
values (t_incident_incident_id_seq.nextval, '003', to_timestamp('2015-01-25 00:00:00', 'yyyy-mm-dd hh24:mi:ss'), 'Charles', 'Moulliard', 'incident webinar-003', 'This is a report incident for webinar-003', 'cmoulliard@fusesource.com', '+111 10 20 300');
insert into t_incident (incident_id, incident_ref, incident_date, given_name, family_name, summary, details, email, phone)
values (t_incident_incident_id_seq.nextval, '004', to_timestamp('2015-01-26 00:00:00', 'yyyy-mm-dd hh24:mi:ss'), 'Charles', 'Moulliard', 'incident webinar-004', 'This is a report incident for webinar-004', 'cmoulliard@fusesource.com', '+111 10 20 300');
