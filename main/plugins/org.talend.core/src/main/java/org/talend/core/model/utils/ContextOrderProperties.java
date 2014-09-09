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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.process.IContextParameter;

/**
 * created by ldong on Aug 7, 2014 Detailled comment
 * 
 */
public class ContextOrderProperties extends Properties {

    private LinkedHashMap<String, String> commentMap = new LinkedHashMap<String, String>();

    List<IContextParameter> parameterList = new ArrayList<IContextParameter>();

    private final static String BUILD_IN_COMMENT = "Build-in context variables";

    private final static String REPOSITORY_COMMENT = "Context variables from repository context ";

    private Map groupMap = new HashMap();

    /**
     * Version ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public ContextOrderProperties() {
        super();
    }

    private final LinkedHashSet<Object> keys = new LinkedHashSet<Object>();

    @Override
    public Enumeration<Object> keys() {
        return Collections.<Object> enumeration(keys);
    }

    @Override
    public Object put(Object key, Object value) {
        keys.add(key);
        return super.put(key, value);
    }

    @Override
    public synchronized Object remove(Object key) {
        keys.remove(key);
        return super.remove(key);
    }

    @Override
    public Set<Object> keySet() {
        return keys;
    }

    @Override
    public Set<String> stringPropertyNames() {
        Set<String> set = new LinkedHashSet<String>();
        for (Object key : this.keys) {
            set.add((String) key);
        }
        return set;

    }

    /**
     * Constructor.
     * 
     * @param properties the java propertis.
     */
    public ContextOrderProperties(List<IContextParameter> parameterList) {
        this.parameterList = parameterList;
        initContextProperties();
    }

