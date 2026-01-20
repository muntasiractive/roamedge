package com.roam;

import com.roam.layout.WindowResizeHelper;
import com.roam.service.DatabaseService;
import com.roam.service.SecurityContext;
import com.roam.service.SettingsService;
import com.roam.util.HibernateUtil;
import com.roam.util.ThemeManager;
import com.roam.view.GlobalSearchDialog;
import com.roam.view.LockScreenLayout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Main entry point for Roam.
 * 
 * Bootstraps the JavaFX application.
 * Initializes the database, settings, themes, and sets up the main window.
 * Handles security features like the lock screen.
 *
 * @author Roam Development Team
 * @version 1.0
 */

public class RoamApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(RoamApplication.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database FIRST
            logger.info("=".repeat(50));
            logger.info("🚀 Starting Roam Application");
            logger.info("=".repeat(50));

            DatabaseService.initializeDatabase();

            logger.info("=".repeat(50));

            // Load settings
            SettingsService settingsService = SettingsService.getInstance();
            String theme = settingsService.getSettings().getTheme();

            // Apply AtlantaFX theme using ThemeManager
            ThemeManager.getInstance().applyTheme(theme);

            // Load custom fonts
            loadFonts();

            // Use undecorated window with transparent background for rounded corners
            primaryStage.initStyle(StageStyle.TRANSPARENT);

            // Create main layout with custom title bar
            MainLayout mainLayout = new MainLayout(primaryStage);

            // Load custom CSS (will complement AtlantaFX)
            String css = Objects.requireNonNull(
                    getClass().getResource("/styles/application.css")).toExternalForm();

            // Check security - only show lock screen if both enabled AND PIN is set
            String pinHash = settingsService.getSettings().getPinHash();
            boolean hasPinSet = pinHash != null && !pinHash.isEmpty();

            // Get ThemeManager instance
            ThemeManager themeManager = ThemeManager.getInstance();
            boolean isDarkTheme = themeManager.isDarkTheme();

            if (SecurityContext.getInstance().isLockEnabled() && hasPinSet) {
                LockScreenLayout lockScreenLayout = new LockScreenLayout(primaryStage, () -> {
                    Scene scene = new Scene(mainLayout, 1024, 600);
                    scene.setFill(Color.TRANSPARENT);
                    scene.getStylesheets().add(css);
                    // Apply dark mode class if needed
                    if (isDarkTheme) {
                        scene.getRoot().getStyleClass().add("dark");
                    }
                    themeManager.setMainScene(scene);
                    primaryStage.setScene(scene);

                    // Setup global keyboard shortcuts for main scene
                    setupGlobalShortcuts(scene, primaryStage, mainLayout);

                    // Re-attach resize helper to new layout
                    new WindowResizeHelper(primaryStage, mainLayout);
                });

                Scene lockScene = new Scene(lockScreenLayout, 1024, 700);
                lockScene.setFill(Color.TRANSPARENT);
                lockScene.getStylesheets().add(css);
                // Apply dark mode class to lock screen too
                if (isDarkTheme) {
                    lockScene.getRoot().getStyleClass().add("dark");
                }
                primaryStage.setScene(lockScene);

                // Enable window resizing for lock screen
                new WindowResizeHelper(primaryStage, lockScreenLayout);
            } else {
                // No lock screen - proceed directly to main app
                Scene scene = new Scene(mainLayout, 1024, 700);
                scene.setFill(Color.TRANSPARENT);
                scene.getStylesheets().add(css);
                // Apply dark mode class if needed
                if (isDarkTheme) {
                    scene.getRoot().getStyleClass().add("dark");
                }

                // Setup global keyboard shortcuts
                setupGlobalShortcuts(scene, primaryStage, mainLayout);

                themeManager.setMainScene(scene);
                primaryStage.setScene(scene);
                SecurityContext.getInstance().setAuthenticated(true); // Auto-authenticate when no lock
            }

            // Set window properties
            primaryStage.setTitle("Roam");

            // Set minimum window size
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(700);

            // Enable window resizing on edges (for undecorated window)
            new WindowResizeHelper(primaryStage, mainLayout);

            // Set application icon (for taskbar)
            try {
                Image icon = new Image(
                        Objects.requireNonNull(
                                getClass().getResourceAsStream("/icons/roam-icon.png")));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                logger.warn("Failed to load application icon: {}", e.getMessage());
            }

            // Center and show window
            primaryStage.centerOnScreen();
            primaryStage.show();

            logger.info("✓ Application started successfully");

        } catch (Exception e) {
            logger.error("✗ Failed to start application: {}", e.getMessage(), e);
            e.printStackTrace();
            showErrorDialog("Database Error",
                    "Failed to initialize database. The application will now close.",
                    e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        // Shutdown resources on application exit
        logger.info("=".repeat(50));
        logger.info("🛑 Shutting down Roam Application");
        logger.info("=".repeat(50));

        // Shutdown thread pools gracefully
        com.roam.util.ThreadPoolManager.getInstance().gracefulShutdown();

        // Shutdown Hibernate
        HibernateUtil.shutdown();

        logger.info("✓ Application shutdown complete");
    }

    private void loadFonts() {
        try {
            Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Poppins-Regular.ttf"), 14);
            Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Poppins-Medium.ttf"), 14);
            Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Poppins-SemiBold.ttf"), 14);
            Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Poppins-Bold.ttf"), 14);
            logger.info("✓ Fonts loaded successfully");
        } catch (Exception e) {
            logger.error("✗ Failed to load fonts: {}", e.getMessage());
        }
    }

    /**
     * Setup global keyboard shortcuts for the application
     */
    private void setupGlobalShortcuts(Scene scene, Stage stage, MainLayout mainLayout) {
        // Ctrl+K - Open Global Search
        KeyCodeCombination searchShortcut = new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN);

        scene.getAccelerators().put(searchShortcut, () -> {
            openGlobalSearch(stage, mainLayout);
        });

        logger.info("✓ Global keyboard shortcuts registered");
    }

    /**
     * Open the global search dialog
     */
    private void openGlobalSearch(Stage owner, MainLayout mainLayout) {
        GlobalSearchDialog searchDialog = new GlobalSearchDialog(owner);

        // Wire up navigation callback
        searchDialog.setOnNavigate(viewType -> {
            // Navigate to the requested view through MainLayout
            mainLayout.navigateTo(viewType);
        });

        searchDialog.showAndWait();
    }

    private void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}