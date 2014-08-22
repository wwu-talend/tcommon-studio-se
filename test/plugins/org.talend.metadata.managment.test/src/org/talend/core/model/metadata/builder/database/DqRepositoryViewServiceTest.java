/**
 *
 */
package org.talend.core.model.metadata.builder.database;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.talend.core.database.EDatabase4DriverClassName;
import org.talend.core.model.metadata.MetadataFillFactory;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.ConnectionPackage;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.util.MetadataConnectionUtils;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.cwm.helper.CatalogHelper;
import org.talend.cwm.helper.ColumnSetHelper;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.PackageHelper;
import org.talend.cwm.helper.SchemaHelper;
import org.talend.cwm.relational.RelationalPackage;
import org.talend.cwm.relational.TdColumn;
import org.talend.cwm.relational.TdTable;
import org.talend.cwm.relational.TdView;
import org.talend.cwm.xml.TdXmlElementType;
import org.talend.cwm.xml.TdXmlSchema;
import org.talend.cwm.xml.XmlFactory;
import org.talend.utils.sql.ConnectionUtils;
import org.talend.utils.sugars.ReturnCode;
import org.talend.utils.sugars.TypedReturnCode;
import orgomg.cwm.objectmodel.core.Package;
import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.ColumnSet;
import orgomg.cwm.resource.relational.Schema;

/**
 * @author zshen
 * 
 */

@PrepareForTest({ MetadataConnectionUtils.class, ConnectionUtils.class, MetadataFillFactory.class, ColumnSetHelper.class,
        ExtractMetaDataUtils.class, PackageHelper.class, CatalogHelper.class, SchemaHelper.class, ConnectionHelper.class })
public class DqRepositoryViewServiceTest {

    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();

    private static final String BASE_NAME = "base_name"; //$NON-NLS-1$

    private static final String EXT_NAME = "ext"; //$NON-NLS-1$

    private static final String CATALOG_PATTERN = "catalog_pattern"; //$NON-NLS-1$

    private static final String SCHEMA_PATTERN = "schema_pattern"; //$NON-NLS-1$

    private static final String TABLE_PATTERN = "table_pattern"; //$NON-NLS-1$

    private static final String VIEW_PATTERN = "view_pattern"; //$NON-NLS-1$

