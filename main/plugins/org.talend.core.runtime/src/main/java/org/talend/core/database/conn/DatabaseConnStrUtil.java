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
package org.talend.core.database.conn;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.template.DbConnStrForHive;
import org.talend.core.database.conn.template.EDatabaseConnTemplate;
import org.talend.core.database.conn.version.EDatabaseVersion4Drivers;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.connection.hive.HiveConnVersionInfo;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.utils.TalendQuoteUtils;

/**
 * cli class global comment. Detailled comment
 */
public class DatabaseConnStrUtil {

    private static final String CONN = TalendQuoteUtils.getStringConnect();

    private static final String QUOTE = TalendQuoteUtils.getQuoteChar();

    private static final String SEMICOLON = ";"; //$NON-NLS-1$

    private static String getStringReplace(final String init, final String before, final String after,
            final boolean supportContext) {
        return getStringReplace(init, before, after, supportContext, false);
    }

    /**
     * 
     * cli Comment method "getStringReplace".
     * 
     * if support context, the "init" string have been quoted, and the "after" should be original(quoted or in context
     * mode).
     * 
     */
    private static String getStringReplace(final String init, final String before, final String after,
            final boolean supportContext, final boolean password) {
        String s = init;
        if (after != null && init != null && before != null) {
            if (supportContext) {
                if (ContextParameterUtils.containContextVariables(after)) {
                    s = init.replace(before, changeAndQuoteValue(after));
                } else {
                    if (password) {
                        // if password, shouldn't remote the quotes. just keep original
                        s = init.replace(before, after);
                    } else {
                        s = init.replace(before, TalendQuoteUtils.removeQuotes(after));
                    }
                }
            } else {
                s = init.replace(before, after);
            }

        }
        return s;
    }

    private static String changeAndQuoteValue(String original) {
        return QUOTE + CONN + original + CONN + QUOTE;
    }

    public static String getURLString(final String dbType, final String dbVersion, final String host, final String login,
            final String password, final String port, final String sid, final String filename, final String datasource) {
        return getURLString(false, dbType, dbVersion, host, login, password, port, sid, filename, datasource);
    }

    /**
     * 
     * cli Comment method "getURLString".
     * 
     * if supportContext is true, the other parameters should be kept original value.
     * 
     * (for example, if quote the parameters, should be kept).
     */
    public static String getURLString(final boolean supportContext, final String dbType, final String dbVersion,
            final String host, final String login, final String password, final String port, final String sid,
            final String filename, final String datasource) {
        EDatabaseConnTemplate connStr = EDatabaseConnTemplate.indexOfTemplate(dbType);
        EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersion(dbVersion);
        if (connStr != null) {
            String s = connStr.getUrlTemplate(version);
            if (s != null) {
                if (supportContext) { // if context mode, should quote the original "connStr".
                    s = TalendQuoteUtils.addQuotes(s);
                }
                s = getStringReplace(s, EDatabaseConnVar.LOGIN.getVariable(), login, supportContext);
                s = getStringReplace(s, EDatabaseConnVar.PASSWORD.getVariable(), password, supportContext, true);
                s = getStringReplace(s, EDatabaseConnVar.HOST.getVariable(), host, supportContext);
                s = getStringReplace(s, EDatabaseConnVar.PORT.getVariable(), port, supportContext);
                s = getStringReplace(s, EDatabaseConnVar.SID.getVariable(), sid, supportContext);
                s = getStringReplace(s, EDatabaseConnVar.SERVICE_NAME.getVariable(), sid, supportContext);
                s = getStringReplace(s, EDatabaseConnVar.DATASOURCE.getVariable(), datasource, supportContext);
                // PTODO OCA : if needed, adapt the file separator to all OS (not only backslashes)
                s = getStringReplace(s, EDatabaseConnVar.FILENAME.getVariable(), filename, supportContext);
                return s;
            }
        }
        return DatabaseConnConstants.EMPTY;
    }

    public static String getURLString(final String dbType, final String dbVersion, final String host, final String login,
            final String password, final String port, final String sid, final String filename, final String datasource,
            final String dbrootPath) {
        return getURLString(false, dbType, dbVersion, host, login, password, port, sid, filename, datasource, dbrootPath);
    }

    public static String getURLString(final boolean supportContext, final String dbType, final String dbVersion,
            final String host, final String login, final String password, final String port, final String sid,
            final String filename, final String datasource, final String dbrootPath) {
        String string = getURLString(supportContext, dbType, dbVersion, host, login, password, port, sid, filename, datasource);
        if (string.equals(DatabaseConnConstants.EMPTY)) {
            return DatabaseConnConstants.EMPTY;
        }
        EDatabaseConnTemplate connStr = EDatabaseConnTemplate.indexOfTemplate(dbType);
        if (connStr == EDatabaseConnTemplate.HSQLDB_IN_PROGRESS) {
            string = getStringReplace(string, EDatabaseConnVar.DBROOTPATH.getVariable(), dbrootPath, supportContext);
        } else {
            string = getStringReplace(string, EDatabaseConnVar.DBROOTPATH.getVariable(), sid, supportContext);
        }
        return string;
    }

