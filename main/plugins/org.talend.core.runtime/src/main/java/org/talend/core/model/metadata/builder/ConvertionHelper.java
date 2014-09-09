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
package org.talend.core.model.metadata.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.EMap;
import org.talend.commons.bridge.ReponsitoryContextBridge;
import org.talend.commons.utils.resource.FileExtensions;
import org.talend.core.IRepositoryContextService;
import org.talend.core.database.EDatabase4DriverClassName;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.template.EDatabaseConnTemplate;
import org.talend.core.model.metadata.Dbms;
import org.talend.core.model.metadata.IConvertionConstants;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.builder.connection.AbstractMetadataObject;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.utils.KeywordsValidator;
import org.talend.cwm.helper.ConnectionHelper;
import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * 
 */
public final class ConvertionHelper {

    /**
     * This method doesn't perform a deep copy. DOC tguiu Comment method "convert".
     * 
     * @param connection
     * @return
     */
    public static IMetadataConnection convert(Connection sourceConnection) {
        return convert(sourceConnection, false);
    }

    /**
     * convert a Connection to IMetadataConnection.
     * 
     * @param sourceConnection
     * @param defaultContext
     * @return
     */
    public static IMetadataConnection convert(Connection sourceConnection, boolean defaultContext) {
        // MOD 20130407 TDQ-7074 popup many times of context selection dialogs
        return convert(sourceConnection, defaultContext, sourceConnection.getContextName());
        // ~
    }

    public static IMetadataConnection convert(Connection sourceConnection, boolean defaultContext, String selectedContext) {
        if (sourceConnection instanceof DatabaseConnection) {
            return convert((DatabaseConnection) sourceConnection, defaultContext, selectedContext);
        } else if (sourceConnection instanceof MDMConnection) {
            return convert((MDMConnection) sourceConnection, defaultContext, selectedContext);
        } else if (sourceConnection instanceof DelimitedFileConnection) {
            return convert((DelimitedFileConnection) sourceConnection, defaultContext, selectedContext);
        }
        return null;
    }

    /**
     * fillUIParams from DatabaseConnection to IMetadataConnection..
     * 
     * @param metadataConnection
     * @param conn
     */
    public static void fillUIParams(IMetadataConnection metadataConnection, DatabaseConnection conn) {
        if (conn == null) {
            return;
        }

        IRepositoryContextService repositoryContextService = CoreRuntimePlugin.getInstance().getRepositoryContextService();

        if (repositoryContextService != null) {
            repositoryContextService.setMetadataConnectionParameter(conn, metadataConnection);
        } else {
            // set product(ProductId) and Schema(UISchema)
            EDatabaseTypeName edatabasetypeInstance = EDatabaseTypeName.getTypeFromDisplayName(conn.getDatabaseType());
            String product = edatabasetypeInstance.getProduct();
            metadataConnection.setProduct(product);
            // set mapping(DbmsId)
            if (!ReponsitoryContextBridge.isDefautProject()) {
                Dbms defaultDbmsFromProduct = MetadataTalendType.getDefaultDbmsFromProduct(product);
                if (defaultDbmsFromProduct != null) {
                    String mapping = defaultDbmsFromProduct.getId();
                    metadataConnection.setMapping(mapping);
                }
            }

            // otherParameter
            metadataConnection.setOtherParameter(ConnectionHelper.getOtherParameter(conn));
        }

        // name
        metadataConnection.setLabel(conn.getLabel());
        // purpose
        metadataConnection.setPurpose(ConnectionHelper.getPurpose(conn));
        // description
        metadataConnection.setDescription(ConnectionHelper.getDescription(conn));
        // author
        metadataConnection.setAuthor(ConnectionHelper.getAuthor(conn));
        // status
        metadataConnection.setStatus(ConnectionHelper.getDevStatus(conn));
        // version
        metadataConnection.setVersion(ConnectionHelper.getVersion(conn));
        // universe
        metadataConnection.setUniverse(ConnectionHelper.getUniverse(conn));

        fillOtherParameters(metadataConnection, conn);

    }

