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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Substitution;
import org.apache.oro.text.regex.Util;
import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.graphics.Point;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.model.context.UpdateContextVariablesHelper;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.repository.ProjectManager;
import org.talend.utils.security.CryptoHelper;

/**
 * cli class global comment. Detailled comment
 */
public final class ParameterValueUtil {

    public static final String EMPTY = ""; //$NON-NLS-1$

    private ParameterValueUtil() {
    }

    @SuppressWarnings("unchecked")
    public static void renameValues(final IElementParameter param, final String oldName, final String newName) {
        if (param == null || oldName == null || newName == null) {
            return;
        }
        boolean flag = true;
        if (param.getFieldType() == EParameterFieldType.MEMO_SQL) {
            flag = false;
        }
        if (param.getValue() instanceof String) { // for TEXT / MEMO etc..
            String value = (String) param.getValue();
            if (value.contains(oldName)) {
                // param.setValue(value.replaceAll(oldName, newName));
                String newValue = renameValues(value, oldName, newName, flag);
                if (!value.equals(newValue)) {
                    param.setValue(newValue);
                }
            }
        } else if (param.getValue() instanceof List) { // for TABLE
            List<Map<String, Object>> tableValues = (List<Map<String, Object>>) param.getValue();
            for (Map<String, Object> line : tableValues) {
                for (String key : line.keySet()) {
                    Object cellValue = line.get(key);
                    if (cellValue instanceof String) { // cell is text so
                        // rename data if
                        // needed
                        String value = (String) cellValue;
                        if (value.contains(oldName)) {
                            // line.put(key, value.replaceAll(oldName,
                            // newName));
                            String newValue = renameValues(value, oldName, newName, flag);
                            if (!value.equals(newValue)) {
                                line.put(key, newValue);
                            }
                        }
                    }
                }
            }
        }
    }

    public static String renameValues(final String value, final String oldName, final String newName, boolean flag) {
        if (value == null || oldName == null || newName == null) {
            return value; // keep original value
        }
        PatternCompiler compiler = new Perl5Compiler();
        Perl5Matcher matcher = new Perl5Matcher();
        matcher.setMultiline(true);
        Perl5Substitution substitution = new Perl5Substitution(newName + "$2", Perl5Substitution.INTERPOLATE_ALL); //$NON-NLS-1$

        Pattern pattern;
        try {
            pattern = compiler.compile(getQuotePattern(oldName));
        } catch (MalformedPatternException e) {
            return value; // keep original value
        }

        if (matcher.contains(value, pattern)) {
            // replace
            String returnValue = EMPTY;
            if (value.contains(TalendQuoteUtils.getQuoteChar()) && !flag) {
                // returnValue = splitQueryData(matcher, pattern, substitution, value, Util.SUBSTITUTE_ALL);
                returnValue = splitQueryData(oldName, newName, value);
            } else {
                returnValue = Util.substitute(matcher, pattern, substitution, value, Util.SUBSTITUTE_ALL);
            }
            return returnValue;

        }
        return value; // keep original value

    }

    // function before TDI-29092 modify,this function seems only rename variables in context,I put this funciton back
    // incase any problem with the new function and we can refer the old one to check the problem.
    public static String splitQueryData(PatternMatcher matcher, Pattern pattern, Substitution sub, String value, int numSubs) {
        String[] split = value.split("\"");
        int i = 0;
        String replace = EMPTY;
        for (String s : split) {
            if (i % 2 == 0) {
                replace = s;
                if (i != 0) {
                    if (matcher.contains(value, pattern)) {
                        replace = split[i].toString();
                        split[i] = Util.substitute(matcher, pattern, sub, replace, numSubs);
                    }
                }
            }
            i++;
        }
        String returnValue = EMPTY;
        for (int t = 1; t < split.length; t++) {
            if (t % 2 == 0) {
                returnValue += split[t];
            } else {
                returnValue += "\"" + split[t] + "\"";
            }
        }
        return returnValue;
    }

