package com.booking.testcontainersexampleapp.repository;

import com.booking.testcontainersexampleapp.advice.CarControllerAdvice;
import com.booking.testcontainersexampleapp.entity.Car;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CarRepository {

    private static final Logger logger = LoggerFactory.getLogger(CarRepository.class);

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

    public Car save(Car car) {
        if (car.getId() == null) {
            String sql = "INSERT INTO car (name) VALUES (?)";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, car.getName());
                preparedStatement.executeUpdate();
                try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                    if (rs.next()) {
                        car.setId(rs.getLong(1));
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException("Error saving car: " + car, e);
            }
        } else {
            String sql = "INSERT INTO car (id, name) VALUES (?, ?) " +
                    "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name";

            try (Connection conn = dataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setLong(1, car.getId());
                preparedStatement.setString(2, car.getName());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error updating car: " + car, e);
            }
        }
        return car;
    }

    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM car WHERE id = ? LIMIT 1";

        try (Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Car does not exist: " + id, e);
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM car WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting car by id: " + id, e);
        }
    }

    public void delete(Car car) {
        if (car.getId() != null) {
            deleteById(car.getId());
        }
    }

    public void deleteAll() {
        String sql = "DELETE FROM car";

        try (Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting all cars", e);
        }
    }

    public void deleteAllById(Iterable<Long> ids) {
        String sql = "DELETE FROM car WHERE id = ?";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                for (var id : ids) {
                    preparedStatement.setLong(1, id);
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting cars by ids", e);
        }
    }

    public List<Car> saveAll(List<Car> cars) {
        String sql = "INSERT INTO car (name) VALUES (?)";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (Car car : cars) {
                    preparedStatement.setString(1, car.getName());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();

                try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                    int i = 0;
                    while (rs.next()) {
                        cars.get(i).setId(rs.getLong(1));
                        i++;
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            logger.error("Error saving cars: " + sql, e);
            throw new RuntimeException("Error saving all cars", e);
        }

        return cars;
    }

    private Car mapRowToCar(ResultSet rs) throws SQLException {
        Car car =  new Car();
        car.setId(rs.getLong("id"));
        car.setName(rs.getString("name"));
        return car;
    }
}
