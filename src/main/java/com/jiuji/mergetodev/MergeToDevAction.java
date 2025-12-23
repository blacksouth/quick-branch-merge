package com.jiuji.mergetodev;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import git4idea.GitUtil;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * 一键将当前分支合并到 dev 分支的 Action
 */
public class MergeToDevAction extends AnAction {

    private static final String TARGET_BRANCH = "dev";
    private static final String NOTIFICATION_GROUP = "Merge to Dev";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        Collection<GitRepository> repositories = GitUtil.getRepositories(project);
        if (repositories.isEmpty()) {
            showError(project, "当前项目不是 Git 仓库");
            return;
        }

        GitRepository repository = repositories.iterator().next();
        String currentBranch = GitBranchUtil.getBranchNameOrRev(repository);

        if (TARGET_BRANCH.equals(currentBranch)) {
            showError(project, "当前已在 dev 分支，无需合并");
            return;
        }

        // 检查是否有未提交的更改
        GitOperationHelper helper = new GitOperationHelper(project, repository);
        
        if (helper.hasUncommittedChanges()) {
            String commitMsg = Messages.showInputDialog(
                    project,
                    "检测到未提交的更改，请输入提交信息：",
                    "提交更改",
                    Messages.getQuestionIcon(),
                    "auto commit before merge to dev",
                    null
            );
            
            if (commitMsg == null) {
                // 用户取消
                return;
            }
            
            if (commitMsg.trim().isEmpty()) {
                commitMsg = "auto commit before merge to dev";
            }
            
            final String finalCommitMsg = commitMsg;
            
            // 先提交更改
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "提交更改", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setText("正在提交更改...");
                    boolean success = helper.commitAll(finalCommitMsg);
                    if (!success) {
                        showError(project, "提交失败，请检查 Git 状态");
                        return;
                    }
                    // 提交成功后执行合并
                    executeMerge(project, helper, currentBranch);
                }
            });
        } else {
            // 没有未提交的更改，直接执行合并
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Merge to Dev", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    executeMerge(project, helper, currentBranch);
                }
            });
        }
    }

    /**
     * 执行合并流程
     */
    private void executeMerge(Project project, GitOperationHelper helper, String currentBranch) {
        ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        
        try {
            // Step 1: fetch origin dev
            if (indicator != null) {
                indicator.setText("正在获取远程 dev 分支...");
                indicator.setFraction(0.1);
            }
            if (!helper.fetch(TARGET_BRANCH)) {
                showError(project, "获取远程 dev 分支失败");
                return;
            }

            // Step 2: checkout dev
            if (indicator != null) {
                indicator.setText("正在切换到 dev 分支...");
                indicator.setFraction(0.2);
            }
            if (!helper.checkout(TARGET_BRANCH)) {
                showError(project, "切换到 dev 分支失败");
                return;
            }

            // Step 3: pull origin dev
            if (indicator != null) {
                indicator.setText("正在拉取 dev 分支最新代码...");
                indicator.setFraction(0.4);
            }
            if (!helper.pull()) {
                showError(project, "拉取 dev 分支失败");
                helper.checkout(currentBranch);
                return;
            }

            // Step 4: merge current branch
            if (indicator != null) {
                indicator.setText("正在合并 " + currentBranch + " 到 dev...");
                indicator.setFraction(0.6);
            }
            if (!helper.merge(currentBranch)) {
                showError(project, "合并失败，请手动解决冲突");
                return;
            }

            // Step 5: push origin dev
            if (indicator != null) {
                indicator.setText("正在推送到远程 dev 分支...");
                indicator.setFraction(0.8);
            }
            if (!helper.push()) {
                showError(project, "推送到远程 dev 分支失败");
                helper.checkout(currentBranch);
                return;
            }

            // Step 6: checkout back to original branch
            if (indicator != null) {
                indicator.setText("正在切回 " + currentBranch + " 分支...");
                indicator.setFraction(0.9);
            }
            helper.checkout(currentBranch);

            // 成功
            if (indicator != null) {
                indicator.setFraction(1.0);
            }
            showSuccess(project, "成功将 " + currentBranch + " 合并到远程 dev 分支！");
            
        } catch (Exception e) {
            showError(project, "操作失败: " + e.getMessage());
            // 尝试切回原分支
            try {
                helper.checkout(currentBranch);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        boolean enabled = project != null && !GitUtil.getRepositories(project).isEmpty();
        e.getPresentation().setEnabledAndVisible(enabled);
    }

    private void showError(Project project, String message) {
        Notification notification = new Notification(
                NOTIFICATION_GROUP,
                "Merge to Dev 失败",
                message,
                NotificationType.ERROR
        );
        Notifications.Bus.notify(notification, project);
    }

    private void showSuccess(Project project, String message) {
        Notification notification = new Notification(
                NOTIFICATION_GROUP,
                "Merge to Dev 成功",
                message,
                NotificationType.INFORMATION
        );
        Notifications.Bus.notify(notification, project);
    }
}
