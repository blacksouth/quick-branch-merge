package com.jiuji.mergetodev;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 插件配置持久化服务
 */
@Service(Service.Level.APP)
@State(
    name = "MergeToDevSettings",
    storages = @Storage("MergeToDevSettings.xml")
)
public final class MergeToDevSettings implements PersistentStateComponent<MergeToDevSettings.State> {

    private State myState = new State();

    public static MergeToDevSettings getInstance() {
        return ApplicationManager.getApplication().getService(MergeToDevSettings.class);
    }

    @Override
    public @Nullable State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public String getTargetBranch() {
        return myState.targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        myState.targetBranch = targetBranch;
    }

    /**
     * 配置状态类
     */
    public static class State {
        public String targetBranch = "dev";
    }
}
