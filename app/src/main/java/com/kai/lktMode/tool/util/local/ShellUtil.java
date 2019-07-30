package com.kai.lktMode.tool.util.local;

import com.stericson.RootShell.execution.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ShellUtil {
    private Shell shell;
    private String output;
    boolean exit=false;
    private Process process;

    public static String command(String[] args){
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
        return result;
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
