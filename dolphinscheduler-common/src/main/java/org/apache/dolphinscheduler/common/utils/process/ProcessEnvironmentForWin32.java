/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.common.utils.process;

import com.sun.jna.platform.win32.Kernel32Util;

import java.util.*;

final class ProcessEnvironmentForWin32 extends HashMap<String,String> {

    private static final long serialVersionUID = -8017839552603542824L;

    private static String validateName(String name) {
        // An initial `=' indicates a magic Windows variable name -- OK
        if (name.indexOf('=', 1)   != -1 ||
                name.indexOf('\u0000') != -1)
            throw new IllegalArgumentException
                    ("Invalid environment variable name: \"" + name + "\"");
        return name;
    }

    private static String validateValue(String value) {
        if (value.indexOf('\u0000') != -1)
            throw new IllegalArgumentException
                    ("Invalid environment variable value: \"" + value + "\"");
        return value;
    }

    private static String nonNullString(Object o) {
        if (o == null)
            throw new NullPointerException();
        return (String) o;
    }

    @Override
    public String put(String key, String value) {
        return super.put(validateName(key), validateValue(value));
    }
    @Override
    public String get(Object key) {
        return super.get(nonNullString(key));
    }
    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(nonNullString(key));
    }
    @Override
    public boolean containsValue(Object value) {
        return super.containsValue(nonNullString(value));
    }
    @Override
    public String remove(Object key) {
        return super.remove(nonNullString(key));
    }

    private static class CheckedEntry implements Entry<String,String> {
        private final Entry<String,String> e;
        public CheckedEntry(Entry<String,String> e) {this.e = e;}
        public String getKey()   { return e.getKey();}
        public String getValue() { return e.getValue();}
        public String setValue(String value) {
            return e.setValue(validateValue(value));
        }
        public String toString() { return getKey() + "=" + getValue();}
        public boolean equals(Object o) {return e.equals(o);}
        public int hashCode()    {return e.hashCode();}
    }

    private static class CheckedEntrySet extends AbstractSet<Entry<String,String>> {
        private final Set<Entry<String,String>> s;
        public CheckedEntrySet(Set<Entry<String,String>> s) {this.s = s;}
        public int size()        {return s.size();}
        public boolean isEmpty() {return s.isEmpty();}
        public void clear()      {       s.clear();}
        public Iterator<Entry<String,String>> iterator() {
            return new Iterator<Entry<String,String>>() {
                Iterator<Entry<String,String>> i = s.iterator();
                public boolean hasNext() { return i.hasNext();}
                public Entry<String,String> next() {
                    return new CheckedEntry(i.next());
                }
                @Override
                public void remove() { i.remove();}
            };
        }
        private static Entry<String,String> checkedEntry(Object o) {
            @SuppressWarnings("unchecked")
            Entry<String,String> e = (Entry<String,String>) o;
            nonNullString(e.getKey());
            nonNullString(e.getValue());
            return e;
        }
        public boolean contains(Object o) {return s.contains(checkedEntry(o));}
        public boolean remove(Object o)   {return s.remove(checkedEntry(o));}
    }

    private static class CheckedValues extends AbstractCollection<String> {
        private final Collection<String> c;
        public CheckedValues(Collection<String> c) {this.c = c;}
        public int size()                  {return c.size();}
        @Override
        public boolean isEmpty()           {return c.isEmpty();}
        @Override
        public void clear()                {       c.clear();}
        public Iterator<String> iterator() {return c.iterator();}
        @Override
        public boolean contains(Object o)  {return c.contains(nonNullString(o));}
        @Override
        public boolean remove(Object o)    {return c.remove(nonNullString(o));}
    }

    private static class CheckedKeySet extends AbstractSet<String> {
        private final Set<String> s;
        public CheckedKeySet(Set<String> s) {this.s = s;}
        public int size()                  {return s.size();}
        public boolean isEmpty()           {return s.isEmpty();}
        public void clear()                {       s.clear();}
        public Iterator<String> iterator() {return s.iterator();}
        public boolean contains(Object o)  {return s.contains(nonNullString(o));}
        public boolean remove(Object o)    {return s.remove(nonNullString(o));}
    }
    @Override
    public Set<String> keySet() {
        return new CheckedKeySet(super.keySet());
    }
    @Override
    public Collection<String> values() {
        return new CheckedValues(super.values());
    }
    @Override
    public Set<Entry<String,String>> entrySet() {
        return new CheckedEntrySet(super.entrySet());
    }

    private static final class NameComparator implements Comparator<String> {
        public int compare(String s1, String s2) {
            // We can't use String.compareToIgnoreCase since it
            // canonicalizes to lower case, while Windows
            // canonicalizes to upper case!  For example, "_" should
            // sort *after* "Z", not before.
            int n1 = s1.length();
            int n2 = s2.length();
            int min = Math.min(n1, n2);
            for (int i = 0; i < min; i++) {
                char c1 = s1.charAt(i);
                char c2 = s2.charAt(i);
                if (c1 != c2) {
                    c1 = Character.toUpperCase(c1);
                    c2 = Character.toUpperCase(c2);
                    if (c1 != c2)
                        // No overflow because of numeric promotion
                        return c1 - c2;
                }
            }
            return n1 - n2;
        }
    }

    private static final class EntryComparator implements Comparator<Entry<String,String>> {
        public int compare(Entry<String,String> e1,
                           Entry<String,String> e2) {
            return nameComparator.compare(e1.getKey(), e2.getKey());
        }
    }

    // Allow `=' as first char in name, e.g. =C:=C:\DIR
    static final int MIN_NAME_LENGTH = 1;

    private static final NameComparator nameComparator;
    private static final EntryComparator entryComparator;
    private static final ProcessEnvironmentForWin32 theEnvironment;
    private static final Map<String,String> theUnmodifiableEnvironment;
    private static final Map<String,String> theCaseInsensitiveEnvironment;

    static {
        nameComparator  = new NameComparator();
        entryComparator = new EntryComparator();
        theEnvironment  = new ProcessEnvironmentForWin32();
        theUnmodifiableEnvironment = Collections.unmodifiableMap(theEnvironment);

        theEnvironment.putAll(environmentBlock());

        theCaseInsensitiveEnvironment = new TreeMap<>(nameComparator);
        theCaseInsensitiveEnvironment.putAll(theEnvironment);
    }

    private ProcessEnvironmentForWin32() {
        super();
    }

    private ProcessEnvironmentForWin32(int capacity) {
        super(capacity);
    }

    // Only for use by System.getenv(String)
    static String getenv(String name) {
        // The original implementation used a native call to _wgetenv,
        // but it turns out that _wgetenv is only consistent with
        // GetEnvironmentStringsW (for non-ASCII) if `wmain' is used
        // instead of `main', even in a process created using
        // CREATE_UNICODE_ENVIRONMENT.  Instead we perform the
        // case-insensitive comparison ourselves.  At least this
        // guarantees that System.getenv().get(String) will be
        // consistent with System.getenv(String).
        return theCaseInsensitiveEnvironment.get(name);
    }

    // Only for use by System.getenv()
    static Map<String,String> getenv() {
        return theUnmodifiableEnvironment;
    }

    // Only for use by ProcessBuilder.environment()
    @SuppressWarnings("unchecked")
    static Map<String,String> environment() {
        return (Map<String,String>) theEnvironment.clone();
    }

    // Only for use by ProcessBuilder.environment(String[] envp)
    static Map<String,String> emptyEnvironment(int capacity) {
        return new ProcessEnvironmentForWin32(capacity);
    }

    private static Map<String, String> environmentBlock() {
        return Kernel32Util.getEnvironmentVariables();
    }

    // Only for use by ProcessImpl.start()
    String toEnvironmentBlock() {
        // Sort Unicode-case-insensitively by name
        List<Entry<String,String>> list = new ArrayList<>(entrySet());
        Collections.sort(list, entryComparator);

        StringBuilder sb = new StringBuilder(size()*30);
        int cmp = -1;

        // Some versions of MSVCRT.DLL require SystemRoot to be set.
        // So, we make sure that it is always set, even if not provided
        // by the caller.
        final String SYSTEMROOT = "SystemRoot";

        for (Entry<String,String> e : list) {
            String key = e.getKey();
            String value = e.getValue();
            if (cmp < 0 && (cmp = nameComparator.compare(key, SYSTEMROOT)) > 0) {
                // Not set, so add it here
                addToEnvIfSet(sb, SYSTEMROOT);
            }
            addToEnv(sb, key, value);
        }
        if (cmp < 0) {
            // Got to end of list and still not found
            addToEnvIfSet(sb, SYSTEMROOT);
        }
        if (sb.length() == 0) {
            // Environment was empty and SystemRoot not set in parent
            sb.append('\u0000');
        }
        // Block is double NUL terminated
        sb.append('\u0000');
        return sb.toString();
    }

    // add the environment variable to the child, if it exists in parent
    private static void addToEnvIfSet(StringBuilder sb, String name) {
        String s = getenv(name);
        if (s != null)
            addToEnv(sb, name, s);
    }

    private static void addToEnv(StringBuilder sb, String name, String val) {
        sb.append(name).append('=').append(val).append('\u0000');
    }

    static String toEnvironmentBlock(Map<String,String> map) {
        return map == null ? null : ((ProcessEnvironmentForWin32)map).toEnvironmentBlock();
    }
}
