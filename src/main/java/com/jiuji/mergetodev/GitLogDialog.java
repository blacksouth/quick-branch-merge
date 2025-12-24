package com.jiuji.mergetodev;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Git 操作日志展示对话框
 */
public class GitLogDialog extends DialogWrapper {

    private final GitOperationLog log;
    private final String title;

    public GitLogDialog(Project project, String title, GitOperationLog log) {
        super(project, true);
        this.title = title;
        this.log = log;
        setTitle(title);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(600, 400));

        // 使用 JEditorPane 显示 HTML 格式的日志
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setText(log.toHtml());
        editorPane.setCaretPosition(0);

        JBScrollPane scrollPane = new JBScrollPane(editorPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 状态栏
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String statusText = log.hasError() ? "操作过程中出现错误" : "所有操作执行成功";
        String statusColor = log.hasError() ? "#c62828" : "#2e7d32";
        JLabel statusLabel = new JLabel("<html><b style='color: " + statusColor + ";'>" + statusText + "</b></html>");
        statusPanel.add(statusLabel);
        panel.add(statusPanel, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction()};
    }
}
