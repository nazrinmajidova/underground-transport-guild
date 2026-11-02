package team3.view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import team3.service.GameService;

public class EconomyView extends VBox {
    private final Label totalIncomeValue = valueLabel();
    private final Label totalExpensesValue = valueLabel();
    private final Label netProfitValue = valueLabel();
    private final Label deliveryIncomeValue = valueLabel();
    private final Label maintenanceExpenseValue = valueLabel();
    private final Label otherExpensesValue = valueLabel();
    
    public EconomyView() {
        super(0);
        setPadding(new Insets(0));
        getStyleClass().add("panel-compact");
        setPrefWidth(360);
        setMaxWidth(Region.USE_PREF_SIZE);
 
        Label title = new Label("Economy overview");
        title.getStyleClass().add("panel-header");
        title.setMaxWidth(Double.MAX_VALUE);
 
        VBox content = new VBox(3);
        content.setPadding(new Insets(6, 8, 8, 8));
 
        content.getChildren().addAll(
            row("Total income:", totalIncomeValue, false),
            row("Total expenses:", totalExpensesValue, true),
            row("Net profit:", netProfitValue, false),
            sectionLabel("Income sources"),
            indentRow("Delivery income", deliveryIncomeValue, true),
            sectionLabel("Expenses"),
            indentRow("Maintenance", maintenanceExpenseValue, false),
            indentRow("Other costs", otherExpensesValue, true)
        );
 
        getChildren().addAll(title, content);
        updateFinancialSnapshot(new GameService.FinancialSnapshot(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false));
    }
  
    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("section-label");
        return lbl;
    }
 
    private HBox row(String key, Label valueLabel, boolean alternate) {
        Label k = new Label(key);
        k.getStyleClass().add("table-key");
        valueLabel.getStyleClass().add("table-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox hbox = new HBox(k, spacer, valueLabel);
        hbox.getStyleClass().add(alternate ? "table-row-alt" : "table-row");
        return hbox;
    }
 
    private HBox indentRow(String key, Label valueLabel, boolean alternate) {
        HBox hbox = row(key, valueLabel, alternate);
        hbox.setPadding(new Insets(0, 0, 0, 16));
        return hbox;
    }

    private Label valueLabel() {
        return new Label();
    }

    /**
     * Updates all economy labels from the active game financial snapshot.
     */
    public void updateFinancialSnapshot(GameService.FinancialSnapshot snapshot) {
        totalIncomeValue.setText(formatMoney(snapshot.totalIncome()));
        totalExpensesValue.setText(formatMoney(snapshot.totalExpenses()));

        float net = snapshot.totalIncome() - snapshot.totalExpenses();
        netProfitValue.setText((net >= 0 ? "+" : "") + formatMoney(net));
        netProfitValue.getStyleClass().removeAll("money-good", "money-bad");
        netProfitValue.getStyleClass().add(net >= 0 ? "money-good" : "money-bad");

        deliveryIncomeValue.setText(formatMoney(snapshot.deliveryIncome()));
        maintenanceExpenseValue.setText(formatMoney(snapshot.maintenanceExpense()));
        float otherExpenses = Math.max(0f, snapshot.totalExpenses() - snapshot.maintenanceExpense());
        otherExpensesValue.setText(formatMoney(otherExpenses));
    }

    private String formatMoney(float amount) {
        return String.format("$%.2f", amount);
    }
}
