package victorgponce.com.ui.panels.pages;

import fr.flowarg.materialdesignfontfx.MaterialDesignIcon;
import fr.flowarg.materialdesignfontfx.MaterialDesignIconView;
import fr.theshark34.openlauncherlib.util.Saver;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import victorgponce.com.Launcher;
import victorgponce.com.ui.PanelManager;
import victorgponce.com.ui.panel.IPanel;
import victorgponce.com.ui.panel.Panel;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import victorgponce.com.ui.panels.pages.content.Settings;

public class App extends Panel {

    GridPane sideMenu = new GridPane();
    GridPane navContent = new GridPane();

    Node activeLink = null;

    Button homeBtn, settingsBtn;

    Saver saver = Launcher.getInstance().getSaver();

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public String getSylesheetPath() {
        return "css/app.css";
    }

    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);

        // Background
        this.layout.getStyleClass().add("app-layout");
        setCanTakeAllSize(this.layout);

        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setHalignment(HPos.LEFT);
        columnConstraints.setMinWidth(350);
        columnConstraints.setMaxWidth(350);
        this.layout.getColumnConstraints().addAll(columnConstraints, new ColumnConstraints());

        // Side menu
        this.layout.add(sideMenu, 0, 0);
        sideMenu.getStyleClass().add("sidemenu");
        setLeft(sideMenu);
        setCenterH(sideMenu);
        setCenterV(sideMenu);

        // Background Image
        GridPane bgImage = new GridPane();
        setCanTakeAllSize(bgImage);
        bgImage.getStyleClass().add("bg-image");
        this.layout.add(bgImage, 1, 0);

        // Nav content
        this.layout.add(navContent, 1, 0);
        navContent.getStyleClass().add("nav-content");
        setLeft(navContent);
        setCenterH(navContent);
        setCenterV(navContent);

        /*
         * Side menu
         */
        // Titre
        Label title = new Label("Cosmere Launcher");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, FontPosture.REGULAR, 30f));
        title.getStyleClass().add("home-title");
        setCenterH(title);
        setCanTakeAllSize(title);
        setTop(title);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setTranslateY(30d);
        sideMenu.getChildren().add(title);

        // Navigation
        homeBtn = new Button("Home");
        homeBtn.getStyleClass().add("sidemenu-nav-btn");
        homeBtn.setGraphic(new MaterialDesignIconView<>(MaterialDesignIcon.H.HOME));
        setCanTakeAllSize(homeBtn);
        setTop(homeBtn);
        homeBtn.setTranslateY(90d);
        homeBtn.setOnMouseClicked(e -> setPage(null, homeBtn));

        settingsBtn = new Button("Configuration");
        settingsBtn.getStyleClass().add("sidemenu-nav-btn");
        settingsBtn.setGraphic(new MaterialDesignIconView<>(MaterialDesignIcon.C.COG));
        setCanTakeAllSize(settingsBtn);
        setTop(settingsBtn);
        settingsBtn.setTranslateY(130d);
        settingsBtn.setOnMouseClicked(e -> setPage(new Settings(), settingsBtn));

        sideMenu.getChildren().addAll(homeBtn, settingsBtn);

        // Pseudo + Avatar
        GridPane userPane = new GridPane();
        setCanTakeAllSize(userPane);
        userPane.setMaxHeight(80);
        userPane.setMinWidth(80);
        userPane.getStyleClass().add("user-pane");
        setBottom(userPane);

        String avatarUrl = "https://minotar.net/avatar/" + (
                saver.get("offline-username") != null ?
                        "MHF_Steve.png" :
                        Launcher.getInstance().getAuthInfos().getUuid() + ".png"
        );
        ImageView avatarView = new ImageView();
        Image avatarImg = new Image(avatarUrl);
        avatarView.setImage(avatarImg);
        avatarView.setPreserveRatio(true);
        avatarView.setFitHeight(50d);
        setCenterV(avatarView);
        setCanTakeAllSize(avatarView);
        setLeft(avatarView);
        avatarView.setTranslateX(15d);
        userPane.getChildren().add(avatarView);

        Label usernameLabel = new Label(Launcher.getInstance().getAuthInfos().getUsername());
        usernameLabel.setFont(Font.font("Consolas", FontWeight.BOLD, FontPosture.REGULAR, 25f));
        setCanTakeAllSize(usernameLabel);
        setCenterV(usernameLabel);
        setLeft(usernameLabel);
        usernameLabel.getStyleClass().add("username-label");
        usernameLabel.setTranslateX(75d);
        setCanTakeAllSize(usernameLabel);
        userPane.getChildren().add(usernameLabel);

        Button logoutBtn = new Button();
        final var logoutIcon = new MaterialDesignIconView<>(MaterialDesignIcon.L.LOGOUT);
        logoutIcon.getStyleClass().add("logout-icon");
        setCanTakeAllSize(logoutBtn);
        setCenterV(logoutBtn);
        setRight(logoutBtn);
        logoutBtn.getStyleClass().add("logout-btn");
        logoutBtn.setGraphic(logoutIcon);
        logoutBtn.setOnMouseClicked(e -> {
            saver.remove("accessToken");
            saver.remove("clientToken");
            saver.remove("msAccessToken");
            saver.remove("msRefreshToken");
            saver.remove("offline-username");
            saver.save();
            Launcher.getInstance().setAuthInfos(null);
            this.panelManager.showPanel(new Login());
        });
        userPane.getChildren().add(logoutBtn);

        sideMenu.getChildren().add(userPane);
    }

    @Override
    public void onShow() {
        super.onShow();
        setPage(null, homeBtn);
    }

    public void setPage(IPanel panel, Node navButton) {
        if (activeLink != null)
            activeLink.getStyleClass().remove("active");
        activeLink = navButton;
        activeLink.getStyleClass().add("active");
        this.navContent.getChildren().clear();
        if (panel != null) {
            this.navContent.getChildren().add(panel.getLayout());
            if (panel.getSylesheetPath() != null) {
                this.panelManager.getStage().getScene().getStylesheets().clear();
                this.panelManager.getStage().getScene().getStylesheets().addAll(
                        this.getSylesheetPath(),
                        panel.getSylesheetPath()
                );
            }
            panel.init(this.panelManager);
            panel.onShow();
        }
    }
}
