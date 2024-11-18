package victorgponce.com.ui.panels.pages.content;

import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.download.DownloadList;
import fr.flowarg.flowupdater.download.IProgressCallback;
import fr.flowarg.flowupdater.download.Step;
import fr.flowarg.flowupdater.download.json.CurseFileInfo;
import fr.flowarg.flowupdater.utils.ModFileDeleter;
import fr.flowarg.flowupdater.versions.AbstractForgeVersion;
import fr.flowarg.flowupdater.versions.ForgeVersionBuilder;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.flowarg.materialdesignfontfx.MaterialDesignIcon;
import fr.flowarg.materialdesignfontfx.MaterialDesignIconView;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.theshark34.openlauncherlib.util.Saver;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import victorgponce.com.Launcher;
import victorgponce.com.game.MinecraftInfos;
import victorgponce.com.ui.PanelManager;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import victorgponce.com.utils.ConfigDownloader;

import java.io.*;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;

import static victorgponce.com.utils.ConfigDownloader.descomprimir;

public class Home extends ContentPanel {

    private static final Logger log = LoggerFactory.getLogger(Home.class);
    private final Saver saver = Launcher.getInstance().getSaver();
    GridPane boxPane = new GridPane();
    GridPane boxPaneBottom = new GridPane();
    ProgressBar progressBar = new ProgressBar();
    Label stepLabel = new Label();
    Label fileLabel = new Label();
    boolean isDownloading = false;
    private static final String CONFIG_URL = "https://ponchisaohosting.xyz/downloads/cosmere/config.zip";
    String data;

    @Override
    public String getName() {
        return "home";
    }

