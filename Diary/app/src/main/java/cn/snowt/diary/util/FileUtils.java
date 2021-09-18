package cn.snowt.diary.util;

import android.os.Environment;

import org.litepal.LitePalApplication;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-01 13:12
 * @Description: 文件相关的操作
 */
public class FileUtils {

    private static long countSize = 0;

    /**
     * 将String保存到文件
     * 保存在本应用专属文件夹(Android/data)下
     * @param content 要保存的String
     * @param fileName 要保存的文件名字
     * @return
     */
    public static boolean saveAsFileWriter2(String content,String fileName) {
        boolean flag = false;
        FileWriter writer = null;
        try {
            File externalDirectory = LitePalApplication.getContext().getExternalFilesDir("");
            if (!externalDirectory.exists()){
                externalDirectory.mkdirs();
            }
            File file = new File(externalDirectory.getAbsolutePath() + "/" + fileName);
            writer = new FileWriter(file);
            writer.write(content);
            flag = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return flag;
    }

    /**
     * 将String保存到文件
     * 保存在应用外存主目录下的output文件夹下
     * @param content 要保存的String
     * @param fileName 要保存的文件名字
     * @return
     */
    public static boolean saveAsFileWriter(String content,String fileName) {
        boolean flag = false;
        FileWriter writer = null;
        try {
            File externalDirectory = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION + "output/");
            if (!externalDirectory.exists()){
                externalDirectory.mkdirs();
            }
            File file = new File(externalDirectory.getAbsolutePath() + "/" + fileName);
            writer = new FileWriter(file);
            writer.write(content);
            flag = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return flag;
    }

    /**
     * 获取文件夹的大小,10GB以上时以GB单位展示，1MB以下时以KB展示，其他为MB展示
     * @param path 文件夹路径
     * @return
     */
    public static String getStringForDirSize(String path){
        if(null==path || "".equals(path)){
            return "0KB";
        }
        return dirSizeToStr(getCountSize(path));
    }

    /**
     *  根据路径删除指定的目录或文件，无论存在与否
     *@param sPath  要删除的目录或文件
     *@return 删除成功返回 true，否则返回 false。
     */
    public static boolean deleteFolder(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 判断目录或文件是否存在
        if (!file.exists()) {
            return flag;
        } else {
            // 判断是否为文件
            if (file.isFile()) {
                return deleteFile(sPath);
            } else {  // 为目录时调用删除目录方法
                return deleteDirectory(sPath);
            }
        }
    }

    private static String dirSizeToStr(long size){
        countSize = 0;
        String result = "";
        if(size > (1024L *1024*1024*10)){
            //10GB以上显示
            long l = size / (1024L * 1024 * 1024 * 10);
            result = l+"GB";
        }else if( size > (1024*1024)){
            //1MB以上显示
            long l = size / (1024 * 1024);
            result = l+"MB";
        }else{
            //1KB以上显示
            long l = size / 1024;
            result = l+"KB";
        }
        return result;
    }

    private static void countDirSize(String path) {
        File pathFile = new File(path);
        String[] list = pathFile.list();
        if (pathFile.isDirectory()) {
            if (list != null) {
                for (String items : list) {
                    String subItem=path+File.separator+items;
                    //递归调用.
                    countDirSize(subItem);
                }
            }
        } else {
            countSize += pathFile.length();
        }
    }

    private static long getCountSize(String path) {
        countDirSize(path);
        return countSize;
    }

    private static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    private static boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag){
                    break;
                }
            } //删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag){
                    break;
                }
            }
        }
        if (!flag){
            return false;
        }
        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

}
