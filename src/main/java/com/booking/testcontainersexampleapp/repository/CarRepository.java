package com.booking.testcontainersexampleapp.repository;

import com.booking.testcontainersexampleapp.entity.Car;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CarRepository {

    private final DataSource dataSource;

    public List<Car> findAll() {
        String sql = "SELECT * FROM car";
        List<Car> cars = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()
        ) {
            while (rs.next()) {
                Car car = mapRowToCar(rs);
                cars.add(car);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding car ", e);
        }

        return cars;
    }

    public Optional<Car> findById(Long id) {
        String sql = "SELECT * FROM car WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCar(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding car by id: " + id, e);
        }
        return Optional.empty();
    }

    private Car mapRowToCar(ResultSet rs) throws SQLException {
        Car car =  new Car();
        car.setId(rs.getLong("id"));
        car.setName(rs.getString("name"));
        return car;
    }
}
