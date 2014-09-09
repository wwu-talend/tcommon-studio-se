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
package org.talend.osgi.hook;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Properties;

import org.eclipse.osgi.baseadaptor.BaseAdaptor;
import org.eclipse.osgi.baseadaptor.hooks.AdaptorHook;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.talend.osgi.hook.notification.JarMissingObservable;

/**
 * This adaptor hook register the JarMissingObservable as an OSGI service and also initialize the System Property
 * org.talend.external.lib.folder . created by sgandon on 12 sept. 2013
 * 
 */
public class TalendHookAdaptor implements AdaptorHook {

    /**
     * the system property that contains the folder where external libas are stored
     */
    public static final String ORG_TALEND_EXTERNAL_LIB_FOLDER_SYS_PROP = "talend.library.path"; //$NON-NLS-1$

    /**
     * default subfolder path to look for missing jars
     */
    private static final String LIB_JAVA_SUB_FOLDER = "lib/java"; //$NON-NLS-1$

    private final JarMissingObservable jarMissingObservable;

    private FrameworkLog frameworkLog;

    public TalendHookAdaptor(JarMissingObservable jarMissingObservable) {
        this.jarMissingObservable = jarMissingObservable;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.osgi.baseadaptor.hooks.AdaptorHook#initialize(org.eclipse.osgi.baseadaptor.BaseAdaptor)
     */
    @Override
    public void initialize(BaseAdaptor adaptor) {// cannot use the adaptor to get the context cause it is null here
        this.frameworkLog = adaptor.getFrameworkLog();
    }

    /*
     * register the jarMissingObservable as an OSGI service
     * 
     * @see org.eclipse.osgi.baseadaptor.hooks.AdaptorHook#frameworkStart(org.osgi.framework.BundleContext)
     */
    @Override
    public void frameworkStart(BundleContext context) throws BundleException {
        context.registerService(JarMissingObservable.class.getCanonicalName(), jarMissingObservable, null);
        storeTalendLibFolder(context);
    }

    /**
     * DOC sgandon Comment method "storeTalendLibFolder".
     * 
     * @param context
     */
    private void storeTalendLibFolder(BundleContext context) {
        try {
            File talendLibFolder = getLibJavaFolderFile(context);
            System.setProperty(ORG_TALEND_EXTERNAL_LIB_FOLDER_SYS_PROP, talendLibFolder.getAbsolutePath());
        } catch (URISyntaxException e) {
            if (frameworkLog != null) {
                frameworkLog.log(new FrameworkLogEntry("org.talend.osgi.lib.loader", "failed to initialize [" //$NON-NLS-1$ //$NON-NLS-2$
                        + ORG_TALEND_EXTERNAL_LIB_FOLDER_SYS_PROP + "]", 0, e, null)); //$NON-NLS-1$
            } else {
                System.err.println("Failed to initialise the system property [" + ORG_TALEND_EXTERNAL_LIB_FOLDER_SYS_PROP + "]"); //$NON-NLS-1$//$NON-NLS-2$
                e.printStackTrace(System.err);// using syso caus I do not know how to get the loggin instance.
            }
        }
    }

    /**
     * return the folder where to find the missing libraries
     * 
     * @param context
     * */
    protected File getLibJavaFolderFile(BundleContext context) throws URISyntaxException {
        String libFolderSysProp = System.getProperty(ORG_TALEND_EXTERNAL_LIB_FOLDER_SYS_PROP);
        if (libFolderSysProp != null) {
            return new File(libFolderSysProp);
        } else {
            Location installLocation = getConfigurationLocation(context);
            File installFolder = URIUtil.toFile(URIUtil.toURI(installLocation.getURL()));
            return new File(installFolder, System.getProperty("org.talend.lib.subfolder", LIB_JAVA_SUB_FOLDER)); //$NON-NLS-1$
        }

    }

    /**
     * return the eclipse install location
     * 
     * @param bundleContext
     */
    private Location getInstallLocation(BundleContext bundleContext) {
        Filter filter = null;
        try {
            filter = bundleContext.createFilter(Location.INSTALL_FILTER);
        } catch (InvalidSyntaxException e) {
            // ignore this. It should never happen as we have tested the above format.
        }
        ServiceTracker installLocation = new ServiceTracker(bundleContext, filter, null);
        installLocation.open();
        return (Location) installLocation.getService();
    }

    /**
     * return the eclipse configuration location
     * 
     * @param bundleContext
     */
    private Location getConfigurationLocation(BundleContext bundleContext) {
        Filter filter = null;
        try {
            filter = bundleContext.createFilter(Location.CONFIGURATION_FILTER);
        } catch (InvalidSyntaxException e) {
            // ignore this. It should never happen as we have tested the above format.
        }
        ServiceTracker configurationLocation = new ServiceTracker(bundleContext, filter, null);
        configurationLocation.open();
        return (Location) configurationLocation.getService();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.osgi.baseadaptor.hooks.AdaptorHook#frameworkStop(org.osgi.framework.BundleContext)
     */
    @Override
    public void frameworkStop(BundleContext context) throws BundleException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.osgi.baseadaptor.hooks.AdaptorHook#frameworkStopping(org.osgi.framework.BundleContext)
     */
    @Override
    public void frameworkStopping(BundleContext context) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.osgi.baseadaptor.hooks.AdaptorHook#addProperties(java.util.Properties)
     */
    @Override
    public void addProperties(Properties properties) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.osgi.baseadaptor.hooks.AdaptorHook#mapLocationToURLConnection(java.lang.String)
     */
    @Override
    public URLConnection mapLocationToURLConnection(String location) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.osgi.baseadaptor.hooks.AdaptorHook#handleRuntimeError(java.lang.Throwable)
     */
    @Override
    public void handleRuntimeError(Throwable error) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.osgi.baseadaptor.hooks.AdaptorHook#createFrameworkLog()
     */
    @Override
    public FrameworkLog createFrameworkLog() {
        // TODO Auto-generated method stub
        return null;
    }

}
