package aniwash.view.controllers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import aniwash.MainApp;
import aniwash.dao.CustomerDao;
import aniwash.dao.ICustomerDao;
import aniwash.entity.Customer;
import aniwash.view.elements.CustomListViewCell;
import aniwash.view.utilities.ControllerUtilities;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * The CustomersController class controls the behavior of the customer list view and handles user
 * interactions with the list.
 */
public class CustomersController {
    private static Customer selectedCustomer;
    @FXML
    private ListView<Customer> listView;
    @FXML
    private Text customerCount;
    @FXML
    private Button newCustomer;
    @FXML
    private TextField searchField;

    private ICustomerDao customerDao = new CustomerDao();

    /*
     * public void test() {
     * for (Customer customer : customers) {
     * customer.addAnimal(new Animal("Testi111", "Eläin", "TestiEläin", 10,
     * "Tämä eläin on testi"));
     * customer.addAppointment(new Appointment(ZonedDateTime.now(),
     * (ZonedDateTime.now()), "Cancer Treatment"));
     *
     * }
     * }
     */

    private static FXMLLoader loadFXML(String fxml) {
        return new FXMLLoader(MainApp.class.getResource("view/" + fxml + ".fxml"));
    }

    /**
     * This function initializes a ListView with custom ListCells, sets a filter predicate for
     * searching, and opens a popup window to display customer info when a customer is double-clicked.
     */
    public void initialize() {
        // test();
        // Bind the ListView to the ObservableList

        customerDao = new CustomerDao();
        AtomicReference<ObservableList<Customer>> customers = new AtomicReference<>(
                FXCollections.observableList(customerDao.findAll()));
        listView.setItems(customers.get());

        // Bind the customerCount text property to the size of the list

        customerCount.setText(String.valueOf(customers.get().size()));

        // Set the cell factory to create custom ListCells

        listView.setCellFactory(listView -> new CustomListViewCell());
        listView.setStyle("-fx-background-color:  #f2f5f9; -fx-background:  #f2f5f9;");

        // Set the placeholder text for the ListView

        Background background = new Background(
                new BackgroundFill(Color.web("#f2f5f9"), CornerRadii.EMPTY, Insets.EMPTY));
        listView.setPlaceholder(new Label("No items") {
            @Override
            protected void updateBounds() {
                super.updateBounds();
                setBackground(background);
            }
        });

        // Bind the searchField text property to the filter predicate property.

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            Predicate<Customer> filter = customer -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                } else {
                    return customer.getName().toLowerCase().contains(newValue.toLowerCase());
                }
            };
            ObservableList<Customer> filteredCustomers = customers.get().filtered(filter);
            listView.setItems(filteredCustomers);
        });

        // Double click on a customer to open the customer info popup window

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                selectedCustomer = listView.getSelectionModel().getSelectedItem();
                // Create and show the popup window
                // Pass the selected customer object to the popup window to display its info
                try {
                    Stage stage = new Stage();
                    stage.setOnHidden(
                            view -> listView.setItems(FXCollections.observableList(customerDao.findAll())));
                    ControllerUtilities.editCustomer(stage);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    public void newCustomer() throws IOException {
        Stage stage = new Stage();
        stage.setOnHidden(event -> listView.setItems(FXCollections.observableList(customerDao.findAll())));
        ControllerUtilities.newCustomer(stage);
    }

    public Customer getSelectedCustomer() {
        return selectedCustomer;
    }

    @FXML
    private void mySchedule() throws IOException {
        MainApp.setRoot("schedule");
    }

    @FXML
    private void dashBoard() throws IOException {
        MainApp.setRoot("mainView");
    }

    @FXML
    private void products() throws IOException {
        MainApp.setRoot("productsView");
    }

    @FXML
    private void admin() throws IOException {
        MainApp.setRoot("AdminPanel");
    }

    public void removeCustomer(Customer customer) {
        customerDao.deleteById(customer.getId());
    }
}
