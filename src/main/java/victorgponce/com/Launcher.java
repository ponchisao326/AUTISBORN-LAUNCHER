package victorgponce.com;

import fr.flowarg.flowlogger.ILogger;
import fr.flowarg.flowlogger.Logger;
import fr.litarvan.openauth.AuthPoints;
import fr.litarvan.openauth.AuthenticationException;
import fr.litarvan.openauth.Authenticator;
import fr.litarvan.openauth.model.AuthProfile;
import fr.litarvan.openauth.model.response.RefreshResponse;
import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import fr.theshark34.openlauncherlib.util.Saver;
import javafx.application.Application;
import javafx.stage.Stage;
import victorgponce.com.ui.PanelManager;
import victorgponce.com.ui.panels.pages.Login;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Launcher extends Application {

    private PanelManager panelManager;
    private static Launcher instance;
    private final ILogger logger;
    private final Path launcherDir = GameDirGenerator.createGameDir("Phosting-Launcher", true);
    private final Saver saver;

    private AuthProfile authProfile = null;

    public Launcher() {
        instance = this;
        this.logger = new Logger("[PhostingLauncher]", this.launcherDir.resolve("launcher.log"));
        if (Files.notExists(this.launcherDir)) {
            try
            {
                Files.createDirectory(this.launcherDir);
            } catch (IOException e)
            {
                this.logger.err("Unable to create launcher folder");
                this.logger.printStackTrace(e);
            }
        }

        saver = new Saver(this.launcherDir.resolve("config.properties"));
        saver.load();
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.logger.info("Starting Launcher");
        this.panelManager = new PanelManager(this, stage);
        this.panelManager.init();

        if (this.isUserAlreadyLoggedIn()) {
            logger.info("Hello " + authProfile.getName());
        } else {
            this.panelManager.showPanel(new Login());
        }
    }

    public boolean isUserAlreadyLoggedIn() {
        if (saver.get("accessToken") != null && saver.get("clientToken") != null) {
            Authenticator authenticator = new Authenticator(Authenticator.MOJANG_AUTH_URL, AuthPoints.NORMAL_AUTH_POINTS);

            try {
                RefreshResponse response = authenticator.refresh(saver.get("accessToken"), saver.get("clientToken"));
                saver.set("accessToken", response.getAccessToken());
                saver.set("clientToken", response.getClientToken());
                saver.save();
                this.authProfile = response.getSelectedProfile();
                return true;
            } catch (AuthenticationException ignored) {
                saver.remove("accessToken");
                saver.remove("clientToken");
                saver.save();
            }
        }

        return false;
    }

    public void setAuthProfile(AuthProfile authProfile) {
        this.authProfile = authProfile;
    }

    public AuthProfile getAuthProfile() {
        return authProfile;
    }

    public ILogger getLogger() {
        return logger;
    }

    public Saver getSaver() {
        return saver;
    }

    public static Launcher getInstance() {
        return instance;
    }

}
