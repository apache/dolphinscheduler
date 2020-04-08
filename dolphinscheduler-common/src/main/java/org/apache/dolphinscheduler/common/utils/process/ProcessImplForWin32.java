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

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import java.lang.reflect.Field;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import sun.security.action.GetPropertyAction;

import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.jna.platform.win32.WinBase.INVALID_HANDLE_VALUE;
import static com.sun.jna.platform.win32.WinBase.STILL_ACTIVE;
import static java.util.Objects.requireNonNull;

public class ProcessImplForWin32 extends Process {

    private static final Field FD_HANDLE;

    static {
        if (!OSUtils.isWindows()) {
            throw new RuntimeException("ProcessImplForWin32 can be only initialized in " +
                    "Windows environment, but current OS is " + OSUtils.getOSName());
        }

        try {
            FD_HANDLE = requireNonNull(FileDescriptor.class.getDeclaredField("handle"));
            FD_HANDLE.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static final int PIPE_SIZE = 4096 + 24;

    private static final int HANDLE_STORAGE_SIZE = 6;

    private static final int OFFSET_READ = 0;

    private static final int OFFSET_WRITE = 1;

    private static final WinNT.HANDLE JAVA_INVALID_HANDLE_VALUE = new WinNT.HANDLE(Pointer.createConstant(-1));

    private static void setHandle(FileDescriptor obj, long handle) {
        try {
            FD_HANDLE.set(obj, handle);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static long getHandle(FileDescriptor obj) {
        try {
            return (Long) FD_HANDLE.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Open a file for writing. If {@code append} is {@code true} then the file
     * is opened for atomic append directly and a FileOutputStream constructed
     * with the resulting handle. This is because a FileOutputStream created
     * to append to a file does not open the file in a manner that guarantees
     * that writes by the child process will be atomic.
     */
    private static FileOutputStream newFileOutputStream(File f, boolean append)
            throws IOException
    {
        if (append) {
            String path = f.getPath();
            SecurityManager sm = System.getSecurityManager();
            if (sm != null)
                sm.checkWrite(path);
            long handle = openForAtomicAppend(path);
            final FileDescriptor fd = new FileDescriptor();
            setHandle(fd, handle);
            return AccessController.doPrivileged(
                    new PrivilegedAction<FileOutputStream>() {
                        public FileOutputStream run() {
                            return new FileOutputStream(fd);
                        }
                    }
            );
        } else {
            return new FileOutputStream(f);
        }
    }

    // System-dependent portion of ProcessBuilderForWindows.start()
    static Process start(String username,
                         String password,
                         String[] cmdarray,
                         java.util.Map<String,String> environment,
                         String dir,
                         ProcessBuilderForWin32.Redirect[] redirects,
                         boolean redirectErrorStream)
            throws IOException
    {
        String envblock = ProcessEnvironmentForWin32.toEnvironmentBlock(environment);

        FileInputStream  f0 = null;
        FileOutputStream f1 = null;
        FileOutputStream f2 = null;

        try {
            long[] stdHandles;
            if (redirects == null) {
                stdHandles = new long[] { -1L, -1L, -1L };
            } else {
                stdHandles = new long[3];

                if (redirects[0] == ProcessBuilderForWin32.Redirect.PIPE)
                    stdHandles[0] = -1L;
                else if (redirects[0] == ProcessBuilderForWin32.Redirect.INHERIT)
                    stdHandles[0] = getHandle(FileDescriptor.in);
                else {
                    f0 = new FileInputStream(redirects[0].file());
                    stdHandles[0] = getHandle(f0.getFD());
                }

                if (redirects[1] == ProcessBuilderForWin32.Redirect.PIPE)
                    stdHandles[1] = -1L;
                else if (redirects[1] == ProcessBuilderForWin32.Redirect.INHERIT)
                    stdHandles[1] = getHandle(FileDescriptor.out);
                else {
                    f1 = newFileOutputStream(redirects[1].file(),
                            redirects[1].append());
                    stdHandles[1] = getHandle(f1.getFD());
                }

                if (redirects[2] == ProcessBuilderForWin32.Redirect.PIPE)
                    stdHandles[2] = -1L;
                else if (redirects[2] == ProcessBuilderForWin32.Redirect.INHERIT)
                    stdHandles[2] = getHandle(FileDescriptor.err);
                else {
                    f2 = newFileOutputStream(redirects[2].file(),
                            redirects[2].append());
                    stdHandles[2] = getHandle(f2.getFD());
                }
            }

            return new ProcessImplForWin32(username, password, cmdarray, envblock, dir, stdHandles, redirectErrorStream);
        } finally {
            // In theory, close() can throw IOException
            // (although it is rather unlikely to happen here)
            try { if (f0 != null) f0.close(); }
            finally {
                try { if (f1 != null) f1.close(); }
                finally { if (f2 != null) f2.close(); }
            }
        }

    }

    private static class LazyPattern {
        // Escape-support version:
        //    "(\")((?:\\\\\\1|.)+?)\\1|([^\\s\"]+)"
        private static final Pattern PATTERN =
                Pattern.compile("[^\\s\"]+|\"[^\"]*\"");
    }

    /* Parses the command string parameter into the executable name and
     * program arguments.
     *
     * The command string is broken into tokens. The token separator is a space
     * or quota character. The space inside quotation is not a token separator.
     * There are no escape sequences.
     */
    private static String[] getTokensFromCommand(String command) {
        ArrayList<String> matchList = new ArrayList<>(8);
        Matcher regexMatcher = ProcessImplForWin32.LazyPattern.PATTERN.matcher(command);
        while (regexMatcher.find())
            matchList.add(regexMatcher.group());
        return matchList.toArray(new String[matchList.size()]);
    }

    private static final int VERIFICATION_CMD_BAT = 0;
    private static final int VERIFICATION_WIN32 = 1;
    private static final int VERIFICATION_WIN32_SAFE = 2; // inside quotes not allowed
    private static final int VERIFICATION_LEGACY = 3;
    // See Command shell overview for documentation of special characters.
    // https://docs.microsoft.com/en-us/previous-versions/windows/it-pro/windows-xp/bb490954(v=technet.10)
    private static final char[][] ESCAPE_VERIFICATION = {
            // We guarantee the only command file execution for implicit [cmd.exe] run.
            //    http://technet.microsoft.com/en-us/library/bb490954.aspx
            {' ', '\t', '<', '>', '&', '|', '^'},
            {' ', '\t', '<', '>'},
            {' ', '\t', '<', '>'},
            {' ', '\t'}
    };

    private static String createCommandLine(int verificationType,
                                            final String executablePath,
                                            final String[] cmd)
    {
        StringBuilder cmdbuf = new StringBuilder(80);

        cmdbuf.append(executablePath);

        for (int i = 1; i < cmd.length; ++i) {
            cmdbuf.append(' ');
            String s = cmd[i];
            if (needsEscaping(verificationType, s)) {
                cmdbuf.append('"');

                if (verificationType == VERIFICATION_WIN32_SAFE) {
                    // Insert the argument, adding '\' to quote any interior quotes
                    int length = s.length();
                    for (int j = 0; j < length; j++) {
                        char c = s.charAt(j);
                        if (c == DOUBLEQUOTE) {
                            int count = countLeadingBackslash(verificationType, s, j);
                            while (count-- > 0) {
                                cmdbuf.append(BACKSLASH);   // double the number of backslashes
                            }
                            cmdbuf.append(BACKSLASH);       // backslash to quote the quote
                        }
                        cmdbuf.append(c);
                    }
                } else {
                    cmdbuf.append(s);
                }
                // The code protects the [java.exe] and console command line
                // parser, that interprets the [\"] combination as an escape
                // sequence for the ["] char.
                //     http://msdn.microsoft.com/en-us/library/17w5ykft.aspx
                //
                // If the argument is an FS path, doubling of the tail [\]
                // char is not a problem for non-console applications.
                //
                // The [\"] sequence is not an escape sequence for the [cmd.exe]
                // command line parser. The case of the [""] tail escape
                // sequence could not be realized due to the argument validation
                // procedure.
                int count = countLeadingBackslash(verificationType, s, s.length());
                while (count-- > 0) {
                    cmdbuf.append(BACKSLASH);   // double the number of backslashes
                }
                cmdbuf.append('"');
            } else {
                cmdbuf.append(s);
            }
        }
        return cmdbuf.toString();
    }

    /**
     * Return the argument without quotes (1st and last) if present, else the arg.
     * @param str a string
     * @return the string without 1st and last quotes
     */
    private static String unQuote(String str) {
        int len = str.length();
        return (len >= 2 && str.charAt(0) == DOUBLEQUOTE && str.charAt(len - 1) == DOUBLEQUOTE)
                ? str.substring(1, len - 1)
                : str;
    }

    private static boolean needsEscaping(int verificationType, String arg) {
        // Switch off MS heuristic for internal ["].
        // Please, use the explicit [cmd.exe] call
        // if you need the internal ["].
        //    Example: "cmd.exe", "/C", "Extended_MS_Syntax"

        // For [.exe] or [.com] file the unpaired/internal ["]
        // in the argument is not a problem.
        String unquotedArg = unQuote(arg);
        boolean argIsQuoted = !arg.equals(unquotedArg);
        boolean embeddedQuote = unquotedArg.indexOf(DOUBLEQUOTE) >= 0;

        switch (verificationType) {
            case VERIFICATION_CMD_BAT:
                if (embeddedQuote) {
                    throw new IllegalArgumentException("Argument has embedded quote, " +
                            "use the explicit CMD.EXE call.");
                }
                break;  // break determine whether to quote
            case VERIFICATION_WIN32_SAFE:
                if (argIsQuoted && embeddedQuote)  {
                    throw new IllegalArgumentException("Malformed argument has embedded quote: "
                            + unquotedArg);
                }
                break;
            default:
                break;
        }

        if (!argIsQuoted) {
            char[] testEscape = ESCAPE_VERIFICATION[verificationType];
            for (int i = 0; i < testEscape.length; ++i) {
                if (arg.indexOf(testEscape[i]) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getExecutablePath(String path)
            throws IOException
    {
        String name = unQuote(path);
        if (name.indexOf(DOUBLEQUOTE) >= 0) {
            throw new IllegalArgumentException("Executable name has embedded quote, " +
                    "split the arguments: " + name);
        }
        // Win32 CreateProcess requires path to be normalized
        File fileToRun = new File(name);

        // From the [CreateProcess] function documentation:
        //
        // "If the file name does not contain an extension, .exe is appended.
        // Therefore, if the file name extension is .com, this parameter
        // must include the .com extension. If the file name ends in
        // a period (.) with no extension, or if the file name contains a path,
        // .exe is not appended."
        //
        // "If the file name !does not contain a directory path!,
        // the system searches for the executable file in the following
        // sequence:..."
        //
        // In practice ANY non-existent path is extended by [.exe] extension
        // in the [CreateProcess] function with the only exception:
        // the path ends by (.)

        return fileToRun.getPath();
    }

    /**
     * An executable is any program that is an EXE or does not have an extension
     * and the Windows createProcess will be looking for .exe.
     * The comparison is case insensitive based on the name.
     * @param executablePath the executable file
     * @return true if the path ends in .exe or does not have an extension.
     */
    private boolean isExe(String executablePath) {
        File file = new File(executablePath);
        String upName = file.getName().toUpperCase(Locale.ROOT);
        return (upName.endsWith(".EXE") || upName.indexOf('.') < 0);
    }

    // Old version that can be bypassed
    private boolean isShellFile(String executablePath) {
        String upPath = executablePath.toUpperCase();
        return (upPath.endsWith(".CMD") || upPath.endsWith(".BAT"));
    }

    private String quoteString(String arg) {
        StringBuilder argbuf = new StringBuilder(arg.length() + 2);
        return argbuf.append('"').append(arg).append('"').toString();
    }

    // Count backslashes before start index of string.
    // .bat files don't include backslashes as part of the quote
    private static int countLeadingBackslash(int verificationType,
                                             CharSequence input, int start) {
        if (verificationType == VERIFICATION_CMD_BAT)
            return 0;
        int j;
        for (j = start - 1; j >= 0 && input.charAt(j) == BACKSLASH; j--) {
            // just scanning backwards
        }
        return (start - 1) - j;  // number of BACKSLASHES
    }

    private static final char DOUBLEQUOTE = '\"';
    private static final char BACKSLASH = '\\';

    private WinNT.HANDLE handle;
    private OutputStream stdinStream;
    private InputStream stdoutStream;
    private InputStream stderrStream;

    private ProcessImplForWin32(
            String username,
            String password,
            String[] cmd,
            final String envblock,
            final String path,
            final long[] stdHandles,
            final boolean redirectErrorStream)
            throws IOException
    {
        String cmdstr;
        final SecurityManager security = System.getSecurityManager();
        GetPropertyAction action = new GetPropertyAction("jdk.lang.Process.allowAmbiguousCommands",
                (security == null) ? "true" : "false");
        final boolean allowAmbiguousCommands = !"false".equalsIgnoreCase(action.run());
        if (allowAmbiguousCommands && security == null) {
            // Legacy mode.

            // Normalize path if possible.
            String executablePath = new File(cmd[0]).getPath();

            // No worry about internal, unpaired ["], and redirection/piping.
            if (needsEscaping(VERIFICATION_LEGACY, executablePath) )
                executablePath = quoteString(executablePath);

            cmdstr = createCommandLine(
                    //legacy mode doesn't worry about extended verification
                    VERIFICATION_LEGACY,
                    executablePath,
                    cmd);
        } else {
            String executablePath;
            try {
                executablePath = getExecutablePath(cmd[0]);
            } catch (IllegalArgumentException e) {
                // Workaround for the calls like
                // Runtime.getRuntime().exec("\"C:\\Program Files\\foo\" bar")

                // No chance to avoid CMD/BAT injection, except to do the work
                // right from the beginning. Otherwise we have too many corner
                // cases from
                //    Runtime.getRuntime().exec(String[] cmd [, ...])
                // calls with internal ["] and escape sequences.

                // Restore original command line.
                StringBuilder join = new StringBuilder();
                // terminal space in command line is ok
                for (String s : cmd)
                    join.append(s).append(' ');

                // Parse the command line again.
                cmd = getTokensFromCommand(join.toString());
                executablePath = getExecutablePath(cmd[0]);

                // Check new executable name once more
                if (security != null)
                    security.checkExec(executablePath);
            }

            // Quotation protects from interpretation of the [path] argument as
            // start of longer path with spaces. Quotation has no influence to
            // [.exe] extension heuristic.
            boolean isShell = allowAmbiguousCommands ? isShellFile(executablePath)
                    : !isExe(executablePath);
            cmdstr = createCommandLine(
                    // We need the extended verification procedures
                    isShell ? VERIFICATION_CMD_BAT
                            : (allowAmbiguousCommands ? VERIFICATION_WIN32 : VERIFICATION_WIN32_SAFE),
                    quoteString(executablePath),
                    cmd);
        }

        handle = create(username, password, cmdstr, envblock, path, stdHandles, redirectErrorStream);

        AccessController.doPrivileged(
                new PrivilegedAction<Void>() {
                    public Void run() {
                        if (stdHandles[0] == -1L)
                            stdinStream = ProcessBuilderForWin32.NullOutputStream.INSTANCE;
                        else {
                            FileDescriptor stdinFd = new FileDescriptor();
                            setHandle(stdinFd, stdHandles[0]);
                            stdinStream = new BufferedOutputStream(
                                    new FileOutputStream(stdinFd));
                        }

                        if (stdHandles[1] == -1L)
                            stdoutStream = ProcessBuilderForWin32.NullInputStream.INSTANCE;
                        else {
                            FileDescriptor stdoutFd = new FileDescriptor();
                            setHandle(stdoutFd, stdHandles[1]);
                            stdoutStream = new BufferedInputStream(
                                    new FileInputStream(stdoutFd));
                        }

                        if (stdHandles[2] == -1L)
                            stderrStream = ProcessBuilderForWin32.NullInputStream.INSTANCE;
                        else {
                            FileDescriptor stderrFd = new FileDescriptor();
                            setHandle(stderrFd, stdHandles[2]);
                            stderrStream = new FileInputStream(stderrFd);
                        }

                        return null; }});
    }

    public OutputStream getOutputStream() {
        return stdinStream;
    }

    public InputStream getInputStream() {
        return stdoutStream;
    }

    public InputStream getErrorStream() {
        return stderrStream;
    }

    protected void finalize() {
        closeHandle(handle);
    }

    public int exitValue() {
        int exitCode = getExitCodeProcess(handle);
        if (exitCode == STILL_ACTIVE)
            throw new IllegalThreadStateException("process has not exited");
        return exitCode;
    }

    public int waitFor() throws InterruptedException {
        waitForInterruptibly(handle);
        if (Thread.interrupted())
            throw new InterruptedException();
        return exitValue();
    }

    @Override
    public boolean waitFor(long timeout, TimeUnit unit)
            throws InterruptedException
    {
        if (getExitCodeProcess(handle) != STILL_ACTIVE) return true;
        if (timeout <= 0) return false;

        long remainingNanos  = unit.toNanos(timeout);
        long deadline = System.nanoTime() + remainingNanos ;

        do {
            // Round up to next millisecond
            long msTimeout = TimeUnit.NANOSECONDS.toMillis(remainingNanos + 999_999L);
            waitForTimeoutInterruptibly(handle, msTimeout);
            if (Thread.interrupted())
                throw new InterruptedException();
            if (getExitCodeProcess(handle) != STILL_ACTIVE) {
                return true;
            }
            remainingNanos = deadline - System.nanoTime();
        } while (remainingNanos > 0);

        return (getExitCodeProcess(handle) != STILL_ACTIVE);
    }

    public void destroy() { terminateProcess(handle); }

    @Override
    public Process destroyForcibly() {
        destroy();
        return this;
    }
    @Override
    public boolean isAlive() {
        return isProcessAlive(handle);
    }

    private static boolean initHolder(WinNT.HANDLEByReference pjhandles,
                                      WinNT.HANDLEByReference[] pipe,
                                      int offset,
                                      WinNT.HANDLEByReference phStd) {
        if (!pjhandles.getValue().equals(JAVA_INVALID_HANDLE_VALUE)) {
            phStd.setValue(pjhandles.getValue());
            pjhandles.setValue(JAVA_INVALID_HANDLE_VALUE);
        } else {
            if (!Kernel32.INSTANCE.CreatePipe(pipe[0], pipe[1], null, PIPE_SIZE)) {
                throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
            } else {
                WinNT.HANDLE thisProcessEnd = offset == OFFSET_READ ? pipe[1].getValue() : pipe[0].getValue();
                phStd.setValue(pipe[offset].getValue());
                pjhandles.setValue(thisProcessEnd);
            }
        }
        Kernel32.INSTANCE.SetHandleInformation(phStd.getValue(), WinBase.HANDLE_FLAG_INHERIT, WinBase.HANDLE_FLAG_INHERIT);
        return true;
    }

    private static void releaseHolder(boolean complete, WinNT.HANDLEByReference[] pipe, int offset) {
        closeHandle(pipe[offset].getValue());
        if (complete) {
            closeHandle(pipe[offset == OFFSET_READ ? OFFSET_WRITE : OFFSET_READ].getValue());
        }
    }

    private static void prepareIOEHandleState(WinNT.HANDLE[] stdIOE, Boolean[] inherit) {
        for(int i = 0; i < HANDLE_STORAGE_SIZE; ++i) {
            WinNT.HANDLE hstd = stdIOE[i];
            if (!WinBase.INVALID_HANDLE_VALUE.equals(hstd)) {
                inherit[i] = Boolean.TRUE;
                Kernel32.INSTANCE.SetHandleInformation(hstd, WinBase.HANDLE_FLAG_INHERIT, 0);
            }
        }
    }

    private static void restoreIOEHandleState(WinNT.HANDLE[] stdIOE, Boolean[] inherit) {
        for (int i = HANDLE_STORAGE_SIZE - 1; i >= 0; --i) {
            if (!WinBase.INVALID_HANDLE_VALUE.equals(stdIOE[i])) {
                Kernel32.INSTANCE.SetHandleInformation(stdIOE[i], WinBase.HANDLE_FLAG_INHERIT, Boolean.TRUE.equals(inherit[i]) ? WinBase.HANDLE_FLAG_INHERIT : 0);
            }
        }
    }

    private static WinNT.HANDLE processCreate(String username,
                                              String password,
                                              String cmd,
                                              final String envblock,
                                              final String path,
                                              final WinNT.HANDLEByReference[] stdHandles,
                                              final boolean redirectErrorStream) {
        WinNT.HANDLE ret = new WinNT.HANDLE(Pointer.createConstant(0));

        WinNT.HANDLE[] stdIOE = new WinNT.HANDLE[] {
                WinBase.INVALID_HANDLE_VALUE, WinBase.INVALID_HANDLE_VALUE, WinBase.INVALID_HANDLE_VALUE,
                stdHandles[0].getValue(), stdHandles[1].getValue(), stdHandles[2].getValue()
        };
        stdIOE[0] = Kernel32.INSTANCE.GetStdHandle(Wincon.STD_INPUT_HANDLE);
        stdIOE[1] = Kernel32.INSTANCE.GetStdHandle(Wincon.STD_OUTPUT_HANDLE);
        stdIOE[2] = Kernel32.INSTANCE.GetStdHandle(Wincon.STD_ERROR_HANDLE);

        Boolean[] inherit = new Boolean[] {
                Boolean.FALSE, Boolean.FALSE, Boolean.FALSE,
                Boolean.FALSE, Boolean.FALSE, Boolean.FALSE
        };

        prepareIOEHandleState(stdIOE, inherit);

        // input
        WinNT.HANDLEByReference hStdInput = new WinNT.HANDLEByReference();
        WinNT.HANDLEByReference[] pipeIn = new WinNT.HANDLEByReference[] {
                new WinNT.HANDLEByReference(WinBase.INVALID_HANDLE_VALUE), new WinNT.HANDLEByReference(WinBase.INVALID_HANDLE_VALUE) };

        // output
        WinNT.HANDLEByReference hStdOutput = new WinNT.HANDLEByReference();
        WinNT.HANDLEByReference[] pipeOut = new WinNT.HANDLEByReference[] {
                new WinNT.HANDLEByReference(WinBase.INVALID_HANDLE_VALUE), new WinNT.HANDLEByReference(WinBase.INVALID_HANDLE_VALUE) };

        // error
        WinNT.HANDLEByReference hStdError = new WinNT.HANDLEByReference();
        WinNT.HANDLEByReference[] pipeError = new WinNT.HANDLEByReference[] {
                new WinNT.HANDLEByReference(WinBase.INVALID_HANDLE_VALUE), new WinNT.HANDLEByReference(WinBase.INVALID_HANDLE_VALUE) };

        boolean success;
        if (initHolder(stdHandles[0], pipeIn, OFFSET_READ, hStdInput)) {
            if (initHolder(stdHandles[1], pipeOut, OFFSET_WRITE, hStdOutput)) {
                WinBase.STARTUPINFO si = new WinBase.STARTUPINFO();
                si.hStdInput = hStdInput.getValue();
                si.hStdOutput = hStdOutput.getValue();

                if (redirectErrorStream) {
                    si.hStdError = si.hStdOutput;
                    stdHandles[2].setValue(JAVA_INVALID_HANDLE_VALUE);
                    success = true;
                } else {
                    success = initHolder(stdHandles[2], pipeError, OFFSET_WRITE, hStdError);
                    si.hStdError = hStdError.getValue();
                }

                if (success) {
                    WTypes.LPSTR lpEnvironment = envblock == null ? new WTypes.LPSTR() : new WTypes.LPSTR(envblock);
                    WinBase.PROCESS_INFORMATION pi = new WinBase.PROCESS_INFORMATION();
                    si.dwFlags = WinBase.STARTF_USESTDHANDLES;
                    if (!Advapi32.INSTANCE.CreateProcessWithLogonW(
                            username
                            , null
                            , password
                            , Advapi32.LOGON_WITH_PROFILE
                            , null
                            , cmd
                            , WinBase.CREATE_NO_WINDOW
                            , lpEnvironment.getPointer()
                            , path
                            , si
                            , pi)) {
                        throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
                    } else {
                        closeHandle(pi.hThread);
                        ret = pi.hProcess;
                    }
                }
                releaseHolder(ret.getPointer().equals(Pointer.createConstant(0)), pipeError, OFFSET_WRITE);
                releaseHolder(ret.getPointer().equals(Pointer.createConstant(0)), pipeOut, OFFSET_WRITE);
            }
            releaseHolder(ret.getPointer().equals(Pointer.createConstant(0)), pipeIn, OFFSET_READ);
        }
        restoreIOEHandleState(stdIOE, inherit);
        return ret;
    }

    private static synchronized WinNT.HANDLE create(String username,
                                                    String password,
                                                    String cmd,
                                                    final String envblock,
                                                    final String path,
                                                    final long[] stdHandles,
                                                    final boolean redirectErrorStream) {
        WinNT.HANDLE ret = new WinNT.HANDLE(Pointer.createConstant(0));
        WinNT.HANDLEByReference[] handles = new WinNT.HANDLEByReference[stdHandles.length];
        for (int i = 0; i < stdHandles.length; i++) {
            handles[i] = new WinNT.HANDLEByReference(new WinNT.HANDLE(Pointer.createConstant(stdHandles[i])));
        }
        
        if (cmd != null && username != null && password != null) {
            ret = processCreate(username, password, cmd, envblock, path, handles, redirectErrorStream);
        }
        
        for (int i = 0; i < stdHandles.length; i++) {
            stdHandles[i] = handles[i].getPointer().getLong(0);
        }

        return ret;
    }

    private static int getExitCodeProcess(WinNT.HANDLE handle) {
        IntByReference exitStatus = new IntByReference();
        if (!Kernel32.INSTANCE.GetExitCodeProcess(handle, exitStatus)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        return exitStatus.getValue();
    }

    private static void terminateProcess(WinNT.HANDLE handle) {
        Kernel32.INSTANCE.TerminateProcess(handle, 1);
    }

    private static boolean isProcessAlive(WinNT.HANDLE handle) {
        IntByReference exitStatus = new IntByReference();
        Kernel32.INSTANCE.GetExitCodeProcess(handle, exitStatus);
        return exitStatus.getValue() == STILL_ACTIVE;
    }

    private static void closeHandle(WinNT.HANDLE handle) {
        if (!handle.equals(INVALID_HANDLE_VALUE)) {
            Kernel32Util.closeHandle(handle);
        }
    }

    /**
     * Opens a file for atomic append. The file is created if it doesn't
     * already exist.
     *
     * @param path the file to open or create
     * @return the native HANDLE
     */
    private static long openForAtomicAppend(String path) throws IOException {
        int access = WinNT.GENERIC_READ | WinNT.GENERIC_WRITE;
        int sharing = WinNT.FILE_SHARE_READ | WinNT.FILE_SHARE_WRITE;
        int disposition = WinNT.OPEN_ALWAYS;
        int flagsAndAttributes = WinNT.FILE_ATTRIBUTE_NORMAL;
        if (path == null || path.isEmpty()) {
            return -1;
        } else {
            WinNT.HANDLE handle = Kernel32.INSTANCE.CreateFile(path, access, sharing, null, disposition, flagsAndAttributes, null);
            if (handle == WinBase.INVALID_HANDLE_VALUE) {
                throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
            }
            return handle.getPointer().getLong(0);
        }
    }

    private static void waitForInterruptibly(WinNT.HANDLE handle) {
        int result = Kernel32.INSTANCE.WaitForMultipleObjects(1, new WinNT.HANDLE[]{handle}, false, WinBase.INFINITE);
        if (result == WinBase.WAIT_FAILED) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
    }

    private static void waitForTimeoutInterruptibly(WinNT.HANDLE handle, long timeout) {
        int result = Kernel32.INSTANCE.WaitForMultipleObjects(1, new WinNT.HANDLE[]{handle}, false, (int) timeout);
        if (result == WinBase.WAIT_FAILED) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
    }

}
