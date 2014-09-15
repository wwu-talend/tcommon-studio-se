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
package org.talend.core.model.utils;

import junit.framework.Assert;

import org.junit.Test;

/**
 * DOC cmeng class global comment. Detailled comment
 */
@SuppressWarnings("nls")
public class ParameterValueUtilTest {

    @Test
    public void testSplitQueryData4SQL() {
        String testString = null;
        String expectRetValue = null;
        String retValue = null;
        int i = 0;

        // test case 0
        // testString : context.operation+" "+context.schema+"."+context.table+";"
        testString = "context.operation+\" \"+context.schema+\".\"+context.table+\";\"";
        expectRetValue = "context.oper+\" \"+context.schema+\".\"+context.table+\";\"";
        retValue = ParameterValueUtil.splitQueryData("context.operation", "context.oper", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));
        // schema
        expectRetValue = "context.operation+\" \"+context.db+\".\"+context.table+\";\"";
        retValue = ParameterValueUtil.splitQueryData("context.schema", "context.db", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));
        // table
        expectRetValue = "context.operation+\" \"+context.schema+\".\"+context.table1+\";\"";
        retValue = ParameterValueUtil.splitQueryData("context.table", "context.table1", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // part of replacing
        retValue = ParameterValueUtil.splitQueryData("text.schema", "text.schemaABC", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, testString.equals(retValue));

        // only normal sting
        testString = "context.operation+\" \"+context_schema+schema+\".\"+context.table+\";\"";
        expectRetValue = "context.operation+\" \"+context_schema+schema123+\".\"+context.table+\";\"";
        retValue = ParameterValueUtil.splitQueryData("schema", "schema123", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));
        // only normal sting for context
        testString = "context.operation+\" \"+context.schema+schema+\".\"+context.table+\";\"";
        expectRetValue = "context.operation+\" \"+context.schema+schema123+\".\"+context.table+\";\"";
        retValue = ParameterValueUtil.splitQueryData("schema", "schema123", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // same prefix
        testString = "context.operation+\" \"+context.test1+\".\"+context.test11+\";\"";
        expectRetValue = "context.operation+\" \"+context.test2+\".\"+context.test11+\";\"";
        retValue = ParameterValueUtil.splitQueryData("context.test1", "context.test2", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 1
        // testString (For bug:TDI-29092) : "drop table "+context.oracle_schema+".\"TDI_26803\""
        testString = "\"drop table \"+context.oracle_schema+\".\\\"TDI_26803\\\"\"";
        expectRetValue = "\"drop table \"+context.oracl_schema+\".\\\"TDI_26803\\\"\"";
        retValue = ParameterValueUtil.splitQueryData("context.oracle_schema", "context.oracl_schema", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));
        // column, don't replace the file for SQL
        expectRetValue = "\"drop table \"+context.oracl_schema+\".\\\"TDI_12345\\\"\"";
        retValue = ParameterValueUtil.splitQueryData("TDI_26803", "TDI_12345", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, testString.equals(retValue)); // not changed

        // test case 7
        // all are empty
        // testString :
        // ""
        testString = "";
        expectRetValue = "";
        retValue = ParameterValueUtil.splitQueryData("", "", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 8
        // many same varibles
        // testString :
        // "contextA"+context+"contextB"+context+"contextC" + context+" "
        testString = "\"contextA\"+context+\"contextB\"+context+\"contextC\" + context+\" \"";
        expectRetValue = "\"contextA\"+context.db+\"contextB\"+context.db+\"contextC\" + context.db+\" \"";
        retValue = ParameterValueUtil.splitQueryData("context", "context.db", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        expectRetValue = "\"contextA\"+context.db+\"contextB\"+context.db+\"contextCC\" + context.db+\" \"";
        retValue = ParameterValueUtil.splitQueryData("contextC", "contextCC", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, testString.equals(retValue)); // not changed

        // test case 9
        // testString :
        // "contextA"+contextA+"contextB"+context+"contextC" + context+" "
        testString = "\"contextA\"+contextA+\"contextB\"+context+\"contextC\" + context+\" \"";
        expectRetValue = "\"contextA\"+contextA+\"contextB\"+context.db+\"contextC\" + context.db+\" \"";
        retValue = ParameterValueUtil.splitQueryData("context", "context.db", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        expectRetValue = "\"contextA\"+contextAA+\"contextB\"+context+\"contextC\" + context+\" \"";
        retValue = ParameterValueUtil.splitQueryData("contextA", "contextAA", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 10
        // "SELECT
        // "+context.ORA_VIRTULIA_Schema+".PER_ETATCIVIL.IDE_DOSSIER,
        // "+context.ORA_VIRTULIA_Schema+".PER_ETATCIVIL.QUALITE,
        // "+context.ORA_VIRTULIA_Schema+".PER_ETATCIVIL.NOM
        // FROM "+context.ORA_VIRTULIA_Schema+".PER_ETATCIVIL"
        // this function should not replace constant
        testString = "\"SELECT \r\n" + "\"+context.ORA_VIRTULIA_Schema+\".PER_ETATCIVIL.IDE_DOSSIER,\r\n"
                + "\"+context.ORA_VIRTULIA_Schema+\".PER_ETATCIVIL.QUALITE,\r\n"
                + "\"+context.ORA_VIRTULIA_Schema+\".PER_ETATCIVIL.NOM\r\n"
                + "FROM \"+context.ORA_VIRTULIA_Schema+\".PER_ETATCIVIL\"";
        expectRetValue = "\"SELECT \r\n" + "\"+context.ORA_VIRTULIA_Schema+\".PER_ETATCIVIL.IDE_DOSSIER,\r\n"
                + "\"+context.ORA_VIRTULIA_Schema+\".PER_ETATCIVIL.QUALITE,\r\n"
                + "\"+context.ORA_VIRTULIA_Schema+\".PER_ETATCIVIL.NOM\r\n"
                + "FROM \"+context.ORA_VIRTULIA_Schema+\".PER_ETATCIVIL\"";
        retValue = ParameterValueUtil.splitQueryData("PER_ETATCIVIL.IDE_DOSSIER", "simplejoblet_1_PER_ETATCIVIL.IDE_DOSSIER",
                testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        testString = "\"SELECT \r\n" + "\"+context.ORA_VIRTULIA_Schema+\".PER_ETATCIVIL.IDE_DOSSIER,\r\n"
                + "\"+context.ORA_VIRTULIA_Schema+\".PER_ETATCIVIL.QUALITE,\r\n"
                + "\"+context.ORA_VIRTULIA_Schema+\".PER_ETATCIVIL.NOM\r\n"
                + "FROM \"+context.ORA_VIRTULIA_Schema+\".PER_ETATCIVIL\"";
        expectRetValue = "\"SELECT \r\n" + "\"+context.ORA_CHANGE_Schema+\".PER_ETATCIVIL.IDE_DOSSIER,\r\n"
                + "\"+context.ORA_CHANGE_Schema+\".PER_ETATCIVIL.QUALITE,\r\n"
                + "\"+context.ORA_CHANGE_Schema+\".PER_ETATCIVIL.NOM\r\n"
                + "FROM \"+context.ORA_CHANGE_Schema+\".PER_ETATCIVIL\"";
        retValue = ParameterValueUtil.splitQueryData("context.ORA_VIRTULIA_Schema", "context.ORA_CHANGE_Schema", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        testString = "no match";
        expectRetValue = "no match";
        retValue = ParameterValueUtil.splitQueryData("context.schema", "context.db", testString);
        Assert.assertTrue(retValue != null && !"".equals(retValue));
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 11
        // testString : "select * from " + context.table + " where value = \"value from context.table\""
        // expectString : "select * from " + context.table1 + " where value = \"value from context.table\""
        testString = "\"select * from \" + context.table + \" where value = \\\"value from context.table\\\"\"";
        expectRetValue = "\"select * from \" + context.table1 + \" where value = \\\"value from context.table\\\"\"";
        retValue = ParameterValueUtil.splitQueryData("context.table", "context.table1", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 12
        // testString : "select * from " + context.table + " where value = \"context.table\""
        // expectString : "select * from " + context.table1 + " where value = \"context.table\""
        testString = "\"select * from \" + context.table + \" where value = \\\"context.table\\\"\"";
        expectRetValue = "\"select * from \" + context.table1 + \" where value = \\\"context.table\\\"\"";
        retValue = ParameterValueUtil.splitQueryData("context.table", "context.table1", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 13
        // testString : "select * from " + context.table + " where value = \"context.table\"" + context.table
        // expectString : "select * from " + context.table1 + " where value = \"context.table\"" + context.table
        testString = "\"select * from \" + context.table + \" where value = \\\"context.table\\\"\" + context.table";
        expectRetValue = "\"select * from \" + context.table1 + \" where value = \\\"context.table\\\"\" + context.table1";
        retValue = ParameterValueUtil.splitQueryData("context.table", "context.table1", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 14 : incomplete double quota
        // testString : "select a,context.b from " + context.b + "where value = context.b
        // expectString : "select a,context.b from " + context.b1 + "where value = context.b1
        testString = "\"select a,context.b from \" + context.b + \"where value = context.b";
        expectRetValue = "\"select a,context.b from \" + context.b1 + \"where value = context.b1";
        retValue = ParameterValueUtil.splitQueryData("context.b", "context.b1", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 15 : incomplete double quota
        // testString : "select a,context.b from " + context.b + "where value = \"context.b
        // expectString : "select a,context.b from " + context.b1 + "where value = \"context.b1
        testString = "\"select a,context.b from \" + context.b + \"where value = \\\"context.b";
        expectRetValue = "\"select a,context.b from \" + context.b1 + \"where value = \\\"context.b1";
        retValue = ParameterValueUtil.splitQueryData("context.b", "context.b1", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 16
        // testString : "select * from " + context.table + " where value = \"\\" + context.table + "\\context.table\""
        // expectString : "select * from " + context.table1 + " where value = \"\\" + context.table1 +
        // "\\context.table\""
        testString = "\"select * from \" + context.table + \" where value = \\\"\\\\\" + context.table + \"\\\\context.table\\\"\"";
        expectRetValue = "\"select * from \" + context.table1 + \" where value = \\\"\\\\\" + context.table1 + \"\\\\context.table\\\"\"";
        retValue = ParameterValueUtil.splitQueryData("context.table", "context.table1", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 17
        // testString : "select * from ""context.table where value = \"\\" + context.table + "\\context.table\""
        // expectString : "select * from ""context.table where value = \"\\" + context.table1 + "\\context.table\""
        testString = "\"select * from \"\"context.table where value = \\\"\\\\\" + context.table + \"\\\\context.table\\\"\"";
        expectRetValue = "\"select * from \"\"context.table where value = \\\"\\\\\" + context.table1 + \"\\\\context.table\\\"\"";
        retValue = ParameterValueUtil.splitQueryData("context.table", "context.table1", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 18
        // testString : "select * from " + context.table + "where id = " + getId(getHeader(context.header, "CONTEXT_ID")
        // + "CONTEXT_ID")
        // expectString : "select * from " + context.table + "where id = " + getId(getHeader(context.header,
        // "CONTEXT_ID") + "CONTEXT_ID")
        testString = "\"select * from \" + context.table + \"where id = \" + getId(getHeader(context.header, \"CONTEXT_ID\") + \"CONTEXT_ID\")";
        expectRetValue = "\"select * from \" + context.table + \"where id = \" + getId(getHeader(context.header, \"CONTEXT_IDS\") + \"CONTEXT_IDS\")";
        retValue = ParameterValueUtil.splitQueryData("CONTEXT_ID", "CONTEXT_IDS", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 19
        // testString : "select * from " + context.table + "where id = " + getId(getHeader(context.header, "CONTEXT_ID")
        // + "CONTEXT_ID", context.p2, "CONTEXT_ID")
        // expectString : "select * from " + context.table + "where id = " + getId(getHeader(context.header,
        // "CONTEXT_IDS") + "CONTEXT_IDS", context.p2, "CONTEXT_IDS")
        testString = "\"select * from \" + context.table + \"where id = \" + getId(getHeader(context.header, \"CONTEXT_ID\") + \"CONTEXT_ID\", context.p2, \"CONTEXT_ID\")";
        expectRetValue = "\"select * from \" + context.table + \"where id = \" + getId(getHeader(context.header, \"CONTEXT_IDS\") + \"CONTEXT_IDS\", context.p2, \"CONTEXT_IDS\")";
        retValue = ParameterValueUtil.splitQueryData("CONTEXT_ID", "CONTEXT_IDS", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 20
        // testString : "select * from " + context.table + "where id = " + getId(global.getHeader(context.header,
        // "CONTEXT_ID")
        // + "CONTEXT_ID", context.p2, "CONTEXT_ID")
        // expectString : "select * from " + context.table + "where id = " + getId(global.getHeader(context.header,
        // "CONTEXT_IDS") + "CONTEXT_IDS", context.p2, "CONTEXT_IDS")
        testString = "\"select * from \" + context.table + \"where id = \" + getId(global.getHeader(context.header, \"CONTEXT_ID\") + \"CONTEXT_ID\", context.p2, \"CONTEXT_ID\")";
        expectRetValue = "\"select * from \" + context.table + \"where id = \" + getId(global.getHeader(context.header, \"CONTEXT_IDS\") + \"CONTEXT_IDS\", context.p2, \"CONTEXT_IDS\")";
        retValue = ParameterValueUtil.splitQueryData("CONTEXT_ID", "CONTEXT_IDS", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 21
        // testString : "select * from " + context.table + "where id = " + getId(global.getHeader(context.header,
        // "\"CONTEXT_ID\\\"")
        // + "\"CONTEXT_ID", context.p2, "CONTEXT_ID")
        // expectString : "select * from " + context.table + "where id = " + getId(global.getHeader(context.header,
        // "CONTEXT_IDS") + "CONTEXT_IDS", context.p2, "CONTEXT_IDS")
        testString = "\"select * from \" + context.table + \"where id = \" + getId(global.getHeader(context.header, \"\\\"CONTEXT_ID\\\\\\\"\") + \"\\\"CONTEXT_ID\", context.p2, \"CONTEXT_ID\")";
        expectRetValue = "\"select * from \" + context.table + \"where id = \" + getId(global.getHeader(context.header, \"\\\"CONTEXT_IDS\\\\\\\"\") + \"\\\"CONTEXT_IDS\", context.p2, \"CONTEXT_IDS\")";
        retValue = ParameterValueUtil.splitQueryData("CONTEXT_ID", "CONTEXT_IDS", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 22
        // testString : "select * from " + context.table + "where id = " + getId(getHeader(context.header, "CONTEXT_ID")
        // + "CONTEXT_ID", context.p2, "CONTEXT_ID"
        // expectString : "select * from " + context.table + "where id = " + getId(getHeader(context.header,
        // "CONTEXT_IDS") + "CONTEXT_IDS", context.p2, "CONTEXT_IDS"
        testString = "\"select * from \" + context.table + \"where id = \" + getId(getHeader(context.header, \"CONTEXT_ID\") + \"CONTEXT_ID\", context.p2, \"CONTEXT_ID\"";
        expectRetValue = "\"select * from \" + context.table + \"where id = \" + getId(getHeader(context.header, \"CONTEXT_IDS\") + \"CONTEXT_IDS\", context.p2, \"CONTEXT_IDS\"";
        retValue = ParameterValueUtil.splitQueryData("CONTEXT_ID", "CONTEXT_IDS", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 23
        // testString : "select * from " + context.table + "where id = " + getId(getHeader(context.header, "CONTEXT_ID")
        // + "CONTEXT_ID", context.p2, "CONTEXT_ID
        // expectString : "select * from " + context.table + "where id = " + getId(getHeader(context.header,
        // "CONTEXT_IDS") + "CONTEXT_IDS", context.p2, "CONTEXT_IDS
        testString = "\"select * from \" + context.table + \"where id = \" + getId(getHeader(context.header, \"CONTEXT_ID\") + \"CONTEXT_ID\", context.p2, \"CONTEXT_ID";
        expectRetValue = "\"select * from \" + context.table + \"where id = \" + getId(getHeader(context.header, \"CONTEXT_IDS\") + \"CONTEXT_IDS\", context.p2, \"CONTEXT_IDS";
        retValue = ParameterValueUtil.splitQueryData("CONTEXT_ID", "CONTEXT_IDS", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 24
        // testString : "select * from " + context.table + "where id = " + getId(context.id) + globalMap.get("CONST")
        // expectString : "select * from " + context.table + "where id = " + getId(context.id) + globalMap.get("CONST1")
        testString = "\"select * from \" + context.table + \"where id = \" + getId(context.id) + globalMap.get(\"CONST\")";
        expectRetValue = "\"select * from \" + context.table + \"where id = \" + getId(context.id) + globalMap.get(\"CONST1\")";
        retValue = ParameterValueUtil.splitQueryData("CONST", "CONST1", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 25 : should not replace method name
        // testString : "select * from " + context.table + "where id = " + getId(context.id) +
        // globalMap.get("globalMap")
        // expectString : "select * from " + context.table + "where id = " + getId(context.id) +
        // globalMap.get("globalMap1")
        testString = "\"select * from \" + context.table + \"where id = \" + getId(context.id) + globalMap.get(\"globalMap\")";
        expectRetValue = "\"select * from \" + context.table + \"where id = \" + getId(context.id) + globalMap.get(\"globalMap1\")";
        retValue = ParameterValueUtil.splitQueryData("globalMap", "globalMap1", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 26
        // testString : "select * from " + context.table.a.b + contextA.table.a + table.a.b + table.a + "where id = " +
        // getId(table.a) + table.a.get("table.a")
        //
        // expectString : "select * from " + context.table.a.b + contextA.table.a + table.a.b + table.a1 + "where id = "
        // + getId(table.a1) + table.a.get("table.a1")
        testString = "\"select * from \" + context.table.a.b + contextA.table.a + table.a.b + table.a + \"where id = \" + getId(table.a) + table.a.get(\"table.a\")";
        expectRetValue = "\"select * from \" + context.table.a.b + contextA.table.a + table.a.b + table.a1 + \"where id = \" + getId(table.a1) + table.a.get(\"table.a1\")";
        retValue = ParameterValueUtil.splitQueryData("table.a", "table.a1", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

        // test case 2
        // testString : "select * from " + a.CONTEXT_ID + CONTEXT_ID.b + CONTEXT_ID + "where id = " +
        // CONTEXT_ID(CONTEXT_ID(CONTEXT_ID, "\"CONTEXT_ID\"\\" + CONTEXT_ID, CONTEXT_ID, "CONTEXT_ID") + "CONTEXT_ID",
        // CONTEXT_ID(ID, "CONTEXT_ID"), "CONTEXT_ID")
        // expectString : "select * from " + a.CONTEXT_ID + CONTEXT_ID.b + CONTEXT_ID1 + "where id = " +
        // CONTEXT_ID(CONTEXT_ID(CONTEXT_ID1, "\"CONTEXT_ID1\"\\" + CONTEXT_ID1, CONTEXT_ID1, "CONTEXT_ID1") +
        // "CONTEXT_ID1", CONTEXT_ID(ID, "CONTEXT_ID1"), "CONTEXT_ID1")
        testString = "\"select * from \" + a.CONTEXT_ID + CONTEXT_ID.b + CONTEXT_ID + \"where id = \" + CONTEXT_ID(CONTEXT_ID(CONTEXT_ID, \"\\\"CONTEXT_ID\\\"\\\\\" + CONTEXT_ID, CONTEXT_ID, \"CONTEXT_ID\") + \"CONTEXT_ID\", CONTEXT_ID(ID, \"CONTEXT_ID\"), \"CONTEXT_ID\")";
        expectRetValue = "\"select * from \" + a.CONTEXT_ID + CONTEXT_ID.b + CONTEXT_ID1 + \"where id = \" + CONTEXT_ID(CONTEXT_ID(CONTEXT_ID1, \"\\\"CONTEXT_ID1\\\"\\\\\" + CONTEXT_ID1, CONTEXT_ID1, \"CONTEXT_ID1\") + \"CONTEXT_ID1\", CONTEXT_ID(ID, \"CONTEXT_ID1\"), \"CONTEXT_ID1\")";
        retValue = ParameterValueUtil.splitQueryData("CONTEXT_ID", "CONTEXT_ID1", testString);
        Assert.assertTrue("testSplitQueryDataCase_" + i++, expectRetValue.equals(retValue));

    }

    @Test
    public void testSplitQueryData4SQL_Case2() {
        // case 2:
        // "insert into "+context.schema+"."+context.table+"(schema, table) values(\"context.schema\", \"context.table\")"
        testSplitQueryData4SQL_Case2_5("testSplitQueryData4Case2", null, null, null);
    }

    @Test
    public void testSplitQueryData4SQL_Case3() {
        // case 3:
        // ""+"insert into "+context.schema+"."+context.table+"(schema, table) values(\"context.schema\", \"context.table\")"
        testSplitQueryData4SQL_Case2_5("testSplitQueryData4Case3", "\"\"", null, null);
    }

    @Test
    public void testSplitQueryData4SQL_Case4() {
        // case 4:
        // "insert into "+context.schema+"."+context.table+"(schema, table) values(\"context.schema\", \"context.table\")"+""
        testSplitQueryData4SQL_Case2_5("testSplitQueryData4Case4", null, null, "\"\"");
    }

    @Test
    public void testSplitQueryData4SQL_Case5() {
        // case 5:
        // "insert into "+context.schema+"."+context.table+""+
        // "(schema, table) values(\"context.schema\", \"context.table\")"
        testSplitQueryData4SQL_Case2_5("testSplitQueryData4Case5", null, "\"\"", null);
    }

    private void testSplitQueryData4SQL_Case2_5(String message, String prefix, String mid, String suffix) {
        if (prefix == null) {
            prefix = "";
        }
        if (mid == null) {
            mid = "";
        }
        if (suffix == null) {
            suffix = "";
        }

        String testString = null;
        String expectRetValue = null;
        int i = 0;
        // test case 2-5
        // String which is same to the String to be replaced was contained in the testString
        // testString :
        /*
         * case 2:
         * "insert into "+context.schema+"."+context.table+"(schema, table) values(\"context.schema\", \"context.table\")"
         * 
         * case 3: ""+"insert into "+context.schema+"."+context.table+
         * "(schema, table) values(\"context.schema\", \"context.table\")"
         * 
         * 
         * case 4:
         * "insert into "+context.schema+"."+context.table+"(schema, table) values(\"context.schema\", \"context.table\")"
         * +""
         * 
         * case 5: "insert into "+context.schema+"."+context.table+""+
         * "(schema, table) values(\"context.schema\", \"context.table\")"
         */

        testString = "\"insert into \"+context.schema+\".\"+context.table+" + mid
                + "\"(schema, table) values(\\\"context.schema\\\", \\\"context.table\\\")\"";
        expectRetValue = "\"insert into \"+context.db+\".\"+context.table+" + mid
                + "\"(schema, table) values(\\\"context.schema\\\", \\\"context.table\\\")\"";
        assertTest(message, i++, prefix + testString + suffix, prefix + expectRetValue + suffix, "context.schema", "context.db");
        // table
        expectRetValue = "\"insert into \"+context.schema+\".\"+context.table111+" + mid
                + "\"(schema, table) values(\\\"context.schema\\\", \\\"context.table\\\")\"";
        assertTest(message, i++, prefix + testString + suffix, prefix + expectRetValue + suffix, "context.table",
                "context.table111");
        // prefix name 1
        testString = "\"insert into \"+context.schema+\".\"+context.schematable+" + mid
                + "\"(schema, table) values(\\\"context.schema\\\", \\\"context.table\\\")\"";
        expectRetValue = "\"insert into \"+context.db+\".\"+context.schematable+" + mid
                + "\"(schema, table) values(\\\"context.schema\\\", \\\"context.table\\\")\"";
        assertTest(message, i++, prefix + testString + suffix, prefix + expectRetValue + suffix, "context.schema", "context.db");
        // prefix name 2
        testString = "\"insert into \"+context.schema+\".\"+context.schema_table+" + mid
                + "\"(schema, table) values(\\\"context.schema\\\", \\\"context.table\\\")\"";
        expectRetValue = "\"insert into \"+context.db+\".\"+context.schema_table+" + mid
                + "\"(schema, table) values(\\\"context.schema\\\", \\\"context.table\\\")\"";
        assertTest(message, i++, prefix + testString + suffix, prefix + expectRetValue + suffix, "context.schema", "context.db");
    }

    private void assertTest(String message, int index, String testString, String expectRetValue, String oldOne, String newOne) {
        String resultValue = ParameterValueUtil.splitQueryData(oldOne, newOne, testString);
        Assert.assertTrue(message + index, expectRetValue.equals(resultValue));
    }

    @Test
    public void testRenameValues4GlobleMap() {
        String testString = "((String)globalMap.get(\"tFileList_1_CURRENT_FILE\"))";
        String expectedValue = "((String)globalMap.get(\"tFileList_2_CURRENT_FILE\"))";
        String resultValue = ParameterValueUtil.renameValues(testString, "tFileList_1", "tFileList_2", true);
        Assert.assertTrue(expectedValue.equals(resultValue));

        //
        testString = "((String)globalMap.get(\"tFileList_1_CURRENT_FILEDIRECTORY\"))+((String)globalMap.get(\"tFileList_1_CURRENT_FILE\"))";
        expectedValue = "((String)globalMap.get(\"tFileList_2_CURRENT_FILEDIRECTORY\"))+((String)globalMap.get(\"tFileList_2_CURRENT_FILE\"))";
        resultValue = ParameterValueUtil.renameValues(testString, "tFileList_1", "tFileList_2", true);
        Assert.assertTrue(expectedValue.equals(resultValue));

        //
        testString = "((String)globalMap.get(\"tFileList_1_CURRENT_FILEDIRECTORY\"))+((String)globalMap.get(\"tFileList_11_CURRENT_FILE\"))";
        expectedValue = "((String)globalMap.get(\"tFileList_2_CURRENT_FILEDIRECTORY\"))+((String)globalMap.get(\"tFileList_11_CURRENT_FILE\"))";
        resultValue = ParameterValueUtil.renameValues(testString, "tFileList_1", "tFileList_2", true);
        Assert.assertTrue(expectedValue.equals(resultValue));

    }

    @Test
    public void testRenameValues4SQLAndGlobleMap() {
        // case
        // "select A.id, A.name form "+context.table+" A where A.name= "+((String)globalMap.get("tFileList_1_CURRENT_FILE"))
        String testString = "\"select A.id, A.name form \"+context.table+\" A where A.name= \"+((String)globalMap.get(\"tFileList_1_CURRENT_FILE\"))";
        String expectRetValue = "\"select A.id, A.name form \"+context.table123+\" A where A.name= \"+((String)globalMap.get(\"tFileList_1_CURRENT_FILE\"))";
        // if flag is false, means is from SQL. but when replace the globlemap, will be problem.
        String retValue = ParameterValueUtil.renameValues(testString, "context.table", "context.table123", false);
        Assert.assertTrue("testRenameValues4SQLAndGlobleMap", expectRetValue.equals(retValue));

        expectRetValue = "\"select A.id, A.name form \"+context.table+\" A where A.name= \"+((String)globalMap.get(\"tFileList_2_CURRENT_FILE\"))";
        // if flag is false, means is from SQL. but when replace the globlemap, will be problem.
        retValue = ParameterValueUtil.renameValues(testString, "tFileList_1", "tFileList_2", false);
        Assert.assertTrue("testRenameValues4SQLAndGlobleMap", expectRetValue.equals(retValue));
    }

    @Test
    public void testRenameJavaCode() {
        String testString = "\tSystem.out.println(\"=====\");\n\tout.a = b;\n\tout = obj1;";
        String expectRetValue = "\tSystem.out.println(\"=====\");\n\tout.a = b;\n\trow1 = obj1;";

        String retValue = ParameterValueUtil.splitQueryData("out", "row1", testString);
        Assert.assertTrue(expectRetValue.equals(retValue));
    }
}
