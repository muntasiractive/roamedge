package com.roam.view.components;

import com.roam.controller.WikiController;
import com.roam.model.Wiki;
import com.roam.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.List;

/**
 * A dialog component for displaying wiki statistics and analytics.
 * <p>
 * This dialog presents an overview of wiki-related metrics including total wiki
 * count,
 * aggregate word counts, number of favorites, and average word count per wiki.
 * Statistics are presented in a clean grid layout with styled labels for easy
 * reading.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class StatisticsDialog extends Dialog<Void> {

    public StatisticsDialog(WikiController controller) {
        setTitle("Wiki Statistics");
        setHeaderText("Your Wiki Overview");
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Apply theme styling
        ThemeManager.getInstance().styleDialog(this);
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String textSecondary = isDark ? "#b0b0b0" : "#757575";
        String blueColor = "#4285f4";

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));

        // Overall stats
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(20);

        List<Wiki> allWikis = controller.loadAllWikis();

        long totalWikis = allWikis.size();
        long totalWords = allWikis.stream()
                .mapToLong(n -> n.getWordCount() != null ? n.getWordCount() : 0)
                .sum();
        long favorites = allWikis.stream().filter(n -> n.getIsFavorite() != null && n.getIsFavorite()).count();
        long avgWords = totalWikis > 0 ? totalWords / totalWikis : 0;

        addStat(grid, 0, 0, "Total Wikis", String.valueOf(totalWikis), blueColor, textSecondary);
        addStat(grid, 1, 0, "Total Words", String.format("%,d", totalWords), blueColor, textSecondary);
        addStat(grid, 0, 1, "Favorites", String.valueOf(favorites), blueColor, textSecondary);
        addStat(grid, 1, 1, "Avg Word Count", String.valueOf(avgWords), blueColor, textSecondary);

        mainContent.getChildren().add(grid);

        getDialogPane().setContent(mainContent);
        getDialogPane().setPrefWidth(400);
    }

    private void addStat(GridPane grid, int col, int row, String label, String value, String valueColor,
            String labelColor) {
        VBox box = new VBox(5);
        box.setAlignment(javafx.geometry.Pos.CENTER);

        Label valLabel = new Label(value);
        valLabel.setFont(Font.font("Poppins Bold", 28));
        valLabel.setStyle("-fx-text-fill: " + valueColor + ";");

        Label textLabel = new Label(label);
        textLabel.setFont(Font.font("Poppins", 13));
        textLabel.setStyle("-fx-text-fill: " + labelColor + ";");

        box.getChildren().addAll(valLabel, textLabel);
        grid.add(box, col, row);
    }
}
