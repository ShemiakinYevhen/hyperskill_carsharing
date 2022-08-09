package carsharing.dao;

import carsharing.models.Car;

import java.util.List;

public interface CarDao {

    List<Car> getAllCars();

    boolean addCar(Car newCompany);

    boolean updateCar();

    boolean deleteCar();

    int getIndexOfCarByName(String carName);
}
