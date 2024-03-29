package aniwash.view.controllers;

import java.io.IOException;
import java.util.Locale;

import aniwash.MainApp;
import aniwash.dao.EmployeeDao;
import aniwash.datastorage.BiscuitExeption;
import aniwash.entity.Employee;
import aniwash.enums.UserType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

/**
 * The AdminViewController class handles the creation of new employees and navigation between different
 * views in the application.
 */
public class AdminViewController {
    public EmployeeDao ed = new EmployeeDao();

    @FXML
    private RadioButton employee, employer;
    @FXML
    private TextField username, password, firstname, lastname, email, title;

    @FXML
    public void initialize() {
        // Set up the toggle group for the radio buttons.
        ToggleGroup grp = new ToggleGroup();
        this.employee.setToggleGroup(grp);
        this.employer.setToggleGroup(grp);

        // Set the default radio button to employee.
        this.employee.setSelected(true);
        this.employee.setSelected(true);
    }
    @FXML
    public void back(ActionEvent event) {

        try {
            MainApp.setRoot("mainView");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * Submits the form to create and employee.
     *
     * @author henriui
     */
    @FXML
    public void submit(ActionEvent event) {
        // Check priviledges.

        try {
            if (MainApp.getBiscuit().getUser().getUserType() == UserType.EMPLOYEE)
                return;
        } catch (BiscuitExeption e) {
            System.out.println("User has no permission or is timed out.");
        }
        // Create the employee.
        try {
            if (createEmployee())
                System.out.println("Employee created");
            else
                System.out.println("Employee not created");
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    /**
     * Creates an employee when form is submitted.
     *
     * @return true if employee is created, false otherwise.
     * @author henriui
     */
    @FXML
    private boolean createEmployee() {
        String username = this.username.getText();
        String password = this.password.getText();
        String name = this.firstname.getText();
        String lastname = this.lastname.getText();
        String email = this.email.getText();
        String title = this.title.getText();

        // Check if any of the fields are empty.

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || lastname.isEmpty() || email.isEmpty()
                || title.isEmpty())
            return false;

        // Check if the username is already taken.

        if (ed.findByUsername(username) != null)
            return false;

        // Create the new employee

        Employee e = new Employee(username, password, name + " " + lastname, email, title,
                employee.isPressed() ? UserType.EMPLOYEE : UserType.EMPLOYER);

        // Add the employee to the database.
        try {
            ed.add(e);
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        }
        // Clear the form after successful submission.
        this.username.clear();
        this.password.clear();
        this.firstname.clear();
        this.lastname.clear();
        this.email.clear();
        this.title.clear();

        return true;
    }

    private void logout() throws IOException {
        MainApp.setRoot("login");
    }

    @FXML
    private void mySchedule() throws IOException {
        MainApp.setRoot("schedule");
    }

    @FXML
    private void customers() throws IOException {
        MainApp.setRoot("customerView");
    }
    @FXML
    private void dashboard() throws IOException {
        MainApp.setRoot("MainView");
    }

    @FXML
    private void products() throws IOException {
        MainApp.setRoot("productsView");
    }

    @FXML
    private void admin() throws IOException {
        MainApp.setRoot("AdminPanel");
    }

    @FXML
    private void changeLanguage() throws IOException {
        if (MainApp.getLocale().getLanguage().equals("en")) {
            MainApp.setLocale(new Locale.Builder().setLanguage("fr").setRegion("FR").build());
        } else {
            MainApp.setLocale(new Locale.Builder().setLanguage("en").setRegion("US").build());
        }
        MainApp.setRoot("mainView");
    }
}
