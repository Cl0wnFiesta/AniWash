package aniwash.view.controllers;

import aniwash.MainApp;
import aniwash.entity.Animal;
import aniwash.entity.Appointment;
import aniwash.entity.Customer;
import aniwash.entity.Product;
import aniwash.view.elements.CreatePopUp;
import aniwash.view.elements.CustomListViewCellCustomer;
import aniwash.view.elements.CustomListViewCellExtraProduct;
import aniwash.viewmodels.DiscountProduct;
import aniwash.viewmodels.MainViewModel;
import aniwash.viewmodels.ShoppingCart;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.TimeField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ResourceBundle;

import static aniwash.view.utilities.ControllerUtilities.*;

/**
 * The EditAppointmentController class is responsible for handling user input and updating appointments
 * in the appointment scheduling system.
 */
public class EditAppointmentController extends CreatePopUp {

    private final MainViewModel mainViewModel = new MainViewModel();

    @FXML
    private TextArea descriptionArea, servicePane;
    @FXML
    private AnchorPane selectedProductPane;
    @FXML
    private Text selectedProductTitle, selectedProduct, selectedProductCost, selectedProductCostDiscount,
            extraProductTitle, priceText, setDiscountTitle;
    @FXML
    private TextField setDiscount, searchField;
    @FXML
    private Button deleteSelectedProductBtn, saveBtn, applyBtn;
    @FXML
    private ListView<DiscountProduct> extraProducts;
    @FXML
    private ListView<String> services, petList;
    @FXML
    private ListView<Customer> personList;
    @FXML
    private Rectangle mainProductRect, mainProductBackground;
    @FXML
    private DatePicker date = new DatePicker();
    @FXML
    private TimeField startTime = new TimeField();
    @FXML
    private TimeField endTime = new TimeField();
    private Entry<Appointment> newEntry;
    private ObservableList<Calendar<Product>> calendarObservableList;
    private ObservableList<Customer> customerObservableList;
    private final ShoppingCart cart = new ShoppingCart();
    private ResourceBundle bundle;

    /**
     * This function initializes various UI elements and sets event listeners for mouse clicks and
     * button actions in a Java application.
     */
    public void initialize() {
        bundle = MainApp.getBundle();
        newEntry = (Entry<Appointment>) getArg();
        // services.getItems().add(" Create new service +");
        petList.getItems().add("                                   Create new pet  +");
        date.setValue(newEntry.getStartDate());
        startTime.setValue(newEntry.getStartTime());
        endTime.setValue(newEntry.getEndTime());
        // Initialize the person table with the three columns.
        personList.setCellFactory(personList -> new CustomListViewCellCustomer());
        extraProducts.setCellFactory(extraProducts -> new CustomListViewCellExtraProduct(services, priceText, cart,
                newEntry.getUserObject()));
        personList.setStyle("-fx-background-color: #f4f4f4; -fx-background: #f4f4f4;");
        // Set the placeholder text for the ListView
        Background background = new Background(
                new BackgroundFill(Color.web("#f4f4f4"), CornerRadii.EMPTY, Insets.EMPTY));
        personList.setPlaceholder(new Label("No items") {
            @Override
            protected void updateBounds() {
                super.updateBounds();
                setBackground(background);
            }
        });
        // Add data to the table
        calendarObservableList = FXCollections.observableArrayList(mainViewModel.getCalendarMap().values());
        calendarObservableList.forEach(service -> services.getItems().addAll(service.getName()));
        personList.setOnMouseClicked(
                getPersonMouseEvent(mainViewModel, customerObservableList, personList, petList, newEntry, services));
        // services.setOnMouseClicked(getProductMouseEvent(mainViewModel, services,
        // newEntry, petList));
        petList.setOnMouseClicked(
                getAnimalMouseEvent(mainViewModel, customerObservableList, personList, petList, newEntry));

        services.setOnMouseClicked(getProductMouseEvent(mainViewModel, services, newEntry, petList, selectedProductPane,
                selectedProduct, selectedProductCost, selectedProductCostDiscount, deleteSelectedProductBtn,
                extraProducts, priceText, cart));
        deleteSelectedProductBtn.setOnAction(deleteMainProduct(services, selectedProductPane, newEntry, selectedProduct,
                selectedProductCost, selectedProductCostDiscount, priceText, cart, newEntry.getUserObject()));
        applyBtn.setOnAction(applyDiscount(setDiscount, extraProducts, selectedProductCost, selectedProductCostDiscount,
                newEntry, selectedProduct, priceText, cart));
        extraProducts.setOnMouseClicked(selectExtraProduct(selectedProduct, mainProductBackground, extraProducts));
        mainProductRect.setOnMousePressed(selectMainProduct(selectedProduct, extraProducts, mainProductBackground));
        getCurrentAppointment();
    }
    // Save the selected person and send entry .

