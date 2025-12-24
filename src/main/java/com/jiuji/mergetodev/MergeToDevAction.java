package com.jiuji.mergetodev;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import git4idea.GitUtil;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * 一键将当前分支合并到目标分支的 Action
 */
public class MergeToDevAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        Collection<GitRepository> repositories = GitUtil.getRepositories(project);
        if (repositories.isEmpty()) {
            Messages.showErrorDialog(project, "当前项目不是 Git 仓库", "错误");
            return;
        }

        GitRepository repository = repositories.iterator().next();
        String currentBranch = GitBranchUtil.getBranchNameOrRev(repository);

        // 从配置获取目标分支
        String targetBranch = MergeToDevSettings.getInstance().getTargetBranch();

        if (targetBranch.equals(currentBranch)) {
            Messages.showWarningDialog(project, "当前已在 " + targetBranch + " 分支，无需合并", "提示");
            return;
        }

        // 创建实时日志控制台
        MergeLogConsole console = new MergeLogConsole(project);
        GitOperationHelper helper = new GitOperationHelper(project, repository, console);

        // 检查未提交更改
        boolean hasUncommittedChanges = helper.hasUncommittedChanges();
        
        // fetch 当前分支以检查是否有本地领先的提交
        helper.fetchSilent(currentBranch);
        int aheadCount = helper.getAheadCount(currentBranch);
        
        // 如果没有未提交更改且没有领先远程的提交，提示无需操作
        if (!hasUncommittedChanges && aheadCount == 0) {
            Messages.showInfoMessage(project, 
                "当前分支 " + currentBranch + " 与远程分支完全同步，没有需要合并的更改。", 
                "无需操作");
            return;
        }

        String commitMsg = null;
        if (hasUncommittedChanges) {
            commitMsg = Messages.showInputDialog(
                    project,
                    "检测到未提交的更改，请输入提交信息：",
                    "提交更改",
                    Messages.getQuestionIcon(),
                    "auto commit before merge to " + targetBranch,
                    null
            );

            if (commitMsg == null) {
                return; // 用户取消
            }

            if (commitMsg.trim().isEmpty()) {
                commitMsg = "auto commit before merge to " + targetBranch;
            }
        }

        final String finalCommitMsg = commitMsg;
        final String finalTargetBranch = targetBranch;
        final boolean needPushCurrentBranch = hasUncommittedChanges || aheadCount > 0 || aheadCount == -1;

        // 后台执行
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Merge to " + targetBranch, false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                // 初始化控制台
                console.init();
                console.waitForInit();

                console.printInfo("当前分支: " + currentBranch);
                console.printInfo("目标分支: " + finalTargetBranch);
                console.printInfo("");

                // 如果有未提交更改，先提交
                if (finalCommitMsg != null) {
                    indicator.setText("正在提交更改...");
                    if (!helper.commitAll(finalCommitMsg)) {
                        console.printComplete(false);
                        return;
                    }
                    console.printInfo("");
                }

                // 推送到当前分支的远程（无论是否有新提交都推送）
                if (needPushCurrentBranch) {
                    indicator.setText("正在推送到远程 " + currentBranch + " 分支...");
                    if (!helper.push(currentBranch)) {
                        console.printComplete(false);
                        return;
                    }
                    console.printInfo("");
                }

                // Step 1: fetch 目标分支
                indicator.setText("正在获取远程分支...");
                if (!helper.fetch(finalTargetBranch)) {
                    console.printComplete(false);
                    return;
                }
                console.printInfo("");

                // Step 2: checkout target
                indicator.setText("正在切换分支...");
                if (!helper.checkout(finalTargetBranch)) {
                    console.printComplete(false);
                    return;
                }
                console.printInfo("");

                // Step 3: pull
                indicator.setText("正在拉取最新代码...");
                if (!helper.pull(finalTargetBranch)) {
                    console.printComplete(false);
                    helper.checkout(currentBranch);
                    return;
                }
                console.printInfo("");

                // Step 4: merge
                indicator.setText("正在合并分支...");
                if (!helper.merge(currentBranch)) {
                    console.printComplete(false);
                    return;
                }
                console.printInfo("");

                // Step 5: push
                indicator.setText("正在推送到远程...");
                if (!helper.push(finalTargetBranch)) {
                    console.printComplete(false);
                    helper.checkout(currentBranch);
                    return;
                }
                console.printInfo("");

                // Step 6: checkout back
                indicator.setText("正在切回原分支...");
                helper.checkout(currentBranch);
                console.printInfo("");

                // 完成
                console.printComplete(true);
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        boolean enabled = project != null && !GitUtil.getRepositories(project).isEmpty();
        e.getPresentation().setEnabledAndVisible(enabled);
    }
}
