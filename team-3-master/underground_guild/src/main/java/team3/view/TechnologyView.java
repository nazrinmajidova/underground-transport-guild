package team3.view;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class TechnologyView extends VBox {
    private enum TechState {
        AVAILABLE,
        LOCKED,
        RESEARCHED
    }

    private record TechEntry(String name, String effect, String cost, TechState state) {
    }

    private final GridPane grid = new GridPane();
    private final java.util.List<Button> techButtons = new java.util.ArrayList<>();

    public TechnologyView() {
        super(0);
        setPadding(new Insets(0));
        getStyleClass().add("panel-compact");
        setPrefWidth(400);
        setMaxWidth(Region.USE_PREF_SIZE);

        Label title = new Label("Technology");
        title.setMaxWidth(Double.MAX_VALUE);
        title.getStyleClass().add("panel-header");

        ColumnConstraints first = new ColumnConstraints();
        first.setPercentWidth(44);
        ColumnConstraints second = new ColumnConstraints();
        second.setPercentWidth(31);
        ColumnConstraints third = new ColumnConstraints();
        third.setPercentWidth(25);
        third.setHalignment(HPos.RIGHT);
        grid.getColumnConstraints().addAll(first, second, third);
        grid.setHgap(8);
        grid.setVgap(0);
        grid.getStyleClass().add("tech-grid");

        addHeaderRow(0, "Technology", "Effect", "Action");
        addTechRow(1, new TechEntry("Faster engines", "Speed +20%", "500", TechState.AVAILABLE));
        addTechRow(2, new TechEntry("Cargo expansion", "Capacity +30%", "750", TechState.LOCKED));
        addTechRow(3, new TechEntry("Efficient fuel", "Income +10%", "600", TechState.RESEARCHED));
        addTechRow(4, new TechEntry("Advanced tunnels", "Build cost -15%", "900", TechState.LOCKED));
        addTechRow(5, new TechEntry("Extra crew", "Workers +50", "400", TechState.RESEARCHED));

        getChildren().addAll(title, grid);
    }

    private void addHeaderRow(int rowIndex, String c1, String c2, String c3) {
        addCell(headerLabel(c1), 0, rowIndex);
        addCell(headerLabel(c2), 1, rowIndex);
        addCell(headerLabel(c3), 2, rowIndex);
    }

    private void addTechRow(int rowIndex, TechEntry entry) {
        boolean alternate = rowIndex % 2 == 0;
        Label nameLabel = cellLabel(entry.name(), false, alternate);
        Label effectLabel = cellLabel(entry.effect(), false, alternate);
        HBox actionCell = new HBox(8);
        actionCell.setAlignment(Pos.CENTER_RIGHT);
        actionCell.getStyleClass().add(alternate ? "table-row-alt" : "table-row");

        Button actionBtn = new Button(buttonText(entry.state()));
        actionBtn.getStyleClass().add(buttonClass(entry.state()));
        actionBtn.setMinWidth(110);
        actionBtn.setDisable(entry.state() != TechState.AVAILABLE);
        if (entry.state() == TechState.AVAILABLE) {
            actionBtn.setOnAction(e -> onResearch(actionBtn));
        }

        techButtons.add(actionBtn);

        Label costLabel = new Label(entry.state() == TechState.LOCKED ? "Tier" : "$" + entry.cost());
        costLabel.getStyleClass().add(entry.state() == TechState.LOCKED ? "tech-cost-locked" : "muted");
        actionCell.getChildren().addAll(costLabel, actionBtn);

        addCell(nameLabel, 0, rowIndex);
        addCell(effectLabel, 1, rowIndex);
        addCell(actionCell, 2, rowIndex);
    }

    private void onResearch(Button btn) {
        btn.setText("Researched");
        btn.getStyleClass().remove("btn-primary");
        btn.getStyleClass().add("btn-tech-done");
        btn.setDisable(true);
        unlockNextTech();
    }

    private void unlockNextTech() {
        for (Button techButton : techButtons) {
            if ("Locked".equals(techButton.getText())) {
                techButton.setText("Research");
                techButton.getStyleClass().remove("btn-tech-locked");
                techButton.getStyleClass().add("btn-primary");
                techButton.setDisable(false);
                techButton.setOnAction(e -> onResearch(techButton));
                break;
            }
        }
    }

    private String buttonText(TechState state) {
        return switch (state) {
            case AVAILABLE -> "Research";
            case LOCKED -> "Locked";
            case RESEARCHED -> "Researched";
        };
    }

    private String buttonClass(TechState state) {
        return switch (state) {
            case AVAILABLE -> "btn-primary";
            case LOCKED -> "btn-tech-locked";
            case RESEARCHED -> "btn-tech-done";
        };
    }

    private void addCell(javafx.scene.Node node, int columnIndex, int rowIndex) {
        GridPane.setHgrow(node, Priority.ALWAYS);
        grid.add(node, columnIndex, rowIndex);
    }

    private Label headerLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("panel-header");
        lbl.setMaxWidth(Double.MAX_VALUE);
        return lbl;
    }

    private Label cellLabel(String text, boolean grey, boolean alternate) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add(alternate ? "table-row-alt" : "table-row");
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setWrapText(true);
        if (grey) {
            lbl.getStyleClass().add("muted");
        }
        return lbl;
    }
}