    @FXML
    public void save() {
        if ((personList.getSelectionModel().getSelectedItem() == null || newEntry.getLocation() == null
                || newEntry.getTitle().contains("New Entry") || petList.getSelectionModel().getSelectedIndex() == -1)
                || !selectedProductPane.isVisible()) {
            System.out.println("Please select a service and a pet");
            // TODO: Alert popup for missing fields ;)
        } else {
            Stage stage = (Stage) saveBtn.getScene().getWindow();
            stage.close();
            sendEntry();
        }
    }

    @FXML
    public void modifyEntry() {
        customerObservableList = mainViewModel.getPeople();
        personList.setItems(customerObservableList);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                return;
            personList.setItems(customerObservableList
                    .filtered(person -> person.getName().toLowerCase().contains(newValue.toLowerCase())));
            if (newValue.isEmpty())
                personList.setItems(null);
        });
        // Set the selection model to allow only one row to be selected at a time.
        searchField.setOnKeyPressed(getSearchFieldKeyEvent(mainViewModel, searchField, personList,
                customerObservableList, petList, services, newEntry));
    }

    /**
     * This function retrieves and displays information about a current appointment, including the
     * customer, animal, products, and pricing.
     */
    public void getCurrentAppointment() {
        customerObservableList = mainViewModel.getPeople();
        Appointment appointment = newEntry.getUserObject();
        Customer customer = appointment.getCustomerList().get(0);
        Animal a = appointment.getAnimalList().get(0);
        personList.setItems(customerObservableList.filtered(person -> person.getName().equals(customer.getName())));
        personList.getSelectionModel().select(customer);
        services.getItems().remove(newEntry.getCalendar().getName());

        selectedProduct.setText(newEntry.getCalendar().getName());
        selectedProductCost.setText(((Product) newEntry.getCalendar().getUserObject()).getPrice() + " €");

        customer.getAnimals().forEach(animal -> petList.getItems().add(animal.getName()));
        petList.getSelectionModel().select(a.getName());
        priceText.setText(bundle.getString("priceLabel") + ": " + (newEntry.getUserObject().getTotalPrice()) + " €");

        if (appointment.getDescription(MainApp.getLocale().getLanguage()) != null)
            descriptionArea.setText(appointment.getDescription(MainApp.getLocale().getLanguage()));

        appointment.getProducts().forEach(product -> {
            cart.addProduct(product, appointment.getDiscount(product.getId()));
            services.getItems().remove(product.getName(MainApp.getLocale().getLanguage()));
            if (product != newEntry.getCalendar().getUserObject()) {
                double newPrice = product.getPrice()
                        - (product.getPrice()
                        * (0.01 * appointment.getDiscount(product.getId()).getDiscountPercent()));
                extraProducts.getItems()
                        .add(new DiscountProduct(product.getName(MainApp.getLocale().getLanguage()), newPrice));
            } else if (appointment.getDiscount(((Product) newEntry.getCalendar().getUserObject()))
                    .getDiscountPercent() != 0.0) {
                Product mainProduct = (Product) newEntry.getCalendar().getUserObject();
                double newPrice = mainProduct.getPrice()
                        - (mainProduct.getPrice()
                        * (0.01 * appointment.getDiscount(product.getId()).getDiscountPercent()));
                selectedProductCost.strikethroughProperty().set(true);
                selectedProductCostDiscount.setVisible(true);
                selectedProductCostDiscount.setText(String.format("%.2f", newPrice) + " €");
            }
        });
        priceText.setText(
                bundle.getString("priceLabel") + ": " + cart.getTotalDiscountedPrice() + " €");
        selectedProductPane.setVisible(true);
    }

    public void sendEntry() {
        newEntry.setInterval(date.getValue(), startTime.getValue(), date.getValue(), endTime.getValue());
        Appointment appointment = newEntry.getUserObject();
        Customer customer = appointment.getCustomerList().get(0);
        Animal a = appointment.getAnimalList().get(0);

        // MainProductId
        appointment.setMainProductId(((Product) newEntry.getCalendar().getUserObject()).getId());
        /*
         * TODO: Use this map to get the discount for the product
         * new Discount(long productId, double amount);
         */
        mainViewModel.updateAppointment(newEntry.getStartAsZonedDateTime(), newEntry.getEndAsZonedDateTime(),
                newEntry.getUserObject(), customer, a, cart.getProductList(),
                ((Product) newEntry.getCalendar().getUserObject()), descriptionArea);
    }

}
