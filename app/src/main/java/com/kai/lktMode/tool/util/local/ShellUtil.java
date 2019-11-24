package com.kai.lktMode.tool.util.local;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.kai.lktMode.bean.Sdcard;
import com.kai.lktMode.fragment.MainFragment;
import com.kai.lktMode.root.RootFile;
import com.kai.lktMode.root.RootUtils;
import com.kai.lktMode.widget.TerminalDialog;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import org.jsoup.internal.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShellUtil {
    private Shell shell;
    private String output;
    boolean exit=false;
    private Process process;
    public static void writeFile(String path, String text, boolean append, boolean asRoot) {
        if (asRoot) {
            new RootFile(path).write(text, append);
            return;
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(path, append);
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            Log.e("xs", "Failed to write " + path);
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readFile(String file) {
        return readFile(file, true);
    }

    public static String readFile(String file, boolean root) {
        return readFile(file, root ? RootUtils.getSU() : null);
    }
    public static boolean isMagiskInstall(){
        return isInstalled("magisk");
    }
    public static String getMagiskVersion(){
        if (!isMagiskInstall())
            return "";
        String result=command(new String[]{"magisk","-v"});
        Pattern pattern=Pattern.compile("[\\d.]+");
        Matcher matcher=pattern.matcher(result);
        if (matcher.find()){
            return matcher.group();
        }
        return "";
    }
    public static String readFile(String file, RootUtils.SU su) {
        if (su != null) return new RootFile(file, su).readFile();

        StringBuilder s = null;
        FileReader fileReader = null;
        BufferedReader buf = null;
        try {
            fileReader = new FileReader(file);
            buf = new BufferedReader(fileReader);

            String line;
            s = new StringBuilder();
            while ((line = buf.readLine()) != null) s.append(line).append("\n");
        } catch (FileNotFoundException ignored) {
            Log.e("", "File does not exist " + file);
        } catch (IOException e) {
            Log.e("", "Failed to read " + file);
        } finally {
            try {
                if (fileReader != null) fileReader.close();
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return s == null ? null : s.toString().trim();
    }
    public static int[] reverse(int[] arr){
        for(int min =0,max=arr.length-1;min<max;min++,max--){
            //对数组的元素进行位置交换
            int temp=arr[min]; //定义了一个什么都没有的变量 保存下标为min的元素 然后min就空了
            arr[min]=arr[max];
            arr[max]=temp;
        }
        return arr;
    }

    public static String command(String[] args){
        String result="";
        try {
            ProcessBuilder cmd=new ProcessBuilder();
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
    public static boolean existFile(String file) {
        return existFile(file, true);
    }

    public static boolean existFile(String file, boolean root) {
        return existFile(file, root ? RootUtils.getSU() : null);
    }

    public static boolean existFile(String file, RootUtils.SU su) {
        return su == null ? new File(file).exists() : new RootFile(file, su).exists();
    }
    public static boolean isInstalled(String binary){
        String out=RootUtils.runCommand("which "+binary);
        if (out.isEmpty())
            return false;
        return true;
    }
    public static String chown(String path,String grant){
        return "chown -R "+grant+" "+path+"\n";
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
    public static String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    public static String modify(String path,String value){
        RootFile file=new RootFile(path);
        if (!file.exists())
            return "";
        //chown(path);
        StringBuilder builder=new StringBuilder();
        //授予读写权限
        builder.append("chmod 644 "+path+"\n");
        builder.append("echo "+value+">"+path+"\n");
        return builder.toString();
    }
    public static String lock(String path,String value){
        RootFile file=new RootFile(path);
        if (!file.exists())
            return "";
        StringBuilder builder=new StringBuilder();
        //授予读写权限
        builder.append("chmod 644 "+path+"\n");
        builder.append("echo "+value+">"+path+"\n");
        builder.append("chmod 444 "+path+"\n");
        return builder.toString();
    }
    public static int getIntFromFile(File file){
        RootFile rootFile=new RootFile(file.getAbsolutePath());
        if (!rootFile.exists())
            return 0;
        return Integer.valueOf(rootFile.readFile());
    }
    public static String[] concat(String[] a, String[] b) {
        String[] c= new String[a.length+b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
    public static int[] concat(int[] a, int[] b) {
        int[] c= new int[a.length+b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
    public static String getStringFromFile(File file){
        RootFile rootFile=new RootFile(file.getAbsolutePath());
        if (!rootFile.exists())
            return "";
        return rootFile.readFile();
    }

    private static void installBinary(Activity activity,File binary){
        if (!binary.exists()){
            try {
                binary.getParentFile().mkdirs();
                InputStream inputStream=activity.getResources().getAssets().open("update-binary");
                DataInputStream in=new DataInputStream(inputStream);
                BufferedWriter writer=new BufferedWriter(new FileWriter(binary));
                String line=null;
                while ((line=in.readLine())!=null){
                    writer.write(line+"\n");
                }
                writer.flush();
                writer.close();
                in.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    public static String getChown(String path){
        try {
            String[] chown=RootUtils.runCommand("ls -l "+path).split("\\s");
            int index=0;
            for (int i=0;i<chown.length;i++){
                String c=chown[i];
                if (c.contains("-")){
                    index=i;
                    break;
                }
            }
            return chown[index+2]+":"+chown[index+3];
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }

    }
    public static void installWithMagisk(Activity activity, String zip){
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final TerminalDialog dialog=new TerminalDialog(activity);
                dialog.setCancelable(false);
                dialog.show();
                dialog.addText("正在下载安装脚本\n");
                File binary=new File(Sdcard.getPath(activity) +"/lktMode/bin/update-binary");
                installBinary(activity,binary);
                try {
                    dialog.addText("脚本下载完成\n开始安装\n");
                    RootTools.getShell(true).add(new Command(7,"sh "+binary.getAbsolutePath()+"  2 3 "+zip){
                        @Override
                        public void commandOutput(int id, String line) {
                            super.commandOutput(id, line);
                            Log.d("line",line);
                            dialog.addText(line);
                            if (line.contains("Please install the latest Magisk")){
                                dialog.addText("检测到你未安装magisk或magisk过低，请安装最新版本的magisk框架");
                                dialog.addText("安装过程中断");
                                dialog.setPositive("关闭", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });
                            }
                            if (line.contains("Press a Vol Key")){
                                dialog.addText("请点击音量上键或下键");
                            }
                            if (line.contains("Press Vol Up")){
                                dialog.addText("请点击音量上键");
                            }
                            if (line.contains("Press Vol Down")){
                                dialog.addText("请点击音量下键");
                            }
                            if (line.contains("Please choose tweaks mode")){
                                dialog.addText("请点击音量上键或下键来选择模式");
                            }
                            if (line.contains("No such file")){
                                dialog.addText("安装完成");
                                dialog.setPositive("重启", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        RootUtils.runCommand("reboot");
                                    }
                                });
                            }
                        }

                        @Override
                        public void commandCompleted(int id, int exitcode) {
                            super.commandCompleted(id, exitcode);
                            if (exitcode==-1){
                                dialog.addText("安装过程中断");
                                dialog.setPositive("关闭", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });
                                return;
                            }
                            dialog.addText("安装完成");
                            dialog.setPositive("重启", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    RootUtils.runCommand("reboot");
                                }
                            });
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

}
