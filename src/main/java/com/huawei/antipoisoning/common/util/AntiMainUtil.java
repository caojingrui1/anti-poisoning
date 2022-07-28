package com.huawei.antipoisoning.common.util;

import java.io.*;

public class AntiMainUtil {

        /** 执行外部程序,并获取标准输出 */
        public static String execute(String[] cmd, String... encoding) {
        BufferedReader bReader = null;
        InputStreamReader sReader = null;
        try {
           // Runtime.getRuntime().exec(new String[]{"/bin/sh","-c", cmds});
            Process p = Runtime.getRuntime().exec(cmd);

            /* 为"错误输出流"单独开一个线程读取之,否则会造成标准输出流的阻塞 */
         //   Thread t = new Thread(new InputStreamRunnable(p.getErrorStream(), "ErrorStream"));
         //   t.start();


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
    public static void main(String[] args) {
//        String command1 = "export JOERN_HOME=/opt/sscs/joern-cli/" ;
//        String command2 = "export JAVA_HOME=/usr/lib/jvm/java-1.11.0-openjdk-amd64 ";
//        System.out.println("test export : AntiMainUtil.execute(new String[] {\"/bin/sh\",\"-c\",command1})) ====/r/n "+AntiMainUtil.execute(new String[] {"/bin/sh","-c",command1}));
//        System.out.println("test export : AntiMainUtil.execute(new String[] {\"/bin/sh\",\"-c\",command2})) ====/r/n "+AntiMainUtil.execute(new String[] {"/bin/sh","-c",command2}));
      //  System.out.println("test two : AntiMainUtil.execute(new String[] {command1}));====/r/n  "+AntiMainUtil.execute(new String[] {command1}));
        String[] arguments = new String[] {"/bin/sh","-c","time /usr/local/bin/python3 /opt/sscs/SoftwareSupplyChainSecurity-release-openeuler/openeuler_scan.py "
                +"/usr/test/openeuler-os-build-master " + "/usr/result/openeuler-os-build-new.json " +
                "--enable-python"};
      //  AntiMainUtil.execute(arguments);
       System.out.println(AntiMainUtil.execute(arguments));
       String result = getJsonContent("openeuler-os-build-new","/usr/result");
       System.out.println(result);
        //判断文件存在 读取文件 返回json
    }

    /**
     * 获取py文件生成的json文件信息.
     *
     * @param fileName json文件名称
     * @param workspace json文件位置
     */
    public static String getJsonContent(String fileName, String workspace) {
        String jsonStr = "";
        try {
            File file = new File(workspace + File.separator + fileName + ".json");
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
        } catch (Exception e) {
            e.printStackTrace();
            return jsonStr;
        }
    }
}
