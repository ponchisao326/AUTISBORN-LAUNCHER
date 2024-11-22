package victorgponce.com.ui.panels.pages.content;

import fr.flowarg.flowcompat.Platform;
import oshi.hardware.GlobalMemory;
import victorgponce.com.Launcher;
import victorgponce.com.ui.PanelManager;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import oshi.SystemInfo;
import fr.flowarg.materialdesignfontfx.MaterialDesignIcon;
import fr.flowarg.materialdesignfontfx.MaterialDesignIconView;
import fr.theshark34.openlauncherlib.util.Saver;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Settings extends ContentPanel{

    private final Saver saver = Launcher.getInstance().getSaver();
    GridPane contentPane = new GridPane();

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public String getSylesheetPath() {
        return "css/content/settings.css";
    }

    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);

        // Background
        this.layout.getStyleClass().add("settings-layout");
        this.layout.setPadding(new Insets(40));
        setCanTakeAllSize(this.layout);

        // Content
        contentPane.getStyleClass().add("content-pane");
        setCanTakeAllSize(contentPane);
        this.layout.getChildren().add(contentPane);

        // Titre
        Label title = new Label("Settings");
        title.setFont(Font.font("Consolas", FontWeight.BOLD, FontPosture.REGULAR, 25f));
        title.getStyleClass().add("settings-title");
        setLeft(title);
        setCanTakeAllSize(title);
        setTop(title);
        title.setTextAlignment(TextAlignment.LEFT);
        title.setTranslateY(40d);
        title.setTranslateX(25d);
        contentPane.getChildren().add(title);

        // RAM
        Label ramLabel = new Label("Max Memory");
        ramLabel.getStyleClass().add("settings-labels");
        setLeft(ramLabel);
        setCanTakeAllSize(ramLabel);
        setTop(ramLabel);
        ramLabel.setTextAlignment(TextAlignment.LEFT);
        ramLabel.setTranslateX(25d);
        ramLabel.setTranslateY(100d);
        contentPane.getChildren().add(ramLabel);

        // RAM Slider
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getStyleClass().add("ram-selector");
        for(int i = 512; i <= Math.ceil(memory.getTotal() / Math.pow(1024, 2)); i+=512) {
            comboBox.getItems().add(i/1024.0+" Gb");
        }

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

        if (comboBox.getItems().contains(val/1024.0+" Gb")) {
            comboBox.setValue(val / 1024.0 + " Gb");
        } else {
            comboBox.setValue("1.0 Gb");
        }

        setLeft(comboBox);
        setCanTakeAllSize(comboBox);
        setTop(comboBox);
        comboBox.setTranslateX(35d);
        comboBox.setTranslateY(130d);
        contentPane.getChildren().add(comboBox);

        // .Minecraft accessor button

        Button minecraftButton = new Button("");
        minecraftButton.getStyleClass().add("goto-btn");
        final var minecraftButtonView = new MaterialDesignIconView<>(MaterialDesignIcon.F.FOLDER);
        minecraftButtonView.getStyleClass().add("save-icon");
        minecraftButton.setGraphic(minecraftButtonView);
        setCanTakeAllSize(minecraftButton);
        setLeft(minecraftButton);
        setTop(minecraftButton);
        minecraftButton.setTranslateX(200d);
        minecraftButton.setTranslateY(120d);

        // .Minecraft accessor button
        minecraftButton.setOnMouseClicked(e -> {
            Path path = Launcher.getInstance().getLauncherDir().toAbsolutePath();
            File directory = new File(String.valueOf(path));

            switch (Platform.getCurrentPlatform()) {
                case WINDOWS -> {
                    try {
                        Runtime.getRuntime().exec("explorer " + directory.getAbsolutePath());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                }
                case LINUX -> {
                    try {
                        Runtime.getRuntime().exec(
                                new String[]{"sh", "-c", "/usr/bin/xdg-open " + "'" + directory.getAbsolutePath() + "'"}
                        );
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                }
                case MAC -> {
                    try {
                        Runtime.getRuntime().exec(new String[]{"/usr/bin/open", directory.getAbsolutePath()});
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                }
                default -> {

                }
            }
        });

        contentPane.getChildren().add(minecraftButton);


        /*
         * Save Button
         */
        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("save-btn");
        final var iconView = new MaterialDesignIconView<>(MaterialDesignIcon.F.FLOPPY);
        iconView.getStyleClass().add("save-icon");
        saveBtn.setGraphic(iconView);
        setCanTakeAllSize(saveBtn);
        setBottom(saveBtn);
        setCenterH(saveBtn);
        saveBtn.setOnMouseClicked(e -> {
            double _val = Double.parseDouble(comboBox.getValue().replace(" Gb", ""));
            _val *= 1024;
            saver.set("maxRam", String.valueOf((int) _val));
        });
        contentPane.getChildren().add(saveBtn);
    }
}
