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
 * Git 操作工具类
 */
public class GitOperationHelper {

    private final Project project;
    private final GitRepository repository;
    private final VirtualFile root;
    private final Git git;

    public GitOperationHelper(Project project, GitRepository repository) {
        this.project = project;
        this.repository = repository;
        this.root = repository.getRoot();
        this.git = Git.getInstance();
    }

    /**
     * 检查是否有未提交的更改
     */
    public boolean hasUncommittedChanges() {
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        return !changeListManager.getAllChanges().isEmpty();
    }

    /**
     * 添加所有文件并提交
     */
    public boolean commitAll(String message) {
        // git add .
        GitLineHandler addHandler = new GitLineHandler(project, root, GitCommand.ADD);
        addHandler.addParameters(".");
        GitCommandResult addResult = git.runCommand(addHandler);
        if (!addResult.success()) {
            return false;
        }

        // git commit -m "message"
        GitLineHandler commitHandler = new GitLineHandler(project, root, GitCommand.COMMIT);
        commitHandler.addParameters("-m", message);
        GitCommandResult commitResult = git.runCommand(commitHandler);
        return commitResult.success();
    }

    /**
     * fetch 远程分支
     */
    public boolean fetch(String branch) {
        GitLineHandler handler = new GitLineHandler(project, root, GitCommand.FETCH);
        handler.addParameters("origin", branch);
        GitCommandResult result = git.runCommand(handler);
        return result.success();
    }

    /**
     * 切换分支
     */
    public boolean checkout(String branch) {
        GitLineHandler handler = new GitLineHandler(project, root, GitCommand.CHECKOUT);
        handler.addParameters(branch);
        GitCommandResult result = git.runCommand(handler);
        
        if (result.success()) {
            // 刷新仓库状态
            repository.update();
        }
        return result.success();
    }

    /**
     * 拉取当前分支
     */
    public boolean pull() {
        GitLineHandler handler = new GitLineHandler(project, root, GitCommand.PULL);
        handler.addParameters("origin", repository.getCurrentBranchName());
        GitCommandResult result = git.runCommand(handler);
        return result.success();
    }

    /**
     * 合并指定分支到当前分支
     */
    public boolean merge(String branch) {
        GitLineHandler handler = new GitLineHandler(project, root, GitCommand.MERGE);
        handler.addParameters(branch, "--no-edit");
        GitCommandResult result = git.runCommand(handler);
        return result.success();
    }

    /**
     * 推送当前分支到远程
     */
    public boolean push() {
        GitLineHandler handler = new GitLineHandler(project, root, GitCommand.PUSH);
        handler.addParameters("origin", repository.getCurrentBranchName());
        GitCommandResult result = git.runCommand(handler);
        return result.success();
    }
}
