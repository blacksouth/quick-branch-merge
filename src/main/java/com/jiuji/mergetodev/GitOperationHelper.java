package com.jiuji.mergetodev;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.repo.GitRepository;

/**
 * Git 操作工具类（实时日志版本）
 */
public class GitOperationHelper {

    private final Project project;
    private final GitRepository repository;
    private final VirtualFile root;
    private final Git git;
    private final MergeLogConsole console;

    public GitOperationHelper(Project project, GitRepository repository, MergeLogConsole console) {
        this.project = project;
        this.repository = repository;
        this.root = repository.getRoot();
        this.git = Git.getInstance();
        this.console = console;
    }

    /**
     * 检查是否有未提交的更改
     */
    public boolean hasUncommittedChanges() {
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        return !changeListManager.getAllChanges().isEmpty();
    }

    /**
     * 获取本地分支领先远程的提交数
     * @return 领先的提交数，-1 表示远程分支不存在
     */
    public int getAheadCount(String branch) {
        // git rev-list --count origin/branch..branch
        GitLineHandler handler = new GitLineHandler(project, root, GitCommand.REV_LIST);
        handler.addParameters("--count", "origin/" + branch + ".." + branch);
        GitCommandResult result = git.runCommand(handler);
        
        if (!result.success()) {
            // 远程分支可能不存在
            return -1;
        }
        
        try {
            String output = String.join("", result.getOutput()).trim();
            return Integer.parseInt(output);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * fetch 当前分支（静默，不打印日志）
     */
    public boolean fetchSilent(String branch) {
        GitLineHandler handler = new GitLineHandler(project, root, GitCommand.FETCH);
        handler.addParameters("origin", branch);
        GitCommandResult result = git.runCommand(handler);
        return result.success();
    }

    /**
     * 添加所有文件并提交
     */
    public boolean commitAll(String message) {
        // git add .
        console.printInfo("执行: git add .");
        GitLineHandler addHandler = new GitLineHandler(project, root, GitCommand.ADD);
        addHandler.addParameters(".");
        GitCommandResult addResult = git.runCommand(addHandler);

        if (!addResult.success()) {
            console.printError("git add . 失败");
            console.printOutput(getOutput(addResult));
            return false;
        }
        console.printSuccess("git add . 完成");

        // git commit -m "message"
        String cmd = "git commit -m \"" + message + "\"";
        console.printInfo("执行: " + cmd);
        GitLineHandler commitHandler = new GitLineHandler(project, root, GitCommand.COMMIT);
        commitHandler.addParameters("-m", message);
        GitCommandResult commitResult = git.runCommand(commitHandler);

        if (!commitResult.success()) {
            console.printError(cmd + " 失败");
            console.printOutput(getOutput(commitResult));
            return false;
        }
        console.printSuccess(cmd + " 完成");
        String output = getOutput(commitResult);
        if (!output.isEmpty()) {
            console.printOutput(output);
        }
        return true;
    }

    /**
     * fetch 远程分支
     */
    public boolean fetch(String branch) {
        String cmd = "git fetch origin " + branch;
        console.printInfo("执行: " + cmd);

        GitLineHandler handler = new GitLineHandler(project, root, GitCommand.FETCH);
        handler.addParameters("origin", branch);
        GitCommandResult result = git.runCommand(handler);

        if (!result.success()) {
            console.printError(cmd + " 失败");
            console.printOutput(getOutput(result));
            return false;
        }
        console.printSuccess(cmd + " 完成");
        String output = getOutput(result);
        if (!output.isEmpty()) {
            console.printOutput(output);
        }
        return true;
    }

    /**
     * 切换分支
     */
    public boolean checkout(String branch) {
        String cmd = "git checkout " + branch;
        console.printInfo("执行: " + cmd);

        GitLineHandler handler = new GitLineHandler(project, root, GitCommand.CHECKOUT);
        handler.addParameters(branch);
        GitCommandResult result = git.runCommand(handler);

        if (!result.success()) {
            console.printError(cmd + " 失败");
            console.printOutput(getOutput(result));
            return false;
        }
        console.printSuccess(cmd + " 完成");
        String output = getOutput(result);
        if (!output.isEmpty()) {
            console.printOutput(output);
        }

        // 刷新仓库状态
        repository.update();
        return true;
    }

    /**
     * 拉取当前分支
     */
    public boolean pull(String branch) {
        String cmd = "git pull origin " + branch;
        console.printInfo("执行: " + cmd);

        GitLineHandler handler = new GitLineHandler(project, root, GitCommand.PULL);
        handler.addParameters("origin", branch);
        GitCommandResult result = git.runCommand(handler);

        if (!result.success()) {
            console.printError(cmd + " 失败");
            console.printOutput(getOutput(result));
            return false;
        }
        console.printSuccess(cmd + " 完成");
        String output = getOutput(result);
        if (!output.isEmpty()) {
            console.printOutput(output);
        }
        return true;
    }

    /**
     * 合并指定分支到当前分支
     */
    public boolean merge(String branch) {
        String cmd = "git merge " + branch + " --no-edit";
        console.printInfo("执行: " + cmd);

        GitLineHandler handler = new GitLineHandler(project, root, GitCommand.MERGE);
        handler.addParameters(branch, "--no-edit");
        GitCommandResult result = git.runCommand(handler);

        if (!result.success()) {
            console.printError(cmd + " 失败");
            console.printOutput(getOutput(result));
            return false;
        }
        console.printSuccess(cmd + " 完成");
        String output = getOutput(result);
        if (!output.isEmpty()) {
            console.printOutput(output);
        }
        return true;
    }

    /**
     * 推送当前分支到远程
     */
    public boolean push(String branch) {
        String cmd = "git push origin " + branch;
        console.printInfo("执行: " + cmd);

        GitLineHandler handler = new GitLineHandler(project, root, GitCommand.PUSH);
        handler.addParameters("origin", branch);
        GitCommandResult result = git.runCommand(handler);

        if (!result.success()) {
            console.printError(cmd + " 失败");
            console.printOutput(getOutput(result));
            return false;
        }
        console.printSuccess(cmd + " 完成");
        String output = getOutput(result);
        if (!output.isEmpty()) {
            console.printOutput(output);
        }
        return true;
    }

    /**
     * 获取命令输出
     */
    private String getOutput(GitCommandResult result) {
        StringBuilder sb = new StringBuilder();
        if (!result.getOutput().isEmpty()) {
            sb.append(String.join("\n", result.getOutput()));
        }
        if (!result.getErrorOutput().isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(String.join("\n", result.getErrorOutput()));
        }
        return sb.toString();
    }
}
