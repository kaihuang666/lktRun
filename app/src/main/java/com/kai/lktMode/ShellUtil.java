package com.kai.lktMode;

import android.util.Log;

import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ShellUtil {
    private Shell shell;
    private String output;
    boolean exit=false;
    private Process process;
    private ShellUtil(boolean su){
        try {
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static ShellUtil create(boolean su){
        return new ShellUtil(su);
    }
    public  Result command(String[] args){
        String result="";
        try {
            ProcessBuilder cmd=new ProcessBuilder();
            cmd.command("su");
            cmd.command(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[1024];
            while (in.read(re) != -1) {
                result+= new String(re).trim();
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return new Result(result,0);
    }
    public static int getIntFromFile(File file){
        int temp=100;
        try {
            FileInputStream fis = new FileInputStream(file);
            StringBuffer sbTemp = new StringBuffer("");

            // read file
            byte[] buffer = new byte[1024];
            while (fis.read(buffer) != -1) {
                sbTemp.append(new String(buffer));
            }
            fis.close();

            // parse int
            String sTemp = sbTemp.toString().trim();
            temp = Integer.parseInt(sTemp);
        }catch (Exception e){
            e.printStackTrace();
        }
        return temp;
    }
    public static String getStringFromFile(File file){
        String result="";
        try {
            FileInputStream fis = new FileInputStream(file);
            StringBuffer sbTemp = new StringBuffer("");

            // read file
            byte[] buffer = new byte[1024];
            while (fis.read(buffer) != -1) {
                sbTemp.append(new String(buffer));
            }
            fis.close();

            result=sbTemp.toString().trim();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public class Result{
        private String output;
        private int exitValue;
        Result(String output,int exitValue){
            this.output=output;
            this.exitValue=exitValue;
        }

        public int getExitValue() {
            return exitValue;
        }

        public String getOutput() {
            return output;
        }
    }
}
