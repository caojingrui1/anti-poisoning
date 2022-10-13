/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.schedule;

import com.huawei.antipoisoning.business.entity.checkRule.RuleModel;
import com.huawei.antipoisoning.business.operation.CheckRuleOperation;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 定时获取最新的规则保存进数据库
 *
 * @author cqx
 * @since 2022/8/1614:23
 */
@Configuration
@Component
public class SaveCheckRule {
    private static final Logger logger = LoggerFactory.getLogger(SaveCheckRule.class);

    private static final String FILE_PATH = "C:\\code\\test\\防投毒规则.xlsx";

    @Autowired
    private CheckRuleOperation checkRuleOperation;

    /**
     * 遍历获取表格内容
     */
    @SneakyThrows
    public void getRuleFiles() {
        File file = new File(FILE_PATH);
        Workbook workbook = WorkbookFactory.create(file);
        // 获取sheet表的个数
        int sheets = workbook.getNumberOfSheets();
        for (int i = 0; i < sheets; i++) {
            List<RuleModel> ruleModels = new ArrayList<>();
            Sheet sheet = workbook.getSheetAt(i);
            // 获取总行数
            int rows = sheet.getPhysicalNumberOfRows();
            for (int j = 1; j < rows; j++) {
                Row row = sheet.getRow(j);
                // 获取总的列数
                int cells = row.getPhysicalNumberOfCells();
                if (cells != 7) {
                    logger.error("----table is error");
                }
                // 获取每一个单元格的内容
                RuleModel ruleModel = new RuleModel();
                String language = Objects.toString(row.getCell(0), null);
                if ("JS".equals(language)) {
                    language = "JavaScript";
                } else if ("C".equals(language)) {
                    language = "cpp";
                } else if ("JAVA".equals(language)) {
                    language = "Java";
                } else if ("PYTHON".equals(language)) {
                    language = "Python";
                }
                ruleModel.setRuleLanguage(language);
                ruleModel.setRuleName(Objects.toString(row.getCell(1)));
                ruleModel.setRuleDesc(Objects.toString(row.getCell(2), null));
                ruleModel.setFileIds(Objects.toString(row.getCell(3), null));
                ruleModel.setRightExample(Objects.toString(row.getCell(4), null));
                ruleModel.setErrorExample(Objects.toString(row.getCell(5), null));
                ruleModel.setReviseOpinion(Objects.toString(row.getCell(6), null));
                ruleModel.setRuleId(ruleModel.getRuleLanguage() + "_" + ruleModel.getRuleName());
                ruleModels.add(ruleModel);
            }
            // 将规则存入数据库
            checkRuleOperation.createRule(ruleModels);
            System.out.println(ruleModels);
        }
    }
}
