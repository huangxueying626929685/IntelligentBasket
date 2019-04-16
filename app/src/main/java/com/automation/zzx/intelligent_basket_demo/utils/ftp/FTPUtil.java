package com.automation.zzx.intelligent_basket_demo.utils.ftp;

import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.Result;

/**
 * Created by pengchenghu on 2019/4/14.
 * Author Email: 15651851181@163.com
 * Describe: FTP上传/下载
 */

public class FTPUtil {
    private final static String TAG = "FTPUtil";
    /**
     * 服务器名.
     */
    private String ip;

    /**
     * 服务器端口号
     */
    private int port;

    /**
     * 用户名.
     */
    private String userName;

    /**
     * 密码.
     */
    private String password;

    /**
     * FTP连接.
     */
    private FTPClient ftpClient;

    /**
     * FTP列表.
     */
    private List<FTPFile> list;

    /**
     * FTP根目录.
     */
    public static final String BASE_REMOTE_PATH = "../var/ftp/nacelleRent";

    /**
     * FTP当前目录.
     */
    private String currentPath = "";

    /**
     * 统计流量.
     */
    private double response;

    /**
     * 构造函数.
     *
     * @param ip
     *            hostName 服务器名
     * @param user
     *            userName 用户名
     * @param pass
     *            password 密码
     */
    public FTPUtil(String ip, int port, String user, String pass) {
        this.ip = ip;
        this.port = port;
        this.userName = user;
        this.password = pass;
        this.ftpClient = new FTPClient();
        this.list = new ArrayList<FTPFile>();
    }

    /**
     * 打开FTP服务.
     *
     * @throws IOException
     */
    public boolean openConnect() throws IOException {
        // 中文转码
        ftpClient.setControlEncoding("UTF-8");
        int reply; // 服务器响应值
        // 连接至服务器
        ftpClient.connect(ip, port);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        }
        // 登录到服务器
        boolean login = ftpClient.login(userName, password);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        } else {
            // 获取登录信息
            FTPClientConfig config = new FTPClientConfig(ftpClient.getSystemType().split(" ")[0]);
            config.setServerLanguageCode("zh");
            ftpClient.configure(config);
            // 使用被动模式设为默认
            ftpClient.enterLocalPassiveMode();
            // 二进制文件支持
            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            // 更换当前工作目录到根目录下
            if(!ftpClient.changeWorkingDirectory(BASE_REMOTE_PATH)) {
                Log.i(TAG, "更换至根目录失败");
                return false;
            }
            Log.i(TAG, "更换至根目录成功：" + ftpClient.printWorkingDirectory());
        }

        return login;
    }

    /**
     * 关闭FTP服务.
     *
     * @throws IOException
     */
    public void closeConnect() throws IOException {
        if (ftpClient != null) {
            if (ftpClient.isConnected()) {
                // 登出FTP
                ftpClient.logout();
                // 断开连接
                ftpClient.disconnect();
                System.out.println("logout");
            }
        }
    }

    /**
     * 列出FTP下所有文件.
     *
     * @param remotePath
     *            服务器目录
     * @return FTPFile集合
     * @throws IOException
     */
    public List<FTPFile> listFiles(String remotePath) throws IOException {
        if (ftpClient != null) {
            // 获取文件
            try {
                FTPFile[] files = ftpClient.listFiles(remotePath);
                if (files != null && files.length > 0) {
                    // 遍历并且添加到集合
                    for (FTPFile file : files) {
                        list.add(file);
                    }
                }
            } catch (Exception e) {
                Log.e("TAG", "请稍等...");
            }
        }
        return list;
    }

    /**
     * 下载.
     *
     * @param remotePath
     *            FTP目录
     * @param fileName
     *            文件名
     * @param localPath
     *            本地目录
     * @return Result
     * @throws IOException
     */
    public void download(String remotePath, String fileName, String localPath) throws IOException {
        // 初始化FTP当前目录
        currentPath = remotePath;
        // 初始化当前流量
        response = 0;
        // 更换当前工作目录到工作目录下
        ftpClient.changeWorkingDirectory(currentPath);
        Log.i(TAG, "更换至工作目录：" + ftpClient.printWorkingDirectory());
        // 得到FTP当前目录下所有文件
        FTPFile[] ftpFiles = ftpClient.listFiles();
        // 循环遍历
        for (FTPFile ftpFile : ftpFiles) {
            // 找到需要下载的文件
            if (ftpFile.getName().equals(fileName)) {
                System.out.println("download...");
                // 创建本地目录
                File file = new File(localPath + "/" + fileName);
                // 下载前时间
                if (ftpFile.isDirectory()) {
                    // 下载多个文件
                    //downloadMany(file);
                } else {
                    // 下载当个文件
                    downloadSingle(file, ftpFile);
                }
            }
        }
    }

    /**
     * 下载单个文件.
     *
     * @param localFile
     *            本地目录
     * @param ftpFile
     *            FTP目录
     * @return true下载成功, false下载失败
     * @throws IOException
     */
    private boolean downloadSingle(File localFile, FTPFile ftpFile) throws IOException {
        boolean flag = true;
        // 创建输出流
        OutputStream outputStream = new FileOutputStream(localFile);
        // 统计流量
        response += ftpFile.getSize();
        // 下载单个文件
        flag = ftpClient.retrieveFile(localFile.getName(), outputStream);
        // 关闭文件流
        outputStream.close();
        return flag;
    }


    /*
     * pengchenghu 定制功能
     */
    // 初始化上传文件环境
    public void uploadingInit(String remotePath) throws IOException {
        // 初始化FTP当前目录
        currentPath = remotePath;
        // 初始化当前流量
        response = 0;
        // 二进制文件支持
        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        // 使用被动模式设为默认
        ftpClient.enterLocalPassiveMode();
        // 设置模式
        ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
        // 改变FTP目录
        ftpClient.changeWorkingDirectory(currentPath);
        Log.i(TAG, "更换至工作目录：" + ftpClient.printWorkingDirectory());
    }
    /**
     * 上传单个文件.
     *
     * @param localFile
     *            本地文件
     * @return true上传成功, false上传失败
     * @throws IOException
     */
    public void uploadingSingleRenameFile(File localFile, String fileName) throws IOException {
        // 创建输入流
        InputStream inputStream = new FileInputStream(localFile);
        // 统计流量
        response += (double) inputStream.available() / 1;
        // 上传单个文件
        ftpClient.storeFile(fileName, inputStream);
        inputStream.close();
    }
}