    // for bug 12594 split "; for bug 29092(it has JUnits)
    public static String splitQueryData(String oldName, String newName, String value) {
        // example:"drop table "+context.oracle_schema+".\"TDI_26803\""
        // >>>>>>>>_*_(const)__ _____*_(varible)_______ __*_(const)___

        final int length = value.length();
        // quotaStrings which stores the start and end point for all const strings in the value
        LinkedHashMap<Integer, Integer> quotaStrings = new LinkedHashMap<Integer, Integer>();
        List<Point> functionNameAreas = new ArrayList<Point>();

        // get and store all start and end point of const strings
        int start = -1;
        int end = -2;
        char ch;
        for (int i = 0; i < length; i++) {
            ch = value.charAt(i);
            if (ch == '\"') {
                // in case of cases :
                // case 1 : [ "select * from " + context.table + " where value = \"context.table\"" ]
                // case 2 : [ "select * from " + context.table + " where value = \"\\" + context.table +
                // "\\context.table\"" ]
                if (isEscapeSequence(value, i)) {
                    continue;
                }

                // [0 <= start] >> in case the first const String position compute error
                if (0 <= start && end < start) {
                    end = i;
                    quotaStrings.put(start, end);
                } else {
                    start = i;
                }
            }
        }

        {
            // in case the value has not complete quota
            // exapmle > "select a,context.b from " + context.b + "where value = context.b

            // **but** maybe more impossible truth is that
            // they write this(context.b) just want to use it as a varible...
            // so maybe should not set the string behind the quota as a const by default..

            // ---*--- the following code is set the string behind the quota as a const

            // if (0 <= start && end < start) {
            // end = length - 1;
            // quotaStrings.put(start, end);
            // }
        }

        // find the varible string, do replace, then concat them
        StringBuffer strBuffer = new StringBuffer();
        String subString = null;
        int vStart = 0;
        int vEnd = 0;
        int methodMaxIndex = 0;
        int calcMaxIndex = 0;
        start = 0;
        end = 0;

        for (Entry<Integer, Integer> entry : quotaStrings.entrySet()) {
            start = entry.getKey();
            end = entry.getValue() + 1;
            vEnd = start;
            if (vStart == start) {
                // const string follow with const string, maybe won't happen...
                // get the const string
                subString = value.substring(start, end);
                if (start < methodMaxIndex) {
                    subString = subString.replaceAll(oldName, newName);
                }
            } else {
                // get the varible string, do replace, then append it
                subString = value.substring(vStart, vEnd);
                calcMaxIndex = calcMethodArea(subString, value, vStart, functionNameAreas, methodMaxIndex);

                if (methodMaxIndex < calcMaxIndex) {
                    methodMaxIndex = calcMaxIndex;
                }

                String replacedString = doVaribleReplace(oldName, newName, value, functionNameAreas, vStart, vEnd);
                strBuffer.append(replacedString);

                // get the const string
                subString = value.substring(start, end);
                if (start < methodMaxIndex) {
                    subString = subString.replaceAll(oldName, newName);
                }
            }
            // append the const string
            strBuffer.append(subString);
            // update the varible string start point
            vStart = end;
        }

        // in case the last string of the value is a varible string
        // then get it, and do replace, finally append it.
        if (vStart < length) {
            vEnd = length;
            String replacedString = doVaribleReplace(oldName, newName, value, functionNameAreas, vStart, vEnd);
            strBuffer.append(replacedString);
        }

        return strBuffer.toString();
    }

    /**
     * DOC cmeng Comment method "doVaribleReplace".
     * 
     * @param oldName
     * @param newName
     * @param value
     * @param functionNameAreas
     * @param vStart
     * @param vEnd
     */
    private static String doVaribleReplace(String oldName, String newName, String value, List<Point> functionNameAreas,
            int vStart, int vEnd) {

        StringBuffer replacedString = new StringBuffer();
        int replaceableStart = vStart;
        int replaceableEnd = vEnd;
        for (Point functionNameArea : functionNameAreas) {
            if (vEnd <= functionNameArea.x) {
                break;
            }
            if (functionNameArea.y <= vStart) {
                continue;
            }
            if (replaceableStart < functionNameArea.x) {
                replaceableEnd = functionNameArea.x;
                String replaceableString = value.substring(replaceableStart, replaceableEnd);
                replacedString.append(doReplace(oldName, newName, replaceableString));
                replacedString.append(value.substring(functionNameArea.x, functionNameArea.y));
            } else {
                replacedString.append(value.substring(functionNameArea.x, functionNameArea.y));
            }
            replaceableStart = functionNameArea.y;
        }
        if (replaceableStart < vEnd) {
            String replaceableString = value.substring(replaceableStart, vEnd);
            replacedString.append(doReplace(oldName, newName, replaceableString));
        }
        return replacedString.toString();
    }

