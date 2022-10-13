/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service;

import com.huawei.antipoisoning.business.entity.shield.ParamModel;
import com.huawei.antipoisoning.common.entity.MultiResponse;

/**
 * @author zhangshengnian zWX1067200
 * @since 2022/09/23 15:46
 */
public interface ProblemShieldService {
    MultiResponse applyAndAuditNumber(String userId, String scanId);

    MultiResponse auditPassRevoke(ParamModel paramModel);

    MultiResponse getResultDetail(String scanId, String userId, ParamModel paramModel);

    MultiResponse getScanReport(String userId, ParamModel paramModel);

    MultiResponse getScanResult(ParamModel paramModel);

    MultiResponse poisonApply(String userId, String login, ParamModel paramModel);

    MultiResponse problemAudit(ParamModel paramModel);

    MultiResponse problemRevoke(ParamModel paramModel);

    MultiResponse shieldReferral(ParamModel paramModel);

    MultiResponse getPoisoningSelect();
}
