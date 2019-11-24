package com.kai.lktMode.selinux;

import com.kai.lktMode.root.RootFile;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.tool.util.local.ShellUtil;

public class SElinux {
    private static String path="/sys/fs/selinux/enforce";
    public static boolean isSupprot(){
        if (ShellUtil.isInstalled("setenforce"))
            return true;
        return new RootFile(path).exists();
    }
    public static boolean getEnable(){
        String result=RootUtils.runCommand("getenforce");
        if (result.equals("Permissive"))
            return false;
        else if (result.equals("Enforcing"))
            return true;
        else {
            return new RootFile(path).readFile().equals("0")?false:true;
        }
    }
    public static String setEnable(boolean enable){
        new RootFile(path).write(enable?"1":"0",false);
        return ShellUtil.modify(path,enable?"'1'":"'0'");
    }
}
