package com.mycompany.customermanagementsystem;

import java.time.format.DateTimeParseException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;

import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Region;

/**
 * JavaFX App
 */
public class App extends Application {
    
    private TextField searchField;
    private TableView<Customer> tblCustomers;
    private ObservableList<Customer> customerList;
    Customer selectedCustomer;
    private Stage primaryStage;
    private Scene mainScene;
    private GridPane dashboardGridPane;
    
    // Format Titles
    String fontType = "Arial";
    Color fontColor = Color.WHITE;
    int fontSize = 24;
    String backgroundColor = "-fx-background-color: #588076;";
    int buttonFontSize = 12;
       
    // Initialize DB and launch Program
    public static void run(String[] args) {
        try {
            DatabaseUtil.initializeDatabase();
        } catch (SQLException e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error Initializing Database. " + e.getMessage());
            alert.showAndWait();
            return;
        }
        launch();
    }
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        mainScene = createMainScene();
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Customer Management System");
        primaryStage.show();
        
        loadCustomerData();
        refreshDashboard(dashboardGridPane);
    }
    
    // Start of program (Main Stage)
    private Scene createMainScene() {
        // Title Label
        Label lblTitle = new Label("Customer Management System");
        lblTitle.setFont(Font.font(fontType, FontWeight.BOLD, fontSize));
        lblTitle.setTextAlignment(TextAlignment.CENTER);
        lblTitle.setTextFill(fontColor);
        // Center the Title Label using a HBox
        HBox titleBox = new HBox(lblTitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(20, 0, 20, 0));
        titleBox.setStyle(backgroundColor); // Set background color
        // Search Field
        searchField = new TextField();
        searchField.setPromptText("Search by Email");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterCustomersByEmail(newValue));
        // Center the Search Field using a HBox
        HBox searchBox = new HBox(searchField);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setPadding(new Insets(10, 0, 10, 0));
        // Table Title Label
        Label lblTableTitle = new Label("List of Customers");
        lblTableTitle.setFont(Font.font("Arial", 18));
        lblTableTitle.setPadding(new Insets(0, 0, 0, 0));
        // Center Table title using an HBox
        HBox tableTitleBox = new HBox(lblTableTitle);
        tableTitleBox.setAlignment(Pos.CENTER);
        tableTitleBox.setPadding(new Insets(0, 0, 0, 0));
        // Menu Bar (For Exporting, Analysis, and Reports)
        Menu mFile = new Menu("File");
        MenuItem mFile1 = new MenuItem("Save as");
        MenuItem mFile2 = new MenuItem("Add mutliple customers");
        MenuItem mFile3 = new MenuItem("Download Upload Template");
        MenuItem mFile4 = new MenuItem("Delete multiple customers");
        MenuItem mFile5 = new MenuItem("Download Deletion Template");
        mFile.getItems().addAll(mFile1, new SeparatorMenuItem(), mFile2, mFile3, new SeparatorMenuItem(), mFile4, mFile5);
        
        Menu mDataVis = new Menu("Data Visualization");
        MenuItem mDataVis1 = new MenuItem("View Dashboard");
        mDataVis.getItems().add(mDataVis1);
        Menu mHelp = new Menu("Help");
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(mFile, mDataVis, mHelp);

        // Table View
        tblCustomers = new TableView<>();
        customerList = FXCollections.observableArrayList();
        tblCustomers.setItems(customerList);

        // Define Columns
        TableColumn<Customer, String> customerIDColumn = new TableColumn<>("ID");
        customerIDColumn.setCellValueFactory(new PropertyValueFactory<>("CustomerID"));
        customerIDColumn.setMinWidth(50); 
        TableColumn<Customer, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("FirstName"));
        firstNameColumn.setMinWidth(150); 
        TableColumn<Customer, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("LastName"));
        lastNameColumn.setMinWidth(150);
        TableColumn<Customer, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("EmailAddress"));
        emailColumn.setMinWidth(200);
        TableColumn<Customer, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("PhoneNum"));
        phoneColumn.setMinWidth(100);
        TableColumn<Customer, String> streetColumn = new TableColumn<>("Street");
        streetColumn.setCellValueFactory(new PropertyValueFactory<>("StreetName"));
        streetColumn.setMinWidth(200);
        TableColumn<Customer, String> cityColumn = new TableColumn<>("City");
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("City"));
        cityColumn.setMinWidth(150);
        TableColumn<Customer, String> stateColumn = new TableColumn<>("State");
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("State"));
        stateColumn.setMinWidth(50);
        TableColumn<Customer, String> countryColumn = new TableColumn<>("Country");
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("Country"));
        countryColumn.setMinWidth(50);
        TableColumn<Customer, String> ageColumn = new TableColumn<>("Age");
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("Age"));
        ageColumn.setMinWidth(50);
        TableColumn<Customer, String> zipColumn = new TableColumn<>("Zip Code");
        zipColumn.setCellValueFactory(new PropertyValueFactory<>("Zip"));
        zipColumn.setMinWidth(100);
        tblCustomers.getColumns().addAll(customerIDColumn, firstNameColumn, 
                lastNameColumn, emailColumn, phoneColumn, ageColumn, 
                streetColumn, cityColumn, stateColumn, countryColumn, 
                zipColumn);
        tblCustomers.setColumnResizePolicy((param) -> {
            double totalWidth = 0;
            for (TableColumn<?, ?> column : tblCustomers.getColumns()) {
                totalWidth += column.getWidth();
            }
            return true;
        });
        tblCustomers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Buttons -------------------------------------------------------------
        Button btnAddCustomer = new Button("Add new Customer");
        btnAddCustomer.setFont(Font.font(fontType, FontWeight.BOLD, buttonFontSize));
        btnAddCustomer.setStyle(backgroundColor);
        btnAddCustomer.setTextFill(fontColor);  
        btnAddCustomer.setEffect(new DropShadow(5, Color.GRAY));
        btnAddCustomer.setOnMouseEntered(e -> {
            btnAddCustomer.setStyle("-fx-background-color: #7AA499;");
            btnAddCustomer.setTextFill(fontColor);
        });
        btnAddCustomer.setOnMouseExited(e -> {
            btnAddCustomer.setStyle(backgroundColor);
            btnAddCustomer.setTextFill(fontColor);
        });

        Button btnUpdateCustomer = new Button("Update existing Customer");
        btnUpdateCustomer.setFont(Font.font(fontType, FontWeight.BOLD, buttonFontSize));
        btnUpdateCustomer.setStyle(backgroundColor);
        btnUpdateCustomer.setTextFill(fontColor);  
        btnUpdateCustomer.setEffect(new DropShadow(5, Color.GRAY));
        btnUpdateCustomer.setOnMouseEntered(e -> {
            btnUpdateCustomer.setStyle("-fx-background-color: #7AA499;");
            btnUpdateCustomer.setTextFill(fontColor);
        });
        btnUpdateCustomer.setOnMouseExited(e -> {
            btnUpdateCustomer.setStyle(backgroundColor);
            btnUpdateCustomer.setTextFill(fontColor);
        });

        Button btnDeleteCustomer = new Button("Delete Customer");
        btnDeleteCustomer.setFont(Font.font(fontType, FontWeight.BOLD, buttonFontSize));
        btnDeleteCustomer.setStyle(backgroundColor);
        btnDeleteCustomer.setTextFill(fontColor);  
        btnDeleteCustomer.setEffect(new DropShadow(5, Color.GRAY));
        btnDeleteCustomer.setOnMouseEntered(e -> {
            btnDeleteCustomer.setStyle("-fx-background-color: #7AA499;");
            btnDeleteCustomer.setTextFill(fontColor);
        });
        btnDeleteCustomer.setOnMouseExited(e -> {
            btnDeleteCustomer.setStyle(backgroundColor);
            btnDeleteCustomer.setTextFill(fontColor);
        });
        // ---------------------------------------------------------------------
        // Events --------------------------------------------------------------
        // Button Events
        btnAddCustomer.setOnAction(event -> showCustomerForm("add", null));
        btnUpdateCustomer.setOnAction(event -> {
            selectedCustomer = tblCustomers.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                showCustomerForm("update", selectedCustomer);
            } else {
                showAlert("Update Customer", "No customer selected for update.", Alert.AlertType.WARNING);
            }
        });
        btnDeleteCustomer.setOnAction(event -> {
            selectedCustomer = tblCustomers.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                try {
                    DatabaseUtil.deleteCustomer(selectedCustomer.getCustomerID());
                    showAlert("Delete Customer", "Customer successfully deleted.", Alert.AlertType.INFORMATION);
                    loadCustomerData(); // Refresh the table view
                    refreshDashboard(dashboardGridPane);
                } catch (SQLException e) {
                    showAlert("Database Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Delete Customer", "No customer selected for deletion.", Alert.AlertType.WARNING);
            }
        });
        // Menu events
        // Save as
        mFile1.setOnAction(event -> {
            saveAs(customerList);
        });
        // Add customers
        mFile2.setOnAction(event -> {
            uploadFile();
        });
        // Download upload template
        mFile3.setOnAction(event -> {
            downloadUploadTemplate();
        });
        mFile4.setOnAction(event -> {
            massDeleteRecords();
        });
        mFile5.setOnAction(event -> {
            downloadDeletionTemplate();
        });
        // Data visualization
        mDataVis1.setOnAction(event -> {
            showDashboard();
        });
        // Row Count Label
        Label lblRowCount = new Label("Total Rows: 0");

        // Update row count label when customerList changes
        customerList.addListener((ListChangeListener.Change<? extends Customer> change) -> {
            lblRowCount.setText("Total Rows: " + customerList.size());
        });
        // Layout Setup
        HBox buttonBox = new HBox(15, btnAddCustomer, btnUpdateCustomer, btnDeleteCustomer);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox rowCountBox = new HBox(lblRowCount, spacer, searchField);
        rowCountBox.setAlignment(Pos.BOTTOM_LEFT);
        rowCountBox.setPadding(new Insets(10, 0, 0, 0));
        

        VBox tableBox = new VBox(10, tableTitleBox, tblCustomers, rowCountBox, buttonBox);
        tableBox.setPadding(new Insets(20, 20, 20, 20));
        VBox.setVgrow(tblCustomers, Priority.ALWAYS);
        
        VBox mainVBox = new VBox(titleBox, tableBox);
        VBox.setVgrow(tableBox, Priority.ALWAYS);

        BorderPane mainPane = new BorderPane();
        mainPane.setTop(menuBar);
        mainPane.setCenter(mainVBox);
        BorderPane.setAlignment(lblTitle, Pos.CENTER);
        BorderPane.setMargin(lblTitle, new Insets(20, 0, 20, 0));

        return new Scene(mainPane, 800, 600);
    }
    // -------------------------------------------------------------------------
    // Used to get Customer data from the database -----------------------------
    private void loadCustomerData() {
        try {
            List<Customer> customers = DatabaseUtil.populateCustomerTable();
            customerList.setAll(customers);
        } catch (SQLException e) {
            showAlert("Load Data Error", "Error loading customer data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    // -------------------------------------------------------------------------
    // Displays the form to add / update a customer ----------------------------
    public void showCustomerForm(String action, Customer customer) {
        // Define objects
        Label lblTitle = new Label("Customer Information Form");
        lblTitle.setFont(Font.font(fontType, FontWeight.BOLD, fontSize));
        lblTitle.setTextAlignment(TextAlignment.CENTER);
        lblTitle.setTextFill(fontColor);
        // Create a box for centering and background color
        HBox titleBox = new HBox(lblTitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(20, 0, 20, 0));
        titleBox.setStyle(backgroundColor); // Set background color

        Label lblFirstName = new Label("First Name");
        TextField txtFirstName = new TextField();
        Label lblLastName = new Label("Last Name");
        TextField txtLastName = new TextField();
        Label lblEmailAddress = new Label("Email Address");
        TextField txtEmailAddress = new TextField();
        Label lblPhoneNum = new Label("Phone Number");
        TextField txtPhoneNum = new TextField();
        Label lblDateOfBirth = new Label("Date of Birth");
        TextField txtDateOfBirth = new TextField();

        Label lblAddress = new Label("Address");
        Label lblStreetName = new Label("Street");
        TextField txtStreetName = new TextField();
        Label lblCity = new Label("City");
        TextField txtCity = new TextField();
        Label lblState = new Label("State");
        TextField txtState = new TextField();
        Label lblCountry = new Label("Country");
        TextField txtCountry = new TextField();
        Label lblZip = new Label("Zip");
        TextField txtZip = new TextField();

        Button btnSubmit = new Button("Submit");
        btnSubmit.setFont(Font.font(fontType, FontWeight.BOLD, buttonFontSize));
        btnSubmit.setStyle(backgroundColor);
        btnSubmit.setTextFill(fontColor);  
        btnSubmit.setEffect(new DropShadow(5, Color.GRAY));
        btnSubmit.setOnMouseEntered(e -> {
            btnSubmit.setStyle("-fx-background-color: #7AA499;");
            btnSubmit.setTextFill(fontColor);
        });
        btnSubmit.setOnMouseExited(e -> {
            btnSubmit.setStyle(backgroundColor);
            btnSubmit.setTextFill(fontColor);
        });
        

        // GridPane setup
        GridPane gPane = new GridPane();
        gPane.setHgap(10);
        gPane.setVgap(10);
        gPane.setPadding(new Insets(20, 20, 20, 20));

        // Add title to gridpane
        gPane.add(titleBox, 0, 0, 2, 1);
        GridPane.setColumnSpan(titleBox, 4);
        GridPane.setHalignment(titleBox, HPos.CENTER);
        GridPane.setMargin(titleBox, new Insets(0, 0, 20, 0));

        // Add form fields to gridpane
        gPane.add(lblFirstName, 0, 1);
        gPane.add(txtFirstName, 1, 1, 3, 1);
        gPane.add(lblLastName, 0, 2);
        gPane.add(txtLastName, 1, 2, 3, 1);
        gPane.add(lblEmailAddress, 0, 3);
        gPane.add(txtEmailAddress, 1, 3, 3, 1);
        gPane.add(lblPhoneNum, 0, 4);
        gPane.add(txtPhoneNum, 1, 4, 3, 1);
        gPane.add(lblDateOfBirth, 0, 5);
        gPane.add(txtDateOfBirth, 1, 5, 3, 1);

        // Add address fields to gridpane
        gPane.add(lblAddress, 0, 6, 4, 1);
        GridPane.setMargin(lblAddress, new Insets(20, 0, 0, 0));
        GridPane.setColumnSpan(lblAddress, 4);
        GridPane.setHalignment(lblAddress, HPos.CENTER);
        gPane.add(lblStreetName, 0, 7);
        gPane.add(txtStreetName, 1, 7, 3, 1);
        gPane.add(lblCity, 0, 8);
        gPane.add(txtCity, 1, 8, 3, 1);
        gPane.add(lblState, 0, 9);
        gPane.add(txtState, 1, 9);
        gPane.add(lblCountry, 2, 9);
        gPane.add(txtCountry, 3, 9);
        gPane.add(lblZip, 0, 10);
        gPane.add(txtZip, 1, 10, 3, 1);

        // Add submit button to gridpane
        gPane.add(btnSubmit, 0, 11, 4, 1);
        GridPane.setHalignment(btnSubmit, HPos.CENTER);
        GridPane.setMargin(btnSubmit, new Insets(20, 0, 0, 0));
        
        HBox formBox = new HBox(gPane);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(20, 0, 20, 0));

        // Update button is selected - populate fields with data
        if (customer != null) {
            txtFirstName.setText(customer.getFirstName());
            txtLastName.setText(customer.getLastName());
            txtEmailAddress.setText(customer.getEmailAddress());
            txtPhoneNum.setText(customer.getPhoneNum());
            txtStreetName.setText(customer.getStreetName());
            txtCity.setText(customer.getCity());
            txtState.setText(customer.getState());
            txtCountry.setText(customer.getCountry());
            txtZip.setText(customer.getZip());
            txtDateOfBirth.setText(customer.getDateOfBirth());
        }

        // Handle submit button events
        btnSubmit.setOnAction(event -> {
            String firstName = convertToNullIfEmpty(txtFirstName.getText());
            String lastName = convertToNullIfEmpty(txtLastName.getText());
            String emailAddress = convertToNullIfEmpty(txtEmailAddress.getText());
            String phoneNum = convertToNullIfEmpty(txtPhoneNum.getText());
            String streetName = convertToNullIfEmpty(txtStreetName.getText());
            String city = convertToNullIfEmpty(txtCity.getText());
            String state = convertToNullIfEmpty(txtState.getText());
            String country = convertToNullIfEmpty(txtCountry.getText());
            String zip = convertToNullIfEmpty(txtZip.getText());
            String dateOfBirth = convertToNullIfEmpty(txtDateOfBirth.getText());

            try {
                if ("add".equals(action)) {
                    DatabaseUtil.addCustomer(firstName, lastName, emailAddress, phoneNum, streetName, city, state, country, zip, dateOfBirth);
                    showAlert("Add Customer", "Customer successfully added!", Alert.AlertType.INFORMATION);
                    refreshDashboard(dashboardGridPane);
                } else if ("update".equals(action) && customer != null) {
                    DatabaseUtil.updateCustomer(customer.getCustomerID(), firstName, lastName, emailAddress, phoneNum, streetName, city, state, country, zip, dateOfBirth);
                    showAlert("Update Customer", "Customer successfully updated!", Alert.AlertType.INFORMATION);
                    refreshDashboard(dashboardGridPane);
                    ((Stage) btnSubmit.getScene().getWindow()).close(); // Close the form after updating
                }
                txtFirstName.clear();
                txtLastName.clear();
                txtEmailAddress.clear();
                txtPhoneNum.clear();
                txtStreetName.clear();
                txtCity.clear();
                txtState.clear();
                txtCountry.clear();
                txtZip.clear();
                txtDateOfBirth.clear();
                loadCustomerData(); // Refresh the table view
            } catch (SQLException e) {
                showAlert("Database Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
        BorderPane root = new BorderPane();
        root.setTop(titleBox);
        root.setCenter(formBox);
        BorderPane.setAlignment(titleBox, Pos.CENTER);
        BorderPane.setMargin(titleBox, new Insets(10));
        
        
        Scene scene = new Scene(root, 500, 550);
        Stage customerInfoStage = new Stage();
        customerInfoStage.setScene(scene);
        customerInfoStage.setTitle("Customer Information Form");
        customerInfoStage.show();
    }

    private String convertToNullIfEmpty(String text) {
        return text == null || text.trim().isEmpty() ? null : text;
}

    // Shows on screen alerts --------------------------------------------------
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // -------------------------------------------------------------------------
    private void filterCustomersByEmail(String email) {
    ObservableList<Customer> filteredList = FXCollections.observableArrayList();
    for (Customer customer : customerList) {
        if (customer.getEmailAddress().toLowerCase().contains(email.toLowerCase())) {
            filteredList.add(customer);
        }
    }
    tblCustomers.setItems(filteredList);
}
    // Data Analysis & Reporting Options
    // Data Analysis Scene
    private void showDashboard() {
        // Save the current content of the main scene
        Parent mainSceneContent = primaryStage.getScene().getRoot();
        // Title Label
        Label lblTitle = new Label("Customer Information Dashboard");
        lblTitle.setFont(Font.font(fontType, FontWeight.BOLD, fontSize));
        lblTitle.setTextAlignment(TextAlignment.CENTER);
        lblTitle.setTextFill(fontColor);

        // Center the Title Label using a HBox
        HBox titleBox = new HBox(lblTitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(20, 0, 20, 0));
        titleBox.setStyle(backgroundColor); // Set background color

        // GridPane setup for charts
        dashboardGridPane = new GridPane();
        dashboardGridPane.setHgap(20); // Horizontal gap between columns
        dashboardGridPane.setVgap(20); // Vertical gap between rows
        dashboardGridPane.setPadding(new Insets(20)); // Padding around the GridPane
        dashboardGridPane.setAlignment(Pos.CENTER); // Center the GridPane within its parent
        
        refreshDashboard(dashboardGridPane);

        // Create and show the histogram
        try {
            Map<String, Integer> ageDistribution = DatabaseUtil.getAgeDistribution();
            BarChart<String, Number> barChart = createAgeHistogram(ageDistribution);
            dashboardGridPane.add(barChart, 0, 0, 1, 1);
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading age distribution data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        // Create and show the region pie chart
        PieChart pieChart = createRegionPieChart();
        dashboardGridPane.add(pieChart, 1, 0, 1, 1);

        // Return button
        Button btnReturn = new Button("Return to Main Menu");
        btnReturn.setFont(Font.font(fontType, FontWeight.BOLD, buttonFontSize));
        btnReturn.setStyle(backgroundColor);
        btnReturn.setTextFill(fontColor);  
        btnReturn.setEffect(new DropShadow(5, Color.GRAY));
        btnReturn.setOnMouseEntered(e -> {
            btnReturn.setStyle("-fx-background-color: #7AA499;");
            btnReturn.setTextFill(fontColor);
        });
        btnReturn.setOnMouseExited(e -> {
            btnReturn.setStyle(backgroundColor);
            btnReturn.setTextFill(fontColor);
        });
        
         btnReturn.setOnAction(event -> primaryStage.getScene().setRoot(mainSceneContent));

        // Add button to an HBox
        HBox buttonBox = new HBox(btnReturn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 20, 0));

        // Root layout
        BorderPane root = new BorderPane();
        root.setTop(titleBox);
        root.setCenter(dashboardGridPane); // Set GridPane as the center of the BorderPane
        root.setBottom(buttonBox);

        primaryStage.getScene().setRoot(root);
    }
    // Used to show an age distribution via bar chart
    private BarChart<String, Number> createAgeHistogram(Map<String, Integer> ageDistribution) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Age Bracket");

        // Define age brackets in the correct order
        List<String> ageBrackets = Arrays.asList("Under 18", "18-24", "25-34", "35-44", "45-54", "55-64", "65 or over");
        xAxis.setCategories(FXCollections.observableArrayList(ageBrackets));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Customers");

        // Ensure the y-axis displays integer values only
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                if (object.intValue() == object.doubleValue()) {
                    return String.format("%d", object.intValue());
                } else {
                    return super.toString(object);
                }
            }
        });

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Age Distribution of Customers");
        barChart.setCategoryGap(10);
        barChart.setBarGap(5);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Customers");

        for (Map.Entry<String, Integer> entry : ageDistribution.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);

        // Remove the legend
        barChart.setLegendVisible(false);

        // Apply consistent color to all bars
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle(backgroundColor);
        }

        return barChart;
    }

    private PieChart createRegionPieChart() {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Customer Distribution by Region");

        try {
            Map<String, Integer> regionDistribution = DatabaseUtil.getCustomerCountPerRegion();
            int totalCustomers = regionDistribution.values().stream().mapToInt(Integer::intValue).sum();

            // Define the custom colors
            Map<String, String> regionColors = new HashMap<>();
            regionColors.put("Northeast", "#FF6347"); // Red
            regionColors.put("Southeast", "#FFA500"); // Orange
            regionColors.put("Midwest", "#FFD700"); // Yellow
            regionColors.put("Northwest", "#32CD32"); // Green
            regionColors.put("Southwest", "#4682B4"); // Blue
            regionColors.put("Other", "#9370DB"); // Grey

            for (Map.Entry<String, Integer> entry : regionDistribution.entrySet()) {
                PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
                slice.nameProperty().bind(
                    Bindings.concat(entry.getKey(), " ", String.format("%.1f%%", (100.0 * entry.getValue() / totalCustomers)))
                );
                pieChart.getData().add(slice);

                // Apply custom color
                String color = regionColors.getOrDefault(entry.getKey(), "#000000"); // Default to black if region not found
                slice.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading region distribution data: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        // Remove the legend
        pieChart.setLegendVisible(false);

        return pieChart;
    }

    private void refreshDashboard(GridPane gPane) {
        if (gPane == null) {
            gPane = new GridPane(); // Initialize if not already done
        }
        gPane.getChildren().clear(); // Clear existing charts

        // Create and show the histogram
        try {
            Map<String, Integer> ageDistribution = DatabaseUtil.getAgeDistribution();
            BarChart<String, Number> barChart = createAgeHistogram(ageDistribution);
            PieChart pieChart = createRegionPieChart();
            gPane.add(pieChart, 1, 0, 1, 1);
            
            gPane.add(barChart, 0, 0, 1, 1);
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading age distribution: " + e.getMessage(), Alert.AlertType.ERROR);
            
        }
    }
    public void saveAs(ObservableList<Customer> customers) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save As");

    // Set extension filter
    FileChooser.ExtensionFilter extFilterCsv = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
    fileChooser.getExtensionFilters().add(extFilterCsv);

    File file = fileChooser.showSaveDialog(primaryStage);
    if (file != null) {
        String filePath = file.getAbsolutePath();
        if (!filePath.endsWith(".csv")) {
            filePath += ".csv";
        }
        saveAsCSV(filePath, customers);
    }
    }

    private void saveAsCSV(String filePath, List<Customer> customers) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {

            // Write header
            String[] headers = {"CustomerID", "FirstName", "LastName", "EmailAddress", "PhoneNum", "StreetName", "City", "State", "Country", "Zip", "DateOfBirth"};
            for (int i = 0; i < headers.length; i++) {
                fileWriter.append(headers[i]);
                if (i < headers.length - 1) fileWriter.append(",");
            }
            fileWriter.append("\n");

            // Write data
            for (Customer customer : customers) {
                fileWriter.append(String.valueOf(customer.getCustomerID())).append(",");
                fileWriter.append(customer.getFirstName()).append(",");
                fileWriter.append(customer.getLastName()).append(",");
                fileWriter.append(customer.getEmailAddress()).append(",");
                fileWriter.append(customer.getPhoneNum()).append(",");
                fileWriter.append(customer.getStreetName()).append(",");
                fileWriter.append(customer.getCity()).append(",");
                fileWriter.append(customer.getState()).append(",");
                fileWriter.append(customer.getCountry()).append(",");
                fileWriter.append(customer.getZip()).append(",");
                fileWriter.append(customer.getDateOfBirth()).append("\n");
            }

            showAlert("Success", "CSV saved successfully at " + filePath, Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Error", "Error saving CSV: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void uploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload File");

        // Set extension filter
        FileChooser.ExtensionFilter extFilterCsv = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilterCsv);

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            String filePath = file.getAbsolutePath();
            if (filePath.endsWith(".csv")) {
                ProgressBar progressBar = new ProgressBar();
                progressBar.setPrefWidth(300);

                VBox vbox = new VBox(progressBar);
                vbox.setAlignment(Pos.CENTER);
                vbox.setPadding(new Insets(20));

                Scene progressScene = new Scene(vbox);
                Stage progressStage = new Stage();
                progressStage.setScene(progressScene);
                progressStage.setTitle("Uploading Data...");
                progressStage.initOwner(primaryStage);
                progressStage.setResizable(false);

                Task<Void> uploadTask = createUploadTask(file, progressBar);
                uploadTask.setOnSucceeded(event -> progressStage.close());
                uploadTask.setOnFailed(event -> {
                    progressStage.close();
                    showAlert("Error", "Error uploading file: " + uploadTask.getException().getMessage(), Alert.AlertType.ERROR);
                });

                progressStage.show();
                new Thread(uploadTask).start();
            }
        }
    }

    private Task<Void> createUploadTask(File file, ProgressBar progressBar) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<String> errors = new ArrayList<>();
                List<String[]> errorRows = new ArrayList<>();
                int totalRows = 0;

                // Count the total number of rows (excluding header)
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    totalRows = (int) br.lines().count() - 1;
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Error", "Error counting rows in file: " + e.getMessage(), Alert.AlertType.ERROR));
                    return null;
                }

                // Process the file
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    boolean isFirstLine = true;
                    String[] headers = null;
                    int rowNum = 1;

                    while ((line = br.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            headers = line.split(",");
                            rowNum++;
                            continue; // Skip header line
                        }

                        String[] values = line.split(",");
                        try {
                            String firstName = getStringValueFromCSV(values, 0, "FirstName", rowNum, errors, false);
                            String lastName = getStringValueFromCSV(values, 1, "LastName", rowNum, errors, false);
                            String emailAddress = getStringValueFromCSV(values, 2, "EmailAddress", rowNum, errors, false);
                            String phoneNum = getStringValueFromCSV(values, 3, "PhoneNum", rowNum, errors, false);
                            String streetName = getStringValueFromCSV(values, 4, "StreetName", rowNum, errors, false);
                            String city = getStringValueFromCSV(values, 5, "City", rowNum, errors, false);
                            String state = getStringValueFromCSV(values, 6, "State", rowNum, errors, false);
                            String country = getStringValueFromCSV(values, 7, "Country", rowNum, errors, false);
                            String zip = getStringValueFromCSV(values, 8, "Zip", rowNum, errors, false);
                            String dateOfBirth = getStringValueFromCSV(values, 9, "DateOfBirth", rowNum, errors, true);

                            // Use the addCustomer method for each row
                            DatabaseUtil.addCustomer(firstName, lastName, emailAddress, phoneNum, streetName, city, state, country, zip, dateOfBirth);
                        } catch (Exception e) {
                            values = Arrays.copyOf(values, values.length + 1);
                            values[values.length - 1] = "Error in row " + rowNum + ": " + e.getMessage();
                            errorRows.add(values);
                        }
                        rowNum++;
                        updateProgress(rowNum - 1, totalRows); // Update the progress bar
                    }

                    if (!errorRows.isEmpty()) {
                        String errorFilePath = file.getParent() + "/upload_errors.csv";
                        saveErrorCSV(headers, errorRows, errorFilePath);
                        int errorCount = errorRows.size();
                        Platform.runLater(() -> showAlert("Upload Error", errorCount + " rows have errors. Error details saved to " + errorFilePath, Alert.AlertType.ERROR));
                        return null;
                    }

                    Platform.runLater(() -> {
                        showAlert("Success", "File uploaded and data inserted successfully.", Alert.AlertType.INFORMATION);
                        loadCustomerData();
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> showAlert("Error", "Error uploading file: " + e.getMessage(), Alert.AlertType.ERROR));
                } 
                return null;
            }
        };
    }
    private void saveErrorCSV(String[] headers, List<String[]> errorRows, String filePath) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            writer.write(String.join(",", headers) + ",Error\n");
            for (String[] row : errorRows) {
                writer.write(String.join(",", row) + "\n");
            }
        } catch (IOException e) {
            showAlert("Error", "Error downloading list of errors: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
                    return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
    private String getStringValueFromCSV(String[] values, int index, String columnName, int rowNum, List<String> errors, boolean isDate) {
        try {
            String value = values[index];
            if (isDate) {
                value = convertDateFormat(value, "M/d/yyyy", "yyyy-MM-dd");
            }
            return value;
        } catch (Exception e) {
            errors.add("Error in row " + rowNum + ": Column " + columnName + " - " + e.getMessage());
            return "";
        }
    }
    private String convertDateFormat(String date, String inputFormat, String outputFormat) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(inputFormat);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
            LocalDate parsedDate = LocalDate.parse(date, inputFormatter);
            return parsedDate.format(outputFormatter);
        } catch (DateTimeParseException e) {
            return date; // If parsing fails, return the original date string
        }
    }

    private void downloadUploadTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Download Upload Template");

        // Set extension filter for CSV files
        FileChooser.ExtensionFilter extFilterCsv = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilterCsv);

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            String filePath = file.getAbsolutePath();
            if (!filePath.endsWith(".csv")) {
                filePath += ".csv";
            }
            saveTemplateAsCSV(filePath);
        }
    }

    private void saveTemplateAsCSV(String filePath) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {

            // Write header
            String[] headers = {"FirstName", "LastName", "EmailAddress", "PhoneNum", "StreetName", "City", "State", "Country", "Zip", "DateOfBirth"};
            for (int i = 0; i < headers.length; i++) {
                fileWriter.append(headers[i]);
                if (i < headers.length - 1) fileWriter.append(",");
            }
            fileWriter.append("\n");

            showAlert("Success", "Template saved successfully at " + filePath, Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Error", "Error saving template: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    private void massDeleteRecords() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload File for Mass Deletion");

        // Set extension filter
        FileChooser.ExtensionFilter extFilterCsv = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilterCsv);

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            String filePath = file.getAbsolutePath();
            if (filePath.endsWith(".csv")) {
                ProgressBar progressBar = new ProgressBar();
                progressBar.setPrefWidth(300);

                VBox vbox = new VBox(progressBar);
                vbox.setAlignment(Pos.CENTER);
                vbox.setPadding(new Insets(20));

                Scene progressScene = new Scene(vbox);
                Stage progressStage = new Stage();
                progressStage.setScene(progressScene);
                progressStage.setTitle("Deleting Records...");
                progressStage.initOwner(primaryStage);
                progressStage.setResizable(false);

                Task<Void> deleteTask = createDeleteTask(file, progressBar);
                deleteTask.setOnSucceeded(event -> progressStage.close());
                deleteTask.setOnFailed(event -> {
                    progressStage.close();
                    showAlert("Error", "Error deleting records: " + deleteTask.getException().getMessage(), Alert.AlertType.ERROR);
                });

                progressStage.show();
                new Thread(deleteTask).start();
            }
        }
    }

    private Task<Void> createDeleteTask(File file, ProgressBar progressBar) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<String> errors = new ArrayList<>();
                List<String> idsToDelete = new ArrayList<>();

                // Read the file and collect IDs
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    boolean isFirstLine = true;
                    while ((line = br.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false; // Skip header line if present
                            continue;
                        }
                        idsToDelete.add(line.trim());
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> showAlert("Error", "Error reading file: " + e.getMessage(), Alert.AlertType.ERROR));
                    return null;
                }

                int totalRecords = idsToDelete.size();
                int processedRecords = 0;

                // Delete records in the database
                for (String id : idsToDelete) {
                    try {
                        DatabaseUtil.deleteCustomer(Integer.parseInt(id));
                    } catch (Exception e) {
                        errors.add("Error deleting record with ID " + id + ": " + e.getMessage());
                    }
                    processedRecords++;
                    updateProgress(processedRecords, totalRecords); // Update the progress bar
                }

                if (!errors.isEmpty()) {
                    String errorFilePath = file.getParent() + "/delete_errors.csv";
                    saveDeleteErrorCSV(errors, errorFilePath);
                    int errorCount = errors.size();
                    Platform.runLater(() -> showAlert("Delete Error", errorCount + " records could not be deleted. Error details saved to " + errorFilePath, Alert.AlertType.ERROR));
                } else {
                    Platform.runLater(() -> {
                        showAlert("Success", "All records deleted successfully.", Alert.AlertType.INFORMATION);
                        loadCustomerData();
                    });
                }
                return null;
            }
        };
    }
    private void downloadDeletionTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Download Deletion Template");

        // Set extension filter for CSV files
        FileChooser.ExtensionFilter extFilterCsv = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilterCsv);

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            String filePath = file.getAbsolutePath();
            if (!filePath.endsWith(".csv")) {
                filePath += ".csv";
            }
            saveDeletionTemplateAsCSV(filePath);
        }
    }

    private void saveDeletionTemplateAsCSV(String filePath) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            // Write header
            fileWriter.append("CustomerID\n");
            showAlert("Success", "Deletion template saved successfully at " + filePath, Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Error", "Error saving deletion template: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void saveDeleteErrorCSV(List<String> errors, String filePath) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            writer.write("Error\n");
            for (String error : errors) {
                writer.write(error + "\n");
            }
        } catch (IOException e) {
            showAlert("Error", "Unable to delete requested records.", Alert.AlertType.INFORMATION);
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}