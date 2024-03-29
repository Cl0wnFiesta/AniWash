package aniwash.view.controllers;

import aniwash.MainApp;
import aniwash.dao.AppointmentDao;
import aniwash.datastorage.BiscuitExeption;
import aniwash.entity.Appointment;
import aniwash.entity.Product;
import aniwash.viewmodels.MainViewModel;
import com.calendarfx.view.AgendaView;
import com.calendarfx.view.CalendarView;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

/**
 * The DashboardController class controls the main dashboard view of a Java application, including a
 * calendar, agenda, bar chart, and various buttons for navigating to other views.
 */
public class DashboardController {
    @FXML
    private AnchorPane backGround;
    @FXML
    private CalendarView calendarview;
    @FXML
    private AgendaView agendaView;
    @FXML
    private Button scheduleButton;
    @FXML
    private BarChart<String, Double> barChart;
    @FXML
    private Text thisMonth;
    @FXML
    private Text lastMonth;
    @FXML
    private Text customersThisMo;
    @FXML
    private Text customersThisYear;
    @FXML
    private MenuButton languageButton;

    private final MainViewModel mainViewModel = new MainViewModel();
    @FXML
    private Text welcometext;

    public void initialize() {

        agendaView.getCalendarSources().addAll(mainViewModel.getFamilyCalendar());

        try {
            // Write welcome text based on time of day (good morning, afternoon, evening)
            int time = LocalDateTime.now().getHour();
            System.out.println(time);
            if (time >= 0 && time < 12) {
                welcometext.setText("Good morning, " +
                        MainApp.getBiscuit().getUser().getName() + "!");
            } else if (time >= 12 && time < 18) {
                welcometext.setText("Good afternoon, " +
                        MainApp.getBiscuit().getUser().getName() + "!");
            } else {
                welcometext.setText("Good evening, " +
                        MainApp.getBiscuit().getUser().getName() + "!");
            }
        } catch (BiscuitExeption e) {
            System.out.println("Biscuit fuked up");
            try {
                logout();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        // agendaView.setCalendarSourceFactory(new Calendars());

        loadDataHistory();
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
    private void products() throws IOException {
        MainApp.setRoot("productsView");
    }

    @FXML
    private void admin() throws IOException {
        MainApp.setRoot("AdminPanel");
    }

    @FXML
    private void changeLanguageEnglish() throws IOException {
        MainApp.setLocale(new Locale.Builder().setLanguage("en").setRegion("US").build());
        MainApp.setRoot("mainView");
    }

    @FXML
    private void changeLanguageFrance() throws IOException {
        MainApp.setLocale(new Locale.Builder().setLanguage("fr").setRegion("FR").build());
        MainApp.setRoot("mainView");
    }

    /**
     * This function loads data from appointments and calculates monthly revenue and customer counts
     * for the current year, and displays it in a bar chart with tooltips and hover effects.
     */
    private void loadDataHistory() {
        Calendar cal = Calendar.getInstance();
        AppointmentDao apptDao = new AppointmentDao();
        Hashtable<Month, Double> monthlyDataMap = new Hashtable<>();
        int custMo = 0, custYear = 0;
        Month m;

        // Fill in the months that have no data with 0.

        for (int i = 0; i < 12; i++) {
            monthlyDataMap.put(Month.of(i + 1), 0.0);
        }

        // Create series for monthly data
        XYChart.Series<String, Double> monthlyData = new XYChart.Series<String, Double>();

        // Set chart name to current year
        monthlyData.setName(String.valueOf(cal.get(Calendar.YEAR)));

        // Get all appointments from database.
        List<Appointment> appts = apptDao.fetchAppointments();

        for (Appointment appt : appts) {
            // Show only current year.
            if (appt.getEndDate().getYear() != cal.get(Calendar.YEAR))
                break;
            m = appt.getEndDate().getMonth();
            // if the month is the current month, add to customer count.
            if (m == Month.of(cal.get(Calendar.MONTH) + 1))
                custMo++;

            custYear++;

            // go through this years appointments products and calculate monthly revenue.
            for (Product p : appt.getProducts()) {
                // Calculate price of product from price and discount.
                double price = p.getPrice() * (1 - appt.getDiscount(p).getDiscountPercent() / 100);
                // System.out.println(price);
                // if the month is already in the map, add the price to the value.
                if (!monthlyDataMap.containsKey(m))
                    monthlyDataMap.put(m, price);
                else
                    monthlyDataMap.put(m, monthlyDataMap.get(m) + price);
            }
        }
        // If yearly data is empty, return.
        if (monthlyDataMap.isEmpty())
            return;

        // Add data to series in order of months.
        for (int i = 0; i < 12; i++) {
            m = Month.of(i + 1);
            monthlyData.getData().add(new XYChart.Data<>(m.toString(), monthlyDataMap.get(m)));
        }

        // print out the data for debugging.
        // for (Month mo : monthlyDataMap.keySet()) {
        // System.out.println(mo + " " + monthlyDataMap.get(mo));
        // }

        // Add series to chart.
        barChart.getData().add(monthlyData);

        // Add tooltip to every node in the chart.
        for (XYChart.Series<String, Double> s : barChart.getData()) {
            for (XYChart.Data<String, Double> data : s.getData()) {
                // If value is greater than 0, add a label to the bar.
                Tooltip tt = new Tooltip(data.getYValue().toString());
                tt.setShowDelay(Duration.ZERO);
                tt.setHideDelay(Duration.ZERO);
                Tooltip.install(data.getNode(), tt);

                // Adding class on hover
                data.getNode().setOnMouseEntered(event -> data.getNode().getStyleClass().add("onHover"));

                // Removing class on exit
                data.getNode().setOnMouseExited(event -> data.getNode().getStyleClass().remove("onHover"));

            }
        }

        int mo = cal.get(Calendar.MONTH);
        // set text of revenue from this and last month.
        thisMonth.setText(String.valueOf(monthlyDataMap.get(Month.of(mo + 1))));

        lastMonth.setText(String.valueOf(monthlyDataMap.get(Month.of(mo))));

        // set text of customer counts.
        customersThisMo.setText(String.valueOf(custMo));
        customersThisYear.setText(String.valueOf(custYear));
    }
}
