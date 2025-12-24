package com.jiuji.mergetodev;

import java.util.ArrayList;
import java.util.List;

/**
 * Git 操作日志收集器
 */
public class GitOperationLog {

    private final List<LogEntry> entries = new ArrayList<>();
    private boolean hasError = false;

    /**
     * 添加成功日志
     */
    public void addSuccess(String operation, String output) {
        entries.add(new LogEntry(operation, output, true));
    }

    /**
     * 添加失败日志
     */
    public void addError(String operation, String output) {
        entries.add(new LogEntry(operation, output, false));
        hasError = true;
    }

    /**
     * 是否有错误
     */
    public boolean hasError() {
        return hasError;
    }

    /**
     * 获取所有日志条目
     */
    public List<LogEntry> getEntries() {
        return entries;
    }

    /**
     * 转换为 HTML 格式用于显示
     */
    public String toHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: monospace; font-size: 12px;'>");
        
        for (LogEntry entry : entries) {
            String color = entry.success ? "#2e7d32" : "#c62828";
            String icon = entry.success ? "✓" : "✗";
            
            sb.append("<div style='margin-bottom: 10px;'>");
            sb.append("<b style='color: ").append(color).append(";'>").append(icon).append(" ").append(entry.operation).append("</b><br/>");
            
            if (entry.output != null && !entry.output.trim().isEmpty()) {
                String escapedOutput = entry.output
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\n", "<br/>");
                sb.append("<pre style='background: #f5f5f5; padding: 5px; margin: 5px 0; white-space: pre-wrap;'>")
                  .append(escapedOutput)
                  .append("</pre>");
            }
            sb.append("</div>");
        }
        
        sb.append("</body></html>");
        return sb.toString();
    }

    /**
     * 转换为纯文本格式
     */
    public String toPlainText() {
        StringBuilder sb = new StringBuilder();
        for (LogEntry entry : entries) {
            String icon = entry.success ? "[OK]" : "[FAIL]";
            sb.append(icon).append(" ").append(entry.operation).append("\n");
            if (entry.output != null && !entry.output.trim().isEmpty()) {
                sb.append(entry.output).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 日志条目
     */
    public static class LogEntry {
        public final String operation;
        public final String output;
        public final boolean success;

        public LogEntry(String operation, String output, boolean success) {
            this.operation = operation;
            this.output = output;
            this.success = success;
        }
    }
}
