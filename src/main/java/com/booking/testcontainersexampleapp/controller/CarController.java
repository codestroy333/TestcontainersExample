package com.booking.testcontainersexampleapp.controller;

import com.booking.testcontainersexampleapp.entity.Car;
import com.booking.testcontainersexampleapp.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CarController {

    private final CarRepository carRepository;

    @GetMapping("/cars/save")
    public List<Car> saveCars() {
        Car car1 = new Car(null, "Toyota Camry");
        Car car2 = new Car(null, "Honda Accord");
        Car car3 = new Car(null, "Ford Mustang");

        return carRepository.saveAll(List.of(car1, car2, car3));
    }
}
