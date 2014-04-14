<!--
  ~ Copyright 2011 FuseSource
  ~
  ~    Licensed under the Apache License, Version 2.0 (the "License");
  ~    you may not use this file except in compliance with the License.
  ~    You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~    Unless required by applicable law or agreed to in writing, software
  ~    distributed under the License is distributed on an "AS IS" BASIS,
  ~    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~    See the License for the specific language governing permissions and
  ~    limitations under the License.
  -->

# DESCRIPTION

This is the material accompanying the presentation of webinar part II - Transaction Management with Fuse ESB, Camel and Persistent EIPs.
It covers the different demo made during the talk and is organized like that :

- aggregator = Camel route project to persist aggregate in H2 DB using JDBCAggregateRepository
- idempotent = Camel route using JPAIdempotentRepository to persist messages already processed
- dao = DAO layer to persist Incident record in H2 DB using OpenJPA
- dao-jta = Idem but configured to use JTA
- features = features to be deployed on Fuse ESB
- route-one-tx-manager = Camel routes using one Global Tx Manager (Aries Tx Manager on Fuse ESB)
- route-two-tx-manager = Camel routes using two separate Tx Managers (JMS and JDBC)

# H2 DATABASE

1. Open a DOS/UNIX console in the folder persistence/database

2. Download H2 Database version **1.3.160** (http://www.h2database.com/html/download.html) and not 1.3.17x as we have rollback issues, sequence number creation

3. and install it

4. Start H2 Server using the bat or shell script

    ./h2.sh &

    The H2 server is started and to manage the databases from your web browser, simply click on the following url http://localhost:8082/

5. Next create the report database

    In the login.jsp screen, select Generic (H2) - Server
    Add as settings name : Generic H2 (Server) - Webinar
    and modify the JDBC ur las such : jdbc:h2:tcp://localhost/~/reportdb

    Next click on "connect" and the screen to manage the reportdb appears

6. Create Schema and Tables using the script located in the file camel-persistence-part2/datasource/src/config/hsqldb/reportdb-scripts.sql

    Execute the scripts 1), 2) and 3) defined in this file

    Check that the records are well created using the command : SELECT * FROM REPORT.T_INCIDENT;


# JBOSS FUSE INSTALLATION AND CONFIGURATION

1. Download and install JBoss Fuse: https://access.redhat.com/jbossnetwork/
2. Add the following credentials to the `$JBOSS_FUSE_HOME>etc/users.properties` file:

        admin=admin,admin

3. Start JBoss Fuse server ./bin/fuse


# Camel Route with 2 Tx Managers

This example is comprised of the following projects: datasource, dao, route-two-tx-manager.
Ensure you have installed the H2 database and the REPORT schema as per the steps above.
To install and test, perform the following steps:

1. cd camel-persistence-part2/
2. Run: mvn clean install
3. Install the relevant bundles by executing the following command in the JBoss Fuse console:

        features:addurl mvn:com.fusesource.examples.camel-persistence-part2/persistence/1.0/xml/features
        features:install -r reportincident-jpa-two
        osgi:refresh

N.B.: You may safely disregard the openjpa.Runtime Warning if it appears.

4. Execute the "list" command in the ESB shell and check that the following bundles are Active:

        [...] [Active     ] [Created     ] [       ] [   60] FuseSource :: Examples :: Fuse ESB & Persistence :: Datasource (1.0)
        [...] [Active     ] [            ] [Started] [   60] FuseSource :: Examples :: Fuse ESB & Persistence :: DAO (1.0)
        [...] [Active     ] [            ] [Started] [   60] FuseSource :: Examples :: Fuse ESB & Persistence :: Camel - 2 Tx Manager (1.0)

5. Start H2 console and connect to the DB using the following parameters
           Driver class = org.h2.Driver
           JDBC URL : jdbc:h2:tcp://localhost/~/reportdb
           User name : sa
           Password :

       Run the following SQL sentence to ensure that the `REPORT.T_INCIDENT` is empty: `SELECT * FROM REPORT.T_INCIDENT`;

6. Launch JConsole (inside $JAVA_HOME/bin) and connect to the local process named "org.apache.karaf.main.Main". Switch to the MBeans tab at the top.
       On the left pane, expand the org.apache.activemq domain, then navigate to: amq > Queue. You will see the `incident` and `rollback` queues.
       The `registerCall` queue will appear when it is first used. For these queues, you will be interested in tracking the EnqueueCount attribute.

7. Copy the following files and notice the effect in the `registerCall` queue and the `REPORT.T_INCIDENT` table:

           - camel-persistence-part2/data/csv-one-record-allok.txt to $JBOSS_FUSE_HOME/datainsert --> record written in table, new message on registerCall queue
           - camel-persistence-part2/data/csv-one-record-failjms-dbok.txt to $JBOSS_FUSE_HOME/datainsert --> record written in table, NO new message on registerCall queue
           - camel-persistence-part2/data/csv-one-record-jmsok-faildb.txt to $JBOSS_FUSE_HOME/datainsert --> NO record written in table, new message on registerCall queue
           - camel-persistence-part2/data/csv-one-record-failjms-faildb.txt to $JBOSS_FUSE_HOME/datainsert --> NO record written in table, NO new message on registerCall queue


