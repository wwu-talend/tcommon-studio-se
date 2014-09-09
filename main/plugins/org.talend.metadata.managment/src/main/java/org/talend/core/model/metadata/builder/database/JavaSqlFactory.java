// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.model.metadata.builder.database;

import java.sql.SQLException;
import java.util.Properties;

import metadata.managment.i18n.Messages;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.core.IRepositoryContextService;
import org.talend.core.database.conn.ConnParameterKeys;
import org.talend.core.database.conn.HiveConfKeysForTalend;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.database.dburl.SupportDBUrlStore;
import org.talend.core.model.metadata.builder.database.dburl.SupportDBUrlType;
import org.talend.core.model.metadata.builder.util.MetadataConnectionUtils;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.SwitchHelpers;
import org.talend.cwm.helper.TaggedValueHelper;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.utils.sql.ConnectionUtils;
import org.talend.utils.sugars.ReturnCode;
import org.talend.utils.sugars.TypedReturnCode;

/**
 * @author scorreia
 * 
 * This utility class provides methods that convert CWM object into java.sql object. It is a kind of inverse of the
 * DatabaseContentRetriever class.
 */
public final class JavaSqlFactory {

    public static final String DEFAULT_USERNAME = "root"; //$NON-NLS-1$

    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(JavaSqlFactory.class);

    private JavaSqlFactory() {
    }

    /**
     * Method "createConnection" returns the connection with {@link ReturnCode#getObject()} if {@link ReturnCode#isOk()}
     * is true. This is the behaviour when everything goes ok.
     * <p>
     * When something goes wrong, {@link ReturnCode#isOk()} is false and {@link ReturnCode#getMessage()} gives the error
     * message.
     * <p>
     * The created connection must be closed by the caller. (use {@link ConnectionUtils#closeConnection(Connection)})
     * 
     * @param providerConnection the provider connection
     * @return a ReturnCode (never null)
     * @deprecated {@link #createConnection(Connection)}
     */
    @Deprecated
    public static TypedReturnCode<java.sql.Connection> createConnection(DatabaseConnection providerConnection) {
        TypedReturnCode<java.sql.Connection> rc = new TypedReturnCode<java.sql.Connection>(false);
        String url = providerConnection.getURL();
        if (url == null) {
            rc.setMessage(Messages.getString("JavaSqlFactory.DatabaseConnectionNull")); //$NON-NLS-1$
            rc.setOk(false);
        }
        String driverClassName = providerConnection.getDriverClass();
        Properties props = new Properties();
        props.put(TaggedValueHelper.USER, providerConnection.getUsername());
        props.put(TaggedValueHelper.PASSWORD, providerConnection.getRawPassword());
        String pass = props.getProperty(TaggedValueHelper.PASSWORD);
        if (pass != null) {
            String clearTextPassword = providerConnection.getRawPassword();
            if (clearTextPassword == null) {
                rc.setMessage(Messages.getString("JavaSqlFactory.UnableDecryptPassword")); //$NON-NLS-1$
                rc.setOk(false);
            } else {
                props.setProperty(TaggedValueHelper.PASSWORD, clearTextPassword);
            }
        }
        try {
            java.sql.Connection connection = ConnectionUtils.createConnection(url, driverClassName, props);
            rc.setObject(connection);
            rc.setOk(true);
        } catch (SQLException e) {
            rc.setReturnCode(e.getMessage(), false);
        } catch (InstantiationException e) {
            rc.setReturnCode(e.getMessage(), false);
        } catch (IllegalAccessException e) {
            rc.setReturnCode(e.getMessage(), false);
        } catch (ClassNotFoundException e) {
            rc.setReturnCode(e.getMessage(), false);
        }
        return rc;
    }

