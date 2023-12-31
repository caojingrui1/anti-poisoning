/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.business.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.antipoisoning.business.enmu.ConstantsArgs;
import com.huawei.antipoisoning.business.entity.AntiEntity;
import com.huawei.antipoisoning.business.entity.ResultEntity;
import com.huawei.antipoisoning.business.entity.TaskEntity;
import com.huawei.antipoisoning.business.entity.pr.PRAntiEntity;
import com.huawei.antipoisoning.business.entity.pr.PRResultEntity;
import com.huawei.antipoisoning.business.entity.pr.PRTaskEntity;
import com.huawei.antipoisoning.business.entity.pr.PullRequestInfo;
import com.huawei.antipoisoning.business.operation.AntiOperation;
import com.huawei.antipoisoning.business.operation.PoisonResultOperation;
import com.huawei.antipoisoning.business.operation.PoisonTaskOperation;
import com.huawei.antipoisoning.business.service.AntiService;
import com.huawei.antipoisoning.business.util.AntiConstants;
import com.huawei.antipoisoning.business.util.FileUtil;
import com.huawei.antipoisoning.business.util.YamlUtil;
import com.huawei.antipoisoning.common.entity.MultiResponse;
import com.huawei.antipoisoning.common.util.AntiMainUtil;
import com.huawei.antipoisoning.common.util.HttpUtil;
import com.huawei.antipoisoning.common.util.JGitUtil;
import com.huawei.antipoisoning.common.util.StreamConsumer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 下载仓库、扫描
 *
 * @since: 2022/5/30 16:22
 */
