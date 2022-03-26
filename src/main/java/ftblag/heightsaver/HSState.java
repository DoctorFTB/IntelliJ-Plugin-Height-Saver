package ftblag.heightsaver;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@State(name = "MagicState", storages = {@Storage("magic.xml")})
public class HSState implements PersistentStateComponent<HSState> {
    public List<String> enabled = new ArrayList<>(Arrays.asList("TODO", "Terminal"));
    public int height = 200;

    public static HSState getInstance() {
        return ApplicationManager.getApplication().getService(HSState.class);
    }

    @Override
    public @Nullable HSState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull HSState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}