    /**
     * Method "createConnection" returns the connection with {@link ReturnCode#getObject()} if {@link ReturnCode#isOk()}
     * is true. This is the behaviour when everything goes ok.
     * <p>
     * When something goes wrong, {@link ReturnCode#isOk()} is false and {@link ReturnCode#getMessage()} gives the error
     * message.
     * <p>
     * The created connection must be closed by the caller. (use {@link ConnectionUtils#closeConnection(Connection)})
     * 
     * @param connection the connection (DatabaseConnection MDMConnection or others)
     * @return a ReturnCode (never null)
     */
    public static TypedReturnCode<java.sql.Connection> createConnection(Connection connection) {
        TypedReturnCode<java.sql.Connection> rc = new TypedReturnCode<java.sql.Connection>(false);
        String url = getURL(connection);
        if (url == null || !(connection instanceof DatabaseConnection)) {
            rc.setMessage(Messages.getString("JavaSqlFactory.DatabaseConnectionNull")); //$NON-NLS-1$
            rc.setOk(false);
            return rc; // MOD scorreia 2010-10-20 bug 16403 avoid NPE while importing items
        }
        return MetadataConnectionUtils.createConnection((DatabaseConnection) connection);
    }

    /**
     * DOC xqliu Comment method "getDriverClass".
     * 
     * @param conn
     * @return driver class name of the connection or null
     */
    public static String getDriverClass(Connection conn) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        if (dbConn != null) {
            String driverClassName = dbConn.getDriverClass();
            // SG : issue http://talendforge.org/bugs/view.php?id=16199
            if (driverClassName == null) {// no drive is specified so let us try
                                          // to guess it
                SupportDBUrlType dbType = SupportDBUrlStore.getInstance().findDBTypeByName(dbConn.getDatabaseType());
                if (dbType != null) {
                    driverClassName = dbType.getDbDriver();
                }// else we keep the drive class to null, we do not know how to
                 // guess it anymore.
            } // else we are ok
              // ADD mzhao 2012-06-25 bug TDI-21552, driver class should be replaced when in context mode.
            if (conn.isContextMode()) {
                driverClassName = getOriginalConntextValue(dbConn, driverClassName);
            }
            return driverClassName;
        }
        MDMConnection mdmConn = SwitchHelpers.MDMCONNECTION_SWITCH.doSwitch(conn);
        if (mdmConn != null) {
            return ""; //$NON-NLS-1$
        }
        DelimitedFileConnection dfConn = SwitchHelpers.DELIMITEDFILECONNECTION_SWITCH.doSwitch(conn);
        if (dfConn != null) {
            return ""; //$NON-NLS-1$
        }
        return null;
    }

    /**
     * DOC xqliu Comment method "setURL".
     * 
     * @param conn
     * @param url
     */
    public static void setURL(Connection conn, String url) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        if (dbConn != null) {
            dbConn.setURL(url);
        }
        MDMConnection mdmConn = SwitchHelpers.MDMCONNECTION_SWITCH.doSwitch(conn);
        if (mdmConn != null) {
            mdmConn.setPathname(url);
        }
        // MOD qiongli 2011-1-9 feature 16796
        DelimitedFileConnection dfConnection = SwitchHelpers.DELIMITEDFILECONNECTION_SWITCH.doSwitch(conn);
        if (dfConnection != null) {
            dfConnection.setFilePath(url);
        }
    }

    /**
     * DOC xqliu Comment method "getUsername".
     * 
     * @param conn
     * @return username of the connection or null
     */
    public static String getUsername(Connection conn) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        String dbUserName = null;
        if (dbConn != null) {
            dbUserName = dbConn.getUsername();
            if (conn.isContextMode()) {
                dbUserName = getOriginalConntextValue(dbConn, dbUserName);
            }
            return dbUserName;
        }
        MDMConnection mdmConn = SwitchHelpers.MDMCONNECTION_SWITCH.doSwitch(conn);
        if (mdmConn != null) {
            return mdmConn.getUsername();
        }
        return null;
    }

    /**
     * DOC xqliu Comment method "getPassword".
     * 
     * @param conn
     * @return password of the connection or null
     */
    public static String getPassword(Connection conn) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        String psw = "";//$NON-NLS-1$
        if (dbConn != null) {
            psw = getOriginalConntextValue(dbConn, dbConn.getRawPassword());
        } else {
            MDMConnection mdmConn = SwitchHelpers.MDMCONNECTION_SWITCH.doSwitch(conn);
            if (mdmConn != null) {
                psw = ConnectionHelper.getPassword(mdmConn);
            }
        }
        if (psw == null) {
            psw = "";//$NON-NLS-1$
        }
        return psw;
    }

    /**
     * DOC xqliu Comment method "setUsername".
     * 
     * @param conn
     * @param username
     */
    public static void setUsername(Connection conn, String username) {
        ConnectionHelper.setUsername(conn, username);
    }

    /**
     * DOC xqliu Comment method "setPassword".
     * 
     * @param conn
     * @param password
     */
    public static void setPassword(Connection conn, String password) {
        ConnectionHelper.setPassword(conn, password);
    }

    /**
     * DOC xqliu Comment method "getURL".
     * 
     * @param conn
     * @return url string of the connection or null
     */
    public static String getURL(Connection conn) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        String url = null;
        if (dbConn != null) {
            url = dbConn.getURL();
            if (conn.isContextMode()) {
                url = getOriginalConntextValue(dbConn, url);
            }
            return url;
        }
        MDMConnection mdmConn = SwitchHelpers.MDMCONNECTION_SWITCH.doSwitch(conn);
        if (mdmConn != null) {
            return mdmConn.getPathname();
        }
        // MOD qiongli 2011-1-11 feature 16796.
        DelimitedFileConnection dfConnection = SwitchHelpers.DELIMITEDFILECONNECTION_SWITCH.doSwitch(conn);
        if (dfConnection != null) {
            return dfConnection.getFilePath();
        }
        return null;
    }

    /**
     * 
     * get driver jar path only for GENERAL JDBC type.
     * 
     * @param conn
     * @return
     */
    public static String getDriverJarPath(Connection conn) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        String driveJarPath = null;
        if (dbConn != null) {
            driveJarPath = dbConn.getDriverJarPath();
            driveJarPath = getOriginalConntextValue(dbConn, driveJarPath);
        }
        return driveJarPath;
    }

    /**
     * Get the original value for context mode.
     * 
     * @param connection
     * @param rawValue
     * @return
     */
    public static String getOriginalConntextValue(Connection connection, String rawValue) {
        if (rawValue == null) {
            return PluginConstant.EMPTY_STRING;
        }
        String origValu = null;
        if (connection != null && connection.isContextMode()) {
            String contextName = connection.getContextName();
            ContextType contextType = null;
            ContextItem contextItem = ContextUtils.getContextItemById2(connection.getContextId());
            if (contextName == null) {
                contextName = contextItem.getDefaultContext();
            }
            contextType = ContextUtils.getContextTypeByName(contextItem, contextName, true);

            origValu = ContextParameterUtils.getOriginalValue(contextType, rawValue);
        }
        return origValu == null ? rawValue : origValu;
    }

    /**
     * Just for hive pre-setup, some configurations are required to set up to the properties of system. It is just for
     * Hive embedded mode.Added by Marvin Wang on Nov 22, 2012.(Just a reminder: TDQ-6462)
     * 
     * @param conn
     */
    public static void doHivePreSetup(Connection connection) {
        Connection conn = connection;
        if (conn instanceof DatabaseConnection) {
            // put to diffirent folder in case it will conflict when create connection with diffirent distribution
            String id = connection.getId();
            if (id == null) {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                id = factory.getNextId();
            }
            String fullPathTemp;
            if (Platform.isRunning()) {
                IProject project = ProjectManager.getInstance().getResourceProject(
                        ProjectManager.getInstance().getCurrentProject().getEmfProject());
                fullPathTemp = project.getFolder("temp").getLocation().append("metastore_db").append(id).toPortableString(); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                fullPathTemp = new Path(System.getProperty("java.io.tmpdir")).append("metastore_db").append(id).toPortableString();//$NON-NLS-1$ //$NON-NLS-2$
            }

            System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_URL.getKey(), "jdbc:derby:;databaseName=" //$NON-NLS-1$
                    + fullPathTemp + ";create=true"); //$NON-NLS-1$

            DatabaseConnection dbConn = (DatabaseConnection) conn;

            // for dq, need to set this as the user to run mapreduce job when run analysis.
            String userName = dbConn.getUsername();
            if (StringUtils.isNotBlank(userName)) {
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HADOOP_USER_NAME.getKey(), dbConn.getUsername());
            }
            // TODO with thrift way, we must enable the two parameters below whereas in JDBC way, we don't need it.
            // If metastore is local or not.
            System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_LOCAL.getKey(), "false"); //$NON-NLS-1$

            // for dq if connection is not converted
            if (conn.isContextMode()) {
                IRepositoryContextService repositoryContextService = CoreRuntimePlugin.getInstance()
                        .getRepositoryContextService();
                if (repositoryContextService != null) {
                    // get the original value and select the defalut context
                    String contextName = conn.getContextName();
                    conn = repositoryContextService.cloneOriginalValueConnection(dbConn, contextName == null ? true : false,
                            contextName);
                }
            }

            // metastore uris
            String thriftURL = "thrift://" + dbConn.getServerName() + ":" + dbConn.getPort(); //$NON-NLS-1$//$NON-NLS-2$
            System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_URI.getKey(), thriftURL);
            System.setProperty("hive.metastore.warehouse.dir", "/user/hive/warehouse"); //$NON-NLS-1$ //$NON-NLS-2$
            // ugi
            System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_EXECUTE_SETUGI.getKey(), "true"); //$NON-NLS-1$

            // hdfs
            System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_FS_DEFAULT_NAME.getKey(),
                    dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_NAME_NODE_URL));

            boolean useYarn = Boolean.valueOf(dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_USE_YARN));
            if (useYarn) { // yarn
                //                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_MAPREDUCE_FRAMEWORK_NAME.getKey(), "yarn"); //$NON-NLS-1$
                // System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_YARN_RESOURCEMANAGER_ADDRESS.getKey(), dbConn
                // .getParameters().get(ConnParameterKeys.CONN_PARA_KEY_JOB_TRACKER_URL));
            } else { // job tracker
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_MAPRED_JOB_TRACKER.getKey(),
                        dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_JOB_TRACKER_URL));
            }

            boolean useKerberos = Boolean.valueOf(dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_USE_KRB));
            if (useKerberos) {
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_SASL_ENABLED.getKey(), "true"); //$NON-NLS-1$
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_DRIVERNAME.getKey(), dbConn.getParameters()
                        .get(ConnParameterKeys.HIVE_AUTHENTICATION_DRIVERCLASS));
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_SECURITY_AUTHORIZATION_ENABLED.getKey(), "true"); //$NON-NLS-1$
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_URL.getKey(),
                        dbConn.getParameters().get(ConnParameterKeys.HIVE_AUTHENTICATION_METASTOREURL));
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_USERNAME.getKey(), dbConn.getParameters()
                        .get(ConnParameterKeys.HIVE_AUTHENTICATION_USERNAME));
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_PASSWORD.getKey(), dbConn.getParameters()
                        .get(ConnParameterKeys.HIVE_AUTHENTICATION_PASSWORD));
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_KERBEROS_PRINCIPAL.getKey(), dbConn
                        .getParameters().get(ConnParameterKeys.HIVE_AUTHENTICATION_HIVEPRINCIPLA));
            }

            // hive mode for talend
            // String hiveMode = dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_HIVE_MODE);
            // if (HiveConnVersionInfo.MODE_EMBEDDED.getKey().equals(hiveMode)) {
            //                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_TALEND_HIVE_MODE.getKey(), "true"); //$NON-NLS-1$
            // } else {
            //                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_TALEND_HIVE_MODE.getKey(), "false"); //$NON-NLS-1$
            // }
            // For metastore infos.
            // url
            // System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_URL.getKey(),
            // dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_METASTORE_CONN_URL));
            // // user name
            // System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_USERNAME.getKey(),
            // dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_METASTORE_CONN_USERNAME));
            // // password
            // System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_PASSWORD.getKey(),
            // dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_METASTORE_CONN_PASSWORD));
            // // driver name
            // System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_DRIVERNAME.getKey(),
            // dbConn.getParameters()
            // .get(ConnParameterKeys.CONN_PARA_KEY_METASTORE_CONN_DRIVER_NAME));
        }
    }

    /**
     * For these which are pre-set up, we need to clear these. Added by Marvin Wang on Nov 22, 2012.
     */
    public static void doHiveConfigurationClear() {
        System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_LOCAL.getKey());
        System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_URI.getKey());
        System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_EXECUTE_SETUGI.getKey());
        System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_FS_DEFAULT_NAME.getKey());
        System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_MAPRED_JOB_TRACKER.getKey());
        System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HADOOP_USER_NAME.getKey());
        // System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_TALEND_HIVE_MODE.getKey());
        // System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_URL.getKey());
        // System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_USERNAME.getKey());
        // System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_PASSWORD.getKey());
        // System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_DRIVERNAME.getKey());
    }
}
