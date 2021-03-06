<!--

     Copyright 2005-2015 Red Hat, Inc.

     Red Hat licenses this file to you under the Apache License, version
     2.0 (the "License"); you may not use this file except in compliance
     with the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
     implied.  See the License for the specific language governing
     permissions and limitations under the License.

-->
NOTE: This project is not supported on JBoss Fuse version 6.3 or later


# Description

This is the material accompanying the presentation of webinar part II - Transaction Management with Fuse ESB, Camel and Persistent EIPs.
It covers the different demos made during the talk and is organized in the following projects:

* `datasource`: OSGi bundle project containing datasource definitions - both database specific and generic datasources providing pooling and JTA capabilities
* `dao`:
* `dao-jta`:
* `route-two-tx-managers`:
* `route-one-tx-manager`:
* `aggregator`:
* `idempotent`:
* `features`: Karaf features to be deployed on JBoss Fuse

# Initial setup

We will refer to the root directory of `camel-persistence-part2` project as `$PROJECT_HOME`.

# Setting up standalone databases

## H2 Database

This is the simpliest way to configure local database server, accessible over TCP protocol. H2 database supports XA (two phase commit, 2PC) transactions.

1. Download H2 database from [H2 website](http://www.h2database.com/html/main.html) (current version: `1.4.185` from 2015-01-16)
2. Unzip the archive to the location of choice (referred to as `$H2_HOME`)
3. Create `$H2_HOME/databases` which will contain database files
4. Start H2 server and web console using these shell commands:

        $ cd $H2_HOME
        $ java -cp bin/h2*.jar org.h2.tools.Server -tcp -web -baseDir databases

    This will start H2 database (TCP port 9092) and management console (http://localhost:8082) URL.

    Setting `-baseDir` controls the location of created databases.

5. Create `reportdb` database from the management console (http://localhost:8082):

    1. On the `login.jsp` screen, select *Generic H2 (Server)*
    2. Change *Setting Name* to `H2 Server - report DB`
    3. Change *JDBC URL* to `jdbc:h2:tcp://localhost/./reportdb` (after changing `~` to `.`, database will be created under `$H2_HOME/databases`)
    4. Change *User Name* and *Password* to `fuse`
    5. Click *Test connection* (this will create the databse files, e.g., `$H2_HOME/databases/reportdb.mv`)

6. Initialize database `reportdb` by creating schema, table and populating the table with data.

    Simply run:

        $ cd $PROJECT_HOME/datasource
        $ mvn -Ph2

    This will produce standard Maven output with single information:

        [INFO] --- exec-maven-plugin:1.3.2:java (default-cli) @ datasource ---
        12:07:34.116 INFO  [o.j.f.e.p.t.DbInsert] : Database h2 initialized successfully

7. Your H2 database is ready to use.

## Derby Database

Derby database also supports XA transactions.

1. Download Derby database from [Derby website](http://db.apache.org/derby/derby_downloads.html)
2. Unzip the archive to the location of choice (referred to as `$DERBY_HOME` - please define this environmental variable in shell)
3. Add `$DERBY_HOME/bin` to `$PATH` environmental variable
4. Start Derby Server using these shell commands

        $ cd $DERBY_HOME
        $ mkdir databases
        $ cd databases
        $ java -jar $DERBY_HOME/lib/derbyrun.jar server start

    This will start Derby database (TCP port 1527).

5. Create `reportdb` database using `ij` command line utility:

        $ ij
        ij version 10.11
        ij> connect 'jdbc:derby://localhost/reportdb;create=true' user 'fuse' password 'fuse';

    This will create `reportdb` directory in `$DERBY_HOME/databases/reportdb`

6. Initialize database `reportdb` by creating schema, table and populating the table with data.

    Simply run:

        $ cd $PROJECT_HOME/datasource
        $ mvn -Pderby

    This will produce standard Maven output with single information:

        [INFO] --- exec-maven-plugin:1.3.2:java (default-cli) @ datasource ---
        10:56:34.550 INFO  [o.j.f.e.p.t.DbInsert] : Database derby initialized successfully

7. Your Derby database is ready to use.

# Setting up docker-based databases

To perform tests in more realistic environments, we can leverage the power of docker to run more advanced database servers.
Of course you can use existing database instances. The below examples are just here for completeness.

## PostgreSQL database

We can use *official* PostgreSQL docker image available at [docker hub](https://registry.hub.docker.com/_/postgres/).
You can use any of available methods to access PostgreSQL server (e.g., by mapping ports or connecting to containers IP address directly).

1. Start PostgreSQL server docker container:

        $ docker run -d --name fuse-postgresql-server -e POSTGRES_USER=fuse -e POSTGRES_PASSWORD=fuse -p 5432:5432 postgres:9.4

2. Create `reportdb` database by from the `fuse-postgresql-server` container:

        $ docker exec -ti fuse-postgresql-server /bin/bash
        root@3e37b4b579c7:/# psql -U fuse -d fuse
        psql (9.4.0)
        Type "help" for help.
        fuse=# create database reportdb owner fuse encoding 'utf8';
        CREATE DATABASE
        fuse=# \q

3. Initialize database `reportdb` by creating schema, table and populating the table with data.

    Simply run:

        $ cd $PROJECT_HOME/datasource
        $ mvn -Ppostgresql

    This will produce standard Maven output with single information:

        [INFO] --- exec-maven-plugin:1.3.2:java (default-cli) @ datasource ---
        14:32:57.171 INFO  [o.j.f.e.p.t.DbInsert] : Database postgresql initialized successfully

4. Configure PostgreSQL database to allow XA transactions by setting `max_prepared_transactions` to the value equal or greater
than `max_connections` setting (`100` in the case of `postgres:9.4` image).

        root@b052efff5a53:/# sed -i 's/^#max_prepared_transactions = 0/max_prepared_transactions = 200/' /var/lib/postgresql/data/postgresql.conf

5. Restart `fuse-postgresql-server` container. Your PostgreSQL database is ready to use.

## MariaDB database

We can use *official* MariaDB docker image available at [docker hub](https://registry.hub.docker.com/_/mariadb/).

1. Start MariaDB server docker container:

        $ docker run -d --name fuse-mariadb-server -e MYSQL_ROOT_PASSWORD=fuse -p 3306:3306 mariadb:10.0.15

2. Create user `fuse` from the `fuse-mariadb-server` container:

        root@64f9b9714ae1:/# TERM=xterm mysql -h localhost -uroot -pfuse
        Welcome to the MariaDB monitor.  Commands end with ; or \g.
        ...
        MariaDB [(none)]> create user 'fuse'@'%' identified by 'fuse';
        Query OK, 0 rows affected (0.00 sec)

2. Create `report` database (`CREATE SCHEMA` is a synonym for `CREATE DATABASE` in MariaDB/MySQL) and set correct grants:

        MariaDB [(none)]> create database report;
        Query OK, 1 row affected (0.00 sec)

        MariaDB [(none)]> grant all privileges on report.* to 'fuse'@'%';
        Query OK, 0 rows affected (0.00 sec)

        MariaDB [(none)]> flush privileges;
        Query OK, 0 rows affected (0.00 sec)

3. Initialize database `report` by creating table and populating the table with data.

    Simply run:

        $ cd $PROJECT_HOME/datasource
        $ mvn -Pmariadb

    This will produce standard Maven output with single information:

        [INFO] --- exec-maven-plugin:1.3.2:java (default-cli) @ datasource ---
        12:38:39.986 INFO  [o.j.f.e.p.t.DbInsert] : Database mariadb initialized successfully

5. Your MariaDB database is ready to use.

## Oracle database

We can use Oracle XE docker image available at [docker hub](https://registry.hub.docker.com/u/wnameless/oracle-xe-11g/).
You can use any of available methods to access Oracle server (e.g., by mapping ports or connecting to containers IP address directly).

1. Start Oracle server docker container:

        $ docker run -d --name fuse-oracle-server -p 1521:1521 wnameless/oracle-xe-11g

2. Run bash in `fuse-oracle-server` container:

        $ docker exec -ti fuse-oracle-server /bin/bash

3. Connect to Oracle XE database and create `report` user (which is synonym to *schema* in Oracle):

        root@67eb9c9cc8ca:/# sqlplus sys/oracle@localhost as sysdba

        SQL*Plus: Release 11.2.0.2.0 Production on Wed Jan 28 09:09:37 2015

        Copyright (c) 1982, 2011, Oracle.  All rights reserved.


        Connected to:
        Oracle Database 11g Express Edition Release 11.2.0.2.0 - 64bit Production

        SQL> create user report identified by report;

        User created.

        SQL> grant connect, resource, create view to report;

        Grant succeeded.

        SQL> quit
        Disconnected from Oracle Database 11g Express Edition Release 11.2.0.2.0 - 64bit Production

4. Initialize database `xe` as user (= Oracle schema) `report` by table and populating the table with data.

    Simply run:

        $ cd $PROJECT_HOME/datasource
        $ mvn -Poracle

    This will produce standard Maven output with single information:

        [INFO] --- exec-maven-plugin:1.3.2:java (default-cli) @ datasource ---
        11:23:36.991 INFO  [o.j.f.e.p.t.DbInsert] : Database oracle initialized successfully

10. Your Oracle database is ready to use.

## DB2 database

We can use DB2 Express-C docker image available at [docker hub](https://registry.hub.docker.com/u/angoca/db2-instance/).
You can use any of available methods to access DB2 server (e.g., by mapping ports or connecting to containers IP address directly).
The correct way of using dockerized DB2 server would be to create child Dockerfile which sets up and runs DB2 instance (with one or more databases),
 but let's do it manually for now.

1. Start DB2 server docker container:

        $ docker run -dit --privileged=true --name fuse-db2-server -p 50000:50000 angoca/db2-instance /bin/bash
        root@a41e1162442f:/tmp/db2_conf#

    The container will run daemonized, but it runs only `/bin/bash` process. We'll handle it later.

2. Run bash in `fuse-db2-server` container:

        $ docker exec -ti fuse-db2-server /bin/bash
        root@e8c7f3714145:/tmp/db2_conf#

    We are now in the directory which contains prepared response file, which may be used to create DB2 instance listening
    on port TCP/50000.

3. Create DB2 instance using prepared response file:

        root@2286a6283768:/tmp/db2_conf# ${DB2_DIR}/instance/db2isetup -r ${DB2_CONF}/${DB2_RESP_FILE}
        The execution completed successfully.

        For more information see the DB2 installation log at "/tmp/db2isetup.log".

4. Switch to `db2inst1` user (which has correct environment configured):

        root@2286a6283768:/tmp/db2_conf# su - db2inst1
        db2inst1@2286a6283768:~$

5. Start`db2inst1` DB2 instance:

        db2inst1@2286a6283768:~$ db2start
        SQL1063N  DB2START processing was successful.

6. Create `reportdb` database (this may take several minutes...):

        db2inst1@2286a6283768:~$ db2 create database reportdb automatic storage yes
        DB20000I  The CREATE DATABASE command completed successfully.

7. DB2 delegates authentication of users to operating system. We have to create Linux user (as `root`):

        root@2286a6283768:/tmp/db2_conf# useradd fuse
        root@2286a6283768:/tmp/db2_conf# passwd fuse
        Enter new UNIX password: fuse
        Retype new UNIX password: fuse
        passwd: password updated successfully

8. Now we have to grant `DBADM` role to `reportdb` database for `fuse` user (as `db2inst1`):

        db2inst1@2286a6283768:~$ db2 connect to reportdb

           Database Connection Information

         Database server        = DB2/LINUXX8664 10.5.5
         SQL authorization ID   = DB2INST1
         Local database alias   = REPORTDB

        db2inst1@2286a6283768:~$ db2 grant dbadm on database to user fuse
        DB20000I  The SQL command completed successfully.

9. Initialize database `reportdb` by creating schema, table and populating the table with data.

    Simply run:

        $ cd $PROJECT_HOME/datasource
        $ mvn -Pdb2

    This will produce standard Maven output with single information:

        [INFO] --- exec-maven-plugin:1.3.2:java (default-cli) @ datasource ---
        14:21:50.169 INFO  [o.j.f.e.p.t.DbInsert] : Database db2 initialized successfully

10. Your DB2 database is ready to use.

If you have problems connecting to your DB2 database and receive *DB2 JCC Driver error: Unexpected Throwable caught: null. ERRORCODE=-4228, SQLSTATE=null*, please
consult the solution [on IBM website](http://www-01.ibm.com/support/docview.wss?uid=swg21682878).

# JBoss Fuse installation and configuration

1. Unzip the `jboss-fuse-full-6.2.0.redhat-060.zip` archive distribution. The target directory `jboss-fuse-6.2.0.redhat-060` will
    be referred to as `$JBOSS_FUSE_HOME`.
2. Add the following credentials to `$JBOSS_FUSE_HOME/etc/users.properties`:

        admin=admin,admin,manager,viewer,Operator,Maintainer,Deployer,Auditor,Administrator,SuperUser

3. Start JBoss Fuse:

        $JBOSS_FUSE_HOME/bin/fuse

4. Make sure only one `transaction` bundle is installed:

        JBossFuse:karaf@root> osgi:list -t 0 -s | grep transaction
        [ 163] [Active     ] [            ] [       ] [   30] org.apache.aries.transaction.manager (1.1.0)
        [ 164] [Active     ] [Created     ] [       ] [   30] org.apache.aries.transaction.blueprint (1.0.1)
        [ 190] [Active     ] [            ] [       ] [   50] org.apache.aries.transaction.manager (1.0.0)

    If the above is the case, please uninstall all versions except `1.1.0`:

        JBossFuse:karaf@root> osgi:uninstall 190
        You are about to access system bundle 190.  Do you wish to continue (yes/no): yes

    (This problem should be fixed in the next version of JBoss Fuse 6.2)


# Examples

## Camel route with 2 transaction managers

This example is comprised of the following projects:

* `datasource`
* `dao`
* `route-two-tx-managers`

Ensure you have installed and configured a database server of choice (currently: PostgreSQL)

1. `cd $PROJECT_HOME`
2. `mvn clean install -Ppostgresql` (or other DB, like `mariadb`, `h2`, `derby`, `db2`, `oracle`)
3. install features repository:

        features:addurl mvn:org.jboss.fuse.examples.camel-persistence-part2/features/6.2/xml/features

4. install `reportincident-jpa-two` feature:

        features:install -v reportincident-jpa-two

5. make sure relevant bundles are installed:

        JBossFuse:karaf@root> list | grep Examples
        [ 309] [Active     ] [Created     ] [       ] [   80] JBoss Fuse :: Examples :: Fuse ESB & Persistence :: Datasource (6.2.0)
        [ 310] [Active     ] [Created     ] [       ] [   80] JBoss Fuse :: Examples :: Fuse ESB & Persistence :: DAO (6.2.0)
        [ 311] [Active     ] [Created     ] [       ] [   80] JBoss Fuse :: Examples :: Fuse ESB & Persistence :: Camel - 2 Tx Managers (6.2.0)

6. Start PostgreSQL client and observe `REPORT.T_INCIDENT` table:

        $ docker exec -ti fuse-postgresql-server /bin/bash
        root@b052efff5a53:/# psql -d reportdb -U fuse
        psql (9.4.0)
        Type "help" for help.

        reportdb=# select * from report.t_incident order by incident_id asc;
         incident_id | incident_ref |    incident_date    | given_name | family_name |       summary        |                  details                  |           email           |     phone      |      creation_date      | creation_user
        -------------+--------------+---------------------+------------+-------------+----------------------+-------------------------------------------+---------------------------+----------------+-------------------------+---------------
                   1 | 001          | 2015-01-23 00:00:00 | Charles    | Moulliard   | incident webinar-001 | This is a report incident for webinar-001 | cmoulliard@fusesource.com | +111 10 20 300 |                         |
                   2 | 002          | 2015-01-24 00:00:00 | Charles    | Moulliard   | incident webinar-002 | This is a report incident for webinar-002 | cmoulliard@fusesource.com | +111 10 20 300 |                         |
                   3 | 003          | 2015-01-25 00:00:00 | Charles    | Moulliard   | incident webinar-003 | This is a report incident for webinar-003 | cmoulliard@fusesource.com | +111 10 20 300 |                         |
                   4 | 004          | 2015-01-26 00:00:00 | Charles    | Moulliard   | incident webinar-004 | This is a report incident for webinar-004 | cmoulliard@fusesource.com | +111 10 20 300 |                         |
        (5 rows)

        reportdb=#

7. Launch JConsole (inside $JAVA_HOME/bin) and connect using the following information:

    * Remote process: `service:jmx:rmi://localhost:44444/jndi/rmi://localhost:1099/karaf-root`
    * Username: `admin`
    * Password: `admin`

8. Switch to the MBeans tab at the top. On the left pane, expand the `org.apache.activemq` domain, then
    navigate to: Broker > amq > Queue. You will see the `incident` and `rollback` queues. The `registerCall`
    queue will appear when it is first used. For these queues, you will be interested in tracking the
    `EnqueueCount` attribute.

9. Copy the following files to `$JBOSS_FUSE_HOME/datainsert` and notice the effect in the `registerCall` queue and the `REPORT.T_INCIDENT` table:

    * `$PROJECT_HOME/data/csv-one-record-allok.txt`:
        * `REPORT.T_INCIDENT` table: new record
        * `incident` queue: new message enqueued
        * `rollback` queue: no new messages
        * `registerCall` queue: new message enqueued

    * `$PROJECT_HOME/data/csv-one-record-failjms-dbok.txt`:
        * `REPORT.T_INCIDENT` table: new record
        * `incident` queue: new message enqueued
        * `rollback` queue: no new messages
        * `registerCall` queue: no new messages

    * `$PROJECT_HOME/data/csv-one-record-jmsok-faildb.txt`:
        * `REPORT.T_INCIDENT` table: no record inserted
        * `incident` queue: no new messages
        * `rollback` queue: new message enqueued
        * `registerCall` queue: new message enqueued

    * `$PROJECT_HOME/data/csv-one-record-failjms-faildb.txt`:
        * `REPORT.T_INCIDENT` table: no record inserted
        * `incident` queue: no new messages
        * `rollback` queue: new message enqueued
        * `registerCall` queue: no new messages

## Camel route with 1 global transaction manager

This example is comprised of the following projects:

* `datasource`
* `dao-jta`
* `route-one-tx-manager`

Ensure you have installed and configured a database server of choice (currently: PostgreSQL)

To install and test, assuming that you have previously run the "Camel Route with 2 Tx Managers" example above:

1. First uninstall the `reportincident-jpa-two` feature:

        features:uninstall reportincident-jpa-two

2. Install the reportincident-jpa-one feature:

        features:install -v reportincident-jpa-one

3. make sure relevant bundles are installed:

        JBossFuse:karaf@root> list | grep Examples
        [ 314] [Active     ] [Created     ] [       ] [   80] FuseSource :: Examples :: Fuse ESB & Persistence :: Datasource (6.2.0)
        [ 315] [Active     ] [Created     ] [       ] [   80] FuseSource :: Examples :: Fuse ESB & Persistence :: DAO - JTA (6.2.0)
        [ 316] [Active     ] [Created     ] [       ] [   80] FuseSource :: Examples :: Fuse ESB & Persistence :: Camel - 1 Tx Manager (6.2.0)

4. Copy the following files to `$JBOSS_FUSE_HOME/datainsert` and notice the new behaviour in the second and third cases, in terms of the registerCall queue and the REPORT.T_INCIDENT table:

    * `$PROJECT_HOME/data/csv-one-record-allok.txt`:
        * `REPORT.T_INCIDENT` table: new record
        * `incident` queue: new message enqueued
        * `rollback` queue: no new messages
        * `registerCall` queue: new message enqueued

    * `$PROJECT_HOME/data/csv-one-record-failjms-dbok.txt`:
        * `REPORT.T_INCIDENT` table: no record inserted
        * `incident` queue: new message enqueued
        * `rollback` queue: no new messages
        * `registerCall` queue: no new messages

    * `$PROJECT_HOME/data/csv-one-record-jmsok-faildb.txt`:
        * `REPORT.T_INCIDENT` table: no record inserted
        * `incident` queue: no new messages
        * `rollback` queue: new message enqueued
        * `registerCall` queue: no new messages

    * `$PROJECT_HOME/data/csv-one-record-failjms-faildb.txt`:
        * `REPORT.T_INCIDENT` table: no record inserted
        * `incident` queue: no new messages
        * `rollback` queue: new message enqueued
        * `registerCall` queue: no new messages

## Idempotent example

## Aggregator example
