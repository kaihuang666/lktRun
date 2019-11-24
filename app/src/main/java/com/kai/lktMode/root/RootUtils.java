/*
 * Copyright (C) 2015-2016 Willi Ye <williye97@gmail.com>
 *
 * This file is part of Kernel Adiutor.
 *
 * Kernel Adiutor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kernel Adiutor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kernel Adiutor.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kai.lktMode.root;

import android.util.Log;
import com.kai.lktMode.bean.Utils;
import com.kai.lktMode.tool.util.local.ShellUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by willi on 30.12.15.
 */
public class RootUtils {
    public static boolean checkRootPathSU() {
        File f=null;
        final String kSuSearchPaths[]={"/system/bin/","/system/xbin/","/system/sbin/","/sbin/","/vendor/bin/"};
        try{
            for(int i=0;i<kSuSearchPaths.length;i++)
            {
                f=new File(kSuSearchPaths[i]+"su");
                if(f!=null&&f.exists())
                {
                    Log.i("root","find su in : "+kSuSearchPaths[i]);
                    return true;
                }
            }

            boolean su=ShellUtil.isInstalled("su");
            return su;
        }catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
    private static SU sInstance;
    public  interface onCommandComplete{
        void onComplete();
        void onCrash(String error);
        void onOutput(String result);
    }
    public static boolean rootAccess() {
        SU su = getSU();
        su.runCommand("echo /testRoot/");
        return !su.denied;
    }
    public static boolean busyboxInstalled() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"busybox"},null,null);
            DataInputStream input = new DataInputStream(process.getInputStream());
            String line;
            process.waitFor();
            line=input.readLine();
            if (!line.isEmpty()){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    public static String getBusyboxVersion(){
        String box=runCommand("busybox --help");
        Pattern pattern=Pattern.compile("BusyBox\\s+(\\S+)");
        Matcher matcher=pattern.matcher(box);
        if (matcher.find()){
            return matcher.group(1);
        }else {
            if (busyboxInstalled()){
                return "v3.1.0";
            }else {
                return "";
            }
        }
    }

    private static boolean existBinary(String binary) {
        String paths;
        if (System.getenv("PATH") != null) {
            paths = System.getenv("PATH");
        } else {
            paths = "/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin";
        }
        for (String path : paths.split(":")) {
            if (!path.endsWith("/")) path += "/";
            if (Utils.existFile(path + binary, false) || Utils.existFile(path + binary)) {
                return true;
            }
        }
        return RootUtils.existBinary("busybox");
    }

    public static void chmod(String file, String permission) {
        chmod(file, permission, getSU());
    }

    public static void chmod(String file, String permission, SU su) {
        su.runCommand("chmod " + permission + " " + file);
    }

    public static String getProp(String prop) {
        return runCommand("getprop " + prop);
    }

    public static void mount(boolean writeable, String mountpoint) {
        mount(writeable, mountpoint, getSU());
    }

    public static void mount(boolean writeable, String mountpoint, SU su) {
        su.runCommand(String.format("mount -o remount,%s %s %s",
                writeable ? "rw" : "ro", mountpoint, mountpoint));
        su.runCommand(String.format("mount -o remount,%s %s",
                writeable ? "rw" : "ro", mountpoint));
        su.runCommand(String.format("mount -o %s,remount %s",
                writeable ? "rw" : "ro", mountpoint));
    }

    public static String runScript(String text, String... arguments) {
        RootFile script = new RootFile("/data/local/tmp/kerneladiutortmp.sh");
        script.mkdir();
        script.write(text, false);
        return script.execute(arguments);
    }

    public static void closeSU() {
        if (sInstance != null) sInstance.close();
        sInstance = null;
    }

    public static String runCommand(String command) {
        return getSU().runCommand(command);
    }
    public static void runCommand(String command,onCommandComplete onCommandComplete){
        getSU().runCommand(command,onCommandComplete);
    }
    public static void runCommand(String command,onCommandComplete onCommandComplete,boolean enableError){
        getSU().runCommand(command,onCommandComplete,enableError);
    }

    public static SU getSU() {
        if (sInstance == null || sInstance.closed || sInstance.denied) {
            if (sInstance != null && !sInstance.closed) {
                sInstance.close();
            }
            sInstance = new SU();
        }
        return sInstance;
    }

    /*
     * Based on AndreiLux's SU code in Synapse
     * https://github.com/AndreiLux/Synapse/blob/master/src/main/java/com/af/synapse/utils/Utils.java#L238
     */
    public static class SU {

        private Process mProcess;
        private BufferedWriter mWriter;
        private BufferedReader mReader;
        private BufferedReader mError;
        private final boolean mRoot;
        private int commandId=0;
        private final String mTag;
        private boolean closed;
        public boolean denied;
        private boolean firstTry;

        private ReentrantLock mLock = new ReentrantLock();

        public SU() {
            this(true, null);
        }

        public SU(boolean root, String tag) {
            mRoot = root;
            mTag = tag;
            try {
                if (mTag != null) {
                    Log.i(mTag, String.format("%s initialized", root ? "SU" : "SH"));
                }
                firstTry = true;
                mProcess = Runtime.getRuntime().exec(root ? "su" : "sh");
                mWriter = new BufferedWriter(new OutputStreamWriter(mProcess.getOutputStream()));
                mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
                mError=new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
            } catch (IOException e) {
                if (mTag != null) {
                    Log.e(mTag, root ? "Failed to run shell as su" : "Failed to run shell as sh");
                }
                denied = true;
                closed = true;
            }
        }
        public String runCommand(final String command) {
            if (closed) return "";
            try {
                mLock.lock();

                StringBuilder sb = new StringBuilder();
                String callback = "/shellCallback/";
                mWriter.write(command + "\n");
                mWriter.write("echo " + callback + "\n");
                mWriter.flush();

                String line;
                String line1;

                while ((line = mReader.readLine()) != null) {
                    if (line.equals(callback)) {
                        break;
                    }
                    sb.append(line).append("\n");
                }

                firstTry = false;
                if (mTag != null) {
                    Log.i(mTag, "run: " + command + " output: " + sb.toString().trim());
                }

                return sb.toString().trim();
            } catch (IOException e) {
                closed = true;
                e.printStackTrace();
                if (firstTry) denied = true;
            } catch (ArrayIndexOutOfBoundsException e) {
                denied = true;
            } catch (Exception e) {
                e.printStackTrace();
                denied = true;
            } finally {
                mLock.unlock();
            }
            return null;
        }

        public void runCommand(final String command,onCommandComplete complete,boolean isErrorEnable) {
            if (closed) return;
                mLock.lock();
                try {
                    String callback = "/shellCallback/";
                    String start="start";
                    String end="end";
                    mWriter.write(command + "\n");
                    mWriter.write("echo " + callback + "\n");
                    mWriter.write(end + "\n");
                    mWriter.flush();
                    String line;
                    String line1;
                    while ((line = mReader.readLine()) != null) {
                        if (line.equals(callback)) {
                            break;
                        }
                        complete.onOutput(line+"\n");
                    }
                    while ((line1=mError.readLine()) != null){
                        if (line1.contains(end)){
                            break;
                        }
                        complete.onCrash(line1);

                    }
                    complete.onComplete();
                    firstTry = false;
                    if (mTag != null) {
                        Log.i(mTag, "run: " + command + " output: ");
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    closed = true;
                    e.printStackTrace();
                    if (firstTry) denied = true;
                }catch (ArrayIndexOutOfBoundsException e) {
                    denied = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    denied = true;
                }finally {
                    mLock.unlock();
                }



        }
        public void runCommand(final String command,onCommandComplete onCommandComplete) {
            if (closed) return;
            try {
                mLock.lock();
                int count=0;
                StringBuilder sb = new StringBuilder();
                String callback = "/shellCallback/";
                mWriter.write(command + "\n");
                mWriter.write("echo " + callback + "\n");
                mWriter.flush();

                String line;
                while ((line = mReader.readLine()) != null) {
                    if (line.equals(callback)) {
                        //onCommandComplete.onInterrupt(line);
                        break;
                    }
                    sb.append(line).append("\n");
                }
                firstTry = false;
                if (mTag != null) {
                    Log.i(mTag, "run: " + command + " output: " + sb.toString().trim());
                }
                onCommandComplete.onComplete();
                return;
            } catch (IOException e) {
                closed = true;
                e.printStackTrace();
                if (firstTry) denied = true;
            } catch (ArrayIndexOutOfBoundsException e) {
                denied = true;
            } catch (Exception e) {
                e.printStackTrace();
                denied = true;
            } finally {
                mLock.unlock();
            }
            onCommandComplete.onComplete();
            //return null;
        }


        public void close() {
            try {
                try {
                    mLock.lock();
                    if (mWriter != null) {
                        mWriter.write("exit\n");
                        mWriter.flush();

                        mWriter.close();
                    }
                    if (mReader != null) {
                        mReader.close();
                    }
                    if (mError!=null) {
                        mError.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (mProcess != null) {
                    try {
                        mProcess.waitFor();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mProcess.destroy();
                    if (mTag != null) {
                        Log.i(mTag, Utils.strFormat("%s closed: %d",
                                mRoot ? "SU" : "SH", mProcess.exitValue()));
                    }
                }
            } finally {
                mLock.unlock();
                closed = true;
            }
        }

    }
    public interface OnCompleteListener{
        void onComplete();
        void onError();
    }

}
