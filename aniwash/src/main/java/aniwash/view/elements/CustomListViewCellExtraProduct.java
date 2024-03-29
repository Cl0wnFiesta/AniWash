package aniwash.view.elements;

import aniwash.entity.Appointment;
import aniwash.entity.Product;
import aniwash.viewmodels.DiscountProduct;
import aniwash.viewmodels.ShoppingCart;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * This is a custom JavaFX ListCell class for displaying extra products in a shopping cart with the
 * ability to delete them and update the total price.
 */
public class CustomListViewCellExtraProduct extends ListCell<DiscountProduct> {

    private HBox produtInfoHBox;
    private HBox produtNameHBox;
    private ListView listView;
    private Text totalPrice;
    private ShoppingCart cart;
    private Appointment appoitment;

    public CustomListViewCellExtraProduct(ListView listView, Text totalPrice, ShoppingCart cart,
            Appointment appoitment) {
        super();
        this.listView = listView;
        this.totalPrice = totalPrice;
        this.cart = cart;
        this.appoitment = appoitment;

        // Create the customer info HBox once

        Label nameLabel = new Label();
        nameLabel.setMinWidth(100);
        nameLabel.fontProperty().set(new javafx.scene.text.Font(13));
        Button deleteButton = new Button();
        deleteButton.fontProperty().set(new javafx.scene.text.Font(15));
        deleteButton.setMinWidth(10);
        deleteButton.setMinHeight(15);
        deleteButton.translateYProperty().set(-2);
        deleteButton.setStyle("-fx-background-color:  white; -fx-background:  white;");

        VBox nameBox = new VBox(nameLabel);
        VBox deleteBox = new VBox(deleteButton);

        nameBox.setTranslateX(10);
        produtNameHBox = new HBox(nameBox);
        nameBox.setPadding(new Insets(0, 50, 0, 0));
        VBox.setMargin(produtNameHBox, new Insets(10, 0, 0, 0)); // add margin of 10 pixels to top

        Label priceLabel = new Label();
        VBox priceBox = new VBox(priceLabel);
        priceLabel.setMinWidth(85);
        priceLabel.fontProperty().set(new javafx.scene.text.Font(13));

        produtInfoHBox = new HBox(produtNameHBox, priceBox, deleteBox);
        produtInfoHBox.setSpacing(20);
        VBox.setMargin(produtInfoHBox, new Insets(5, 0, 0, 0)); // add margin of 5 pixels to top
        produtInfoHBox.setStyle(
                "-fx-background-color: #ffffff; -fx-padding: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
    }

    @Override
    protected void updateItem(DiscountProduct product, boolean empty) {
        super.updateItem(product, empty);

        if (empty || product == null) {
            setText(null);
            setGraphic(null);
        } else {

            // Update the cell content with the customer information

            Label nameLabel = (Label) ((VBox) produtNameHBox.getChildren().get(0)).getChildren().get(0);
            nameLabel.setText(product.getName());

            Label priceLabel = (Label) ((VBox) produtInfoHBox.getChildren().get(1)).getChildren().get(0);
            priceLabel.setText(String.format("%.2f", product.getPrice()) + "€");

            Button deleteButton = (Button) ((VBox) produtInfoHBox.getChildren().get(2)).getChildren().get(0);
            deleteButton.setText("X");
            deleteButton.setOnAction(event -> {
                getListView().getItems().remove(product);
                listView.getItems().add(product.getName());
                if (appoitment != null) {
                    Product deleteProduct = cart.getProduct(product.getName());
                    appoitment.removeProduct(deleteProduct);
                }
                cart.removeProductString(product.getName());
                totalPrice.setText("Price " + String.valueOf(cart.getTotalDiscountedPrice() + "€"));
            });

            // Set cell content

            setGraphic(produtInfoHBox);
            setStyle("-fx-background-color: #f2f5f9; -fx-pref-height: 55;");
        }
    }

}
