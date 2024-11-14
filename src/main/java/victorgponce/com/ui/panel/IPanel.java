package victorgponce.com.ui.panel;


import javafx.scene.layout.GridPane;
import victorgponce.com.ui.PanelManager;

public interface IPanel {

    void init(PanelManager panelManager);
    GridPane getLayout();
    void onShow();
    String getName();
    String getSylesheetPath();

}
