package carsharing.dao;

import carsharing.models.Customer;

import java.util.List;

public interface CustomerDao {

    List<Customer> getAllCustomers();

    boolean addCustomer(Customer newCustomer);

    boolean updateCustomer();

    boolean deleteCustomer();

    int getIndexOfCustomerByName(String customerName);
}
