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
package org.jboss.fuse.examples.persistence2.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class DataSources {

    private static Properties properties = new Properties();

    static {
        try {
            properties.load(DataSources.class.getResourceAsStream("/jdbc.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static DataSource findDataSource(String db) throws Exception {
        switch (db) {
            case "postgresql":
                return postgresql();
            case "mariadb":
                return mariadb();
            case "h2":
                return h2();
            case "derby":
                return derby();
            case "db2":
                return db2();
            case "oracle":
                return oracle();
        }
        return null;
    }

    private static DataSource postgresql() throws Exception {
        Class<?> dsClass = Class.forName("org.postgresql.ds.PGSimpleDataSource");
        DataSource ds = (DataSource) dsClass.newInstance();
        set(ds, "url", "jdbc.postgresql.url");
        set(ds, "user", "jdbc.postgresql.user");
        set(ds, "password", "jdbc.postgresql.password");

        return ds;
    }

    private static DataSource mariadb() throws Exception {
        Class<?> dsClass = Class.forName("org.mariadb.jdbc.MySQLDataSource");
        DataSource ds = (DataSource) dsClass.newInstance();
        set(ds, "url", "jdbc.mariadb.url");
        set(ds, "user", "jdbc.mariadb.user");
        set(ds, "password", "jdbc.mariadb.password");

        return ds;
    }

    private static DataSource h2() throws Exception {
        Class<?> dsClass = Class.forName("org.h2.jdbcx.JdbcDataSource");
        DataSource ds = (DataSource) dsClass.newInstance();
        set(ds, "url", "jdbc.h2.url");
        set(ds, "user", "jdbc.h2.user");
        set(ds, "password", "jdbc.h2.password");

        return ds;
    }

    private static DataSource derby() throws Exception {
        Class<?> dsClass = Class.forName("org.apache.derby.jdbc.ClientDataSource");
        DataSource ds = (DataSource) dsClass.newInstance();
        set(ds, "serverName", "jdbc.derby.serverName");
        setInt(ds, "portNumber", "jdbc.derby.portNumber");
        set(ds, "databaseName", "jdbc.derby.databaseName");
        set(ds, "user", "jdbc.derby.user");
        set(ds, "password", "jdbc.derby.password");

        return ds;
    }

    private static DataSource db2() throws Exception {
        // this datasource requires native libraries...
//        Class<?> dsClass = Class.forName("com.ibm.db2.jcc.DB2DataSource");
//        DataSource ds = (DataSource) dsClass.newInstance();
//
//        set(ds, "serverName", "jdbc.db2.serverName");
//        setInt(ds, "portNumber", "jdbc.db2.portNumber");
//        set(ds, "databaseName", "jdbc.db2.databaseName");
//        set(ds, "user", "jdbc.db2.user");
//        set(ds, "password", "jdbc.db2.password");

//        return ds;

        // Let's creat the connection the old school way then
        final Connection connection = DriverManager.getConnection(
                properties.getProperty("jdbc.db2.url"),
                properties.getProperty("jdbc.db2.user"),
                properties.getProperty("jdbc.db2.password")
        );

        return new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return connection;
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return connection;
            }

            @Override
            public PrintWriter getLogWriter() throws SQLException {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter out) throws SQLException {

            }

            @Override
            public void setLoginTimeout(int seconds) throws SQLException {

            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return 0;
            }

            @Override
            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                return null;
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;
            }
        };
    }

    private static DataSource oracle() throws Exception {
        Class<?> dsClass = Class.forName("oracle.jdbc.pool.OracleDataSource");
        DataSource ds = (DataSource) dsClass.newInstance();
        set(ds, "URL", "jdbc.oracle.url");
        set(ds, "user", "jdbc.oracle.user");
        set(ds, "password", "jdbc.oracle.password");

        return ds;
    }

    private static void set(DataSource ds, String property, String key) throws Exception {
        String method = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
        ds.getClass().getMethod(method, String.class).invoke(ds, properties.getProperty(key));
    }

    private static void setInt(DataSource ds, String property, String key) throws Exception {
        String method = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
        ds.getClass().getMethod(method, Integer.TYPE).invoke(ds, Integer.parseInt(properties.getProperty(key)));
    }

}