# Camel route with 1 Global Tx Manager

This example is comprised of the following projects: datasource, dao-jta, route-one-tx-manager. (NOTICE bundle names: dao-*jta* and route-*one*-tx-manager)
Ensure you have installed the H2 database and the REPORT schema as per the steps above.

To install and test, assuming that you have previously run the "Camel Route with 2 Tx Managers" example above:

1. First uninstall the reportincident-jpa-two feature:

          features:uninstall reportincident-jpa-two

2. Install the reportincident-jpa-one feature:

        features:install -r reportincident-jpa-one
        osgi:refresh

3. Execute the "list" command in the ESB shell and check that the following bundles are Active:

        [...] [Active     ] [Created     ] [       ] [   60] FuseSource :: Examples :: Fuse ESB & Persistence :: Datasource (1.0)
        [...] [Active     ] [            ] [       ] [   60] FuseSource :: Examples :: Fuse ESB & Persistence :: DAO - JTA (1.0)
        [...] [Active     ] [            ] [Started] [   60] FuseSource :: Examples :: Fuse ESB & Persistence :: Camel - 1 Tx Manager (1.0)

4. Copy the following files and notice the new behaviours in the second and third cases, in terms of the registerCall queue and the REPORT.T_INCIDENT table:

         - camel-persistence-part2/data/csv-one-record-allok.txt to $JBOSS_FUSE_HOME/datainsert --> record written in table, new message on registerCall queue
         - camel-persistence-part2/data/csv-one-record-failjms-dbok.txt to $JBOSS_FUSE_HOME/datainsert --> NO record written in table, NO new message on registerCall queue
         - camel-persistence-part2/data/csv-one-record-jmsok-faildb.txt to $JBOSS_FUSE_HOME/datainsert --> NO record written in table, NO new message on registerCall queue
         - camel-persistence-part2/data/csv-one-record-failjms-faildb.txt to $JBOSS_FUSE_HOME/datainsert --> NO record written in table, NO new message on registerCall queue

# Idempotent example

1. Cd idempotent
2. Execute mvn camel:run
3. Start H2 console and connect to the DB using the following parameters
       Driver class = org.h2.Driver
       JDBC URL : jdbc:h2:tcp://localhost/~/idempotentReport
       User name : sa
       Password :
4. Enter the following request to verify that no records have been inserted
       SELECT * FROM CAMEL_MESSAGEPROCESSED
5. Copy the following file
       cp ../data/csv-one-record.txt datainsert/
6. The exchange is not filtered out and camel logs that
       %%% File receive -> csv-one-record.txt
7. Shutdown the camel route and restart
       Verify after copying the file that the camel route will not display the following message
       %%% File receive -> csv-one-record.txt

# Aggregator example

1. Cd aggregator
2. Start H2 console and connect to the DB using the following parameters
       Driver class = org.h2.Driver
       JDBC URL : jdbc:h2:tcp://localhost/~/aggregationReport
       User name : sa
       Password :
3. Create the DB using script in directory src/main/resources/sql/init.sql
4. Execute mvn camel:run
5. Shutdown camel when 2-3 exchanges have been aggregated

       >>> (file-to-queue) from(timer://demo?period=2000&repeatCount=15) --> ref:users method: getUser <<< Pattern:InOnly, Headers:{firedTime=Wed Nov 23 11:38:51 CET 2011, breadcrumbId=ID-biker-chm-local-53796-1322044729997-0-2}, BodyType:null, Body:[Body is null]
       >>> (file-to-queue) ref:users method: getUser --> aggregate <<< Pattern:InOnly, Headers:{firedTime=Wed Nov 23 11:38:51 CET 2011, breadcrumbId=ID-biker-chm-local-53796-1322044729997-0-2, id=FUSE}, BodyType:String, Body:Charles,
       >>> (file-to-queue) from(timer://demo?period=2000&repeatCount=15) --> ref:users method: getUser <<< Pattern:InOnly, Headers:{breadcrumbId=ID-biker-chm-local-53796-1322044729997-0-5, firedTime=Wed Nov 23 11:38:53 CET 2011}, BodyType:null, Body:[Body is null]
       >>> (file-to-queue) ref:users method: getUser --> aggregate <<< Pattern:InOnly, Headers:{firedTime=Wed Nov 23 11:38:53 CET 2011, id=FUSE, breadcrumbId=ID-biker-chm-local-53796-1322044729997-0-5}, BodyType:String, Body:Raul,

6. Verify that a blob object exist in the DB
       SELECT * FROM AGGREGATIONREPO1
7. Restart camel route and verify that aggregation process continues





