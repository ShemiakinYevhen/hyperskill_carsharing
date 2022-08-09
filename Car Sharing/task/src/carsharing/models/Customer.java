package carsharing.models;

import java.util.Objects;

public class Customer {

    private String name;
    private int rentedCarId;

    public Customer (String name) {
        this.name = name;
        rentedCarId = -1;
    }

    public Customer (String name, int rentedCarId) {
        this.name = name;
        this.rentedCarId = rentedCarId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRentedCarId() {
        return rentedCarId;
    }

    public void setRentedCarId(int rentedCarId) {
        this.rentedCarId = rentedCarId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return rentedCarId == customer.rentedCarId && Objects.equals(name, customer.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rentedCarId);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "name='" + name + '\'' +
                ", rentedCarId=" + rentedCarId +
                '}';
    }
}
