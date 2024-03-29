package aniwash.view.controllers;

import aniwash.MainApp;
import aniwash.dao.CustomerDao;
import aniwash.dao.ICustomerDao;
import aniwash.entity.Animal;
import aniwash.entity.Appointment;
import aniwash.entity.Customer;
import aniwash.view.elements.CustomListViewCellAnimal;
import aniwash.view.elements.CustomListViewCellAppointment;
import aniwash.view.utilities.ControllerUtilities;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * The EditCustomerController class is responsible for handling the editing of
 * customer information and
 * managing the display of associated pets and appointments.
 */
public class EditCustomerController {
    // Create text fields for Customer section
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField postalCodeField;

    // Create text fields for Pets section
    @FXML
    private TextField petNameField;
    @FXML
    private TextField petTypeField;
    @FXML
    private TextField petBreedField;
    @FXML
    private TextField petAgeField;
    @FXML
    private TextField petDescriptionField;

    @FXML
    private Button saveButton, removeCustomer;
    private static final ObservableList<Animal> animals = FXCollections.observableArrayList();
    private static final ObservableList<Appointment> appointmentsList = FXCollections.observableArrayList();
    @FXML
    private ListView<Animal> listView;
    @FXML
    private ListView<Appointment> appointmentListView;

    private static Customer customer;
    private final CustomersController customersController = new CustomersController();
    private ResourceBundle bundle;

    /**
     * This function initializes the UI elements and populates them with data from
     * the selected
     * customer object.
     */
    public void initialize() {
        bundle = MainApp.getBundle();
        customer = customersController.getSelectedCustomer();

        animals.clear();
        appointmentsList.clear();

        animals.addAll(customer.getAnimals());
        appointmentsList.addAll(customer.getAppointments());

        appointmentListView.setItems(appointmentsList);
        listView.setItems(animals);
        listView.setCellFactory(listView -> new CustomListViewCellAnimal());
        appointmentListView.setCellFactory(appointmentListView -> new CustomListViewCellAppointment());

        Background background = new Background(
                new BackgroundFill(Color.web("#f2f5f9"), CornerRadii.EMPTY, Insets.EMPTY));
        appointmentListView.setPlaceholder(new Label("No appointments") {
            @Override
            protected void updateBounds() {
                super.updateBounds();
                setBackground(background);
            }
        });

        listView.setStyle("-fx-background-color:  #f2f5f9; -fx-background:  #f2f5f9;");
        appointmentListView.setStyle("-fx-background-color:  #f2f5f9; -fx-background:  #f2f5f9;");

        nameField.setText(customer.getName());
        phoneField.setText(customer.getPhone());
        emailField.setText(customer.getEmail());
        addressField.setText(customer.getAddress());
        postalCodeField.setText(customer.getPostalCode());

        listView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                Animal selectedItem = listView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    customer.removeAnimal(selectedItem);

                    listView.getItems().remove(selectedItem);
                }
            }
        });

        removeCustomer.setOnAction(event -> {
            customersController.removeCustomer(customer);
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        });

        // Set the text fields to be editable
        saveButton.disableProperty().bind(
                // Bind the save button's disable property to a BooleanBinding
                // that checks if all mandatory fields have been filled
                nameField.textProperty().isEmpty()
                        .or(phoneField.textProperty().isEmpty())
                        .or(emailField.textProperty().isEmpty()));
    }

    /**
     * This function updates a customer object with input values and saves it to a database.
     * 
     * @param event An ActionEvent object that represents the event that occurred when the "Save"
     * button was clicked.
     */
    @FXML
    public void onSaveButtonClicked(ActionEvent event) {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String postalCode = postalCodeField.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            // Show error message if mandatory fields are empty
            ControllerUtilities.showAlert(bundle.getString("fillAllFieldsText"));
            return;
        }

        if (!ControllerUtilities.isNumeric(phone)
                || !postalCodeField.getText().trim().isEmpty() && !ControllerUtilities.isNumeric(postalCode)) {
            // Show error message if phone or postal code fields contain non-numeric
            // characters
            ControllerUtilities.showAlert(bundle.getString("fillNumericText"));
            return;
        }

        // All input values are valid, edit the Customer object

        customer.setAddress(address);
        customer.setEmail(email);
        customer.setName(name);
        customer.setPhone(phone);
        customer.setPostalCode(postalCode);

        ICustomerDao customerDao = new CustomerDao();
        customerDao.update(customer);

        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void cancelButtonClicked(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    public void setCustomer(Customer customers) {
        customer = customers;
    }

    public Customer getCustomer() {
        return customer;
    }

    @FXML
    public void createNewAnimal() throws IOException {
        Stage popupStage = new Stage();
        ControllerUtilities.newAnimal(popupStage);

        popupStage.setOnHidden(view -> listView.setItems(FXCollections.observableList(customer.getAnimalList())));
        CreateNewAnimalController.setCustomer(customer);
    }

}
