package com.huawei.antipoisoning.common.util;

import java.io.*;
import java.rmi.NoSuchObjectException;

public class AntiMainUtil {

    public static String execute1(String[] arguments) {
        BufferedReader reader = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(arguments);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
        /** 执行外部程序,并获取标准输出 */
        public static String execute(String[] cmd, String... encoding) {
        BufferedReader bReader = null;
        InputStreamReader sReader = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            /* 为"错误输出流"单独开一个线程读取之,否则会造成标准输出流的阻塞 */
            Thread t = new Thread(new InputStreamRunnable(p.getErrorStream(), "ErrorStream"));
            t.start();
            /* "标准输出流"就在当前方法中读取 */
            BufferedInputStream bis = new BufferedInputStream(p.getInputStream());

            if (encoding != null && encoding.length != 0) {
                sReader = new InputStreamReader(bis, encoding[0]);// 设置编码方式
            } else {
                sReader = new InputStreamReader(bis, "utf-8");
            }
            bReader = new BufferedReader(sReader);
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
    public static String getJsonContent(String resultPath, String fileName) throws NoSuchObjectException,IOException {
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
    public static String getTxtContent(String resultPath, String fileName) throws NoSuchObjectException,IOException {
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
