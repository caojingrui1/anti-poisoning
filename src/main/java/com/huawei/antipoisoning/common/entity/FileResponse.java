package com.huawei.antipoisoning.common.entity;

import org.springframework.core.io.ByteArrayResource;

import java.util.Objects;

/**
 * file结果参数体
 *
 * @since 2022-06-02 16:30:00
 */
public class FileResponse extends ByteArrayResource {
    private String fileName;

    private String filename;

    /**
     * 结果参数返回
     *
     * @param byteArray   byteArray
     * @param description 描述
     */
    public FileResponse(byte[] byteArray, String description) {
        super(byteArray, description);
        this.filename = description;
    }

    /**
     * 设置setFileName
     *
     * @param fileName 文件名
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 得到文件名
     *
     * @return String
     */
    @Override
    public String getFilename() {
        return Objects.isNull(this.filename) ? "xxx.xlsx" : this.filename;
    }
}