    public static String getURLString(final String dbType, final String dbVersion, final String host, final String login,
            final String password, final String port, final String sid, final String filename, final String datasource,
            final String dbrootPath, final String addParams, final String... otherParams) {
        return getURLString(false, dbType, dbVersion, host, login, password, port, sid, filename, datasource, dbrootPath,
                addParams, otherParams);
    }

    public static String getURLString(final boolean supportContext, final String dbType, final String dbVersion,
            final String host, final String login, final String password, final String port, final String sid,
            final String filename, final String datasource, final String dbrootPath, final String addParams,
            final String... otherParams) {
        String string = getURLString(supportContext, dbType, dbVersion, host, login, password, port, sid, filename, datasource,
                dbrootPath);
        if (string.equals(DatabaseConnConstants.EMPTY)) {
            return DatabaseConnConstants.EMPTY;
        }
        if (EDatabaseConnTemplate.isAddtionParamsNeeded(dbType)) {
            string = getStringReplace(string, EDatabaseConnVar.PROPERTY.getVariable(), addParams, supportContext);
        }
        return string;
    }

    public static String getHiveURLString(DatabaseConnection dbConn, String server, String port, String sidOrDatabase,
            String template) {
        String hiveModel = dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_HIVE_MODE);
        boolean useKrb = Boolean.valueOf(dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_USE_KRB));
        String hivePrincipal = null;
        if (useKrb) {
            hivePrincipal = StringUtils.trimToNull(dbConn.getParameters()
                    .get(ConnParameterKeys.HIVE_AUTHENTICATION_HIVEPRINCIPLA));
        }
        // DbConnStrForHive.URL_HIVE_2_TEMPLATE or DbConnStrForHive.URL_HIVE_1_TEMPLATE
        // set a default
        String url = null;
        if (template.startsWith(DbConnStrForHive.URL_HIVE_2_TEMPLATE)) {
            if (HiveConnVersionInfo.MODE_EMBEDDED.getKey().equalsIgnoreCase(hiveModel)) {
                url = getHive2EmbeddedURLString();
            } else {
                url = getHive2StandaloneURLString(false, server, port, sidOrDatabase, hivePrincipal);
            }
        } else if (template.startsWith(DbConnStrForHive.URL_HIVE_1_TEMPLATE)) {
            if (HiveConnVersionInfo.MODE_EMBEDDED.getKey().equalsIgnoreCase(hiveModel)) {
                url = getHive1EmbeddedURLString();
            } else {
                url = getHive1StandaloneURLString(false, server, port, sidOrDatabase);
            }
        }
        if (url == null) {
            // set a default
            url = getHive1EmbeddedURLString();
        }
        return url;
    }

    private static String getHive1EmbeddedURLString() {
        return EDatabaseConnTemplate.HIVE.getUrlTemplate(EDatabaseVersion4Drivers.HIVE_EMBEDDED);
    }

    private static String getHive2EmbeddedURLString() {
        return EDatabaseConnTemplate.HIVE.getUrlTemplate(EDatabaseVersion4Drivers.HIVE_2_EMBEDDED);
    }

    private static String getHive1StandaloneURLString(boolean supportContext, String server, String port, String sid) {
        String s = EDatabaseConnTemplate.HIVE.getUrlTemplate(EDatabaseVersion4Drivers.HIVE);
        return getHiveStandaloneURlString(s, supportContext, server, port, sid);
    }

    private static String getHiveStandaloneURlString(String template, boolean supportContext, String server, String port,
            String sid) {
        String s = template;
        if (s != null) {
            if (supportContext) { // if context mode, should quote the original "connStr".
                s = TalendQuoteUtils.addQuotes(s);
            }
            s = getStringReplace(s, EDatabaseConnVar.HOST.getVariable(), server, supportContext);
            s = getStringReplace(s, EDatabaseConnVar.PORT.getVariable(), port, supportContext);
            s = getStringReplace(s, EDatabaseConnVar.SID.getVariable(), sid, supportContext);
        }
        return s;
    }

    private static String getHive2StandaloneURLString(boolean supportContext, String server, String port, String sid,
            String hivePrincipal) {
        String s = EDatabaseConnTemplate.HIVE.getUrlTemplate(EDatabaseVersion4Drivers.HIVE_2_STANDALONE);
        String standardURlString = getHiveStandaloneURlString(s, supportContext, server, port, sid);
        String principalSuffix = "principal="; //$NON-NLS-1$
        boolean hasPrinc = false;
        String[] urlArray = standardURlString.split(SEMICOLON);
        if (urlArray[urlArray.length - 1].startsWith(principalSuffix)) {
            hasPrinc = true;
        }
        if (hasPrinc) {
            if (hivePrincipal == null) {
                standardURlString = standardURlString.substring(0, standardURlString.lastIndexOf(principalSuffix));
            }
        } else {
            if (hivePrincipal != null) {
                standardURlString = standardURlString.concat(SEMICOLON).concat(principalSuffix).concat(hivePrincipal);
            }
        }

        return standardURlString;
    }

    public static String getURLString(DatabaseConnection conn) {
        // mzhao 2012-06-25 bug TDI-21552 , context url of generic JDBC cannot be replanced correctly, here
        // just return the origin url.
        if (conn.getDatabaseType().equals(EDatabaseTypeName.GENERAL_JDBC.getDisplayName())) {
            return conn.getURL();
        }
        return getURLString(false, conn);
    }

    public static String getURLString(final boolean supportContext, final DatabaseConnection conn) {
        if (conn != null) {
            return getURLString(conn.getDatabaseType(), conn.getDbVersionString(), conn.getServerName(), conn.getUsername(),
                    conn.getRawPassword(), conn.getPort(), conn.getSID(), conn.getFileFieldName(), conn.getDatasourceName(),
                    conn.getDBRootPath(), conn.getAdditionalParams());
        }
        return DatabaseConnConstants.EMPTY;
    }

    public static String[] analyseURL(String currentDbType, String dbVersion, String url) {
        String[] s = { currentDbType, DatabaseConnConstants.EMPTY, DatabaseConnConstants.EMPTY, DatabaseConnConstants.EMPTY,
                DatabaseConnConstants.EMPTY, DatabaseConnConstants.EMPTY };
        EDatabaseConnTemplate template = EDatabaseConnTemplate.indexOfTemplate(currentDbType);
        if (template == null || url == null || url == DatabaseConnConstants.EMPTY) {
            return s;
        }
        EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersion(dbVersion);
        String regex = template.getUrlPattern(version);

        Perl5Compiler compiler = new Perl5Compiler();
        Perl5Matcher matcher = new Perl5Matcher();
        Pattern pattern = null;
        try {
            pattern = compiler.compile(regex);
            if (matcher.contains(url, pattern)) {
                matcher.matches(url, pattern);
                MatchResult matchResult = matcher.getMatch();
                s[0] = currentDbType;
                if (matchResult != null) {
                    for (int i = 1; i < matchResult.groups(); i++) {
                        s[i] = matchResult.group(i);
                    }
                }
            } else {
                // search if another regex corresponding at the string of connection
                String newDbType = searchGoodRegex(currentDbType, dbVersion, url);
                if (!newDbType.equals(currentDbType)) {
                    currentDbType = newDbType;
                    s = analyseURL(currentDbType, dbVersion, url);
                }
            }

        } catch (MalformedPatternException e) {
            e.printStackTrace();
        }
        return s;
    }

    private static String searchGoodRegex(String currentDbType, String dbVersion, String stringConnection) {
        String startStringConnection;
        String startTemplateString;
        EDatabaseVersion4Drivers version = EDatabaseVersion4Drivers.indexOfByVersion(dbVersion);

        // Added by Marvin Wang on May. 6, 2013 for bug TDI-25873.
        if (version != null) {
            for (EDatabaseConnTemplate template : EDatabaseConnTemplate.values()) {
                if (template == EDatabaseConnTemplate.GODBC || template == EDatabaseConnTemplate.MSODBC) {
                    continue;
                }
                String urlTemplate = template.getUrlTemplate(version);
                if (urlTemplate.indexOf("<") != -1) {
                    startTemplateString = urlTemplate.substring(0, urlTemplate.indexOf("<")); //$NON-NLS-1$
                    if (startTemplateString.length() <= stringConnection.length()) {
                        startStringConnection = stringConnection.substring(0, startTemplateString.length());
                        if (stringConnection.contains("(description=(address=(protocol=tcp)")) { //$NON-NLS-1$
                            return EDatabaseConnTemplate.ORACLESN.getDBDisplayName();
                        } else if (!startTemplateString.equals("") && startTemplateString.equals(startStringConnection)) {
                            return template.getDBDisplayName();
                        }
                    }
                }
            }
        }
        return currentDbType;
    }

    // test
    public static void main(String[] args) {
        for (EDatabaseConnTemplate temp : EDatabaseConnTemplate.values()) {
            System.out.println();
            System.out.println(temp.getDBTypeName());
            if (temp == EDatabaseConnTemplate.ACCESS) {
                System.out.println(temp.getUrlTemplate(EDatabaseVersion4Drivers.ACCESS_2003));
                System.out.println(temp.getUrlPattern(EDatabaseVersion4Drivers.ACCESS_2003));
            }
            System.out.println(temp.getUrlTemplate(null));
            System.out.println(temp.getUrlPattern(null));
        }
    }
}
