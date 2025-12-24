package com.jiuji.mergetodev;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * 插件设置界面
 */
public class MergeToDevConfigurable implements Configurable {

    private JTextField targetBranchField;
    private final MergeToDevSettings settings = MergeToDevSettings.getInstance();

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Quick Branch Merge";
    }

    @Override
    public @Nullable JComponent createComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 目标分支标签
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("目标分支:"), gbc);

        // 目标分支输入框
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        targetBranchField = new JTextField(20);
        targetBranchField.setText(settings.getTargetBranch());
        panel.add(targetBranchField, gbc);

        // 说明文字
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel hintLabel = new JLabel("<html><font color='gray'>设置合并操作的目标分支名称，默认为 dev</font></html>");
        panel.add(hintLabel, gbc);

        // 占位，让内容靠上
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    @Override
    public boolean isModified() {
        return !settings.getTargetBranch().equals(targetBranchField.getText().trim());
    }

    @Override
    public void apply() throws ConfigurationException {
        String branch = targetBranchField.getText().trim();
        if (branch.isEmpty()) {
            throw new ConfigurationException("目标分支不能为空");
        }
        settings.setTargetBranch(branch);
    }

    @Override
    public void reset() {
        targetBranchField.setText(settings.getTargetBranch());
    }
}
