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
# JDBC datasources

This maven artifact is blueprint OSGi bundle which defines JDBC datasources to be used by other bundles in `camel-persistence-part2' project.
It is part of features defined in `features`, but may also be used as a template for creating *datasource bundles* in
other projects.

## Background

There are 3 different *data source* interfaces defined in JDBC:

* `javax.sql.DataSource` - main interface defining *resource factory* for JDBC data access. Created *resources* are JDBC
    connections.
* `javax.sql.XADataSource` - this interface is used to acquire XA Connections which may be associated with distributed transactions
* `javax.sql.ConnectionPoolDataSource` - *resource factory* for connections with special hooks for connection pool management. We won't discuss
    these data source types.

Non-managed, non *resource factory pattern* style of acquiring JDBC connections is the old:

        java.sql.DriverManager.getConnection(jdbcUrl)

But we simply don't access databases this way. JBoss Fuse is managed environment which encourages to use established patterns
to implement data access.

## JDBC data access in Fuse

Fuse 6.2, as OSGi environment provides standard services which help use JDBC in standard way:

* Aries JDBC - wraps `javax.sql.DataSource` and `javax.sql.XADataSource` OSGi services in an implementation of
    `javax.resource.spi.ManagedConnectionFactory` which will enlist XA resources in JTA transaction an provide pooling
    capabilities.
* Aries Transaction - provides implementation of `javax.transaction.TransactionManager` which implements Two Phase Commit protocol and coordinates
    XA Resources within JTA transactions

The role of developer is to define lower-level `javax.sql.XADataSource` implementation as OSGi service (usually using Blueprint programming model)
specific to the database of choice.

For example, here are relevant XA Data Source implementations for different database servers:

* PostgreSQL: `org.postgresql.xa.PGXADataSource`
* MariaDB: `org.mariadb.jdbc.MySQLDataSource`
* MySQL: `com.mysql.jdbc.jdbc2.optional.MysqlXADataSource`
* H2: `org.h2.jdbcx.JdbcDataSource`
* Derby: `org.apache.derby.jdbc.ClientXADataSource`
* DB2: `com.ibm.db2.jcc.DB2XADataSource`
* Oracle: `oracle.jdbc.xa.client.OracleXADataSource`
* MS SQL Server: `com.microsoft.sqlserver.jdbc.SQLServerXADataSource`

The above datasource do not provide JTA enlisting capabilities. Their role is to implement XA protocol for specific dataabse.
Some of those database-specific data sources *may* provide pooling features, but it is not relevant, as usually pooling
is provided by *wrapping*, *higher-level* datasources.

JTA enlisting data sources (such as the ones provided by Aries JDBC) need to be configured with an instance of JTA Transaction
Manager.
There are other implementations of pooling and/or JTA enlisting data sources:

* [commons-dbcp2 connection pool](http://commons.apache.org/proper/commons-dbcp/):

    Provides e.g., `org.apache.commons.dbcp2.managed.BasicManagedDataSource` pooling/enlisting datasource but without
    transaction recovery support.

* [Tomcat JDBC](http://tomcat.apache.org/tomcat-8.0-doc/jdbc-pool.html)

    Provides e.g., `org.apache.tomcat.jdbc.pool.DataSource` pooling (but not enlisting) data source.

## How does it work?

Assuming the following are installed in JBoss Fuse:

* `transaction` feature (part of Karaf Enterprise features)
* `connector` feature (JCA) (part of Karaf Enterprise features)
* `org.apache.aries.transaction.jdbc` bundle (should be part of JBoss Fuse' version of Karaf enterprise `transaction` feature)
* correct JDBC driver (installed probably using wrap:mvn: protocol to convert JAR file into regular OSGi bundle)

If `javax.sql.XADataService` OSGi service (e.g., `org.postgresql.xa.PGXADataSource`) is installed either in code (e.g., in bundle activator) or using blueprint file:

1. At startup, Aries JDBC bundle installed OSGi `ServiceListener` with the following filter:

        (&(|(objectClass=javax.sql.XADataSource)(objectClass=javax.sql.DataSource))(!(aries.managed=true)))

    The last part (`!(aries.managed=true)`) protects against infinite loop of discovering data sources, but also may
    be used as a way of preventing your `javax.sql.XADataSource` services to be wrapped by Aries JDBC.

2. Aries JDBC listener gets notified about our data source as `org.osgi.framework.ServiceReference`
3. Aries JDBC wraps our `javax.sql.XADataService` in JCA `javax.resource.spi.ManagedConnectionFactory` (using tranql library)
4. Aries JDBC creates JCA `javax.resource.spi.ConnectionManager` (using Geronimo-Connector library)

    This connection manager is used to obtain actual JDBC connections, but using a stack of interceptors. Each interceptor may
    alter the connection, adding different behavior (transaction enlisting, pooling, tracking, XA protocol handling, ...)

5. Aries JDBC creates `javax.sql.DataSource` implementation (not `XADataSource`) which delegates `getConnection()` call
    to JCA `javax.resource.spi.ConnectionManager.allocateConnection()` created earlier.
6. This datasource (which does enlisting and pooling at JCA level) is then registered in OSGi as `javax.sql.DataSource` service
    with all the properties of original `javax.sql.XADataSource` service, plus:
    * `aries.xa.aware` = `true`
    * `aries.managed` = `true`
    * `service.ranking` = original `service.ranking` + 1000

    Due to higher *service ranking*, further JNDI lookups of original data source will return new managed datasource (pooling and enlisting)

7. Aries JDBC initiates XA recovery for XA data source. Recovery is performed by transaction manager, which checks if there are prepared and not finished XA transactions
    related to this XA data source.

## Example data source

For example, we may deploy the following blueprint receipe:

        <blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0">

            <bean id="dataSource" class="org.postgresql.xa.PGXADataSource">
                <property name="url" value="jdbc:postgresql://localhost/reportdb" />
                <property name="user" value="fuse" />
                <property name="password" value="fuse" />
            </bean>

            <service ref="dataSource" interface="javax.sql.XADataSource">
                <service-properties>
                    <entry key="osgi.jndi.service.name" value="jdbc/reportdb" />
                    <entry key="aries.xa.name" value="reportdb" />
                </service-properties>
            </service>

        </blueprint>

Without Aries JDBC, we would get the folowing services after these JNDI lookups:

* JNDI name `osgi:service/javax.sql.XADataSource/(osgi.jndi.service.name=jdbc/reportdb)`: `org.postgresql.xa.PGXADataSource` object
* JNDI name `osgi:service/javax.sql.DataSource/(osgi.jndi.service.name=jdbc/reportdb)`: nothing
* JNDI name `osgi:service/jdbc/reportdb`: `org.postgresql.xa.PGXADataSource` object (OSGi JNDI shortcut if using `osgi.jndi.service.name` service property)

After Aries JDBC wraps our XA data source, we would get:

* JNDI name `osgi:service/javax.sql.XADataSource/(osgi.jndi.service.name=jdbc/reportdb)`: `org.postgresql.xa.PGXADataSource` object (same as above!)
* JNDI name `osgi:service/javax.sql.DataSource/(osgi.jndi.service.name=jdbc/reportdb)`: enlisting, pooling, JCA-based Aries managed data source (delegating eventually to `org.postgresql.xa.PGXADataSource`)
* JNDI name `osgi:service/jdbc/reportdb`: Aries managed data source, because such JNDI service lookup doesn't take into account interface names, only property filter

So, to be sure we use correct data source (e.g., in JPA `persistence.xml` or in code) the best is to use the following form:

        osgi:service/jdbc/reportdb

Why we are effectively switching from using `javax.sql.XADataSource` to using `javax.sql.DataSource`?
The answer is - application code (or JPA provider for example) operates on `java.sql.Connection` objects, not on `javax.sql.XAConnection`.
So all we need is `javax.sql.DataSource` object. Fuse provides such data source with connection pooling support as
well as support in enlisting XA Resources in JTA transactions.

If we don't want XA support, we can publish for example `org.postgresql.ds.PGSimpleDataSource` service with interface `javax.sql.DataSource`. Such service
will also be wrapped by Aries JDBC and *republished* as another service with **the same interface** (`javax.sql.DataSource`) but with higher `service.ranking`.
Applications obtaining such OSGi interface will get wrapped data source, ultimately obtaining connection from `org.postgresql.ds.PGSimpleDataSource`, but with added
Aries JDBC connection pooling.
