package ftblag.heightsaver;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RestoreAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(RestoreAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        if (project == null) {
            return;
        }

        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        HSState state = HSState.getInstance();

        int needed = state.height;

        String active = manager.getActiveToolWindowId();
        if (active != null) {
            ToolWindow window = manager.getToolWindow(active);
            if (window != null) {
                needed = window.getComponent().getHeight();
            }
        }

        LOG.info(String.format("Run restore, height: %s", needed));

        go(state, manager, needed, 0);
    }

    private void go(HSState state, ToolWindowManager manager, int needed, int idx) {
        if (state.enabled.size() > idx) {
            ToolWindowEx tw = (ToolWindowEx) manager.getToolWindow(state.enabled.get(idx));

            boolean canActive = tw != null && tw.getType() == ToolWindowType.DOCKED && tw.getAnchor() == ToolWindowAnchor.BOTTOM;
            String title = tw != null ? tw.getStripeTitle() : "unknown";

            LOG.info(String.format("Restore window \"%s\" (idx: %s), activate: %b", title, idx, canActive));

            if (canActive) {
                tw.activate(() -> {
                    tw.stretchHeight(needed - tw.getComponent().getHeight());
                    Timer timer = new Timer(500, e -> go(state, manager, needed, idx + 1));
                    timer.setRepeats(false);
                    timer.start();
                });
            } else {
                Timer timer = new Timer(500, e -> go(state, manager, needed, idx + 1));
                timer.setRepeats(false);
                timer.start();
            }
        }
    }
}
