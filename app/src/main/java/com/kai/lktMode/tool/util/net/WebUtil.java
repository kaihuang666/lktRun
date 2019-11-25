package com.kai.lktMode.tool.util.net;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.kai.lktMode.BuildConfig;
import com.kai.lktMode.R;
import com.kai.lktMode.activity.MainActivity;
import com.kai.lktMode.bean.Sdcard;
import com.kai.lktMode.bean.SystemInfo;
import com.kai.lktMode.tool.ToastUtil;
import com.kai.lktMode.widget.CloudLoginDialog;
import com.kai.lktMode.tool.Preference;
import com.kai.lktMode.tool.ZipUtils;
import com.kai.lktMode.webdav.WebDavFile;
import com.kai.lktMode.webdav.http.HttpAuth;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    /* @author suncat
     * @category 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     * @return
     */
    public static final boolean isNetworkAccess() {

        String result = null;
        try {
            String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.d("------ping-----", "result content : " + stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            Log.d("----result---", "result = " + result);
        }
        return false;
    }
    public static String getRealUrl(String url){
        try {
            Document document = Jsoup.connect(url)
                    .header("User-Agent","Mozilla/5.0 (Android 4.4; Mobile; rv:41.0) Gecko/41.0 Firefox/41.0")
                    .get();
            String js=document.getElementsByTag("script").get(0).html();
            Log.d("js",js);
            return extractUrl(js);
        }catch (IOException e){
            e.printStackTrace();
        }
        return "";
    }
    public static String extractUrl(String js){
        String urlheader=match("(https?://[^']+)",js);
        String urltail=match("(\\?[^\"]+)",js);
        return (urlheader+urltail);
    }
    public static String match(String m,String p){
        Pattern pattern=Pattern.compile(m);
        Matcher matcher=pattern.matcher(p);
        if (matcher.find()){
            return matcher.group(1);
        }
        return "";
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
        if (BuildConfig.BUILD_TYPE.equals("debug"))
            return false;
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
    public static boolean isDonation(Context context) throws Exception{
        final String website="https://dav.jianguoyun.com/dav/donation/";
        HttpAuth.setAuth("1354268264@qq.com", "kai20134548");
        String username=Preference.getString(context,"username");
        WebDavFile webDavFiles = new WebDavFile(website+username+".txt");
        if (webDavFiles.exists()){
            SystemInfo.setIsDonated(true);
            return true;
        }
        else{
            SystemInfo.setIsDonated(false);
            return false;
        }

    }
    public static void login(final String user, final String password, final CloudLoginDialog dialog, final Activity context,boolean list){
        final String website="https://dav.jianguoyun.com/dav/";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpAuth.setAuth(user, password);
                    WebDavFile webDavFiles = new WebDavFile(website+"lktMode");
                    if (webDavFiles.makeAsDir()){
                        Preference.saveString(context,"username",user);
                        Preference.saveString(context,"password",password);
                        Preference.saveBoolean(context,"cloud",true);
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.shortShow(context,"登录成功");
                                MainActivity activity=(MainActivity)context;
                                activity.refreshLogin();
                                activity.refreshLoginV1();
                                if (list)
                                    list(context);
                            }
                        });
                        if (dialog!=null)
                            dialog.dismiss();
                    }else {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.shortShow(context,"登录失败");
                            }
                        });
                    }
                }catch (final Exception e){
                    e.printStackTrace();
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.shortAlert(context,e.toString());
                        }
                    });

                }

            }
        }).start();

    }

    public static void list(final Activity activity){
        ProgressDialog progressDialog=new ProgressDialog(activity,R.style.AppDialog);
        progressDialog.setMessage("加载中");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(new Runnable() {
            String filesPath= Sdcard.getPath(activity) +"/lktMode";
            List<String> items=new ArrayList<>();
            List<String> selected=new ArrayList<>();
            @Override
            public void run() {
                try {
                    String website="https://dav.jianguoyun.com/dav";
                    String user=(String)Preference.getString(activity,"username");
                    String password=(String)Preference.getString(activity,"password");
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
                            progressDialog.dismiss();
                            final AlertDialog dialog=new AlertDialog.Builder(activity, R.style.AppDialog)
                                    .setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            int last=items.size()-1;
                                            if (i==last){
                                                if (i>=1&&!SystemInfo.getIsDonated()){
                                                    ToastUtil.shortAlert(activity,"非捐贈版只支持1个备份");
                                                    return;
                                                }
                                                try {
                                                    delFolder(filesPath+"/backup");
                                                    //将自定义调度的所有代码备份到本地
                                                    for (int index=1;index<=6;index++){
                                                        try {
                                                            File file=new File(Sdcard.getPath(activity)+"/lktMode/backup/");
                                                            if (!file.mkdirs()&&!file.exists()){
                                                                return;
                                                            }
                                                            BufferedWriter writer=new BufferedWriter(new FileWriter(Sdcard.getPath(activity)+"/lktMode/backup/"+"code"+index+".sh"));
                                                            writer.write((String) Preference.getString(activity,"code"+index));
                                                            writer.flush();
                                                            writer.close();

                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                        }

                                                    }
                                                    //将不需要权限的所有参数都备份到本地
                                                    Properties properties=new Properties();
                                                    properties.setProperty("autoBoot",String.valueOf(Preference.getBoolean(activity,"autoBoot")));
                                                    properties.setProperty("autoLock",String.valueOf(Preference.getBoolean(activity,"autoLock")));
                                                    properties.setProperty("custom",String.valueOf(Preference.getBoolean(activity,"custom")));
                                                    properties.setProperty("autoClean",String.valueOf(Preference.getBoolean(activity,"autoClean")));
                                                    properties.setProperty("gameMode",String.valueOf(Preference.getBoolean(activity,"gameMode")));
                                                    properties.setProperty("imClean",String.valueOf(Preference.getBoolean(activity,"imClean")));
                                                    properties.setProperty("games",String.valueOf(Preference.getStringSet(activity,"games")));
                                                    properties.setProperty("softwares",String.valueOf(Preference.getStringSet(activity,"softwares")));
                                                    properties.setProperty("default",String.valueOf(Preference.getInt(activity,"default")));
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
                                            Preference.saveBoolean(activity,"cloud",false);
                                            Preference.saveString(activity,"username","");
                                            Preference.saveString(activity,"password","");
                                            MainActivity mainActivity=(MainActivity)activity;
                                            ((MainActivity) activity).refreshLogin();
                                            ((MainActivity) activity).refreshLoginV1();
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
        final String filesPath=Sdcard.getPath(activity)+"/lktMode";
        ProgressDialog dialog=new ProgressDialog(activity,R.style.AppDialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("正在恢复");
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String user=Preference.getString(activity,"username");
                String password=Preference.getString(activity,"password");
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
                        restoreProp(activity,dialog);
                    }else {

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                ToastUtil.shortAlert(activity,"下载备份失败，请检查网络");
                            }
                        });
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }
    public static void restoreProp(final Activity activity,ProgressDialog dialog) throws Exception{
        final String filesPath=Sdcard.getPath(activity)+"/lktMode";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Properties properties=new Properties();
                    FileInputStream in=new FileInputStream(new File(filesPath+"/restore/backup.properties"));
                    properties.load(in);
                    in.close();
                    Preference.saveBoolean(activity,"autoBoot",Boolean.valueOf(properties.getProperty("autoBoot","false")));
                    Preference.saveBoolean(activity,"custom",Boolean.valueOf(properties.getProperty("custom","false")));
                    Preference.saveBoolean(activity,"autoLock",Boolean.valueOf(properties.getProperty("autoLock","false")));
                    Preference.saveBoolean(activity,"autoClean",Boolean.valueOf(properties.getProperty("autoClean","false")));
                    Preference.saveBoolean(activity,"imClean",Boolean.valueOf(properties.getProperty("imClean","false")));
                    Preference.saveBoolean(activity,"gameMode",Boolean.valueOf(properties.getProperty("gameMode","false")));
                    String[] games=properties.getProperty("games").replaceAll("\\[|\\]","").split(", ");
                    Preference.saveList(activity,"games",Arrays.asList(games));
                    String[] softwares=properties.getProperty("softwares").replaceAll("\\[|\\]","").split(", ");
                    Preference.saveList(activity,"softwares",Arrays.asList(softwares));
                    Preference.saveInt(activity,"default",Integer.valueOf(properties.getProperty("default","0")));
                    for (int i=1;i<=6;i++){
                        MainActivity.restore(activity,"code"+i,"code"+i+".sh");
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            ToastUtil.shortShow(activity,"恢复完成");
                            MainActivity activity1=(MainActivity)activity;
                            activity1.getFragment(0).Refresh();
                            activity1.getFragment(1).Refresh();
                            activity1.getFragment(2).Refresh();
                            activity1.getFragment(3).Refresh();
                            activity1.getFragment(4).Refresh();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();

    }
    public static void delete(final String[] paths, final Activity activity){
        ProgressDialog dialog=new ProgressDialog(activity,R.style.AppDialog);
        dialog.setCancelable(false);
        dialog.setMessage("正在从云端删除");
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String user=Preference.getString(activity,"username");
                String password=(String)Preference.getString(activity,"password");
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
                            dialog.cancel();
                            ToastUtil.shortShow(activity,"删除完成");
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }
    public static void backup(final String filePath, final Activity context){
        ProgressDialog dialog=new ProgressDialog(context,R.style.AppDialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("备份中");
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String website="https://dav.jianguoyun.com/dav";
                String user=Preference.getString(context,"username");
                String password=(String)Preference.getString(context,"password");
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
                                dialog.dismiss();
                                ToastUtil.shortShow(context,"已备份到云端");
                            }
                        });
                    }else {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                ToastUtil.shortShow(context,"备份失败，请检查网络");
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
