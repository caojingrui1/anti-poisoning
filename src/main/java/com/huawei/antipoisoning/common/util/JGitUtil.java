/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.huawei.antipoisoning.common.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;


/**
 * Java 调用Git进行checkout操作的工具类。
 *
 * @since 2021-08-30
 * @author zyx
 */
public class JGitUtil implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(JGitUtil.class);

    private String repo;
    private String user;
    private String pass;
    private String branch;
    private String revision;
    private String git_config;
    private String workspace;

    public JGitUtil(String repo, String user, String pass, String branch, String revision, String workspace){
        this.repo = repo;
        this.user = user;
        this.pass = pass;
        this.branch = branch;
        this.revision = revision;
        this.workspace = workspace;
        this.git_config = workspace + "/.git";
    }

    /**
     * 通过url拉取全量代码。版本级扫描使用
     *
     * @param gitUrl
     * @return
     */
    public int pullVersion(String gitUrl){
        String pullMsg = "";
        // 标记拉取代码的标志
        int pullFlag = 0;
        // 提供用户名和密码的验证
        UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(
                this.user, this.pass);
        // 指定要加载的代码路径
        File dir = new File(workspace);
        // 判断代码路径下是否有内容，如果有就删除
        if(dir.exists()){
            deleteFolder(dir);
        }

        Git git = null;
        try {
            git = Git.cloneRepository().setURI(gitUrl)
                    .setDirectory(dir).setCredentialsProvider(provider).call();
            pullMsg = "检出代码成功 success";
        } catch (org.eclipse.jgit.api.errors.TransportException e) {
            LOGGER.error("errInfo is {}", e.getMessage());
            pullMsg = "用户名NAME或密码PASSWORD错误或远程链接URL错误 failed";
            pullFlag = 1;
        } catch (org.eclipse.jgit.api.errors.JGitInternalException e) {
            LOGGER.error("errInfo is {}", e.getMessage());
            pullMsg = "已经存在了项目的下载目录，并且目录正在被操作 failed";
            pullFlag = 2;
        } catch (GitAPIException e) {
            LOGGER.error("errInfo is {}", e.getMessage());
            pullMsg = "调用GitAPI异常，failed";
            pullFlag = 3;
        } catch (NoClassDefFoundError e) {
            LOGGER.error("errInfo is {}", e.getMessage());
            pullMsg = "未找到相应的类文件异常，failed";
            pullFlag = 4;
        } finally {
            LOGGER.info("{} --code-- {}", pullMsg, pullFlag);
            if (git != null) {
                git.close();
            }
        }
        return pullFlag;
    }

    /**
     * 通过url拉取PR代码.门禁级扫描使用
     *
     * @param gitUrl gitee clone url
     * @return int
     */
    public int pullPr(String gitUrl) {
        String pullMsg = "";
        // 标记拉取代码的标志
        int pullFlag = 0;
        // 提供用户名和密码的验证
        UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(this.user, this.pass);
        // 指定要加载的代码路径
        File dir = new File(workspace);
        // 判断代码路径下是否有内容，如果有就删除
        if (dir.exists()) {
            deleteFolder(dir);
        }
        Git git = null;
        try {
            git = Git.cloneRepository().setURI(gitUrl)
                    .setDirectory(dir).setCredentialsProvider(provider).call();
            RefSpec spec1 = new RefSpec("refs/heads/*:refs/remotes/origin/*");
            RefSpec spec2 = new RefSpec("refs/pull/*/MERGE:refs/pull/*/MERGE");
            git.fetch().setRefSpecs(spec1).setRefSpecs(spec2).setCredentialsProvider(provider).call();
            git.checkout().setName(repo).call();
            pullMsg = "检出代码成功 success";

        } catch (org.eclipse.jgit.api.errors.TransportException e) {
            LOGGER.error("errInfo is {}", e.getMessage());
            pullMsg = "用户名NAME或密码PASSWORD错误或远程链接URL错误 failed";
            pullFlag = 1;
        } catch (org.eclipse.jgit.api.errors.JGitInternalException e) {
            LOGGER.error("errInfo is {}", e.getMessage());
            pullMsg = "已经存在了项目的下载目录，并且目录正在被操作 failed";
            pullFlag = 2;
        } catch (GitAPIException e) {
            LOGGER.error("errInfo is {}", e.getMessage());
            pullMsg = "调用GitAPI异常，failed";
            pullFlag = 3;
        } catch (NoClassDefFoundError e) {
            LOGGER.error("errInfo is {}", e.getMessage());
            pullMsg = "未找到相应的类文件异常，failed";
            pullFlag = 4;
        } finally {
            LOGGER.info("{} --code-- {}", pullMsg, pullFlag);
            if (git != null) {
                git.close();
            }
        }
        return pullFlag;
    }


    /**
     * 检出分支
     *
     * @return int
     */
    public int checkoutBranch(){
        String checkoutMsg = "";
        int checkoutFlag = 0;

        if (this.branch.equals("master")) {
            checkoutMsg = "Check out code OK. ->" + this.branch;
            LOGGER.info("{} --code-- {}",checkoutMsg, checkoutFlag);
            return checkoutFlag;
        }
        Git git = null;
        try {
            git = Git.open( new File(this.git_config) );
            //列出所有的分支名称
            List<Ref> branchList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            for (Ref ref : branchList){
                if (this.branch.equals(ref.getName())) {
                    LOGGER.info("The branch is exist!");
                }
            }
            git.checkout().setName("origin/" + this.branch).setForce(true).call();
            checkoutMsg = "检出分支代码 success! code OK ->" + this.branch;
        } catch (GitAPIException | IOException e) {
            LOGGER.error("errInfo is {}", e.getMessage());
            checkoutMsg = "检出分支代码 failed ! ->" + this.branch;
            checkoutFlag = 6;
        } finally {
            LOGGER.info("{} --code-- {}",checkoutMsg, checkoutFlag);
            if (git != null) {
                git.close();
            }
        }

        return checkoutFlag;
    }
    /**
     * 检出代码
     *
     * @return int
     */
    public int checkoutRevision(){
        String checkoutMsg = "";
        int checkoutFlag = 0;
        if (this.revision == null || this.revision.length() == 0) {
            checkoutMsg = "Check out code OK. ->" + this.revision;
            LOGGER.info("{} --code-- {}",checkoutMsg, checkoutFlag);
            return checkoutFlag;
        }
        Git git = null;
        try {
            git = Git.open( new File(this.git_config) );
            git.checkout().setName( this.revision ).setForce(true).call();
            checkoutMsg = "检出代码版本 success! code OK. ->" + this.revision;
        } catch (GitAPIException | IOException e) {
            LOGGER.error("errInfo is {}", e.getMessage());
            checkoutMsg = "检出代码版本 failed ! ->" + this.revision;
            checkoutFlag = 8;
        } finally {
            LOGGER.info("{} --code-- {}",checkoutMsg, checkoutFlag);
            if (git != null) {
                git.close();
            }
        }
        return checkoutFlag;
    }


    /**
     * git指令获取PR差异文件。
     *
     * @param workspace 工作区间
     * @param giteeSourceBranch 源分支
     * @return sb shell指令
     */
    public StringBuffer cmdOfPullRequest (String workspace, String giteeSourceBranch) {
        StringBuffer sb = new StringBuffer();
        sb.append("mkdir -p " + workspace +  File.separator + "modify_dirs && ");
        sb.append("cd " + workspace + " && ");
        sb.append("git diff-tree -r --name-only --no-commit-id origin/" + giteeSourceBranch
                + " HEAD > modify_list.txt  && ");
        sb.append("cat modify_list.txt && ");
        sb.append("while read -r line;  " ).append("do ").append("dir_name=${line%/*}; ")
                .append("file_name=${line##*/};  ");
        sb.append("if [ $(echo $line |grep '/') ]; then  ");
        sb.append("mkdir -p modify_dirs" + File.separator + "${dir_name} ; ");
        sb.append("if [ -f ${line} ]; then  ");
        sb.append("cp -rf ${line} modify_dirs" + File.separator + "${dir_name};  ");
        sb.append("fi; " ).append("else ");
        sb.append("mkdir -p modify_dirs; ");
        sb.append("if [ -f ${line} ]; then  ");
        sb.append("cp -rf ${line} modify_dirs;  " );
        sb.append("fi;  " ).append("fi; ");
        sb.append("done < modify_list.txt ");
        return sb;
    }

    /**
     * 删除目录
     * @param file
     */
    private void deleteFolder(File file){
        try {
            if (file.isFile() || file.list().length==0) {
                file.delete();
            } else {
                File[] files = file.listFiles();
                for (File getFile : files) {
                    deleteFolder(getFile);
                    getFile.delete();
                }
            }
        } catch (Exception e) {
            LOGGER.error("errInfo is {}", e.getMessage());
        }
    }

    public static void main(String[] args) {
        JGitUtil gfxly = new JGitUtil("pull/2/MERGE", "", "", "master",
                "b19cf211470cb6841cd5f3340e62db74b61849b2", "C:\\workspace\\poison-test");
        gfxly.pullPr("https://gitee.com/zzyy95_1/helper.git");
        StringBuffer sb = gfxly.cmdOfPullRequest("C:\\workspace\\poison-test", "master");

    }
}