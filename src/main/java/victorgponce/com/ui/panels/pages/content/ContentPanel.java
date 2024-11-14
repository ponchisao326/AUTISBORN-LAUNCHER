package victorgponce.com.ui.panels.pages.content;

import javafx.animation.FadeTransition;
import javafx.util.Duration;
import victorgponce.com.ui.panel.Panel;

public abstract class ContentPanel extends Panel {

    @Override
    public void onShow() {
        FadeTransition transition = new FadeTransition(Duration.millis(250), this.layout);
        transition.setFromValue(0);
        transition.setToValue(1);
        transition.setAutoReverse(true);
        transition.play();
    }
}