    /**
     * fillOtherParameters from DatabaseConnection to IMetadataConnection.
     * 
     * @param metaConn
     * @param dbConn
     */
    private static void fillOtherParameters(IMetadataConnection metaConn, DatabaseConnection dbConn) {
        EMap<String, String> map = dbConn.getParameters();
        if (map != null && map.size() > 0) {
            Map<String, Object> metadataMap = metaConn.getOtherParameters();
            if (metadataMap == null) {
                metadataMap = new HashMap<String, Object>();
            }
            for (Entry<String, String> entry : map.entrySet()) {
                metadataMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * convert a DatabaseConnection to IMetadataConnection.
     * 
     * @param sourceConnection
     * @param defaultContext
     * @param selectedContext
     * @return
     */
    public static IMetadataConnection convert(DatabaseConnection sourceConnection, boolean defaultContext, String selectedContext) {

        if (sourceConnection == null) {
            return null;
        }
        // if sourceConnection is not context mode, will be same as before.
        DatabaseConnection connection = null;
        DatabaseConnection originalValueConnection = null;
        IRepositoryContextService repositoryContextService = CoreRuntimePlugin.getInstance().getRepositoryContextService();
        if (repositoryContextService != null) {
            originalValueConnection = repositoryContextService.cloneOriginalValueConnection(sourceConnection, defaultContext,
                    selectedContext);
        }
        if (originalValueConnection == null) {
            connection = sourceConnection;
        } else {
            connection = originalValueConnection;
        }
        IMetadataConnection result = new MetadataConnection();
        result.setComment(connection.getComment());
        result.setDatabase(connection.getSID());
        result.setDataSourceName(connection.getDatasourceName());

        if (connection.getDatabaseType() == null || "".equals(connection.getDatabaseType())) { // 0009594 //$NON-NLS-1$
            String trueDbType = getDbTypeByClassNameAndDriverJar(connection.getDriverClass(), null);
            result.setDbType(trueDbType);
        } else {
            result.setDbType(connection.getDatabaseType());
        }
        result.setDriverJarPath(connection.getDriverJarPath());
        result.setDbVersionString(connection.getDbVersionString());
        result.setDriverClass(connection.getDriverClass());
        result.setFileFieldName(connection.getFileFieldName());
        result.setId(connection.getId());
        result.setLabel(connection.getLabel());
        result.setNullChar(connection.getNullChar());
        result.setPassword(connection.getRawPassword());
        result.setPort(connection.getPort());
        result.setServerName(connection.getServerName());
        result.setSqlSyntax(connection.getSqlSynthax());
        result.setUiSchema(connection.getUiSchema());
        if (result.getSchema() == null) {
            result.setSchema(connection.getUiSchema());
        }
        result.setStringQuote(connection.getStringQuote());
        result.setUrl(connection.getURL());
        result.setAdditionalParams(connection.getAdditionalParams());
        result.setUsername(connection.getUsername());
        String dbmsId = connection.getDbmsId();
        if (dbmsId == null || "".equals(dbmsId)) {
            dbmsId = "mysql_id";
        }
        result.setMapping(dbmsId);
        result.setProduct(connection.getProductId());
        result.setDbRootPath(connection.getDBRootPath());
        result.setSqlMode(connection.isSQLMode());
        result.setCurrentConnection(connection); // keep the connection for the metadataconnection
        result.setContentModel(connection.isContextMode());
        result.setContextId(connection.getContextId());
        result.setContextName(connection.getContextName());
        // handle oracle database connnection of general_jdbc.
        result.setSchema(getMeataConnectionSchema(result));
        convertOtherParameters(result, connection);
        // ADD msjian TDQ-5908 2012-9-3:should set the UI parameters
        fillUIParams(result, connection);
        // TDQ-5908~

        return result;

    }

    /**
     * Copies other parameters from <code>DatabaseConnection</code> to <code>IMetadataConnection</code>. Added by Marvin
     * Wang on Aug.8, 2012.
     * 
     * @param result
     * @param connection
     */
    static void convertOtherParameters(IMetadataConnection result, DatabaseConnection connection) {
        EMap<String, String> otherParameters = connection.getParameters();

        if (otherParameters != null && otherParameters.size() > 0) {
            Set<Entry<String, String>> set = otherParameters.entrySet();
            for (Entry<String, String> entry : set) {
                result.setParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 
     * DOC zshen Comment method "convert".
     * 
     * @param sourceConnection
     * @param defaultContext
     * @param selectedContext
     * @return convert from sourceConnection to MetadataConnection
     */
    public static IMetadataConnection convert(MDMConnection sourceConnection, boolean defaultContext, String selectedContext) {

        if (sourceConnection == null) {
            return null;
        }
        // if sourceConnection is not context mode, will be same as before.
        MDMConnection connection = null;

        connection = sourceConnection;

        IMetadataConnection result = new MetadataConnection();
        result.setComment(connection.getComment());

        result.setId(connection.getId());
        result.setLabel(connection.getLabel());

        result.setPassword(connection.getPassword());
        result.setPort(connection.getPort());
        result.setServerName(connection.getServer());

        result.setUsername(connection.getUsername());
        result.setUniverse(connection.getUniverse());
        result.setDatamodel(connection.getDatamodel());
        result.setDatacluster(connection.getDatacluster());

        result.setCurrentConnection(connection); // keep the connection for the metadataconnection
        // handle oracle database connnection of general_jdbc.
        result.setSchema(getMeataConnectionSchema(result));

        return result;

    }

    /**
     * 
     * DOC zshen Comment method "convert".
     * 
     * @param sourceConnection
     * @param defaultContext
     * @param selectedContext
     * @return convert form DelimitedFileConnection to MetadataConnection
     */
    public static IMetadataConnection convert(DelimitedFileConnection sourceConnection, boolean defaultContext,
            String selectedContext) {

        if (sourceConnection == null) {
            return null;
        }
        // if sourceConnection is not context mode, will be same as before.
        DelimitedFileConnection connection = null;

        connection = sourceConnection;

        IMetadataConnection result = new MetadataConnection();
        result.setComment(connection.getComment());

        result.setId(connection.getId());
        result.setLabel(connection.getLabel());

        result.setServerName(connection.getServer());

        result.setCurrentConnection(connection); // keep the connection for the metadataconnection
        // handle oracle database connnection of general_jdbc.
        result.setSchema(getMeataConnectionSchema(result));

        return result;

    }

    public static IMetadataTable convert(MetadataTable old) {
        IMetadataTable result = new org.talend.core.model.metadata.MetadataTable();
        result.setComment(old.getComment());
        result.setId(old.getId());
        result.setLabel(old.getLabel());
        String sourceName = old.getName();
        if (sourceName == null) {
            sourceName = old.getLabel();
        }
        result.setTableName(sourceName);
        List<IMetadataColumn> columns = new ArrayList<IMetadataColumn>(old.getColumns().size());
        for (Object o : old.getColumns()) {
            MetadataColumn column = (MetadataColumn) o;
            IMetadataColumn newColumn = new org.talend.core.model.metadata.MetadataColumn();
            columns.add(newColumn);
            newColumn.setComment(column.getComment());
            newColumn.setDefault(column.getDefaultValue());
            newColumn.setKey(column.isKey());
            String label2 = column.getLabel();
            if (KeywordsValidator.isKeyword(label2)) {
                label2 = "_" + label2; //$NON-NLS-1$
            }
            newColumn.setLabel(label2);
            newColumn.setPattern(column.getPattern());
            if (column.getLength() < 0) {
                newColumn.setLength(null);
            } else {
                newColumn.setLength(Long.valueOf(column.getLength()).intValue());
            }
            if (column.getOriginalLength() < 0) {
                newColumn.setOriginalLength(null);
            } else {
                newColumn.setOriginalLength(Long.valueOf(column.getOriginalLength()).intValue());
            }

            if (column.getTaggedValue().size() > 0) {
                for (TaggedValue tv : column.getTaggedValue()) {
                    String additionalTag = tv.getTag();
                    if (additionalTag.startsWith(IConvertionConstants.ADDITIONAL_FIELD_PREFIX)) {
                        String[] splits = additionalTag.split(":");
                        additionalTag = splits[1];
                    }
                    newColumn.getAdditionalField().put(additionalTag, tv.getValue());
                }
            }
            newColumn.setNullable(column.isNullable());
            if (column.getPrecision() < 0) {
                newColumn.setPrecision(null);
            } else {
                newColumn.setPrecision(Long.valueOf(column.getPrecision()).intValue());
            }
            newColumn.setTalendType(column.getTalendType());
            newColumn.setType(column.getSourceType());
            if (column.getName() == null || column.getName().equals("")) { //$NON-NLS-1$
                String label = label2;
                if (label != null && label.length() > 0) {
                    String substring = label.substring(1);
                    if (label.startsWith("_") && KeywordsValidator.isKeyword(substring)) { //$NON-NLS-1$
                        label = substring;
                    }
                }
                newColumn.setOriginalDbColumnName(label);
            } else {
                newColumn.setOriginalDbColumnName(column.getName());
            }
            // columns.add(convertToIMetaDataColumn(column));
        }
        result.setListColumns(columns);
        Map<String, String> newProperties = result.getAdditionalProperties();
        EMap<String, String> oldProperties = old.getAdditionalProperties();
        for (Entry<String, String> entry : oldProperties) {
            newProperties.put(entry.getKey(), entry.getValue());
        }
        for (TaggedValue tv : old.getTaggedValue()) {
            String additionalTag = tv.getTag();
            result.getAdditionalProperties().put(additionalTag, tv.getValue());
        }

        return result;
    }

    public static IMetadataTable convertServicesOperational(AbstractMetadataObject old) {
        IMetadataTable result = new org.talend.core.model.metadata.MetadataTable();
        result.setComment(old.getComment());
        result.setId(old.getId());
        result.setLabel(old.getLabel());
        String sourceName = old.getName();
        if (sourceName == null) {
            sourceName = old.getLabel();
        }
        result.setTableName(sourceName);
        return result;
    }

    public static MetadataColumn convertToMetaDataColumn(IMetadataColumn column) {
        MetadataColumn newColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
        newColumn.setComment(column.getComment());
        newColumn.setDefaultValue(column.getDefault());
        newColumn.setKey(column.isKey());
        newColumn.setLabel(column.getLabel());
        newColumn.setPattern(column.getPattern());
        if (column.getLength() == null || column.getLength() < 0) {
            newColumn.setLength(0);
        } else {
            newColumn.setLength(column.getLength());
        }
        newColumn.setNullable(column.isNullable());
        if (column.getPrecision() == null || column.getPrecision() < 0) {
            newColumn.setPrecision(0);
        } else {
            newColumn.setPrecision(column.getPrecision());
        }
        newColumn.setTalendType(column.getTalendType());
        newColumn.setSourceType(column.getType());
        if (column.getOriginalDbColumnName() == null || column.getOriginalDbColumnName().equals("")) { //$NON-NLS-1$
            newColumn.setName(column.getLabel());
        } else {
            newColumn.setName(column.getOriginalDbColumnName());
        }
        return newColumn;
    }

    public static IMetadataColumn convertToIMetaDataColumn(MetadataColumn column) {
        IMetadataColumn newColumn = new org.talend.core.model.metadata.MetadataColumn();
        newColumn.setComment(column.getComment());
        newColumn.setDefault(column.getDefaultValue());
        newColumn.setKey(column.isKey());
        newColumn.setLabel(column.getLabel());
        newColumn.setPattern(column.getPattern());
        if (column.getLength() < 0) {
            newColumn.setLength(null);
        } else {
            newColumn.setLength(Long.valueOf(column.getLength()).intValue());
        }
        newColumn.setNullable(column.isNullable());
        if (column.getPrecision() < 0) {
            newColumn.setPrecision(null);
        } else {
            newColumn.setPrecision(Long.valueOf(column.getPrecision()).intValue());
        }
        newColumn.setTalendType(column.getTalendType());
        newColumn.setType(column.getSourceType());
        if (column.getName() == null || column.getName().equals("")) { //$NON-NLS-1$
            newColumn.setOriginalDbColumnName(column.getLabel());
        } else {
            newColumn.setOriginalDbColumnName(column.getName());
        }
        return newColumn;
    }

    private ConvertionHelper() {
    }

    /**
     * DOC qiang.zhang Comment method "convert".
     * 
     * @param metadataTable
     * @return
     */
    public static MetadataTable convert(IMetadataTable old) {
        MetadataTable result = ConnectionFactory.eINSTANCE.createMetadataTable();
        result.setComment(old.getComment());
        result.setId(old.getId());
        result.setLabel(old.getLabel());
        result.setSourceName(old.getTableName());
        List<MetadataColumn> columns = new ArrayList<MetadataColumn>(old.getListColumns().size());
        for (IMetadataColumn column : old.getListColumns()) {
            MetadataColumn newColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
            columns.add(newColumn);
            newColumn.setComment(column.getComment());
            newColumn.setDefaultValue(column.getDefault());
            newColumn.setKey(column.isKey());
            newColumn.setLabel(column.getLabel());
            newColumn.setPattern(column.getPattern());
            if (column.getLength() == null || column.getLength() < 0) {
                newColumn.setLength(0);
            } else {
                newColumn.setLength(column.getLength());
            }
            newColumn.setNullable(column.isNullable());
            if (column.getPrecision() == null || column.getPrecision() < 0) {
                newColumn.setPrecision(0);
            } else {
                newColumn.setPrecision(column.getPrecision());
            }
            newColumn.setTalendType(column.getTalendType());
            newColumn.setSourceType(column.getType());
            if (column.getOriginalDbColumnName() == null || column.getOriginalDbColumnName().equals("")) { //$NON-NLS-1$
                newColumn.setName(column.getLabel());
            } else {
                newColumn.setName(column.getOriginalDbColumnName());
            }
            // columns.add(convertToMetaDataColumn(column));
        }
        result.getColumns().addAll(columns);
        return result;
    }

    /**
     * DOC ycbai Comment method "getMeataConnectionSchema".
     * 
     * @param metadataConnection
     * @return
     */
    public static String getMeataConnectionSchema(IMetadataConnection metadataConnection) {
        String schema = metadataConnection.getSchema();
        String dbType = metadataConnection.getDbType();
        String url = metadataConnection.getUrl();
        String generalJDBCDisplayName = EDatabaseConnTemplate.GENERAL_JDBC.getDBDisplayName();
        if (generalJDBCDisplayName.equals(dbType) && url.contains("oracle")) {//$NON-NLS-1$
            schema = metadataConnection.getUsername().toUpperCase();
        }
        return schema;
    }

    // hywang add for bug 7575
    public static String getDbTypeByClassNameAndDriverJar(String driverClassName, String driverJar) {
        List<EDatabase4DriverClassName> t4d = EDatabase4DriverClassName.indexOfByDriverClass(driverClassName);
        if (t4d.size() == 1) {
            return t4d.get(0).getDbTypeName();
        } else if (t4d.size() > 1) {
            // for some dbs use the same driverClassName.
            if (driverJar == null || "".equals(driverJar) || !driverJar.contains(FileExtensions.JAR_FILE_SUFFIX)) {
                return t4d.get(0).getDbTypeName();
            } else if (driverJar.contains("postgresql-8.3-603.jdbc3.jar") || driverJar.contains("postgresql-8.3-603.jdbc4.jar")
                    || driverJar.contains("postgresql-8.3-603.jdbc2.jar")) {//
                return EDatabase4DriverClassName.PSQL.getDbTypeName();
            } else {
                return t4d.get(0).getDbTypeName(); // first default
            }
        }
        return null;
    }
}
