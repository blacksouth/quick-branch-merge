package com.jiuji.mergetodev;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 实时日志控制台管理器
 */
public class MergeLogConsole {

    private static final String TOOL_WINDOW_ID = "Quick Merge";
    private ConsoleView consoleView;
    private final Project project;

    public MergeLogConsole(Project project) {
        this.project = project;
    }

    /**
     * 初始化并显示控制台
     */
    public void init() {
        ApplicationManager.getApplication().invokeLater(() -> {
            ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
            ToolWindow toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);

            if (toolWindow == null) {
                // 创建新的 ToolWindow
                toolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, true, ToolWindowAnchor.BOTTOM);
            }

            // 创建控制台视图
            consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();

            // 清空并添加内容
            ContentFactory contentFactory = ContentFactory.getInstance();
            toolWindow.getContentManager().removeAllContents(true);
            Content content = contentFactory.createContent(consoleView.getComponent(), "", false);
            toolWindow.getContentManager().addContent(content);

            // 显示工具窗口
            toolWindow.show();

            // 打印开始信息
            printInfo("========== Quick Branch Merge 开始执行 ==========\n\n");
        });
    }

    /**
     * 打印信息日志（白色）
     */
    public void printInfo(@NotNull String message) {
        print(message, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    /**
     * 打印成功日志（绿色）
     */
    public void printSuccess(@NotNull String message) {
        print("✓ " + message, ConsoleViewContentType.USER_INPUT);
    }

    /**
     * 打印错误日志（红色）
     */
    public void printError(@NotNull String message) {
        print("✗ " + message, ConsoleViewContentType.ERROR_OUTPUT);
    }

    /**
     * 打印命令输出（灰色）
     */
    public void printOutput(@NotNull String message) {
        print("  " + message.replace("\n", "\n  "), ConsoleViewContentType.SYSTEM_OUTPUT);
    }

    /**
     * 打印完成信息
     */
    public void printComplete(boolean success) {
        if (success) {
            print("\n========== 所有操作执行成功 ✓ ==========\n", ConsoleViewContentType.USER_INPUT);
        } else {
            print("\n========== 操作执行失败 ✗ ==========\n", ConsoleViewContentType.ERROR_OUTPUT);
        }
    }

    private void print(@NotNull String message, @NotNull ConsoleViewContentType type) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (consoleView != null) {
                consoleView.print(message + "\n", type);
            }
        });
    }

    /**
     * 等待控制台初始化完成
     */
    public void waitForInit() {
        try {
            // 给控制台初始化一点时间
            Thread.sleep(300);
        } catch (InterruptedException ignored) {
        }
    }
}
