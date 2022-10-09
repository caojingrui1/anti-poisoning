package com.huawei.antipoisoning.common.util;

import com.huawei.antipoisoning.business.service.impl.AntiServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

/**
 * 防投毒工具执行类。
 *
 * @author prk
 * @since 2022-07-20
 */
public class AntiMainUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AntiMainUtil.class);

    /** 执行外部程序,并获取标准输出 */
    public static String execute(String[] cmd, String... encoding) throws IOException {
        String encode = "utf-8";
        if (encoding != null && encoding.length != 0) {
            encode =  encoding[0];// 设置编码方式
        }
        Process p = Runtime.getRuntime().exec(cmd);
        try (BufferedInputStream bis = new BufferedInputStream(p.getInputStream());
             InputStreamReader sReader = new InputStreamReader(bis, encode);
             BufferedReader bReader = new BufferedReader(sReader);) {
            /* 为"错误输出流"单独开一个线程读取之,否则会造成标准输出流的阻塞 */
            Thread t = new Thread(new InputStreamRunnable(p.getErrorStream(), "ErrorStream"));
            t.setUncaughtExceptionHandler((tr, ex) -> LOGGER.error( "{} : {}", tr.getName(), ex.getMessage()));
            t.start();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bReader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            bReader.close();
            p.destroy();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取py文件生成的json文件信息.
     *
     * @param fileName json文件名称
     * @param resultPath json文件位置
     */
    public static String getJsonContent(String resultPath, String fileName) throws IOException {
        String jsonStr = "";
        File file = new File(resultPath + File.separator + fileName + ".json");
        FileReader fileReader = new FileReader(file);
        Reader reader = new InputStreamReader(new FileInputStream(file), "Utf-8");
        int ch = 0;
        StringBuffer sb = new StringBuffer();
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        fileReader.close();
        reader.close();
        jsonStr = sb.toString();
        return jsonStr;
    }

    /**
     * 获取py文件生成的json文件信息.
     *
     * @param fileName json文件名称
     * @param resultPath json文件位置
     */
    public static String getTxtContent(String resultPath, String fileName) throws IOException {
        String jsonStr = "";
        File file = new File(resultPath + fileName + ".txt");
        FileReader fileReader = new FileReader(file);
        Reader reader = new InputStreamReader(new FileInputStream(file),"Utf-8");
        int ch = 0;
        StringBuffer sb = new StringBuffer();
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        fileReader.close();
        reader.close();
        jsonStr = sb.toString();
        return jsonStr;
    }
}
