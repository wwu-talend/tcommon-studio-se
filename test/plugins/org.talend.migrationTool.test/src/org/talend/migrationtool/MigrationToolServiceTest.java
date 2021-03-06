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
package org.talend.migrationtool;

import static org.mockito.Mockito.*;
import junit.framework.Assert;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.migration.IMigrationToolService;
import org.talend.core.model.properties.MigrationStatus;
import org.talend.core.model.properties.MigrationTask;
import org.talend.core.model.properties.Project;
import org.talend.core.model.utils.MigrationUtil;

/**
 * DOC ycbai class global comment. Detailled comment
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ VersionUtils.class })
public class MigrationToolServiceTest {

    private static IMigrationToolService service;

    private MigrationStatus DEFAULT_STATUS = MigrationUtil.DEFAULT_STATUS;

    @BeforeClass
    public static void setUp() throws Exception {
        service = new MigrationToolService();
    }

    @AfterClass
    public static void tearDown() {
        service = null;
    }

    /**
     * Test method for
     * {@link org.talend.migrationtool.MigrationToolService#checkMigrationTasks(org.talend.core.model.properties.Project)}
     * .
     */
    @Test
    public void testCheckMigrationTasks() {

        /* If top break version < Studio version, can apply the migration. */
        Assert.assertTrue(checkMigrationTasks("5.2.0", "5.1.2", "5.1.1"));

        /*
         * If top break version == Studio version and data version:Mm == Studio version:Mm and data version:s > Studio
         * version:s, cannot apply the migration.
         */
        Assert.assertFalse(checkMigrationTasks("5.1.1", "5.1.2", "5.1.1"));

        /*
         * If top break version == Studio version and data version:Mm == Studio version:Mm and data version:s < Studio
         * version:s, can apply the migration.
         */
        Assert.assertTrue(checkMigrationTasks("5.2.1", "5.2.0", "5.2.1"));

        /*
         * If top break version == Studio version and data version:Mm != Studio version:Mm , cannot apply the migration.
         */
        Assert.assertFalse(checkMigrationTasks("5.2.1", "5.1.1", "5.2.1"));

        /*
         * If top break version > Studio version and data version:Mm == Studio version:Mm and data version:s > Studio
         * version:s, cannot apply the migration.
         */
        Assert.assertFalse(checkMigrationTasks("5.2.1", "5.2.2", "5.2.3"));

        /*
         * If top break version > Studio version and data version:Mm == Studio version:Mm and data version:s < Studio
         * version:s, can apply the migration.
         */
        Assert.assertTrue(checkMigrationTasks("5.2.1", "5.2.0", "5.2.3"));

        /*
         * If top break version > Studio version and data version:Mm == Studio version:Mm and data version:s == Studio
         * version:s, can apply the migration.
         */
        Assert.assertTrue(checkMigrationTasks("5.2.1", "5.2.1", "5.2.3"));

        /*
         * If top break version > Studio version and data version:Mm != Studio version:Mm , cannot apply the migration.
         */
        Assert.assertFalse(checkMigrationTasks("5.2.1", "5.1.1", "5.2.3"));

    }

    private boolean checkMigrationTasks(String talendVersion, String topVersion, String topBreaks) {
        Project project = mock(Project.class);
        PowerMockito.mockStatic(VersionUtils.class);

        EList<MigrationTask> tasks = new BasicEList<MigrationTask>();
        tasks.add(MigrationUtil.createMigrationTask("task", topVersion, topBreaks, DEFAULT_STATUS));
        when(project.getMigrationTask()).thenReturn(tasks);

        when(VersionUtils.getTalendVersion()).thenReturn(talendVersion);

        return service.checkMigrationTasks(project);
    }

}
