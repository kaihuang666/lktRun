package com.kai.lktMode.tool.util.net;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.kai.lktMode.BuildConfig;
import com.kai.lktMode.R;
import com.kai.lktMode.activity.MainActivity;
import com.kai.lktMode.widget.CloudLoginDialog;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.tool.ZipUtils;
import com.kai.lktMode.webdav.WebDavFile;
import com.kai.lktMode.webdav.http.HttpAuth;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;



public class WebUtil {
    String versionLog;
    String versionName;
    public WebUtil(){
        //链接到酷安手动调度app地址
        try {
            Document document = Jsoup.connect("https://www.coolapk.com/apk/234953").get();
            Elements element=document.getElementsByClass("list_app_info");
            versionName=element.text();
            Elements elements=document.getElementsByClass("apk_left_title_info");
            versionLog=elements.get(0).html();
            versionLog=versionLog.replaceAll("<br>","\n");
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public String getVersionName() {
        return versionName;
    }

    public String getVersionLog() {
        return versionLog;
    }
    public int[] getCodes(String versionName){
        if (versionName==null){
            return null;
        }
        String[] codes=versionName.split("\\.");
        int[] codes_int=new int[codes.length+1];
        for (int i=0;i<codes.length;i++){
            String code=codes[i];
            if (code.contains("beta")){
                int start=code.indexOf("(");
                int end=code.indexOf(")");
                String beta=code.substring(start+5,end);
                code=code.substring(0,start);
                codes_int[i]= Integer.valueOf(code);
                codes_int[i+1]=Integer.valueOf(beta);
            }else {
                codes_int[i]= Integer.valueOf(code);
            }
        }
        return codes_int;
    }
    public boolean isToUpdate(){
        boolean update=false;
        int[] codes_web=getCodes(getVersionName());
        if (codes_web==null){
            return false;
        }
        int[] codes_local=getCodes(BuildConfig.VERSION_NAME);
        int length=(codes_local.length<codes_web.length?codes_local.length:codes_web.length);
        for (int i=0;i<length;i++){
            if (codes_local[i]>codes_web[i]){
                return false;
            }else if (codes_local[i]==codes_web[i]){
                continue;
            }else if (codes_local[i]<codes_web[i]){
                return true;
            }
        }
        if (!update){
            update=codes_local.length<codes_web.length;
        }
        return update;
    }
    public static void login(final String user, final String password, final CloudLoginDialog dialog, final Activity context){
        final String website="https://dav.jianguoyun.com/dav/";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpAuth.setAuth(user, password);
                    WebDavFile webDavFiles = new WebDavFile(website+"lktMode");
                    if (webDavFiles.makeAsDir()){
                        Preference.save(context,"username",user);
                        Preference.save(context,"password",password);
                        Preference.save(context,"cloud",true);
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context,"登录成功",Toast.LENGTH_SHORT).show();
                                list(context);
                            }
                        });
                        dialog.dismiss();
                    }else {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context,"登录失败，请检查邮箱密码是否正确",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }catch (final Exception e){
                    e.printStackTrace();
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        }).start();

    }
    public static Object getPrefrence(Activity activity,String key,String type){
        return Preference.get(activity,key,type);

    }
    public static void list(final Activity activity){
        new Thread(new Runnable() {
            String filesPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode";
            List<String> items=new ArrayList<>();
            List<String> selected=new ArrayList<>();
            @Override
            public void run() {
                try {
                    String website="https://dav.jianguoyun.com/dav";
                    String user=(String)Preference.get(activity,"username","String");
                    String password=(String)Preference.get(activity,"password","String");
                    HttpAuth.setAuth(user, password);
                    List<WebDavFile> webDavFiles = new WebDavFile(website+"/lktMode").listFiles();
                    for (WebDavFile f:webDavFiles){
                        if (!f.getDisplayName().contains(".zip"))
                            continue;
                        items.add(f.getDisplayName());
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    items.add("创建备份");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog dialog=new AlertDialog.Builder(activity, R.style.AppDialog)
                                    .setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            int last=items.size()-1;
                                            if (i==last){
                                                try {
                                                    delFolder(filesPath+"/backup");
                                                    //将自定义调度的所有代码备份到本地
                                                    for (int index=1;index<=6;index++){
                                                        try {
                                                            File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/backup/");
                                                            if (!file.mkdirs()&&!file.exists()){
                                                                return;
                                                            }
                                                            BufferedWriter writer=new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode/backup/"+"code"+index+".sh"));
                                                            writer.write((String) Preference.get(activity,"code"+index,"String"));
                                                            writer.flush();
                                                            writer.close();

                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                        }

                                                    }
                                                    //将不需要权限的所有参数都备份到本地
                                                    Properties properties=new Properties();
                                                    properties.setProperty("autoBoot",String.valueOf(getPrefrence(activity,"autoBoot","Boolean")));
                                                    properties.setProperty("autoLock",String.valueOf(getPrefrence(activity,"autoLock","Boolean")));
                                                    properties.setProperty("custom",String.valueOf(getPrefrence(activity,"custom","Boolean")));
                                                    properties.setProperty("autoClean",String.valueOf(getPrefrence(activity,"autoClean","Boolean")));
                                                    properties.setProperty("gameMode",String.valueOf(getPrefrence(activity,"gameMode","Boolean")));
                                                    properties.setProperty("imClean",String.valueOf(getPrefrence(activity,"imClean","Boolean")));
                                                    properties.setProperty("games",String.valueOf(getPrefrence(activity,"games","StringSet")));
                                                    properties.setProperty("softwares",String.valueOf(getPrefrence(activity,"softwares","StringSet")));
                                                    properties.setProperty("default",String.valueOf(getPrefrence(activity,"default","int")));
                                                    //将自定义调度备份到备份文件夹内
                                                    try {
                                                        MainActivity.copyFile(filesPath+"/powercfg/powercfg.sh",filesPath+"/backup/powercfg.sh");
                                                    }catch (Exception e){
                                                        e.printStackTrace();
                                                    }
                                                    FileOutputStream out=new FileOutputStream(new File(filesPath+"/backup/backup.properties"));
                                                    properties.store(out,"Comment");
                                                    out.close();
                                                    //将整个备份文件夹压缩
                                                    ZipUtils.zip(filesPath+"/backup/",filesPath+"/backup.zip");
                                                    backup(filesPath+"/backup.zip",activity);
                                                }catch (Exception e){
                                                    e.printStackTrace();
                                                }

                                            }else {
                                                delFolder(filesPath+"/restore");
                                                restore(items.get(i),activity);
                                            }
                                        }
                                    })
                                    .setNegativeButton("删除备份", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();

                                            items.remove(items.size()-1);
                                            new AlertDialog.Builder(activity,R.style.AppDialog)
                                                    .setTitle("删除备份")
                                                    .setMultiChoiceItems(items.toArray(new String[items.size()]), new boolean[items.size()], new DialogInterface.OnMultiChoiceClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                                            if (b)
                                                                selected.add(items.get(i));
                                                            else
                                                                selected.remove(items.get(i));
                                                        }
                                                    })
                                                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            delete(selected.toArray(new String[selected.size()]),activity);
                                                        }
                                                    })
                                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                                    .create().show();
                                        }
                                    })
                                    .setPositiveButton("注销账号", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Preference.save(activity,"cloud",false);
                                        }
                                    })
                                    .setTitle("选择备份")
                                    .show();
                        }
                    });
                }

            }
        }).start();

    }
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void restore(final String webpath, final Activity activity){
        final String filesPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode";
        new Thread(new Runnable() {
            @Override
            public void run() {
                String user=(String)Preference.get(activity,"username","String");
                String password=(String)Preference.get(activity,"password","String");
                String website="https://dav.jianguoyun.com/dav";
                try {
                    //准备好本地用来接受的文件夹
                    File file=new File(filesPath);
                    if (!file.exists()){
                        file.mkdirs();
                    }
                    //将云端备份下载到本地
                    HttpAuth.setAuth(user, password);
                    WebDavFile webDavFile=new WebDavFile(website+"/lktMode/"+webpath);
                    boolean isSuccess=webDavFile.download(filesPath+"/restore.zip",true);
                    if (isSuccess) {
                        ZipUtils.unZip(filesPath+"/restore.zip",filesPath+"/restore/");
                        restoreProp(activity);
                    }else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity,"下载备份失败，请检查网络",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }
    public static void restoreProp(final Activity activity){
        final String filesPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/lktMode";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Properties properties=new Properties();
                    FileInputStream in=new FileInputStream(new File(filesPath+"/restore/backup.properties"));
                    properties.load(in);
                    in.close();
                    Preference.save(activity,"autoBoot",Boolean.valueOf(properties.getProperty("autoBoot","false")));
                    Preference.save(activity,"custom",Boolean.valueOf(properties.getProperty("custom","false")));
                    Preference.save(activity,"autoLock",Boolean.valueOf(properties.getProperty("autoLock","false")));
                    Preference.save(activity,"autoClean",Boolean.valueOf(properties.getProperty("autoClean","false")));
                    Preference.save(activity,"imClean",Boolean.valueOf(properties.getProperty("imClean","false")));
                    Preference.save(activity,"gameMode",Boolean.valueOf(properties.getProperty("gameMode","false")));
                    String[] games=properties.getProperty("games").replaceAll("\\[|\\]","").split(", ");
                    Preference.save(activity,"games",Arrays.asList(games));
                    String[] softwares=properties.getProperty("softwares").replaceAll("\\[|\\]","").split(", ");
                    Preference.save(activity,"softwares",Arrays.asList(softwares));
                    Preference.save(activity,"default",Integer.valueOf(properties.getProperty("default","0")));
                    for (int i=1;i<=6;i++){
                        MainActivity.restore(activity,"code"+i,"code"+i+".sh");
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity,"恢复完成",Toast.LENGTH_SHORT).show();
                            MainActivity activity1=(MainActivity)activity;
                            activity1.getFragment(0).Refresh();
                            activity1.getFragment(1).Refresh();
                            activity1.getFragment(2).Refresh();
                            activity1.getFragment(3).Refresh();
                            activity1.getFragment(4).Refresh();
                            activity1.getFragment(5).Refresh();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();

    }
    public static void delete(final String[] paths, final Activity activity){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String user=(String)Preference.get(activity,"username","String");
                String password=(String)Preference.get(activity,"password","String");
                String website="https://dav.jianguoyun.com/dav";
                try {
                    HttpAuth.setAuth(user, password);
                    List<WebDavFile> webDavFiles = new WebDavFile(website+"/lktMode").listFiles();
                    for (String path:paths){
                        new WebDavFile(website+"/lktMode/"+path).delete();
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity,"删除完成",Toast.LENGTH_SHORT).show();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }
    public static void backup(final String filePath, final Activity context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String website="https://dav.jianguoyun.com/dav";
                String user=(String)Preference.get(context,"username","String");
                String password=(String)Preference.get(context,"password","String");
                HttpAuth.setAuth(user, password);
                try {
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH)+1;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int hour=calendar.get(Calendar.HOUR);
                    int minute=calendar.get(Calendar.MINUTE);
                    //sardine.(website+"lktMode/code1.sh");
                    WebDavFile webDavFile=new WebDavFile(website+"/lktMode/backup-"+year+"-"+month+"-"+day+"-"+hour+"-"+minute+".zip");
                    if (webDavFile.upload(filePath)){
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context,"已备份到云端",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context,"备份失败，请检查网络",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();


    }
}
