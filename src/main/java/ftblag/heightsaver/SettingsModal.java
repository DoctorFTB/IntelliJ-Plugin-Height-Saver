package ftblag.heightsaver;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindowType;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Map;
import java.util.HashMap;

public class SettingsModal extends DialogWrapper {
    private static final Logger LOG = Logger.getInstance(SettingsModal.class);

    private final Project project;
    private final ToolWindowManager manager;

    public SettingsModal(Project project) {
        super(project, false);
        this.project = project;
        this.manager = ToolWindowManager.getInstance(project);

        init();
        setTitle("Height Saver Settings");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        HSState state = HSState.getInstance();

        Map<String, String> possibleToolWindows = new HashMap<>();

        for (String id : this.manager.getToolWindowIds()) {
            ToolWindow window = this.manager.getToolWindow(id);

            boolean added = window != null && window.getType() == ToolWindowType.DOCKED && window.getAnchor() == ToolWindowAnchor.BOTTOM;
            String title = window != null ? window.getStripeTitle() : "unknown";

            LOG.info(String.format("Checking \"%s\" (%s), result: %b", title, id, added));

            if (added) {
                possibleToolWindows.put(id, window.getStripeTitle());
            }
        }

        JPanel dialogPanel = new JPanel(new GridLayout(possibleToolWindows.size() + 3, 1));

        String lastActiveToolHeight = "unknown";

        String last = this.manager.getActiveToolWindowId();
        if (last != null) {
            ToolWindow window = this.manager.getToolWindow(last);
            if (window != null) {
                lastActiveToolHeight = "" + window.getComponent().getHeight();
            }
        }

        dialogPanel.add(new JLabel("Last active window height: " + lastActiveToolHeight));

        JFormattedTextField currentHeightField = new JFormattedTextField();

        currentHeightField.setValue(state.height);
        currentHeightField.addPropertyChangeListener("value", evt -> {
            state.height = ((Number)evt.getNewValue()).intValue();
            this.project.save();
        });

        dialogPanel.add(currentHeightField);

        dialogPanel.add(new JLabel("List of available restore windows: "));

        for (Map.Entry<String, String> entry : possibleToolWindows.entrySet()) {
            String id = entry.getKey();

            JCheckBox checkBox = new JCheckBox(": " + entry.getValue(), state.enabled.contains(id));

            checkBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    state.enabled.add(id);
                } else {
                    state.enabled.remove(id);
                }

                this.project.save();
            });

            checkBox.setPreferredSize(new Dimension(100, 20));

            dialogPanel.add(checkBox);
        }

        return dialogPanel;
    }
}
