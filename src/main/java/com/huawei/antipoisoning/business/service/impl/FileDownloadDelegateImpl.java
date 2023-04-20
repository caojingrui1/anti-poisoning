package com.huawei.antipoisoning.business.service.impl;

import com.huawei.antipoisoning.business.entity.checkrule.RuleModel;
import com.huawei.antipoisoning.business.operation.CheckRuleOperation;
import com.huawei.antipoisoning.common.util.ExcelUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * file方法实现类
 *
 * @author ly
 * @since 2021-10-18
 */
@Component
public class FileDownloadDelegateImpl {
    private static final Logger logger = LoggerFactory.getLogger(FileDownloadDelegateImpl.class);

    private static HashMap<Class, String[]> ruleTitle = new HashMap<>();

    static {

        ruleTitle.put(RuleModel.class,
                new String[]{"语言类型", "规则唯一路径", "检测点描述", "扫描文件后缀", "正确示例",
                        "错误示例", "修改建议", "规则状态", "标签"});
    }

    @Autowired
    private CheckRuleOperation checkRuleOperation;

    /**
     * webhook公共处理
     *
     * @param workbook workbook
     * @param type     type
     * @return workbookToBytes
     */
    private byte[] workbookToBytes(Workbook workbook, String type) {
        if (Objects.isNull(workbook)) {
            return new byte[0];
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            logger.error(" export {}s To File  error ", type);
            return new byte[0];
        }
    }

    /**
     * 导出语言规则集
     *
     * @return byte[]
     */
    public byte[] exportRuleByLanguage(RuleModel ruleModel) {
        List<RuleModel> ruleModels = checkRuleOperation.getAllPoisonRule(ruleModel);
        Workbook workbook = new XSSFWorkbook();
        ExcelUtil.export(workbook, ruleModels, "防投毒语言规则集", ruleTitle);
        return ExcelUtil.workbookToBytes(workbook, "rule");
    }
}