    private static final String COLUMN_PATTERN = "column_pattern"; //$NON-NLS-1$

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#createTechnicalName(java.lang.String)}
     * .
     */
    @Test
    public void testCreateTechnicalName() {
        Assert.assertNotNull(DqRepositoryViewService.createTechnicalName(BASE_NAME));
    }

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#refreshDataProvider(org.talend.core.model.metadata.builder.connection.Connection, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testRefreshDataProvider() {
        Connection dataProvider = null; // need to create a real Connection object if the method
                                        // refreshDataProvider() implement
        Assert.assertFalse(DqRepositoryViewService.refreshDataProvider(dataProvider, CATALOG_PATTERN, SCHEMA_PATTERN));
    }

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#refreshTables(orgomg.cwm.objectmodel.core.Package, java.lang.String)}
     * .
     */
    @Test
    public void testRefreshTables() {
        Package schema = null; // need to create a real Package object if the method
                               // refreshTables() implement
        Assert.assertFalse(DqRepositoryViewService.refreshTables(schema, TABLE_PATTERN));
    }

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#refreshViews(orgomg.cwm.objectmodel.core.Package, java.lang.String)}
     * .
     */
    @Test
    public void testRefreshViews() {
        Package schema = null; // need to create a real Package object if the method
                               // refreshViews() implement
        Assert.assertFalse(DqRepositoryViewService.refreshViews(schema, VIEW_PATTERN));
    }

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#refreshColumns(orgomg.cwm.resource.relational.ColumnSet, java.lang.String)}
     * .
     */
    @Test
    public void testRefreshColumns() {
        ColumnSet table = null; // need to create a real Package object if the method
                                // refreshColumns() implement
        Assert.assertFalse(DqRepositoryViewService.refreshColumns(table, COLUMN_PATTERN));
    }

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#createFilename(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCreateFilename() {
        Assert.assertNotNull(DqRepositoryViewService.createFilename(BASE_NAME, EXT_NAME));
    }

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#buildElementName(org.talend.core.model.properties.Property)}
     * .
     */
    @Test
    public void testBuildElementName() {
        String displayName = "displayName"; //$NON-NLS-1$
        String version = "version"; //$NON-NLS-1$
        String elementName = displayName + " " + version; //$NON-NLS-1$

        Property createProperty = PropertiesFactory.eINSTANCE.createProperty();
        createProperty.setDisplayName(displayName);
        createProperty.setVersion(version);

        Assert.assertTrue(elementName.equals(DqRepositoryViewService.buildElementName(createProperty)));
    }

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#hasChildren(org.talend.cwm.xml.TdXmlElementType)}
     * .
     */
    @Test
    public void testHasChildren() {
        String schemaName = "XmlSchemaName"; //$NON-NLS-1$
        TdXmlSchema createTdXmlSchema = XmlFactory.eINSTANCE.createTdXmlSchema();
        createTdXmlSchema.setName(schemaName);

        String elementTypeName = "XmlElementTypeName"; //$NON-NLS-1$
        TdXmlElementType createTdXmlElementType = XmlFactory.eINSTANCE.createTdXmlElementType();
        createTdXmlElementType.setName(elementTypeName);
        createTdXmlElementType.setOwnedDocument(createTdXmlSchema);

        Assert.assertTrue(DqRepositoryViewService.hasChildren(createTdXmlElementType));
    }

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#getColumns(org.talend.core.model.metadata.builder.connection.Connection, orgomg.cwm.resource.relational.ColumnSet, java.lang.String, boolean)}
     * .
     * 
     * @throws SQLException
     */
    @Test
    public void testGetColumns() {
        ColumnSet columnSet = RelationalPackage.eINSTANCE.getRelationalFactory().createTdTable();
        TdColumn column1 = RelationalPackage.eINSTANCE.getRelationalFactory().createTdColumn();
        column1.setName("col1"); //$NON-NLS-1$
        ColumnSetHelper.addColumn(column1, columnSet);
        TdColumn column2 = RelationalPackage.eINSTANCE.getRelationalFactory().createTdColumn();
        column2.setName("col2"); //$NON-NLS-1$
        ColumnSetHelper.addColumn(column2, columnSet);
        String connFailedMessage = "CONNECTION TO DB FAILED"; //$NON-NLS-1$
        try {
            // Assert the case that is not loading from DB
            List<TdColumn> columns = DqRepositoryViewService.getColumns(null, columnSet, null, false);
            assertEquals(2, columns.size());

            // Assert the case that is loading from DB but connection cannot be established.
            DatabaseConnection dbConn = createDatabaseConn();
            // Mock the MetadataConnectionUtils.checkConnection()
            TypedReturnCode<java.sql.Connection> retCode = new TypedReturnCode<java.sql.Connection>();
            retCode.setOk(Boolean.FALSE);
            retCode.setMessage(connFailedMessage);
            PowerMockito.mockStatic(MetadataConnectionUtils.class);
            when(MetadataConnectionUtils.createConnection(dbConn)).thenReturn(retCode);
            DqRepositoryViewService.getColumns(dbConn, columnSet, null, true);

            // Assert the case that loading from DB and connection can be established, it will need a real database
            // (mocked) connection.
            List<TdColumn> columnsExpected = new ArrayList<TdColumn>(3);
            java.sql.Connection sqlConn = Mockito.mock(java.sql.Connection.class);
            DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
            TypedReturnCode<java.sql.Connection> rc = new TypedReturnCode<java.sql.Connection>();
            rc.setOk(Boolean.TRUE);
            rc.setObject(sqlConn);
            PowerMockito.mockStatic(MetadataConnectionUtils.class);
            when(MetadataConnectionUtils.createConnection(dbConn)).thenReturn(rc);
            when(ExtractMetaDataUtils.getInstance().getDatabaseMetaData(sqlConn, dbConn)).thenReturn(databaseMetaData);
            MetadataFillFactory metadataFactory = mock(MetadataFillFactory.class);
            when(metadataFactory.fillColumns(columnSet, databaseMetaData, null, null)).thenReturn(columnsExpected);
            PowerMockito.mockStatic(MetadataFillFactory.class);
            when(MetadataFillFactory.getDBInstance()).thenReturn(metadataFactory);
            List<TdColumn> columnsActual = DqRepositoryViewService.getColumns(dbConn, columnSet, null, true);
            assertEquals(columnsExpected.size(), columnsActual.size());

        } catch (Exception e) {
            // Assert the error message when failed connecting to database.
            assertEquals(connFailedMessage, e.getMessage());
        }

    }

    private DatabaseConnection createDatabaseConn() {
        DatabaseConnection databaseConnection = ConnectionPackage.eINSTANCE.getConnectionFactory().createDatabaseConnection();
        databaseConnection.setLabel(getClass().getName() + "_CONN"); //$NON-NLS-1$
        databaseConnection.setURL("mysql:jdbc://FACK_IP:3306/tbi"); //$NON-NLS-1$
        databaseConnection.setRawPassword(""); //$NON-NLS-1$
        databaseConnection.setUsername(""); //$NON-NLS-1$
        databaseConnection.setDatabaseType(EDatabase4DriverClassName.MYSQL.getDbTypeName());
        databaseConnection.setDriverClass(EDatabase4DriverClassName.MYSQL.getDriverClass());
        databaseConnection.setSID("tbi"); //$NON-NLS-1$
        return databaseConnection;
    }

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#isContainsTable(org.talend.core.model.metadata.builder.connection.Connection, orgomg.cwm.resource.relational.Catalog, java.lang.String)}
     * .
     * 
     */
    @Test
    public void testIsContainsTableConnectionCatalogString() {
        String catalogName = "catalog1";//$NON-NLS-1$
        String tablePattern = null;
        boolean containsTable = false;
        Connection dataProvider = null;
        DatabaseMetaData metaData = null;
        java.sql.Connection sqlConn = null;
        Catalog catalog = null;
        // mock ResultSet
        ResultSet tables = Mockito.mock(ResultSet.class);
        try {
            Mockito.when(tables.next()).thenReturn(true);

            // ~ResultSet
            // mock TeraDatabaseMetaData
            metaData = Mockito.mock(DatabaseMetaData.class);
            // catalogOrSchema.getName(), null, tablePattern, tableType

            Mockito.when(metaData.getTables(catalogName, null, tablePattern, DqRepositoryViewService.TABLE_TYPES)).thenReturn(
                    tables);

            // ~mock
            // mock dataProvider
            // 1.case of DatabaseConnection
            dataProvider = Mockito.mock(DatabaseConnection.class);
            // 2.case of others if have
            // ~dataProvider
            // prepare data for reConn
            TypedReturnCode<java.sql.Connection> reConn = new TypedReturnCode<java.sql.Connection>();
            sqlConn = Mockito.mock(java.sql.Connection.class);
            reConn.setOk(true);
            reConn.setObject(sqlConn);

            Mockito.when(sqlConn.getMetaData()).thenReturn(metaData);
            Mockito.when(sqlConn.isClosed()).thenReturn(true);

            // ~reConn

            // mock catalog
            catalog = Mockito.mock(Catalog.class);
            Mockito.when(catalog.getName()).thenReturn(catalogName);
            // ~catalog

            // MetadataConnectionUtils.checkConnection(mockDbConn)
            PowerMockito.mockStatic(MetadataConnectionUtils.class);
            Mockito.when(MetadataConnectionUtils.createConnection((DatabaseConnection) dataProvider)).thenReturn(reConn);

            // ExtractMetaDataUtils.getDatabaseMetaData()
            PowerMockito.mockStatic(ExtractMetaDataUtils.class);
            ExtractMetaDataUtils extract = Mockito.mock(ExtractMetaDataUtils.class);
            Mockito.when(ExtractMetaDataUtils.getInstance()).thenReturn(extract);
            Mockito.when(extract.getDatabaseMetaData(sqlConn, (DatabaseConnection) dataProvider, false)).thenReturn(metaData);

            // PackageHelper.getCatalogOrSchema()
            PowerMockito.mockStatic(PackageHelper.class);
            Mockito.when(PackageHelper.getCatalogOrSchema(catalog)).thenReturn(catalog);

            // PowerMock.replay(MetadataConnectionUtils.class, ExtractMetaDataUtils.class, PackageHelper.class);

            containsTable = DqRepositoryViewService.isCatalogHasChildren(dataProvider, catalog, tablePattern,
                    DqRepositoryViewService.TABLE_TYPES);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertTrue(containsTable);
        // fail("Not yet implemented");
    }

    // //
    // /**
    // * Test method for
    // * {@link
    // org.talend.core.model.metadata.builder.database.DqRepositoryViewService#isContainsTable(org.talend.core.model.metadata.builder.connection.Connection,
    // orgomg.cwm.resource.relational.Schema, java.lang.String)}
    // * .
    // */
    @Test
    public void testIsContainsTableConnectionSchemaString() {
        String schemaName = "schema1";//$NON-NLS-1$
        String tablePattern = null;
        boolean containsTable = false;
        Connection dataProvider = null;
        DatabaseMetaData metaData = null;
        java.sql.Connection sqlConn = null;
        Schema schema = null;
        // mock ResultSet
        ResultSet tables = Mockito.mock(ResultSet.class);
        try {
            Mockito.when(tables.next()).thenReturn(true);

            // ~ResultSet
            // mock TeraDatabaseMetaData
            metaData = Mockito.mock(DatabaseMetaData.class);
            // catalogOrSchema.getName(), null, tablePattern, tableType

            Mockito.when(metaData.getTables(null, schemaName, tablePattern, DqRepositoryViewService.TABLE_TYPES)).thenReturn(
                    tables);

            // ~mock
            // mock dataProvider
            // 1.case of DatabaseConnection
            dataProvider = Mockito.mock(DatabaseConnection.class);
            // 2.case of others if have
            // ~dataProvider
            // prepare data for reConn
            TypedReturnCode<java.sql.Connection> reConn = new TypedReturnCode<java.sql.Connection>();
            sqlConn = Mockito.mock(java.sql.Connection.class);
            reConn.setOk(true);
            reConn.setObject(sqlConn);

            Mockito.when(sqlConn.getMetaData()).thenReturn(metaData);
            Mockito.when(sqlConn.isClosed()).thenReturn(true);

            // ~reConn

            // mock catalog
            schema = Mockito.mock(Schema.class);
            Mockito.when(schema.getName()).thenReturn(schemaName);
            // ~catalog

            // MetadataConnectionUtils.checkConnection(mockDbConn)
            PowerMockito.mockStatic(MetadataConnectionUtils.class);
            Mockito.when(MetadataConnectionUtils.createConnection((DatabaseConnection) dataProvider)).thenReturn(reConn);

            // ExtractMetaDataUtils.getDatabaseMetaData()
            PowerMockito.mockStatic(ExtractMetaDataUtils.class);
            ExtractMetaDataUtils extract = Mockito.mock(ExtractMetaDataUtils.class);
            Mockito.when(ExtractMetaDataUtils.getInstance()).thenReturn(extract);
            Mockito.when(extract.getDatabaseMetaData(sqlConn, (DatabaseConnection) dataProvider, false)).thenReturn(metaData);

            // PackageHelper.getCatalogOrSchema()
            PowerMockito.mockStatic(PackageHelper.class);
            Mockito.when(PackageHelper.getCatalogOrSchema(schema)).thenReturn(schema);
            // PackageHelper.getParentPackage
            Mockito.when(PackageHelper.getParentPackage(schema)).thenReturn(null);

            // PowerMock.replay(MetadataConnectionUtils.class, ExtractMetaDataUtils.class, PackageHelper.class);

            containsTable = DqRepositoryViewService.isSchemaHasChildren(dataProvider, schema, tablePattern,
                    DqRepositoryViewService.TABLE_TYPES);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertTrue(containsTable);
        // fail("Not yet implemented");
    }

    //
    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#hasChildren(org.talend.core.model.metadata.builder.connection.Connection, orgomg.cwm.resource.relational.Catalog, java.lang.String)}
     * .
     */
    @Test
    public void testIsContainsViewConnectionCatalogString() {
        String catalogName = "catalog1";//$NON-NLS-1$
        String tablePattern = null;
        boolean containsTable = false;
        Connection dataProvider = null;
        DatabaseMetaData metaData = null;
        java.sql.Connection sqlConn = null;
        Catalog catalog = null;
        // mock ResultSet
        ResultSet tables = Mockito.mock(ResultSet.class);
        try {
            Mockito.when(tables.next()).thenReturn(true);

            // ~ResultSet
            // mock TeraDatabaseMetaData
            metaData = Mockito.mock(DatabaseMetaData.class);
            // catalogOrSchema.getName(), null, tablePattern, tableType

            Mockito.when(metaData.getTables(catalogName, null, tablePattern, DqRepositoryViewService.VIEW_TYPES)).thenReturn(
                    tables);

            // ~mock
            // mock dataProvider
            // 1.case of DatabaseConnection
            dataProvider = Mockito.mock(DatabaseConnection.class);
            // 2.case of others if have
            // ~dataProvider
            // prepare data for reConn
            TypedReturnCode<java.sql.Connection> reConn = new TypedReturnCode<java.sql.Connection>();
            sqlConn = Mockito.mock(java.sql.Connection.class);
            reConn.setOk(true);
            reConn.setObject(sqlConn);

            Mockito.when(sqlConn.getMetaData()).thenReturn(metaData);
            Mockito.when(sqlConn.isClosed()).thenReturn(true);

            // ~reConn

            // mock catalog
            catalog = Mockito.mock(Catalog.class);
            Mockito.when(catalog.getName()).thenReturn(catalogName);
            // ~catalog

            // MetadataConnectionUtils.checkConnection(mockDbConn)
            PowerMockito.mockStatic(MetadataConnectionUtils.class);
            Mockito.when(MetadataConnectionUtils.createConnection((DatabaseConnection) dataProvider)).thenReturn(reConn);

            // ExtractMetaDataUtils.getDatabaseMetaData()
            PowerMockito.mockStatic(ExtractMetaDataUtils.class);
            ExtractMetaDataUtils extract = Mockito.mock(ExtractMetaDataUtils.class);
            Mockito.when(ExtractMetaDataUtils.getInstance()).thenReturn(extract);
            Mockito.when(extract.getDatabaseMetaData(sqlConn, (DatabaseConnection) dataProvider, false)).thenReturn(metaData);

            // PackageHelper.getCatalogOrSchema()
            PowerMockito.mockStatic(PackageHelper.class);
            Mockito.when(PackageHelper.getCatalogOrSchema(catalog)).thenReturn(catalog);

            // PowerMock.replay(MetadataConnectionUtils.class, ExtractMetaDataUtils.class, PackageHelper.class);

            containsTable = DqRepositoryViewService.isCatalogHasChildren(dataProvider, catalog, tablePattern,
                    DqRepositoryViewService.VIEW_TYPES);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertTrue(containsTable);
        // fail("Not yet implemented");
    }

    //
    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#hasChildren(org.talend.core.model.metadata.builder.connection.Connection, orgomg.cwm.resource.relational.Schema, java.lang.String)}
     * .
     */
    @Test
    public void testIsContainsViewConnectionSchemaString() {
        String schemaName = "schema1";//$NON-NLS-1$
        String tablePattern = null;
        boolean containsTable = false;
        Connection dataProvider = null;
        DatabaseMetaData metaData = null;
        java.sql.Connection sqlConn = null;
        Schema schema = null;
        // mock ResultSet
        ResultSet tables = Mockito.mock(ResultSet.class);
        try {
            Mockito.when(tables.next()).thenReturn(true);

            // ~ResultSet
            // mock TeraDatabaseMetaData
            metaData = Mockito.mock(DatabaseMetaData.class);
            // catalogOrSchema.getName(), null, tablePattern, tableType

            Mockito.when(metaData.getTables(null, schemaName, tablePattern, DqRepositoryViewService.VIEW_TYPES)).thenReturn(
                    tables);

            // ~mock
            // mock dataProvider
            // 1.case of DatabaseConnection
            dataProvider = Mockito.mock(DatabaseConnection.class);
            // 2.case of others if have
            // ~dataProvider
            // prepare data for reConn
            TypedReturnCode<java.sql.Connection> reConn = new TypedReturnCode<java.sql.Connection>();
            sqlConn = Mockito.mock(java.sql.Connection.class);
            reConn.setOk(true);
            reConn.setObject(sqlConn);

            Mockito.when(sqlConn.getMetaData()).thenReturn(metaData);
            Mockito.when(sqlConn.isClosed()).thenReturn(true);

            // ~reConn

            // mock catalog
            schema = Mockito.mock(Schema.class);
            Mockito.when(schema.getName()).thenReturn(schemaName);
            // ~catalog

            // MetadataConnectionUtils.checkConnection(mockDbConn)
            PowerMockito.mockStatic(MetadataConnectionUtils.class);
            Mockito.when(MetadataConnectionUtils.createConnection((DatabaseConnection) dataProvider)).thenReturn(reConn);

            // ExtractMetaDataUtils.getDatabaseMetaData()
            PowerMockito.mockStatic(ExtractMetaDataUtils.class);
            ExtractMetaDataUtils extract = Mockito.mock(ExtractMetaDataUtils.class);
            Mockito.when(ExtractMetaDataUtils.getInstance()).thenReturn(extract);
            Mockito.when(extract.getDatabaseMetaData(sqlConn, (DatabaseConnection) dataProvider, false)).thenReturn(metaData);

            // PackageHelper.getCatalogOrSchema()
            PowerMockito.mockStatic(PackageHelper.class);
            Mockito.when(PackageHelper.getCatalogOrSchema(schema)).thenReturn(schema);
            // PackageHelper.getParentPackage
            Mockito.when(PackageHelper.getParentPackage(schema)).thenReturn(null);

            // PowerMock.replay(MetadataConnectionUtils.class, ExtractMetaDataUtils.class, PackageHelper.class);

            containsTable = DqRepositoryViewService.isSchemaHasChildren(dataProvider, schema, tablePattern,
                    DqRepositoryViewService.VIEW_TYPES);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertTrue(containsTable);
        // fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#getTables(org.talend.core.model.metadata.builder.connection.Connection, orgomg.cwm.resource.relational.Catalog, java.lang.String, boolean)}
     * .
     */
    @Test
    public void testGetTablesConnectionCatalogStringBoolean() {
        java.sql.Connection sqlConn = null;
        DatabaseMetaData metaData = null;
        String catalogName = "tbi";//$NON-NLS-1$
        List<TdTable> tableList = new ArrayList<TdTable>();
        List<TdTable> retableList = new ArrayList<TdTable>();
        List<TdTable> resultList1 = null;
        List<TdTable> resultList2 = null;
        List<TdTable> resultList3 = null;
        String tablePattern = null;
        boolean loadFromDB = true;
        // mock TdTable
        TdTable tableMock = Mockito.mock(TdTable.class);
        // ~TdTable
        retableList.add(tableMock);

        // mock ResultSet
        ResultSet tables = Mockito.mock(ResultSet.class);
        try {
            // mock Connection
            Connection dataProvider = Mockito.mock(DatabaseConnection.class);
            // ~Connection
            // mock Catalog
            Catalog catalog = Mockito.mock(Catalog.class);
            Mockito.when(catalog.getName()).thenReturn(catalogName);
            // ~Catalog

            // prepare data for reConn
            TypedReturnCode<java.sql.Connection> reConn = new TypedReturnCode<java.sql.Connection>();
            sqlConn = Mockito.mock(java.sql.Connection.class);
            reConn.setOk(true);
            reConn.setObject(sqlConn);
            ReturnCode rc = new ReturnCode(true);

            // mock DatabaseMetaData
            metaData = Mockito.mock(DatabaseMetaData.class);
            // catalogOrSchema.getName(), null, tablePattern, tableType
            // Mockito.when(metaData.getTables(null, schemaName, tablePattern, tableType)).thenReturn(tables);

            // MetadataConnectionUtils.checkConnection(mockDbConn)
            PowerMockito.mockStatic(MetadataConnectionUtils.class);
            Mockito.when(MetadataConnectionUtils.createConnection((DatabaseConnection) dataProvider)).thenReturn(reConn);

            // ExtractMetaDataUtils.getDatabaseMetaData()
            PowerMockito.mockStatic(ExtractMetaDataUtils.class);
            ExtractMetaDataUtils extract = Mockito.mock(ExtractMetaDataUtils.class);
            Mockito.when(ExtractMetaDataUtils.getInstance()).thenReturn(extract);
            Mockito.when(extract.getDatabaseMetaData(sqlConn, (DatabaseConnection) dataProvider, false)).thenReturn(metaData);

            // PackageHelper.getCatalogOrSchema()
            PowerMockito.mockStatic(PackageHelper.class);
            Mockito.when(PackageHelper.getTables(catalog)).thenReturn(tableList);

            // ~FillFactory
            MetadataFillFactory metadataMock = Mockito.mock(MetadataFillFactory.class);
            Mockito.when(
                    metadataMock.fillTables((orgomg.cwm.objectmodel.core.Package) Mockito.eq(catalog), Mockito.eq(metaData),
                            Mockito.anyList(), Mockito.eq(tablePattern), (String[]) Mockito.any())).thenReturn(retableList);

            // Mockito.when(
            // metadataMock.fillColumns(Mockito.eq(mockColumnSet), (DatabaseMetaData) Mockito.any(),
            // (List<String>) Mockito.isNull(), (String) Mockito.isNull())).thenReturn(returnColumns);
            // MetadataFillFactory.getDBInstance()
            PowerMockito.mockStatic(MetadataFillFactory.class);
            Mockito.when(MetadataFillFactory.getDBInstance()).thenReturn(metadataMock);
            // ConnectionUtils.closeConnection(sqlConn)
            PowerMockito.mockStatic(ConnectionUtils.class);
            Mockito.when(ConnectionUtils.closeConnection(sqlConn)).thenReturn(rc);

            // CatalogHelper.getTables
            PowerMockito.mockStatic(CatalogHelper.class);
            Mockito.when(CatalogHelper.getTables(catalog)).thenReturn(retableList);

            // PowerMock.replay(MetadataConnectionUtils.class, ExtractMetaDataUtils.class, PackageHelper.class,
            // MetadataFillFactory.class, CatalogHelper.class, ConnectionUtils.class);
            // case1: catalog no tables
            resultList1 = DqRepositoryViewService.getTables(dataProvider, catalog, tablePattern, loadFromDB);
            // case2:catalog hava tables
            tableList.add(tableMock);
            resultList2 = DqRepositoryViewService.getTables(dataProvider, catalog, tablePattern, loadFromDB);
            // // case3:get tables from file
            loadFromDB = false;
            resultList3 = DqRepositoryViewService.getTables(dataProvider, catalog, tablePattern, loadFromDB);

            assertTrue(resultList1.size() == 1);
            assertTrue(resultList2.size() == 1);
            assertTrue(resultList3.size() == 1);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#getTables(org.talend.core.model.metadata.builder.connection.Connection, orgomg.cwm.resource.relational.Schema, java.lang.String, boolean)}
     * .
     */
    @Test
    public void testGetTablesConnectionSchemaStringBoolean() {
        java.sql.Connection sqlConn = null;
        DatabaseMetaData metaData = null;
        String schemaName = "schema1";//$NON-NLS-1$

        List<TdTable> tableList = new ArrayList<TdTable>();
        List<TdTable> retableList = new ArrayList<TdTable>();

        List<TdTable> resultList1 = null;
        List<TdTable> resultList2 = null;
        List<TdTable> resultList3 = null;
        String tablePattern = null;
        boolean loadFromDB = true;
        // mock TdTable
        TdTable tableMock = Mockito.mock(TdTable.class);
        // ~TdTable
        retableList.add(tableMock);

        // mock ResultSet
        ResultSet tables = Mockito.mock(ResultSet.class);
        try {
            // mock Connection
            Connection dataProvider = Mockito.mock(DatabaseConnection.class);
            // ~Connection
            // mock Catalog
            Catalog catalog = Mockito.mock(Catalog.class);
            // Mockito.when(methodCall)
            // ~Catalog
            // mock Schema
            Schema schema = Mockito.mock(Schema.class);
            // Mockito.when(schema.getName()).thenReturn(schemaName);
            // ~Schema

            // prepare data for reConn
            TypedReturnCode<java.sql.Connection> reConn = new TypedReturnCode<java.sql.Connection>();
            sqlConn = Mockito.mock(java.sql.Connection.class);
            reConn.setOk(true);
            reConn.setObject(sqlConn);
            ReturnCode rc = new ReturnCode(true);

            // mock DatabaseMetaData
            metaData = Mockito.mock(DatabaseMetaData.class);
            // catalogOrSchema.getName(), null, tablePattern, tableType
            // Mockito.when(metaData.getTables(null, schemaName, tablePattern, tableType)).thenReturn(tables);

            // MetadataConnectionUtils.checkConnection(mockDbConn)
            PowerMockito.mockStatic(MetadataConnectionUtils.class);
            Mockito.when(MetadataConnectionUtils.createConnection((DatabaseConnection) dataProvider)).thenReturn(reConn);

            // ExtractMetaDataUtils.getDatabaseMetaData()
            PowerMockito.mockStatic(ExtractMetaDataUtils.class);
            ExtractMetaDataUtils extract = Mockito.mock(ExtractMetaDataUtils.class);
            Mockito.when(ExtractMetaDataUtils.getInstance()).thenReturn(extract);

            Mockito.when(extract.getDatabaseMetaData(sqlConn, (DatabaseConnection) dataProvider, false)).thenReturn(metaData);

            // PackageHelper.getCatalogOrSchema()
            PowerMockito.mockStatic(PackageHelper.class);
            Mockito.when(PackageHelper.getTables(schema)).thenReturn(tableList);

            // ~FillFactory
            MetadataFillFactory metadataMock = Mockito.mock(MetadataFillFactory.class);
            Mockito.when(
                    metadataMock.fillTables((orgomg.cwm.objectmodel.core.Package) Mockito.eq(schema), Mockito.eq(metaData),
                            Mockito.anyList(), Mockito.eq(tablePattern), (String[]) Mockito.any())).thenReturn(retableList)
                    .thenReturn(retableList);

            // Mockito.when(
            // metadataMock.fillColumns(Mockito.eq(mockColumnSet), (DatabaseMetaData) Mockito.any(),
            // (List<String>) Mockito.isNull(), (String) Mockito.isNull())).thenReturn(returnColumns);
            // MetadataFillFactory.getDBInstance()
            PowerMockito.mockStatic(MetadataFillFactory.class);
            Mockito.when(MetadataFillFactory.getDBInstance()).thenReturn(metadataMock);
            // ConnectionUtils.closeConnection(sqlConn)
            PowerMockito.mockStatic(ConnectionUtils.class);
            Mockito.when(ConnectionUtils.closeConnection(sqlConn)).thenReturn(rc);

            // SchemaHelper.getTables
            PowerMockito.mockStatic(SchemaHelper.class);
            Mockito.when(SchemaHelper.getTables(schema)).thenReturn(retableList);
            // CatalogHelper.getParentCatalog
            PowerMockito.mockStatic(CatalogHelper.class);
            Mockito.when(CatalogHelper.getParentCatalog(schema)).thenReturn(catalog);

            // PowerMock.replay(MetadataConnectionUtils.class, ExtractMetaDataUtils.class,
            // PackageHelper.class,MetadataFillFactory.class, CatalogHelper.class, SchemaHelper.class,
            // ConnectionUtils.class);
            // case1: catalog no tables
            resultList1 = DqRepositoryViewService.getTables(dataProvider, schema, tablePattern, loadFromDB);
            // case2:catalog hava tables
            tableList.add(tableMock);
            resultList2 = DqRepositoryViewService.getTables(dataProvider, schema, tablePattern, loadFromDB);
            // case3:get tables from file
            loadFromDB = false;
            resultList3 = DqRepositoryViewService.getTables(dataProvider, schema, tablePattern, loadFromDB);

            assertTrue(resultList1.size() == 1);
            assertTrue(resultList2.size() == 1);
            assertTrue(resultList3.size() == 1);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#getViews(org.talend.core.model.metadata.builder.connection.Connection, orgomg.cwm.resource.relational.Catalog, java.lang.String, boolean)}
     * .
     */
    @Test
    public void testGetViewsConnectionCatalogStringBoolean() {
        java.sql.Connection sqlConn = null;
        DatabaseMetaData metaData = null;
        String catalogName = "tbi";//$NON-NLS-1$
        List<TdView> tableList = new ArrayList<TdView>();
        List<TdView> retableList = new ArrayList<TdView>();
        List<TdView> resultList1 = null;
        List<TdView> resultList2 = null;
        List<TdView> resultList3 = null;
        String tablePattern = null;
        boolean loadFromDB = true;
        // mock TdTable
        TdView tableMock = Mockito.mock(TdView.class);
        // ~TdTable
        retableList.add(tableMock);

        // mock ResultSet
        ResultSet tables = Mockito.mock(ResultSet.class);
        try {
            // mock Connection
            Connection dataProvider = Mockito.mock(DatabaseConnection.class);
            // ~Connection
            // mock Catalog
            Catalog catalog = Mockito.mock(Catalog.class);
            // Mockito.when(catalog.getName()).thenReturn(catalogName);
            // ~Catalog

            // prepare data for reConn
            TypedReturnCode<java.sql.Connection> reConn = new TypedReturnCode<java.sql.Connection>();
            sqlConn = Mockito.mock(java.sql.Connection.class);
            reConn.setOk(true);
            reConn.setObject(sqlConn);
            ReturnCode rc = new ReturnCode(true);

            // mock DatabaseMetaData
            metaData = Mockito.mock(DatabaseMetaData.class);
            // catalogOrSchema.getName(), null, tablePattern, tableType
            // Mockito.when(metaData.getTables(null, schemaName, tablePattern, tableType)).thenReturn(tables);

            // MetadataConnectionUtils.checkConnection(mockDbConn)
            PowerMockito.mockStatic(MetadataConnectionUtils.class);
            Mockito.when(MetadataConnectionUtils.createConnection((DatabaseConnection) dataProvider)).thenReturn(reConn);

            // ExtractMetaDataUtils.getDatabaseMetaData()
            PowerMockito.mockStatic(ExtractMetaDataUtils.class);
            ExtractMetaDataUtils extract = Mockito.mock(ExtractMetaDataUtils.class);
            Mockito.when(ExtractMetaDataUtils.getInstance()).thenReturn(extract);
            Mockito.when(extract.getDatabaseMetaData(sqlConn, (DatabaseConnection) dataProvider, false)).thenReturn(metaData);

            // PackageHelper.getCatalogOrSchema()
            PowerMockito.mockStatic(PackageHelper.class);
            Mockito.when(PackageHelper.getViews(catalog)).thenReturn(tableList);

            // ~FillFactory
            MetadataFillFactory metadataMock = Mockito.mock(MetadataFillFactory.class);
            Mockito.when(
                    metadataMock.fillViews(Mockito.eq(catalog), Mockito.eq(metaData), Mockito.anyList(),
                            Mockito.eq(tablePattern), Mockito.eq(DqRepositoryViewService.VIEW_TYPES))).thenReturn(retableList)
                    .thenReturn(retableList);

            // Mockito.when(
            // metadataMock.fillColumns(Mockito.eq(mockColumnSet), (DatabaseMetaData) Mockito.any(),
            // (List<String>) Mockito.isNull(), (String) Mockito.isNull())).thenReturn(returnColumns);
            // MetadataFillFactory.getDBInstance()
            PowerMockito.mockStatic(MetadataFillFactory.class);
            Mockito.when(MetadataFillFactory.getDBInstance()).thenReturn(metadataMock);
            // ConnectionUtils.closeConnection(sqlConn)
            PowerMockito.mockStatic(ConnectionUtils.class);
            Mockito.when(ConnectionUtils.closeConnection(sqlConn)).thenReturn(rc);

            // CatalogHelper.getTables
            PowerMockito.mockStatic(CatalogHelper.class);
            Mockito.when(CatalogHelper.getViews(catalog)).thenReturn(retableList);

            // PowerMock.replay(MetadataConnectionUtils.class, ExtractMetaDataUtils.class,
            // PackageHelper.class,MetadataFillFactory.class, CatalogHelper.class, ConnectionUtils.class);
            // case1: catalog no tables
            resultList1 = DqRepositoryViewService.getViews(dataProvider, catalog, tablePattern, loadFromDB);
            // case2:catalog hava tables
            tableList.add(tableMock);
            resultList2 = DqRepositoryViewService.getViews(dataProvider, catalog, tablePattern, loadFromDB);
            // // case3:get tables from file
            loadFromDB = false;
            resultList3 = DqRepositoryViewService.getViews(dataProvider, catalog, tablePattern, loadFromDB);

            assertTrue(resultList1.size() == 1);
            assertTrue(resultList2.size() == 1);
            assertTrue(resultList3.size() == 1);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Test method for
     * {@link org.talend.core.model.metadata.builder.database.DqRepositoryViewService#getViews(org.talend.core.model.metadata.builder.connection.Connection, orgomg.cwm.resource.relational.Schema, java.lang.String, boolean)}
     * .
     */
    @Test
    public void testGetViewsConnectionSchemaStringBoolean() {
        java.sql.Connection sqlConn = null;
        DatabaseMetaData metaData = null;
        String schemaName = "schema1";//$NON-NLS-1$

        List<TdView> tableList = new ArrayList<TdView>();
        List<TdView> retableList = new ArrayList<TdView>();

        List<TdView> resultList1 = null;
        List<TdView> resultList2 = null;
        List<TdView> resultList3 = null;
        String tablePattern = null;
        boolean loadFromDB = true;
        // mock TdTable
        TdView tableMock = Mockito.mock(TdView.class);
        // ~TdTable
        retableList.add(tableMock);

        // mock ResultSet
        ResultSet tables = Mockito.mock(ResultSet.class);
        try {
            // mock Connection
            Connection dataProvider = Mockito.mock(DatabaseConnection.class);
            // ~Connection
            // mock Catalog
            Catalog catalog = Mockito.mock(Catalog.class);
            // Mockito.when(methodCall)
            // ~Catalog
            // mock Schema
            Schema schema = Mockito.mock(Schema.class);
            // Mockito.when(schema.getName()).thenReturn(schemaName);
            // ~Schema

            // prepare data for reConn
            TypedReturnCode<java.sql.Connection> reConn = new TypedReturnCode<java.sql.Connection>();
            sqlConn = Mockito.mock(java.sql.Connection.class);
            reConn.setOk(true);
            reConn.setObject(sqlConn);
            ReturnCode rc = new ReturnCode(true);

            // mock DatabaseMetaData
            metaData = Mockito.mock(DatabaseMetaData.class);
            // catalogOrSchema.getName(), null, tablePattern, tableType
            // Mockito.when(metaData.getTables(null, schemaName, tablePattern, tableType)).thenReturn(tables);

            // MetadataConnectionUtils.checkConnection(mockDbConn)
            PowerMockito.mockStatic(MetadataConnectionUtils.class);
            Mockito.when(MetadataConnectionUtils.createConnection((DatabaseConnection) dataProvider)).thenReturn(reConn);

            // ExtractMetaDataUtils.getDatabaseMetaData()
            PowerMockito.mockStatic(ExtractMetaDataUtils.class);
            ExtractMetaDataUtils extract = Mockito.mock(ExtractMetaDataUtils.class);
            Mockito.when(ExtractMetaDataUtils.getInstance()).thenReturn(extract);
            Mockito.when(extract.getDatabaseMetaData(sqlConn, (DatabaseConnection) dataProvider, false)).thenReturn(metaData);

            // PackageHelper.getCatalogOrSchema()
            PowerMockito.mockStatic(PackageHelper.class);
            Mockito.when(PackageHelper.getViews(schema)).thenReturn(tableList);

            // ~FillFactory
            MetadataFillFactory metadataMock = Mockito.mock(MetadataFillFactory.class);
            Mockito.when(
                    metadataMock.fillViews(Mockito.eq(schema), Mockito.eq(metaData), Mockito.anyList(), Mockito.eq(tablePattern),
                            Mockito.eq(DqRepositoryViewService.VIEW_TYPES))).thenReturn(retableList).thenReturn(retableList);

            // Mockito.when(
            // metadataMock.fillColumns(Mockito.eq(mockColumnSet), (DatabaseMetaData) Mockito.any(),
            // (List<String>) Mockito.isNull(), (String) Mockito.isNull())).thenReturn(returnColumns);
            // MetadataFillFactory.getDBInstance()
            PowerMockito.mockStatic(MetadataFillFactory.class);
            Mockito.when(MetadataFillFactory.getDBInstance()).thenReturn(metadataMock);
            // ConnectionUtils.closeConnection(sqlConn)
            PowerMockito.mockStatic(ConnectionUtils.class);
            Mockito.when(ConnectionUtils.closeConnection(sqlConn)).thenReturn(rc);

            // SchemaHelper.getTables
            PowerMockito.mockStatic(SchemaHelper.class);
            Mockito.when(SchemaHelper.getViews(schema)).thenReturn(retableList);
            // CatalogHelper.getParentCatalog
            PowerMockito.mockStatic(CatalogHelper.class);
            Mockito.when(CatalogHelper.getParentCatalog(schema)).thenReturn(catalog);

            // PowerMock.replay(MetadataConnectionUtils.class, ExtractMetaDataUtils.class, PackageHelper.class,
            // MetadataFillFactory.class, CatalogHelper.class, SchemaHelper.class, ConnectionUtils.class);
            // case1: catalog no tables
            resultList1 = DqRepositoryViewService.getViews(dataProvider, schema, tablePattern, loadFromDB);
            // case2:catalog hava tables
            tableList.add(tableMock);
            resultList2 = DqRepositoryViewService.getViews(dataProvider, schema, tablePattern, loadFromDB);
            // case3:get tables from file
            loadFromDB = false;
            resultList3 = DqRepositoryViewService.getViews(dataProvider, schema, tablePattern, loadFromDB);

            assertTrue(resultList1.size() == 1);
            assertTrue(resultList2.size() == 1);
            assertTrue(resultList3.size() == 1);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