    private static String doReplace(String oldName, String newName, String value) {

        String vOldName = oldName.replaceAll("\\.", "\\\\."); //$NON-NLS-1$ //$NON-NLS-2$

        // ((\b\w+\s*\.\s*)+schema(\s*\.\s*\w+)*)|((\b\w+\s*\.\s*)*schema(\s*\.\s*\w+)+)
        String regex = "((\\b\\w+\\s*\\.\\s*)+" + vOldName + "(\\s*\\.\\s*\\w+)*)|((\\b\\w+\\s*\\.\\s*)*" + vOldName + "(\\s*\\.\\s*\\w+)+)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // obtain all varibles
        String[] split = value.split(regex);
        Map<String, String> replacedStrings = new HashMap<String, String>();
        StringBuffer returnValue = new StringBuffer();
        // replace the variables & store both value of old and new
        for (String s : split) {
            if (s.contains(oldName)) {
                replacedStrings.put(s, s.replaceAll("\\b" + oldName + "\\b", newName)); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                replacedStrings.put(s, s);
            }
        }
        if (split.length == 1) {
            returnValue.append(replacedStrings.get(split[0]));
        }
        // obtain consts & concat the consts with the variables
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            int x = matcher.start();
            int y = matcher.end();
            int curPos = 0;
            int valueLength = value.length();
            String oldFill = null;
            String newFill = null;
            while (true) {
                if (curPos == valueLength) {
                    break;
                }
                if (curPos < x) {
                    oldFill = value.substring(curPos, x);
                    if ((newFill = replacedStrings.get(oldFill)) != null) {
                        returnValue.append(newFill);
                    } else {
                        returnValue.append(oldFill);
                    }
                    curPos = x;
                    continue;
                }
                returnValue.append(matcher.group());
                curPos = y;
                if (!matcher.find()) {
                    x = valueLength;
                } else {
                    x = matcher.start();
                    y = matcher.end();
                }
            }
        }
        return returnValue.toString();
    }

    /**
     * DOC cmeng Comment method "isEscapeSequence".
     * 
     * @param value
     * @param i
     * @return
     */
    private static boolean isEscapeSequence(String value, int i) {
        boolean isEscapeSequence = false;
        for (int index = i; 0 < index; index--) {
            if (value.charAt(index - 1) == '\\') {
                isEscapeSequence = !isEscapeSequence;
            } else {
                break;
            }
        }
        return isEscapeSequence;
    }

    private static int calcMethodArea(String varibleString, String wholeString, int beginIndex, List<Point> functionNameAreas,
            int lastIndex) {
        // globalMap.get(...)
        //        String regex = "\\b\\S*\\s*\\.\\s*\\S*\\s*\\(\\z"; //$NON-NLS-1$
        // maybe get(...) also is target
        String regex = "\\b[\\S\\.]*?\\s*\\("; //$NON-NLS-1$

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(varibleString);
        int i = 0;
        int varibleStringMaxIndex = beginIndex + varibleString.length() - 1;
        while (matcher.find()) {
            boolean isInQuota = false;
            int parenthesisNum = 0;
            Point functionNameArea = new Point(beginIndex + matcher.start(), beginIndex + matcher.end());
            functionNameAreas.add(functionNameArea);

            if (varibleStringMaxIndex < i || varibleStringMaxIndex < lastIndex) {
                continue;
            }

            for (i = matcher.end(); i < wholeString.length(); i++) {
                char ch = wholeString.charAt(i);
                if (ch == '\"' && !isEscapeSequence(wholeString, i)) {
                    isInQuota = !isInQuota;
                }
                if (isInQuota) {
                    continue;
                }
                if (ch == '(') {
                    parenthesisNum++;
                } else if (ch == ')') {
                    parenthesisNum--;
                }
                if (parenthesisNum < 0) {
                    break;
                }
            }
        }
        return i;
    }

    public static boolean isUseData(final IElementParameter param, final String name) {
        if (param == null || name == null) {
            return false;
        }
        if (param.getValue() instanceof String) { // for TEXT / MEMO etc..
            String value = (String) param.getValue();
            if (ParameterValueUtil.valueContains(value, name)) {
                return true;
            }
        } else if (param.getValue() instanceof List) { // for TABLE
            List<Map<String, Object>> tableValues = (List<Map<String, Object>>) param.getValue();
            for (Map<String, Object> line : tableValues) {
                for (String key : line.keySet()) {
                    Object cellValue = line.get(key);
                    if (cellValue instanceof String) { // cell is text so
                        // test data
                        if (ParameterValueUtil.valueContains((String) cellValue, name)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static boolean isUseData(final ElementParameterType param, final String name) {
        if (param == null || name == null) {
            return false;
        }
        if (param.getField().equals(EParameterFieldType.TABLE.getName())) { // for TABLE
            EList elementValue = param.getElementValue();
            if (elementValue != null) {
                for (ElementValueType valueType : (List<ElementValueType>) elementValue) {
                    if (valueType.getValue() != null) { // cell is text so
                        // test data
                        if (ParameterValueUtil.valueContains(valueType.getValue(), name)) {
                            return true;
                        }
                    }
                }
            }
        } else {
            String value = param.getValue();
            if (value != null && ParameterValueUtil.valueContains(value, name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean valueContains(String value, String toTest) {
        if (value.contains(toTest)) {
            Perl5Matcher matcher = new Perl5Matcher();
            Perl5Compiler compiler = new Perl5Compiler();
            Pattern pattern;

            try {
                pattern = compiler.compile(getQuotePattern(toTest));
                if (matcher.contains(value, pattern)) {
                    return true;
                }
            } catch (MalformedPatternException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private static String getQuotePattern(String toTest) {
        final String prefix = "\\b("; //$NON-NLS-1$
        String suffix = ")\\b"; //$NON-NLS-1$
        if (!ContextParameterUtils.isContainContextParam(toTest)) { // context parameter renamed
            suffix = ")(\\b|\\_)"; //$NON-NLS-1$
        }

        return prefix + UpdateContextVariablesHelper.replaceSpecialChar(toTest) + suffix;

    }

    public static String getValue4Doc(ContextParameterType contextParam) {
        if (contextParam != null) {
            String docValue = contextParam.getValue();
            if (docValue != null) {
                if (isHidePassword() && PasswordEncryptUtil.isPasswordType(contextParam.getType())) {
                    // use the raw value to display.
                    docValue = PasswordEncryptUtil.getPasswordDisplay(contextParam.getRawValue());
                }
                return docValue;
            }
        }
        return EMPTY;
    }

    public static String getValue4Doc(IContextParameter contextParam) {
        if (contextParam != null) {
            String docValue = contextParam.getValue();
            if (docValue != null) {
                if (isHidePassword() && PasswordEncryptUtil.isPasswordType(contextParam.getType())) {
                    if (isHidePassword()) { // if hide will display the *
                        docValue = PasswordEncryptUtil.getPasswordDisplay(docValue.toString());
                    } else { // the value has been raw, so need encrypt it like the ContextParameterType.
                        String encryptValue = getEncryptValue(contextParam);
                        if (encryptValue != null) {
                            docValue = encryptValue;
                        }
                    }
                }
                return docValue;
            }
        }
        return EMPTY;
    }

    public static String getEncryptValue(IContextParameter contextParam) {
        if (contextParam != null) {
            String docValue = contextParam.getValue();
            if (docValue != null) {
                String encryptValue = CryptoHelper.DEFAULT.encrypt(docValue);
                if (encryptValue != null) {
                    return encryptValue;
                }
            }
        }
        return null;
    }

    public static String getEncryptValue(IElementParameter param) {
        if (param != null) {
            Object docValue = param.getValue();
            if (docValue != null && docValue instanceof String) {
                String encryptValue = CryptoHelper.DEFAULT.encrypt(docValue.toString());
                if (encryptValue != null) {
                    return encryptValue;
                }
            }
        }
        return null;
    }

    public static Object getValue4Doc(IElementParameter param) {
        if (param != null) {
            Object docValue = param.getValue();
            if (docValue != null) {
                if ((param.getRepositoryValue() != null && param.getRepositoryValue().toUpperCase().contains("PASSWORD") //$NON-NLS-1$
                || EParameterFieldType.PASSWORD.equals(param.getFieldType()))//
                        && !ContextParameterUtils.containContextVariables((String) docValue)) {

                    if (isHidePassword()) { // if hide will display the *
                        docValue = PasswordEncryptUtil.getPasswordDisplay(docValue.toString());
                    } else { // the value has been raw, so need encrypt it like the ElementParameterType.
                        String encryptValue = getEncryptValue(param);
                        if (encryptValue != null) {
                            docValue = encryptValue;
                        }
                    }
                }
                return docValue;
            }
        }
        return EMPTY;
    }

    public static String getValue4Doc(ElementParameterType param) {
        if (param != null) {
            String docValue = param.getValue();
            if (docValue != null && param.getField() == EParameterFieldType.PASSWORD.getName() && isHidePassword()
                    && !ContextParameterUtils.containContextVariables(docValue)) {
                // the value has been raw, so just get dispaly value.
                docValue = PasswordEncryptUtil.getPasswordDisplay(docValue);
            }
            return docValue;
        }
        return EMPTY;
    }

    public static boolean isHidePassword() {
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        if (currentProject != null) {
            return currentProject.getEmfProject().isHidePassword();
        }
        return false;
    }
}
