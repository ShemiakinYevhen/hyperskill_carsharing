package carsharing.impl;

import carsharing.dao.CarDao;
import carsharing.models.Car;

import java.util.ArrayList;
import java.util.List;

public class CarDaoImpl implements CarDao {

    private List<Car> cars;

    public CarDaoImpl() {
        cars = new ArrayList<>();
    }

    public CarDaoImpl(List<Car> cars) {
        this.cars = cars;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    @Override
    public List<Car> getAllCars() {
        return cars;
    }

    @Override
    public boolean addCar(Car newCar) {
        if (cars.contains(newCar)) return false;
        else return cars.add(newCar);
    }

    @Override
    public boolean updateCar() {
        return false;
    }

    @Override
    public boolean deleteCar() {
        return false;
    }

    @Override
    public int getIndexOfCarByName(String carName) {
        for (int i = 0; i < cars.size(); i++) {
            if (cars.get(i).getName().equals(carName)) return i;
        }
        return -1;
    }
}
