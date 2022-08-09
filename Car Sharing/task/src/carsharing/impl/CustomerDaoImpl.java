package carsharing.impl;

import carsharing.dao.CustomerDao;
import carsharing.models.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomerDaoImpl implements CustomerDao {

    private List<Customer> customers;

    public CustomerDaoImpl() {
        customers = new ArrayList<>();
    }

    public CustomerDaoImpl(List<Customer> customers) {
        this.customers = customers;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customers;
    }

    @Override
    public boolean addCustomer(Customer newCustomer) {
        if (customers.contains(newCustomer)) return false;
        else return customers.add(newCustomer);
    }

    @Override
    public boolean updateCustomer() {
        return false;
    }

    @Override
    public boolean deleteCustomer() {
        return false;
    }

    @Override
    public int getIndexOfCustomerByName(String customerName) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getName().equals(customerName)) return i;
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerDaoImpl that = (CustomerDaoImpl) o;
        return Objects.equals(customers, that.customers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customers);
    }

    @Override
    public String toString() {
        return "CustomerDaoImpl {\n" +
                "customers:\n" + customers.stream()
                .map(customer -> String.format("( name = %s, rented car id = %s )\n",
                        customer.getName(), customer.getRentedCarId() == -1 ? "No car rented" : customer.getRentedCarId()))
                .collect(Collectors.joining()) +
                "\n}";
    }
}
