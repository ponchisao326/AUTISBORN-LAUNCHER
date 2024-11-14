package victorgponce.com;

import fr.flowarg.flowlogger.ILogger;
import fr.flowarg.flowlogger.Logger;
import fr.litarvan.openauth.AuthPoints;
import fr.litarvan.openauth.AuthenticationException;
import fr.litarvan.openauth.Authenticator;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import fr.litarvan.openauth.model.AuthProfile;
import fr.litarvan.openauth.model.response.RefreshResponse;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import fr.theshark34.openlauncherlib.util.Saver;
import javafx.application.Application;
import javafx.stage.Stage;
import victorgponce.com.ui.PanelManager;
import victorgponce.com.ui.panels.pages.App;
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
    private AuthInfos authInfos = null;

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
            logger.info("Hello " + authInfos.getUsername());

            this.panelManager.showPanel(new App());
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

                this.setAuthInfos(new AuthInfos(
                        response.getSelectedProfile().getName(),
                        response.getAccessToken(),
                        response.getClientToken(),
                        response.getSelectedProfile().getId()
                ));

                return true;
            } catch (AuthenticationException ignored) {
                saver.remove("accessToken");
                saver.remove("clientToken");
                saver.save();
            }
        } else if (saver.get("msAccessToken") != null && saver.get("msRefreshToken") != null) {
            try {
                MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                MicrosoftAuthResult response = authenticator.loginWithRefreshToken(saver.get("msRefreshToken"));

                saver.set("msAccessToken", response.getAccessToken());
                saver.set("msRefreshToken", response.getRefreshToken());
                saver.save();

                this.setAuthInfos(new AuthInfos(
                        response.getProfile().getName(),
                        response.getAccessToken(),
                        response.getProfile().getId()
                ));

                return true;
            } catch (MicrosoftAuthenticationException e) {
                saver.remove("msAccessToken");
                saver.remove("msRefreshToken");
                saver.save();
            }
        } else if (saver.get("offline-username") != null) {
            this.authInfos = new AuthInfos(saver.get("offline-username"), null, null);
            return true;
        }

        return false;
    }

    public AuthInfos getAuthInfos() {
        return authInfos;
    }

    public void setAuthInfos(AuthInfos authInfos) {
        this.authInfos = authInfos;
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
