package carsharing.models;

import java.util.Objects;

public class Car {

    private String name;
    private int companyId;

public Car(String name, int companyId) {
        this.name = name;
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return companyId == car.companyId && Objects.equals(name, car.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, companyId);
    }

    @Override
    public String toString() {
        return String.format("Car: { Name: '%s', Company id: %d }", name, companyId);
    }
}
