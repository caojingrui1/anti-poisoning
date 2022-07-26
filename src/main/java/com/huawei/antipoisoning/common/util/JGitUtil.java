package com.huawei.antipoisoning.common.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.Serializable;
import java.util.List;


/**
 * Java 调用Git进行checkout操作的工具类。
 *
 * @since 2021-08-30
 * @author zyx
 */
public class JGitUtil implements Serializable {
    private String module;
    private String user;
    private String pass;
    private String branch;
    private String revision;
    private String git_config;
    private String workspace;

    public JGitUtil(String module, String user, String pass, String branch, String revision, String workspace){
        this.module = module;
        this.user = user;
        this.pass = pass;
        this.branch = branch;
        this.revision = revision;
        this.workspace = workspace;
        this.git_config = workspace + "/.git";
    }
    public JGitUtil(String user, String pass, String branch, String workspace){
        this.user = user;
        this.pass = pass;
        this.branch = branch;
        this.workspace = workspace;
        this.git_config = workspace + "/.git";
    }

    /**
     * 通过url拉取代码
     * @param gitUrl
     * @return
     */
    public int pull(String gitUrl){
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
            RefSpec spec1 = new RefSpec("refs/heads/*:refs/remotes/origin/*");
            RefSpec spec2 = new RefSpec("refs/pull/*/MERGE:refs/pull/*/MERGE");
            git.fetch().setRefSpecs(spec1).setRefSpecs(spec2).setCredentialsProvider(provider).call();
            git.checkout().setName(module).setCreateBranch(true).call();
            pullMsg = "检出代码成功 success";
        } catch (org.eclipse.jgit.api.errors.TransportException e){
            e.printStackTrace();
            pullMsg = "用户名NAME或密码PASSWORD错误或远程链接URL错误 failed";
            pullFlag = 1;
        } catch (org.eclipse.jgit.api.errors.JGitInternalException e) {
            e.printStackTrace();
            pullMsg = "已经存在了项目的下载目录，并且目录正在被操作 failed";
            pullFlag = 2;
        } catch (GitAPIException e) {
            e.printStackTrace();
            pullMsg = "调用GitAPI异常，failed";
            pullFlag = 3;
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
            pullMsg = "未找到相应的类文件异常，failed";
            pullFlag = 4;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println(pullMsg +"--code--"+ pullFlag);
            if (git != null) {
                git.close();
            }
        }

        return pullFlag;
    }
    /**
     * 检出分支
     * @return
     */
    public int checkoutBranch(){
        String checkoutMsg = "";
        int checkoutFlag = 0;

        if (this.branch.equals("master")) {
            checkoutMsg = "Check out code OK. ->" + this.branch;
            System.out.println(checkoutMsg +"--code--"+ checkoutFlag);
            return checkoutFlag;
        }
        Git git = null;
        try {
            git = Git.open( new File(this.git_config) );
            //列出所有的分支名称
            List<Ref> branchList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            for (Ref ref : branchList){
                if (this.branch.equals(ref.getName())) {
                    System.out.println("代码分支列表中存在给定分支");
                }
            }
            git.checkout().setName("origin/" + this.branch).setForce(true).call();
            checkoutMsg = "检出分支代码 success! code OK ->" + this.branch;
        } catch (Exception e) {
            e.printStackTrace();
            checkoutMsg = "检出分支代码 failed ! ->" + this.branch;
            checkoutFlag = 6;
        } finally {
            System.out.println(checkoutMsg +"--code--"+ checkoutFlag);
            if (git != null) {
                git.close();
            }
        }

        return checkoutFlag;
    }
    /**
     * 检出代码
     * @return
     */
    public int checkoutRevision(){
        String checkoutMsg = "";
        int checkoutFlag = 0;
        if (this.revision == null || this.revision.length() == 0) {
            checkoutMsg = "Check out code OK. ->" + this.revision;
            System.out.println(checkoutMsg +"--code--"+ checkoutFlag);
            return checkoutFlag;
        }
        Git git = null;
        try {
            git = Git.open( new File(this.git_config) );
//            git.checkout().setUpstreamMode()
            git.checkout().setName( this.revision ).setForce(true).call();
            checkoutMsg = "检出代码版本 success! code OK. ->" + this.revision;
        } catch (Exception e) {
            e.printStackTrace();
            checkoutMsg = "检出代码版本 failed ! ->" + this.revision;
            checkoutFlag = 8;
        } finally {
            System.out.println(checkoutMsg +"--code--"+ checkoutFlag);
            if (git != null) {
                git.close();
            }
        }
        return checkoutFlag;
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
                for (File getFile: files) {
                    deleteFolder(getFile);
                    getFile.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        String baseUrl = "http://git/url";
//        String baseUrl = "https://gitee.com/ZYX_95/scanoss-pr";
        String baseUrl = "https://gitee.com/openeuler/";
//        String baseUrl = "https://gitee.com/openlookeng/";
//        String module = args[0];
//        String module = "scanoss-pr";
        String module = "openeuler-os-build";
//        String module = "hetu-core";
        String user = "openlibing@163.com";
        String pass = "Jszb2022h1";
        String branch = "master";
//        String branch = "pull/1198/MERGE";
//        String branch = args[1];
//        String revision = "e87e01a5c77d29ad340a5e7e1c771dd64d9ad9c2";
        String revision = "7c2f9fa05ec24426a289d881814745d8f2482f4b";
//        String workspace = "c:/Temp/build/test";
//        String workspace = "c:/Temp/build1/"+module;
        String workspace = "/usr/test/down/"+module;
//        String revision = args[2];
//        JGitUtil gfxly = new JGitUtil(user, pass, branch, workspace);
        JGitUtil gfxly = new JGitUtil(module, user, pass, branch, revision, workspace);

        int getPullCode = gfxly.pull(baseUrl  + module + ".git");
        if (getPullCode == 0) {
            System.out.println("检出代码成功===0");
        } else if (getPullCode == 1) {
            System.exit(1);
        } else if (getPullCode == 2) {
            System.exit(2);
        } else if (getPullCode == 3) {
            System.exit(3);
        } else if (getPullCode == 4) {
            System.exit(4);
        } else {
            System.out.println("检出代码未知异常===5");
            System.exit(5);
        }
//        int getBranchCode = gfxly.checkoutBranch();
//        if (getBranchCode == 0) {
//            System.out.println("检出分支成功===0");
//        } else if (getBranchCode == 6) {
//            System.exit(6);
//        } else {
//            System.out.println("检出分支未知异常===7");
//            System.exit(7);
//        }
//        int getRevisionCode = gfxly.checkoutRevision();
//        if (getRevisionCode == 0) {
//            System.out.println("检出版本成功===0");
//        } else if (getBranchCode == 8) {
//            System.exit(8);
//        } else {
//            System.out.println("检出版本未知异常===9");
//            System.exit(9);
//        }
    }
}