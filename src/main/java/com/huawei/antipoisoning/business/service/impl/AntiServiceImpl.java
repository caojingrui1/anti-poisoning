package com.huawei.antipoisoning.business.service.impl;

import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import com.huawei.antipoisoning.common.util.AntiMainUtil;
import com.huawei.antipoisoning.common.util.Constants;
import com.huawei.antipoisoning.common.util.JGitUtil;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * vms接口服务实现类
 *
 * @since: 2022/5/30 16:22
 */
@Service("vmsService")
public class AntiServiceImpl implements AntiService {
    private static final String url = "http://apigw.04.huawei.com/api/vms/publicservices/vuln/notice/getvuln";
    private static final String workspace = "/usr/result";
//    @Autowired
//    VmsDataOperation vmsDataOperation;

//    /**
//     * 查询外部源漏洞
//     *
//     * @param vmsQueryInfoDTO 查询主体
//     * @return MultiResponse<List < VmsExternalCveSourceDTO>>
//     */
    @Override
    public MultiResponse<List<AntiEntity>> getSourceOut(AntiEntity antiEntity) {
//        MultiResponse multiResponse = getReturnResult(vmsQueryInfoDTO, AntiConstants.REQUEST_TYPE_SOURCE_OUT);
//        if (200 != multiResponse.getCode()) {
//            return multiResponse;
//        }
        List<AntiEntity> antiEntities = new ArrayList<>();
//        if (Objects.nonNull(multiResponse.getResult()) && multiResponse.getResult() instanceof List) {
//            vmsExternalCveSourceDTOS = (List<VmsExternalCveSourceDTO>) multiResponse.getResult();
//        }
        return MultiResponse.success(200, "success", antiEntities);
    }

    /**
     * 调用vms接口，获取信息
     *
     * @param vmsQueryInfoDTO 查询条件
     * @param requestType     请求类型
     * @return MultiResponse
     */
//    private MultiResponse getReturnResult(VmsQueryInfoDTO vmsQueryInfoDTO, String requestType) {
//        // 组装请求body
//        JSONObject bodyjson = new JSONObject();
//        bodyjson.put("requestType", requestType);
//        bodyjson.put("param", vmsQueryInfoDTO);
//        MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
//        RequestBody body = RequestBody.create(mediaType, String.valueOf(bodyjson));
//        Map<String, String> xhwData = getXHWData();
//        Response response = HttpRequestUtil.sendPost(url, body, xhwData);
//        // 解析返回数据
//        JSONObject jsonObject = HttpRequestUtil.getDataByResponse(url, response);
//        if (jsonObject == null) {
//            return MultiResponse.error(400, "no return value");
//        }
//        String success = Objects.nonNull(jsonObject.get("success")) ? jsonObject.get("success").toString() : "";
//        if (!"true".equals(success)) {
//            String message = Objects.nonNull(jsonObject.get("msg")) ? jsonObject.get("msg").toString() : "";
//            return MultiResponse.error(400, message);
//        }
//        return MultiResponse.success(200, "success", jsonObject.get("result"));
//    }

    /**
     * 执行漏洞
     *
     * @return MultiResponse
     */
    @Override
    public MultiResponse scanRepo(String repoName, String language) {

        //扫描指定仓库 下载后放入文件夹 扫描 产生报告
        String[] arguments = new String[] {"/bin/sh","-c","time /usr/local/bin/python3 /opt/sscs/SoftwareSupplyChainSecurity-release-openeuler/openeuler_scan.py "
                +"/usr/test/"+ repoName + "/usr/result/" + repoName + ".json " +
                "--enable-" + language};
        System.out.println(AntiMainUtil.execute(arguments));
        String result = AntiMainUtil.getJsonContent(repoName, workspace);
        System.out.println(result);
        // 获取执行时间
        return MultiResponse.success(200, "success", result);
    }
}