    @Override
    public String getSylesheetPath() {
        return "css/content/home.css";
    }

    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);

        try {
            // Inicialización de la interfaz de usuario
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setValignment(VPos.CENTER);
            rowConstraints.setMinHeight(75);
            rowConstraints.setMaxHeight(75);
            this.layout.getRowConstraints().addAll(rowConstraints, new RowConstraints());
            boxPane.getStyleClass().add("box-pane");
            setCanTakeAllSize(boxPane);
            boxPane.setPadding(new Insets(20));
            this.layout.add(boxPane, 0, 0);
            this.layout.getStyleClass().add("home-layout");

            RowConstraints rowConstraintsBottom = new RowConstraints();
            rowConstraintsBottom.setValignment(VPos.CENTER);
            rowConstraintsBottom.setMinHeight(525);
            rowConstraintsBottom.setMaxHeight(525);
            this.layout.getRowConstraints().addAll(rowConstraintsBottom, new RowConstraints());
            boxPaneBottom.getStyleClass().add("box-pane-bottom");
            setCanTakeAllSize(boxPaneBottom);
            boxPaneBottom.setPadding(new Insets(20));
            this.layout.add(boxPaneBottom, 0, 3);
            this.layout.getStyleClass().add("home-layout-bottom");

            progressBar.getStyleClass().add("download-progress");
            stepLabel.getStyleClass().add("download-status");
            fileLabel.getStyleClass().add("download-status");

            progressBar.setTranslateY(-15);
            setCenterH(progressBar);
            setCanTakeAllWidth(progressBar);

            stepLabel.setTranslateY(5);
            setCenterH(stepLabel);
            setCanTakeAllSize(stepLabel);

            fileLabel.setTranslateY(20);
            setCenterH(fileLabel);
            setCanTakeAllSize(fileLabel);

            this.showPlayButton();
            this.showResetConfigButton();
        } catch (Exception e) {
            log.error("Error during initialization", e);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("An error occurred during initialization.");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });
        }
    }


    private void showPlayButton() {
        boxPane.getChildren().clear();
        Button playBtn = new Button("Play");
        final var playIcon = new MaterialDesignIconView<>(MaterialDesignIcon.G.GAMEPAD);
        playIcon.getStyleClass().add("play-icon");
        setCanTakeAllSize(playBtn);
        setCenterH(playBtn);
        setCenterV(playBtn);
        playBtn.getStyleClass().add("play-btn");
        playBtn.setGraphic(playIcon);
        playBtn.setOnMouseClicked(e -> this.play());
        boxPane.getChildren().add(playBtn);
    }

    private void showResetConfigButton() {
        Button resetButton = new Button("Reset");
        final var resetIcon = new MaterialDesignIconView<>(MaterialDesignIcon.R.RELOAD);
        resetIcon.getStyleClass().add("play-icon");
        setCanTakeAllSize(resetButton);
        setCenterH(resetButton);
        setCenterV(resetButton);
        resetButton.getStyleClass().add("play-btn");
        resetButton.setGraphic(resetIcon);
        resetButton.setOnMouseClicked(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Submit");
            alert.setHeaderText("Are you sure you want to delete all the files and restart the installation?");
            alert.setContentText("""
                    This will remove all the launcher and Cosmere data on your PC.
                    \s
                    Once you press the accept button the launcher will close, \
                    open it again to re-generate the files
                    \s
                     \
                    This is normally used if you are through some troubles with the game like not opening or so""");

            // Mostrar la alerta y esperar la respuesta del usuario
            ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);

            // Procesar la respuesta
            if (result == ButtonType.OK) {
                System.out.println("User confirmed deletion.");
                new Thread(this::resetConfig).start();
            } else {
                System.out.println("User canceled the operation.");
                alert.close();
            }
        });

        boxPaneBottom.getChildren().add(resetButton);
    }

    private void resetConfig() {
        File configFolder = new File(String.valueOf(Launcher.getInstance().getLauncherDir()));
        deleteDirectoryRecursively(configFolder);
        Platform.exit();
        System.exit(0);
    }

    private void play() {
        isDownloading = true;
        boxPane.getChildren().clear();
        setPogress(0, 0);
        boxPane.getChildren().addAll(progressBar, stepLabel, fileLabel);

        try {
            File myObj = new File(Launcher.getInstance().getLauncherDir() + "/done.txt");
            if (myObj.exists()) {
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) {
                    data = myReader.nextLine();
                }
                myReader.close();
            }
            else {
                data = "false";
            }
        } catch (FileNotFoundException e) {
            logger.info("An error occurred.");
            logger.info(String.valueOf(e));
        }

        if (!data.equals("true")) {
            new Thread(this::startConfigDownloadThread).start();
            // Check & write if the config folder is already installed
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(Launcher.getInstance().getLauncherDir() + "/done.txt"));
                writer.write("true");

                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        new Thread(this::update).start();
    }

    public void update() {
        IProgressCallback callback = new IProgressCallback() {
            private final DecimalFormat decimalFormat = new DecimalFormat("#.#");
            private String stepTxt = "";
            private String percentTxt = "0.0%";

            @Override
            public void step(Step step) {
                Platform.runLater(() -> {
                    stepTxt = StepInfo.valueOf(step.name()).getDetails();
                    setStatus(String.format("%s (%s)", stepTxt, percentTxt));
                });
            }

            @Override
            public void update(DownloadList.DownloadInfo info) {
                Platform.runLater(() -> {
                    percentTxt = decimalFormat.format(info.getDownloadedBytes() * 100.d / info.getTotalToDownloadBytes()) + "%";
                    setStatus(String.format("%s (%s)", stepTxt, percentTxt));
                    setPogress(info.getDownloadedBytes(), info.getTotalToDownloadBytes());
                });
            }

            @Override
            public void onFileDownloaded(Path path) {
                Platform.runLater(() -> {
                    String p = path.toString();
                    fileLabel.setText("..." + p.replace(Launcher.getInstance().getLauncherDir().toFile().getAbsolutePath(), ""));
                });
            }
        };

        try {
            final VanillaVersion vanillaVersion = new VanillaVersion.VanillaVersionBuilder()
                    .withName(MinecraftInfos.GAME_VERSION)
                    .build();

            List<CurseFileInfo> curseMods = CurseFileInfo.getFilesFromJson(MinecraftInfos.CURSE_MODS_LIST_URL);

            final AbstractForgeVersion forge = new ForgeVersionBuilder(MinecraftInfos.FORGE_VERSION_TYPE)
                    .withForgeVersion(MinecraftInfos.FORGE_VERSION)
                    .withCurseMods(curseMods)
                    .withFileDeleter(new ModFileDeleter(true))
                    .build();

            final FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                    .withVanillaVersion(vanillaVersion)
                    .withModLoaderVersion(forge)
                    .withLogger(Launcher.getInstance().getLogger())
                    .withProgressCallback(callback)
                    .build();

            updater.update(Launcher.getInstance().getLauncherDir());
            this.startGame(updater.getVanillaVersion().getName());
        } catch (Exception e) {
            Launcher.getInstance().getLogger().printStackTrace(e);
            Platform.runLater(() -> this.panelManager.getStage().show());
        }
    }

    public void startGame(String gameVersion) {
        try {
            NoFramework noFramework = new NoFramework(
                    Launcher.getInstance().getLauncherDir(),
                    Launcher.getInstance().getAuthInfos(),
                    GameFolder.FLOW_UPDATER
            );

            noFramework.getAdditionalVmArgs().add(this.getRamArgsFromSaver());

            Process p = noFramework.launch(gameVersion, MinecraftInfos.FORGE_VERSION.split("-")[1], NoFramework.ModLoader.FORGE);

            Platform.runLater(() -> {
                try {
                    p.waitFor();
                    Platform.exit();
                } catch (InterruptedException e) {
                    Launcher.getInstance().getLogger().printStackTrace(e);
                }
            });
        } catch (Exception e) {
            Launcher.getInstance().getLogger().printStackTrace(e);
        }
    }

    public String getRamArgsFromSaver() {
        int val = 1024;
        try {
            if (saver.get("maxRam") != null) {
                val = Integer.parseInt(saver.get("maxRam"));
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException error) {
            saver.set("maxRam", String.valueOf(val));
            saver.save();
        }

        return "-Xmx" + val + "M";
    }

    public void setStatus(String status) {
        this.stepLabel.setText(status);
    }

    public void setPogress(double current, double max) {
        this.progressBar.setProgress(current / max);
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    private void startConfigDownloadThread() {
        try {
            File configFolder = new File(Launcher.getInstance().getLauncherDir() + "/config/");
            File optionsFile = new File(Launcher.getInstance().getLauncherDir() + "/options.txt");

            if (configFolder.exists()) {
                logger.info("Carpeta 'config' encontrada, procedo a eliminarla y recrearla vacía");
                deleteDirectoryRecursively(configFolder);
            } else {
                logger.info("Carpeta 'config' no encontrada, procedo a crear la carpeta y a descargar");
            }

            configFolder.mkdir();

            if (optionsFile.exists()) {
                logger.info("archivo 'options.txt' encontrado, procedo a eliminarlo y recrearlo vacío");
                deleteDirectoryRecursively(configFolder);
            } else {
                logger.info("archivo 'options.txt' encontrado, procedo a descargarlo");
            }

            ConfigDownloader.configDownloader(CONFIG_URL, Launcher.getInstance().getLauncherDir().toString() + "/config.zip");
            descomprimir(Launcher.getInstance().getLauncherDir().toString() + "/config.zip", Launcher.getInstance().getLauncherDir().toString() + "/config/");
        } catch (IOException e) {
            logger.info("Ocurrió un error al descargar el archivo: " + e.getMessage());
        }
    }

    public void deleteDirectoryRecursively(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteDirectoryRecursively(file); // Llamada recursiva
            }
        }
        dir.delete(); // Finalmente eliminamos el archivo o directorio vacío
    }


    public enum StepInfo {
        READ("Reading the JSON file..."),
        DL_LIBS("Downloading libraries..."),
        DL_ASSETS("Downloading resources..."),
        EXTRACT_NATIVES("Extracting native files..."),
        FORGE("Installing Forge..."),
        FABRIC("Installing Fabric..."),
        MODS("Downloading mods..."),
        EXTERNAL_FILES("Downloading external files..."),
        POST_EXECUTIONS("Running post-installation tasks..."),
        MOD_LOADER("Installing mod loader..."),
        INTEGRATION("Integrating mods..."),
        END("Done!");

        final String details;

        StepInfo(String details) {
            this.details = details;
        }

        public String getDetails() {
            return details;
        }
    }
}
