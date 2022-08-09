package carsharing;

import carsharing.dao.CarDao;
import carsharing.dao.CompanyDao;
import carsharing.dao.CustomerDao;
import carsharing.impl.CarDaoImpl;
import carsharing.impl.CompanyDaoImpl;
import carsharing.impl.CustomerDaoImpl;
import carsharing.models.Car;
import carsharing.models.Company;
import carsharing.models.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    private static final String JDBC_DRIVER = "org.h2.Driver";
    private static String DB_URL = "jdbc:h2:file:./src/carsharing/db/%s";

    private static final String CREATE_COMPANY_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS COMPANY (ID INT PRIMARY KEY AUTO_INCREMENT," +
            " NAME VARCHAR(20) NOT NULL UNIQUE);";
    private static final String INSERT_COMPANY_INTO_TABLE_QUERY = "INSERT INTO COMPANY (NAME) VALUES %s;";

    private static final String CREATE_CAR_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS CAR (ID INT PRIMARY KEY AUTO_INCREMENT, " +
            "NAME VARCHAR(20) UNIQUE NOT NULL, " +
            "COMPANY_ID INT NOT NULL, " +
            "CONSTRAINT FK_COMPANY " +
            "FOREIGN KEY (COMPANY_ID) " +
            "REFERENCES COMPANY(ID));";
    private static final String INSERT_CAR_INTO_TABLE_QUERY = "INSERT INTO CAR (NAME, COMPANY_ID) VALUES %s;";

    private static final String CREATE_CUSTOMER_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS CUSTOMER (ID INT PRIMARY KEY AUTO_INCREMENT, " +
            "NAME VARCHAR(20) UNIQUE NOT NULL, " +
            "RENTED_CAR_ID INT DEFAULT NULL, " +
            "CONSTRAINT FK_RENTED_CAR " +
            "FOREIGN KEY (RENTED_CAR_ID) " +
            "REFERENCES CAR(ID));";
    private static final String INSERT_CUSTOMER_WITHOUT_RENTED_CAR_ID_INTO_TABLE_QUERY = "INSERT INTO CUSTOMER (NAME) VALUES %s;";
    private static final String UPDATE_CUSTOMER_RENTED_CAR_ID_QUERY = "UPDATE CUSTOMER SET RENTED_CAR_ID = %s WHERE NAME = '%s'";

    private static Connection connection = null;
    private static Statement statement = null;

    private static CompanyDao companyDao;
    private static CarDao carDao;
    private static CustomerDao customerDao;

    private static String companyName;
    private static String carName;

    public static void main(String[] args) {
        try {
            setDBUrl(args);
            createDB();
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n1. Log in as a manager\n" +
                        "2. Log in as a customer\n" +
                        "3. Create a customer\n" +
                        "0. Exit");
                int command;

                try {
                    command = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Unknown command!");
                    continue;
                }

                getAllCompaniesFromDB();
                getAllCustomersFromDB();
                getAllCarsFromDB();

                switch (command) {
                    case 1:
                        loginAsAManagerAndStartWork(scanner);
                        break;
                    case 2:
                        if (printCustomerList()) {
                            String customerName;
                            try {
                                customerName = promptToSelectACustomer(scanner);
                            } catch (IllegalArgumentException e) {
                                continue;
                            }
                            workWithCustomer(scanner, customerName);
                        }
                        break;
                    case 3:
                        createACustomer(scanner);
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Unknown command!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                if (statement != null) statement.close();
            } catch(SQLException se2) {
                System.out.println("Statement was not closed! Nothing can be done(");
            }
            try {
                if (connection != null) connection.close();
            } catch(SQLException se){
                System.out.println("Connection was not closed! Nothing can be done(");
                se.printStackTrace();
            }
        }
    }

    private static void createACustomer(Scanner scanner) throws SQLException {
        System.out.println("\nEnter the customer name:");
        String newCustomerName = scanner.nextLine();
        boolean isCustomerAdded = customerDao.addCustomer(new Customer(newCustomerName));

        if (isCustomerAdded) {
            statement.executeUpdate(String.format(INSERT_CUSTOMER_WITHOUT_RENTED_CAR_ID_INTO_TABLE_QUERY, "('" + newCustomerName + "')"));
            System.out.println("The customer was added!\n");
        } else {
            System.out.printf("Customer '%s' was not added! Try again\n", newCustomerName);
        }
    }

    private static boolean printCustomerList() {
        List<Customer> customers = customerDao.getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("\nThe customer list is empty!");
            return false;
        } else {
            System.out.println("\nCustomer list:");
            int customerId = 1;
            for (Customer customer : customers) {
                System.out.printf("%d. %s\n", customerId++, customer.getName());
            }
            System.out.println("0. Back");
            return true;
        }
    }

    private static String promptToSelectACustomer(Scanner scanner) {
        int command;

        while (true) {
            try {
                command = Integer.parseInt(scanner.nextLine());
                if (command == 0) throw new IllegalArgumentException();
                return customerDao.getAllCustomers().get(command - 1).getName();
            } catch (NumberFormatException e) {
                System.out.println("Unknown command!");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("There is no customer with such index! Try again or go back");
            }
        }
    }

    private static void workWithCustomer(Scanner scanner, String customerName) throws SQLException {
        while (true) {
            System.out.println("\n1. Rent a car\n" +
                    "2. Return a rented car\n" +
                    "3. My rented car\n" +
                    "0. Back");

            int command;

            try {
                command = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Unknown command!");
                continue;
            }

            switch (command) {
                case 1:
                    int rentedCarId = customerDao.getAllCustomers().stream()
                            .filter(customer -> customer.getName().equals(customerName))
                            .findFirst().orElse(new Customer("")).getRentedCarId();
                    if (rentedCarId != -1 && rentedCarId != 0) {
                        System.out.println("\nYou've already rented a car!");
                        continue;
                    }

                    if (printCompanyList()) {
                        try {
                            companyName = promptToSelectACompany(scanner);
                        } catch (IllegalArgumentException e) {
                            continue;
                        }

                        if (printCarsListForCustomer(companyName)) {
                            try {
                                carName = promptToSelectACar(scanner);
                            } catch (IllegalArgumentException e) {
                                continue;
                            }
                            rentACar(customerName);
                        }
                    }
                    break;
                case 2:
                    returnARentedCar(customerName);
                    break;
                case 3:
                    printRentedCar(customerName);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Unknown command!");
            }
        }
    }

    private static void printRentedCar(String customerName) {
        Customer customer = customerDao.getAllCustomers().stream()
                .filter(tempCustomer -> tempCustomer.getName().equals(customerName))
                .findFirst()
                .orElse(new Customer(""));
        if (customer.getRentedCarId() == 0 || customer.getRentedCarId() == -1) {
            System.out.println("\nYou didn't rent a car!");
        } else {
            Car car = carDao.getAllCars().get(customer.getRentedCarId() - 1);
            String companyName = companyDao.getAllCompanies().get(car.getCompanyId() - 1).getName();

            System.out.printf("\nYour rented car:\n%s\nCompany:\n%s\n", car.getName(), companyName);
        }
    }

    private static void returnARentedCar(String customerName) throws SQLException {
        Customer customer = customerDao.getAllCustomers().stream()
                .filter(tempCustomer -> tempCustomer.getName().equals(customerName))
                .findFirst()
                .orElse(new Customer(""));
        if (customer.getRentedCarId() == -1 || customer.getRentedCarId() == 0) {
            System.out.println("\nYou didn't rent a car!");
        } else {
            customerDao.getAllCustomers().get(customerDao.getIndexOfCustomerByName(customerName)).setRentedCarId(-1);
            statement.executeUpdate(String.format(UPDATE_CUSTOMER_RENTED_CAR_ID_QUERY, "NULL", customerName));
            System.out.println("\nYou've returned a rented car!");
        }
    }

    private static void loginAsAManagerAndStartWork(Scanner scanner) throws SQLException {
        while (true) {
            System.out.println("\n1. Company list\n" +
                    "2. Create a company\n" +
                    "0. Back");

            int command;

            try {
                command = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Unknown command!");
                continue;
            }

            switch (command) {
                case 1:
                    if (printCompanyList()) {
                        String companyName;
                        try {
                            companyName = promptToSelectACompany(scanner);
                        } catch (IllegalArgumentException e) {
                            continue;
                        }
                        workWithCompany(scanner, companyName);
                    }
                    break;
                case 2:
                    createACompany(scanner);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Unknown command!");
            }
        }
    }

    private static void rentACar(String customerName) throws SQLException {
        Car car = carDao.getAllCars().stream().filter(tempCar -> tempCar.getName().equals(carName)).findFirst().orElse(new Car("", -1));
        if (!car.getName().equals(carName) || car.getCompanyId() == -1 || car.getCompanyId() == 0) {
            System.out.println("\nCar was not added!");
        } else {
            int carId = carDao.getIndexOfCarByName(carName) + 1;
            List<Customer> customers = customerDao.getAllCustomers();
            customers.get(customerDao.getIndexOfCustomerByName(customerName)).setRentedCarId(carId);
            customerDao = new CustomerDaoImpl(customers);
            statement.executeUpdate(String.format(UPDATE_CUSTOMER_RENTED_CAR_ID_QUERY, carId, customerName));
            System.out.printf("\nYou rented '%s'\n", carName);
        }
    }

    private static String promptToSelectACar(Scanner scanner) {
        int command;

        while (true) {
            try {
                command = Integer.parseInt(scanner.nextLine());
                if (command == 0) throw new IllegalArgumentException();
                int companyId = companyDao.getIndexOfCompanyByName(companyName) + 1;
                return carDao.getAllCars().stream()
                        .filter(car -> car.getCompanyId() == companyId)
                        .collect(Collectors.toList())
                        .get(command - 1).getName();
            } catch (NumberFormatException e) {
                System.out.println("Unknown command!");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("There is no car with such index! Try again or go back");
            }
        }
    }

    private static boolean printCarsListForCustomer(String companyName) {
        int companyId = companyDao.getIndexOfCompanyByName(companyName) + 1;
        List<Customer> customers = customerDao.getAllCustomers();
        List<Car> cars = carDao.getAllCars().stream()
                .filter(car -> car.getCompanyId() == companyId)
                .filter(car -> customers.stream().noneMatch(customer -> customer.getRentedCarId() == carDao.getIndexOfCarByName(car.getName()) + 1))
                .collect(Collectors.toList());

        if (cars.isEmpty()) {
            System.out.println("\nThe cars list is empty!");
            return false;
        } else {
            System.out.println("\nChoose a car:");
            int carId = 1;
            for (Car car : cars) {
                System.out.printf("%d. %s\n", carId++, car.getName());
            }
            System.out.println("0. Back");
            return true;
        }
    }

    private static void workWithCompany(Scanner scanner, String companyName) throws SQLException {
        while (true) {
            System.out.printf("\n'%s' company\n" +
                    "1. Car list\n" +
                    "2. Create a car\n" +
                    "0. Back\n", companyName);

            int command;

            try {
                command = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Unknown command!");
                continue;
            }

            int companyId = companyDao.getIndexOfCompanyByName(companyName);

            switch (command) {
                case 1:
                    printCarsList(companyId + 1);
                    break;
                case 2:
                    if (companyId == -1) {
                        System.out.printf("There is no such company with name: '%s'\n", companyName);
                        continue;
                    }

                    createACar(scanner, companyId + 1);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Unknown command!");
            }
        }
    }

    private static void printCarsList(int companyId) {
        List<Car> cars = carDao.getAllCars().stream()
                .filter(car -> car.getCompanyId() == companyId)
                .collect(Collectors.toList());

        if (cars.isEmpty()) {
            System.out.println("\nThe car list is empty!");
        } else {
            System.out.println("\nCar list:");
            int carId = 1;
            for (Car car : cars) {
                System.out.printf("%d. %s\n", carId++, car.getName());
            }
            System.out.println("0. Back");
        }
    }

    private static void createACar(Scanner scanner, int companyId) throws SQLException {
        if (companyId == -1) {
            System.out.println("There is no such company with specified name");
        }
        System.out.println("\nEnter the car name:");
        String newCarName = scanner.nextLine();
        boolean isCarAdded = carDao.addCar(new Car(newCarName, companyId));

        if (isCarAdded) {
            statement.executeUpdate(String.format(INSERT_CAR_INTO_TABLE_QUERY, "('" + newCarName + "', " + companyId + ")"));
            System.out.println("The car was created!");
        } else {
            System.out.printf("Car '%s' was not added! Try again", newCarName);
        }
    }

    private static String promptToSelectACompany(Scanner scanner) {
        int command;

        while (true) {
            try {
                command = Integer.parseInt(scanner.nextLine());
                if (command == 0) throw new IllegalArgumentException();
                return companyDao.getAllCompanies().get(command - 1).getName();
            } catch (NumberFormatException e) {
                System.out.println("Unknown command!");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("There is no company with such index! Try again or go back");
            }
        }
    }

    private static void createACompany(Scanner scanner) throws SQLException {
        System.out.println("\nEnter the company name:");
        String newCompanyName = scanner.nextLine();
        boolean isCompanyAdded = companyDao.addCompany(new Company(newCompanyName));

        if (isCompanyAdded) {
            statement.executeUpdate(String.format(INSERT_COMPANY_INTO_TABLE_QUERY, "('" + newCompanyName + "')"));
            System.out.println("The company was created!");
        } else {
            System.out.printf("Company '%s' was not added! Try again\n", newCompanyName);
        }
    }

    private static boolean printCompanyList() {
        List<Company> companies = companyDao.getAllCompanies();

        if (companies.isEmpty()) {
            System.out.println("\nThe company list is empty!");
            return false;
        } else {
            System.out.println("\nChoose the company:");
            int companyId = 1;
            for (Company company : companies) {
                System.out.printf("%d. %s\n", companyId++, company.getName());
            }
            System.out.println("0. Back");
            return true;
        }
    }

    private static void createDB() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        connection = DriverManager.getConnection(DB_URL);
        connection.setAutoCommit(true);
        statement = connection.createStatement();
        statement.executeUpdate(CREATE_COMPANY_TABLE_QUERY);
        statement.executeUpdate(CREATE_CAR_TABLE_QUERY);
        statement.executeUpdate(CREATE_CUSTOMER_TABLE_QUERY);
        statement.executeUpdate("ALTER TABLE COMPANY ALTER COLUMN ID RESTART WITH 1;");
    }

    private static void getAllCompaniesFromDB() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM COMPANY ORDER BY ID ASC;");
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Company> companies = new ArrayList<>();
        while (resultSet.next()) {
            companies.add(new Company(resultSet.getString("NAME")));
        }
        companyDao = new CompanyDaoImpl(companies);
    }

    private static void getAllCustomersFromDB() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM CUSTOMER ORDER BY ID ASC;");
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Customer> customers = new ArrayList<>();
        while (resultSet.next()) {
            customers.add(new Customer(resultSet.getString("NAME"), resultSet.getInt("RENTED_CAR_ID")));
        }
        customerDao = new CustomerDaoImpl(customers);
    }

    private static void getAllCarsFromDB() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM CAR ORDER BY ID ASC;");
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Car> cars = new ArrayList<>();
        while (resultSet.next()) {
            cars.add(new Car(resultSet.getString("NAME"), resultSet.getInt("COMPANY_ID")));
        }
        carDao = new CarDaoImpl(cars);
    }

    private static void setDBUrl(String[] args) {
        List<String> argsList = Arrays.asList(args);

        DB_URL = String.format(DB_URL,
                argsList.contains("-databaseFileName") ? argsList.get(argsList.indexOf("-databaseFileName") + 1) : "dbFile");
    }
}