package com.rods.magicreator.domain.ports.out;

import com.rods.magicreator.domain.models.House;
import org.springframework.core.NestedCheckedException;

import java.util.List;
import java.util.Optional;

public interface IObtainHousesInfo {
    List<House> getHouses() throws ErrorObtainingHousesException;
    Optional<House> getHouseById(String id) throws ErrorObtainingHousesException;

    class ErrorObtainingHousesException extends NestedCheckedException {
        public ErrorObtainingHousesException(Throwable cause) {
            super("Error obtaining houses", cause);
        }
    }
}