    private void initContextProperties() {
        // group the context and set the comment
        for (IContextParameter parameter : parameterList) {
            // for the temp parameter in expression builder,its source is empty,need to filter from sort
            if (!parameter.getSource().equals("")) {
                int currentIndex = parameterList.indexOf(parameter);
                if (currentIndex == 0) {
                    if (parameter.getSource().equals(IContextParameter.BUILT_IN)) {
                        this.setProperty(parameter.getName(), TalendTextUtils.trimParameter(parameter.getValue()),
                                BUILD_IN_COMMENT);
                    } else {
                        String contextName = ContextUtils.getContextItemById3(parameter.getSource()).getProperty().getLabel();
                        this.setProperty(parameter.getName(), TalendTextUtils.trimParameter(parameter.getValue()),
                                REPOSITORY_COMMENT + contextName);
                    }
                } else {
                    int previousIndex = currentIndex - 1;
                    if (parameter.getSource().equals(IContextParameter.BUILT_IN)) {
                        if (parameterList.get(previousIndex).getSource().equals(IContextParameter.BUILT_IN)) {
                            // same neighbor build-in,so should be in one group,no need to add comment since the
                            // previous has added comment
                            this.setProperty(parameter.getName(), TalendTextUtils.trimParameter(parameter.getValue()), "");
                        } else {
                            this.setProperty(parameter.getName(), TalendTextUtils.trimParameter(parameter.getValue()),
                                    BUILD_IN_COMMENT);
                        }
                    } else {
                        String contextName = ContextUtils.getContextItemById3(parameter.getSource()).getProperty().getLabel();
                        if (parameterList.get(previousIndex).getSource().equals(IContextParameter.BUILT_IN)) {
                            this.setProperty(parameter.getName(), TalendTextUtils.trimParameter(parameter.getValue()),
                                    REPOSITORY_COMMENT + contextName);
                        } else {
                            // need to check if the same repository's context
                            if (parameterList.get(previousIndex).getSource().equals(parameter.getSource())) {
                                this.setProperty(parameter.getName(), TalendTextUtils.trimParameter(parameter.getValue()), "");
                            } else {
                                this.setProperty(parameter.getName(), TalendTextUtils.trimParameter(parameter.getValue()),
                                        REPOSITORY_COMMENT + contextName);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean addComment(String key, String comment) {
        if (this.contains(key)) {
            this.commentMap.put(key, comment);
            return true;
        }
        return false;
    }

    public void setProperty(String key, String value, String comment) {
        this.commentMap.put(key, comment);
        keys.add(key);
        this.setProperty(key, value);
    }

    @Override
    public void store(OutputStream out, String comments) throws IOException {
        orderStore(new BufferedWriter(new OutputStreamWriter(out, "8859_1")), comments);
    }

    public void orderStore(Writer writer, String comments) throws IOException {
        BufferedWriter bufferedWriter = (writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer);
        if (comments != null) {
            writeComments(bufferedWriter, comments);
        }
        bufferedWriter.write("#" + new Date().toString());
        bufferedWriter.newLine();

        synchronized (this) {
            Iterator<String> iterator = this.commentMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = this.getProperty(key);
                String comment = this.commentMap.get(key);
                key = saveConvert(key, true, false);
                value = saveConvert(value, false, false);
                if (comment != null && !comment.equals("")) {
                    bufferedWriter.newLine();
                    writeComments(bufferedWriter, comment);
                }
                bufferedWriter.write(key + "=" + value);
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.toString();
        bufferedWriter.flush();
    }

    // copy from the java sourceCode
    private String saveConvert(String theString, boolean escapeSpace, boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch (aChar) {
            case ' ':
                if (x == 0 || escapeSpace) {
                    outBuffer.append('\\');
                }
                outBuffer.append(' ');
                break;
            case '\t':
                outBuffer.append('\\');
                outBuffer.append('t');
                break;
            case '\n':
                outBuffer.append('\\');
                outBuffer.append('n');
                break;
            case '\r':
                outBuffer.append('\\');
                outBuffer.append('r');
                break;
            case '\f':
                outBuffer.append('\\');
                outBuffer.append('f');
                break;
            case '=': // Fall through
            case ':': // Fall through
            case '#': // Fall through
            case '!':
                outBuffer.append('\\');
                outBuffer.append(aChar);
                break;
            default:
                if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
                    outBuffer.append('\\');
                    outBuffer.append('u');
                    outBuffer.append(toHex((aChar >> 12) & 0xF));
                    outBuffer.append(toHex((aChar >> 8) & 0xF));
                    outBuffer.append(toHex((aChar >> 4) & 0xF));
                    outBuffer.append(toHex(aChar & 0xF));
                } else {
                    outBuffer.append(aChar);
                }
            }
        }
        return outBuffer.toString();
    }

    private void writeComments(BufferedWriter bw, String comments) throws IOException {
        bw.write("#");
        int len = comments.length();
        int current = 0;
        int last = 0;
        char[] uu = new char[6];
        uu[0] = '\\';
        uu[1] = 'u';
        while (current < len) {
            char c = comments.charAt(current);
            if (c > '\u00ff' || c == '\n' || c == '\r') {
                if (last != current) {
                    bw.write(comments.substring(last, current));
                }
                if (c > '\u00ff') {
                    uu[2] = toHex((c >> 12) & 0xf);
                    uu[3] = toHex((c >> 8) & 0xf);
                    uu[4] = toHex((c >> 4) & 0xf);
                    uu[5] = toHex(c & 0xf);
                    bw.write(new String(uu));
                } else {
                    bw.newLine();
                    if (c == '\r' && current != len - 1 && comments.charAt(current + 1) == '\n') {
                        current++;
                    }
                    if (current == len - 1 || (comments.charAt(current + 1) != '#' && comments.charAt(current + 1) != '!')) {
                        bw.write("#");
                    }
                }
                last = current + 1;
            }
            current++;
        }
        if (last != current) {
            bw.write(comments.substring(last, current));
        }
        bw.newLine();
    }

    /*
     * !!! Copy from java source code.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /** A table of hex digits */
    private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
}