@Service
public class AntiServiceImpl implements AntiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AntiServiceImpl.class);

    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    private static final String GITLAB = "https://source.openeuler.sh";

    @Value("${git.username}")
    private String gitUser;

    @Value("${git.password}")
    private String gitPassword;

    @Value("${gitlab.username}")
    private String gitlabUser;

    @Value("${gitlab.password}")
    private String gitlabPass;

    @Autowired
    private AntiOperation antiOperation;

    @Autowired
    private PoisonTaskOperation poisonTaskOperation;

    @Autowired
    private PoisonResultOperation poisonResultOperation;

    /**
     * 执行版本级防投毒扫描
     *
     * @param uuid 任务id
     * @return MultiResponse
     */
    @Override
    public MultiResponse scanRepo(String uuid) {
        AntiEntity antiEntity = antiOperation.queryAntiEntity(uuid);
        TaskEntity taskEntity = poisonTaskOperation.queryTaskEntity(uuid);
        // 扫描指定仓库 下载后放入文件夹 扫描 产生报告
        if (null != antiEntity) {
            try {
                if (antiEntity.getIsDownloaded()) {
                    String[] arguments = versionScan(antiEntity.getRepoName(), antiEntity.getBranch(),
                            antiEntity.getRulesName(), uuid);
                    // 设置任务开始时间
                    long startTime = System.currentTimeMillis();
                    String taskStartTime = DATE_FORMAT.format(startTime);
                    taskEntity.setExecuteStartTime(taskStartTime);
                    // 工具执行
                    String sb = AntiMainUtil.execute(arguments);
                    // 保存日志内容
                    String url = YamlUtil.getToolPath() + AntiConstants.SCANTOOLFILE + "poison_logs"  + File.separator;
                    taskEntity.setLogs(AntiMainUtil.getTxtContent(url, uuid));
                    // 设置任务结束时间
                    long endTime = System.currentTimeMillis();
                    String taskEndTime = DATE_FORMAT.format(endTime);
                    taskEntity.setExecuteEndTime(taskEndTime);
                    String taskConsuming = (endTime - startTime) / 1000 + "s";
                    taskEntity.setTaskConsuming(taskConsuming);
                    // 设置总耗时
                    String downloadConsuming = taskEntity.getDownloadConsuming();
                    if (StringUtils.isNotBlank(taskConsuming) && StringUtils.isNotBlank(downloadConsuming)) {
                        long taskTime = Long.parseLong(taskConsuming.substring(0, taskConsuming.length() - 1));
                        long downloadTime = Long.parseLong(downloadConsuming.substring(0,
                                downloadConsuming.length() - 1));
                        long time = taskTime + downloadTime;
                        taskEntity.setTimeConsuming(time + "s");
                    }
                    String result = AntiMainUtil.getJsonContent(YamlUtil.getToolPath() +
                                    AntiConstants.SCANRESULTPATH, antiEntity.getRepoName());
                    LOGGER.info("The scan result is : {}", result);
                    List<ResultEntity> results = JSONArray.parseArray(result, ResultEntity.class);
                    // 扫描结果详情
                    for (ResultEntity resultEntity : results) {
                        insertVersionScanDetail(resultEntity, antiEntity, taskEntity, uuid);
                    }
                    // 扫描是否成功
                    antiEntity.setIsSuccess(true);
                    antiEntity.setIsPass(true);
                    // 结果计数
                    antiEntity.setResultCount(results.size());
                    if (results.size() > 0) {
                        int solveCount = poisonResultOperation.getCountByStatus("2", results.get(0).getScanId());
                        antiEntity.setSolveCount(solveCount);
                        antiEntity.setIssueCount(results.size() - solveCount);
                        if ((results.size() - solveCount) > 0) { // 未解决问题数大于0，扫描不通过
                            antiEntity.setIsPass(false);
                        }
                    } else {
                        antiEntity.setIssueCount(0);
                    }
                    // 执行成功  0：未执行、1：执行中、2：执行成功、3：执行失败
                    taskEntity.setExecutionStatus(2);
                    antiEntity.setTips("");
                    // 更新扫描结果
                    antiOperation.updateScanResult(antiEntity);
                    // 更新版本级结果
                    poisonTaskOperation.updateTask(antiEntity, taskEntity);
                    return MultiResponse.success(ConstantsArgs.CODE_SUCCESS, "success", results);
                } else {
                    // 扫描是否成功
                    antiEntity.setIsSuccess(false);
                    antiEntity.setIsPass(false);
                    // 原因
                    antiEntity.setTips("repo not Downloaded.");
                    taskEntity.setExecutionStatus(3);
                    antiOperation.updateScanResult(antiEntity);
                    poisonTaskOperation.updateTask(antiEntity, taskEntity);
                    return MultiResponse.error(ConstantsArgs.CODE_FAILED, "repoNotDownloaded error");
                }
            } catch (IOException e) {
                antiEntity.setIsSuccess(false);
                antiEntity.setIsPass(false);
                antiEntity.setTips(e.toString());
                taskEntity.setExecutionStatus(3);
                antiOperation.updateScanResult(antiEntity);
                poisonTaskOperation.updateTask(antiEntity, taskEntity);
                LOGGER.error(e.getMessage());
                return MultiResponse.error(ConstantsArgs.CODE_FAILED, "scan error : " + e.getCause());
            } finally {
                // 删除下载的代码仓
                FileUtil.deleteDirectory(YamlUtil.getToolPath() + AntiConstants.REPOPATH +
                        File.separator + antiEntity.getRepoName() +
                        "-" + antiEntity.getBranch());
            }
        } else {
            return MultiResponse.error(ConstantsArgs.CODE_FAILED, "scan error , task not exist!");
        }
    }

    /**
     * 保存版本级扫描详情数据。
     *
     * @param resultEntity 扫描详情
     * @param antiEntity 扫描信息
     * @param taskEntity 任务信息
     * @param uuid 扫描ID
     */
    public void insertVersionScanDetail(ResultEntity resultEntity, AntiEntity antiEntity,
                                        TaskEntity taskEntity, String uuid) {
        String status = countResultByHash(resultEntity, taskEntity);
        resultEntity.setProjectName(antiEntity.getProjectName());
        resultEntity.setRepoName(antiEntity.getRepoName());
        resultEntity.setBranch(antiEntity.getBranch());
        resultEntity.setStatus(status);
        resultEntity.setScanId(uuid);
        resultEntity.setTaskId(taskEntity.getTaskId());
        poisonResultOperation.insertResultDetails(resultEntity);
    }

    /**
     * 统计同一hash值的数据已被屏蔽条数，返回状态信息。
     *
     * @param resultEntity 详情数据信息
     * @param taskEntity 任务信息
     * @return int
     */
    public String countResultByHash(ResultEntity resultEntity, TaskEntity taskEntity) {
        int count = poisonResultOperation.getResultDetailByHash(resultEntity.getHash(),
                taskEntity.getTaskId());
        return  count > 0 ? "2" : "0";
    }

    /**
     * 执行门禁级防投毒扫描漏洞
     *
     * @param scanId 扫描任务ID
     * @param info pr信息
     * @return MultiResponse
     */
    @Override
    public MultiResponse scanPRFile(String scanId, PullRequestInfo info) {
        Map<String, Object> responseResult = new HashMap<>();
        PRAntiEntity prAntiEntity = antiOperation.queryPRAntiEntity(scanId);
        PRTaskEntity prTaskEntity = poisonTaskOperation.queryPRTaskEntity(scanId);
        // 扫描指定仓库 下载后放入文件夹 扫描 产生报告
        if (null != prAntiEntity) {
            try {
                if (prAntiEntity.getIsDownloaded()) {
                    String[] arguments = new String[]{"/bin/sh", "-c",
                            "python3 " + YamlUtil.getToolPath() + AntiConstants.SCANTOOLPATH +
                                    // 仓库下载后存放地址
                                    " " + YamlUtil.getToolPath() + AntiConstants.PR_REPOPATH + info.getWorkspace() +
                                    File.separator + "modify_dirs " +
                                    // 扫描完成后结果存放地址
                                    YamlUtil.getToolPath() + AntiConstants.PR_SCANRESULTPATH +
                                    prAntiEntity.getRepoName() + ".json " +
                                    // 支持多语言规则扫描
                                    "--custom-yaml " + YamlUtil.getToolPath() + AntiConstants.CONFIG_PATH +
                                    prAntiEntity.getRulesName() + " > " + YamlUtil.getToolPath() + AntiConstants.SCANTOOLFILE +
                                    "poison_logs" + File.separator + scanId + ".txt"};
                    // 设置任务开始时间
                    LOGGER.info("扫描指令：{}" , arguments[2]);
                    long startTime = System.currentTimeMillis();
                    String taskStartTime = DATE_FORMAT.format(startTime);
                    prTaskEntity.setExecuteStartTime(taskStartTime);
                    // 工具执行
                    String sb = AntiMainUtil.execute(arguments);
                    // 保存日志内容
                    String url = YamlUtil.getToolPath() + AntiConstants.SCANTOOLFILE + "poison_logs"  + File.separator;
                    prTaskEntity.setLogs(AntiMainUtil.getTxtContent(url, scanId));
                    // 设置任务结束时间
                    long endTime = System.currentTimeMillis();
                    String taskEndTime = DATE_FORMAT.format(endTime);
                    prTaskEntity.setExecuteEndTime(taskEndTime);
                    String taskConsuming = (endTime - startTime) / 1000 + "s";
                    prTaskEntity.setTaskConsuming(taskConsuming);
                    // 设置总耗时
                    String downloadConsuming = prTaskEntity.getDownloadConsuming();
                    if (StringUtils.isNotBlank(taskConsuming) && StringUtils.isNotBlank(downloadConsuming)) {
                        long taskTime = Long.parseLong(taskConsuming.substring(0, taskConsuming.length() - 1));
                        long downloadTime = Long.parseLong(downloadConsuming.substring(0,
                                downloadConsuming.length() - 1));
                        long time = taskTime + downloadTime;
                        prTaskEntity.setTimeConsuming(time + "s");
                    }
                    String result = AntiMainUtil.getJsonContent(YamlUtil.getToolPath() +
                                    AntiConstants.PR_SCANRESULTPATH, prAntiEntity.getRepoName());
                    LOGGER.info("The scan result is : {}", result);
                    List<PRResultEntity> results = JSONArray.parseArray(result, PRResultEntity.class);
                    // 扫描结果详情
                    for (PRResultEntity resultEntity : results) {
                        int count = poisonResultOperation.getResultDetailByHash(resultEntity.getHash(),
                                prTaskEntity.getTaskId());
                        String status = count > 0 ? "2" : "0";
                        resultEntity.setProjectName(prAntiEntity.getProjectName());
                        resultEntity.setRepoName(prAntiEntity.getRepoName());
                        resultEntity.setBranch(prAntiEntity.getBranch());
                        resultEntity.setStatus(status);
                        resultEntity.setScanId(scanId);
                        resultEntity.setTaskId(prTaskEntity.getTaskId());
                        poisonResultOperation.insertPRResultDetails(resultEntity);
                    }
                    // 扫描是否成功
                    prAntiEntity.setIsSuccess(true);
                    prAntiEntity.setIsPass(true);
                    // 结果计数
                    prAntiEntity.setResultCount(results.size());
                    if (results.size() > 0) {
                        int solveCount = poisonResultOperation.getCountByStatus("2", results.get(0).getScanId());
                        prAntiEntity.setSolveCount(solveCount);
                        prAntiEntity.setIssueCount(results.size() - solveCount);
                        if ((results.size() - solveCount) > 0) { // 未解决问题数大于0，扫描不通过
                            prAntiEntity.setIsPass(false);
                        }
                    }
                    // 执行成功  0：未执行、1：执行中、2：执行成功、3：执行失败
                    prTaskEntity.setExecutionStatus(2);
                    prAntiEntity.setTips("");
                    // 更新扫描结果
                    antiOperation.updatePRScanResult(prAntiEntity);
                    // 更新门禁级结果
                    poisonTaskOperation.updatePRTask(prAntiEntity, prTaskEntity);
                    responseResult.put("scanId", info.getScanId());
                    return MultiResponse.success(ConstantsArgs.CODE_SUCCESS, "success", responseResult);
                } else {
                    // 扫描是否成功
                    prAntiEntity.setIsSuccess(false);
                    prAntiEntity.setIsPass(false);
                    // 原因
                    prAntiEntity.setTips("repo not Downloaded.");
                    prTaskEntity.setExecutionStatus(3);
                    antiOperation.updatePRScanResult(prAntiEntity);
                    poisonTaskOperation.updatePRTask(prAntiEntity, prTaskEntity);
                    return MultiResponse.error(ConstantsArgs.CODE_FAILED, "repoNotDownloaded error");
                }
            } catch (IOException e) {
                prAntiEntity.setIsSuccess(false);
                prAntiEntity.setIsPass(false);
                prAntiEntity.setTips(e.toString());
                prTaskEntity.setExecutionStatus(3);
                antiOperation.updatePRScanResult(prAntiEntity);
                poisonTaskOperation.updatePRTask(prAntiEntity, prTaskEntity);
                LOGGER.error(e.getMessage());
                return MultiResponse.error(ConstantsArgs.CODE_FAILED, "scan error : " + e.getCause());
            } finally {
                FileUtil.deleteDirectory(AntiConstants.DOWN_PATH + File.separator + info.getWorkspace());
            }
        } else {
            return MultiResponse.error(ConstantsArgs.CODE_FAILED, "scan error , task not exist!");
        }
    }

    /**
     * 下载代码仓库。
     *
     * @param antiEntity 扫描任务实体
     * @param id id
     * @return MultiResponse
     */
    @Override
    public MultiResponse downloadRepo(AntiEntity antiEntity, String id) {
        String createTime = DATE_FORMAT.format(System.currentTimeMillis());
        antiEntity.setCreateTime(createTime);
        // 生成任务id
        TaskEntity taskEntity = taskIdGenerate(antiEntity);
        JSONObject param = new JSONObject();
        param.put("id", id);
        param.put("taskId", taskEntity.getTaskId());
        String url = "/api/ci-backend/webhook/schedule/v1/poison/update-repo";
        HttpUtil httpUtil = new HttpUtil("prod".equals(ConstantsArgs.DB_ENV)
                ? ConstantsArgs.MAJUN_URL : ConstantsArgs.MAJUN_BETA_URL);
        String body = httpUtil.doPost(param, url);
        LOGGER.info("update info : {}", body);
        if (StringUtils.isEmpty(antiEntity.getBranch())) {
            antiEntity.setBranch("master");
        }
        String workspace = YamlUtil.getToolPath() + AntiConstants.REPOPATH + File.separator + antiEntity.getRepoName() +
                "-" + antiEntity.getBranch();
        antiOperation.insertScanResult(antiEntity);
        long startTime = System.currentTimeMillis();
        String userName ;
        String pass ;
        if (antiEntity.getRepoUrl().contains("source.openeuler.sh")) {
            userName = gitlabUser;
            pass = gitlabPass;
        } else {
            userName = gitUser;
            pass = gitPassword;
        }
        JGitUtil gfxly = new JGitUtil(antiEntity.getRepoName(), userName, pass,
                antiEntity.getBranch(), null, workspace);
        int getPullCode = gfxly.pullVersion(antiEntity.getRepoUrl());
        long endTime = System.currentTimeMillis();
        String downloadConsuming = (endTime - startTime) / 1000 + "s";
        taskEntity.setDownloadConsuming(downloadConsuming);
        taskEntity.setBranchRepositoryId(antiEntity.getBranchRepositoryId());
        poisonTaskOperation.updateTaskDownloadTime(taskEntity);
        if (getPullCode == 0) {
            LOGGER.info("checkout success code : {}", getPullCode);
            antiEntity.setIsDownloaded(true);
            antiOperation.updateScanResult(antiEntity);
            return MultiResponse.success(ConstantsArgs.CODE_SUCCESS, "success");
        } else {
            LOGGER.info("checkout error code : {}", getPullCode);
            antiEntity.setIsDownloaded(false);
            antiOperation.updateScanResult(antiEntity);
            return MultiResponse.error(ConstantsArgs.CODE_FAILED, "downloadRepo error");
        }
    }

    /**
     * 下载PR增量文件
     *
     * @param antiEntity 扫描任务实体
     * @param info pr信息
     * @param fileArray 差异文件数
     * @param type 代码托管平台 gitee&gitlab
     * @return MultiResponse<List<VmsExternalCveSourceDTO>>
     */
    @Override
    public MultiResponse downloadPRRepoFile(PRAntiEntity antiEntity, PullRequestInfo info,
                                            JSONArray fileArray, String type) {
        String createTime = DATE_FORMAT.format(System.currentTimeMillis());
        antiEntity.setCreateTime(createTime);
        // 生成任务id
        PRTaskEntity prTaskEntity = prTaskIdGenerate(antiEntity);
        antiOperation.insertPRScanResult(antiEntity);
        long startTime = System.currentTimeMillis();
        int getPullCode = 0;
        if (fileArray.size() < 100) {
            getPullCode = "gitlab".equals(type) ? curlGitlabFile(fileArray, info) : curlFile(fileArray, info);
        } else {
            getPullCode = cloneRepository(info);
        }
        long endTime = System.currentTimeMillis();
        String downloadConsuming = (endTime - startTime) / 1000 + "s";
        prTaskEntity.setDownloadConsuming(downloadConsuming);
        poisonTaskOperation.updatePRTaskDownloadTime(prTaskEntity);
        if (getPullCode == 0) {
            LOGGER.info("checkout success code : {}", getPullCode);
            antiEntity.setIsDownloaded(true);
            antiOperation.updatePRScanResult(antiEntity);
            return MultiResponse.success(ConstantsArgs.CODE_SUCCESS, "success");
        } else {
            LOGGER.info("checkout error code : {}", getPullCode);
            antiEntity.setIsDownloaded(false);
            antiOperation.updatePRScanResult(antiEntity);
            return MultiResponse.error(ConstantsArgs.CODE_FAILED, "downloadPRRepoFile error");
        }
    }

    /**
     * 通过curl指令下载差异文件到指定文件夹.
     *
     * @param fileArray 差异文件信息
     * @param info pr信息
     * @return int
     */
    public int curlFile(JSONArray fileArray, PullRequestInfo info) {
        final int[] pullCode = {0};
        LOGGER.info("get diff tree start!");
        fileArray.stream().forEach(file->{
            JSONObject json = (JSONObject) file;
            String url = json.getString("raw_url");
            StringBuffer sb = cmdOfCurl(info, url);
            try {
                List<String> strList = new ArrayList<>();
                Process proc = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", sb.toString()}, null, null);
                StreamConsumer errConsumer = new StreamConsumer(proc.getErrorStream(), strList,ConstantsArgs.ERR_CONSUMER);
                StreamConsumer outputConsumer = new StreamConsumer(proc.getInputStream(), strList,ConstantsArgs.OUTPUT_CONSUMER);
                errConsumer.start();
                outputConsumer.start();
                proc.waitFor();
                errConsumer.join();
                outputConsumer.join();
            } catch (IOException | InterruptedException e) {
                pullCode[0] = 1;
                LOGGER.error("errInfo is {}", e.getMessage());
            }
        });
        LOGGER.info("get diff tree end!");
        return pullCode[0];
    }

    /**
     * 通过curl指令下载gitlab差异文件到指定文件夹.
     *
     * @param fileArray 差异文件信息
     * @param info pr信息
     * @return int
     */
    public int curlGitlabFile(JSONArray fileArray, PullRequestInfo info) {
        final int[] pullCode = {0};
        LOGGER.info("get gitlab diff tree start!");
        fileArray.stream().forEach(file->{
            JSONObject json = (JSONObject) file;
            String url = GITLAB + "/openMajun/anti-poisoning/-/raw/" + info.getBranch() +
                    "/" + json.getString("new_path");
            StringBuffer sb = cmdOfGitlabCurl(info, json.getString("new_path"));
            try {
                List<String> strList = new ArrayList<>();
                Process proc = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", sb.toString()}, null, null);
                StreamConsumer errConsumer = new StreamConsumer(proc.getErrorStream(), strList,ConstantsArgs.ERR_CONSUMER);
                StreamConsumer outputConsumer = new StreamConsumer(proc.getInputStream(), strList,ConstantsArgs.OUTPUT_CONSUMER);
                errConsumer.start();
                outputConsumer.start();
                proc.waitFor();
                errConsumer.join();
                outputConsumer.join();
            } catch (IOException | InterruptedException e) {
                pullCode[0] = 1;
                LOGGER.error("errInfo is {}", e.getMessage());
            }
        });
        LOGGER.info("get gitlab diff tree end!");
        return pullCode[0];
    }

    /**
     * 下载全量代码后拉取差异文件。
     *
     * @param info pr信息
     * @return int
     */
    public int cloneRepository(PullRequestInfo info) {
        JGitUtil jGitUtil = new JGitUtil(info.getPullInfo(), info.getUser(), info.getPassword(),
                info.getBranch(), info.getVersion(), AntiConstants.DOWN_PATH + info.getWorkspace());
        int getPullCode = jGitUtil.pullPr(info.getGitUrl());
        StringBuffer sb = jGitUtil.cmdOfPullRequest(AntiConstants.DOWN_PATH +
                info.getWorkspace(), info.getTarget());
        List<String> strList = new ArrayList<String>();
        try {
            LOGGER.info("get diff tree start!");
            Process proc = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", sb.toString()}, null, null);
            StreamConsumer errConsumer = new StreamConsumer(proc.getErrorStream(), strList,ConstantsArgs.ERR_CONSUMER);
            StreamConsumer outputConsumer = new StreamConsumer(proc.getInputStream(), strList,ConstantsArgs.OUTPUT_CONSUMER);
            errConsumer.start();
            outputConsumer.start();
            proc.waitFor();
            errConsumer.join();
            outputConsumer.join();
            LOGGER.info("get diff tree end!");
        } catch (IOException | InterruptedException e) {
            LOGGER.error("errInfo is {}", e.getMessage());
        }
        return getPullCode;
    }

    /**
     * 通过curl方式下载文件至指定文件夹。
     *
     * @param info pr信息
     * @param url 文件下载链接
     * @return StringBuffer
     */
    public StringBuffer cmdOfCurl(PullRequestInfo info, String url) {
        String prePath = url.substring(url.indexOf("gitee.com") + 10, url.indexOf("raw/"));
        String lastPath = url.substring(url.indexOf("raw/") + 4);
        String filePath = prePath + lastPath.substring(lastPath.indexOf("/") + 1);
        StringBuffer buffer = new StringBuffer();
        buffer.append("curl -u ");
        // 设置下载所需的用户权限
        buffer.append(info.getUser()).append(":").append(info.getPassword()).append(" -o ");
        // 设置文件下载存放路径
        buffer.append(AntiConstants.DOWN_PATH).append(info.getWorkspace()).append("/modify_dirs/")
                .append(filePath).append(" >/dev/null 2>&1");
        buffer.append(" --create-dir ").append(url);
        return buffer;
    }

    /**
     * 通过curl方式下载gitlab文件至指定文件夹。
     *
     * @param info pr信息
     * @param url 文件下载链接
     * @return StringBuffer
     */
    public StringBuffer cmdOfGitlabCurl(PullRequestInfo info, String url) {
        String filePath = info.getProjectName() + "-" + info.getRepoName() + "-" + info.getBranch() +
                "-" + info.getPullNumber() + "/" + url;
        StringBuffer buffer = new StringBuffer();
        buffer.append("curl");
        if (StringUtils.isNotEmpty(info.getAccessToken())) {
            buffer.append(" --header 'PRIVATE-TOKEN:");
            // 设置下载所需的用户权限
            buffer.append(info.getAccessToken()).append("'");
        }
        buffer.append(" -o ");
        // 设置文件下载存放路径
        buffer.append(AntiConstants.DOWN_PATH).append(info.getWorkspace()).append("/modify_dirs/")
                .append(filePath).append(" >/dev/null 2>&1");
        try {
            // 通过gitlab API进行代码文件下载
            buffer.append(" --create-dir ").append(GITLAB + "/api/v4/" + info.getProjectId() + "/repository/files/" +
                    URLEncoder.encode(url, "UTF-8") + "/raw?ref=" + info.getBranch());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage());
        }
        return buffer;
    }

    /**
     * 版本扫描任务ID生成。
     *
     * @param antiEntity 任务对象
     * @return TaskEntity
     */
    public TaskEntity taskIdGenerate(AntiEntity antiEntity) {
        List<TaskEntity> taskEntity = poisonTaskOperation.queryTaskId(antiEntity);
        if (taskEntity != null && taskEntity.size() != 0) {
            String taskId = taskEntity.get(0).getProjectName() + "-" +
                    taskEntity.get(0).getRepoName() + "-" + taskEntity.get(0).getBranch();
            taskEntity.get(0).setExecutionStatus(1);
            taskEntity.get(0).setTaskId(taskId);
            poisonTaskOperation.updateTaskDownload(antiEntity, taskEntity.get(0));
            return taskEntity.get(0);
        }else {
            TaskEntity newTaskEntity = new TaskEntity();
            String taskId = antiEntity.getProjectName() + "-" + antiEntity.getRepoName() + "-" + antiEntity.getBranch();
            newTaskEntity.setTaskId(taskId);
            newTaskEntity.setExecutionStatus(1);
            poisonTaskOperation.insertTaskResult(antiEntity, newTaskEntity);
            return newTaskEntity;
        }
    }

    /**
     * 门禁扫描任务ID生成。
     *
     * @param prAntiEntity 任务对象
     * @return TaskEntity
     */
    public PRTaskEntity prTaskIdGenerate(PRAntiEntity prAntiEntity) {
        String taskId = prAntiEntity.getProjectName() + "-" +
                prAntiEntity.getRepoName() + "-" + prAntiEntity.getBranch();
        PRTaskEntity newTaskEntity = new PRTaskEntity();
        newTaskEntity.setTaskId(taskId);
        newTaskEntity.setExecutionStatus(1);
        poisonTaskOperation.insertPRTaskResult(prAntiEntity, newTaskEntity);
        return poisonTaskOperation.queryPRTaskEntity(prAntiEntity.getScanId());
    }

    /**
     * 封装版本扫描指令。
     *
     * @param repoName 仓库名称
     * @param branch 仓库分支
     * @param ruleName 规则名称
     * @param scanId 扫描ID
     * @return String[]
     */
    public String[] versionScan(String repoName, String branch, String ruleName, String scanId) {
        return new String[]{"/bin/sh", "-c",
                    "python3 " + YamlUtil.getToolPath() + AntiConstants.SCANTOOLPATH +
                        // 仓库下载后存放地址
                        " " + YamlUtil.getToolPath() + AntiConstants.REPOPATH +
                        repoName + "-" + branch + " " +
                        // 扫描完成后结果存放地址
                        YamlUtil.getToolPath() + AntiConstants.SCANRESULTPATH + repoName + ".json " +
                        // 支持多语言规则扫描
                        "--custom-yaml " + YamlUtil.getToolPath() + AntiConstants.CONFIG_PATH +
                        ruleName + " > " + YamlUtil.getToolPath() + AntiConstants.SCANTOOLFILE +
                        "poison_logs" + File.separator + scanId + ".txt"};
    }
}
