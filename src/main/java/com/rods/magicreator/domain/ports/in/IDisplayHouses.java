package com.rods.magicreator.domain.ports.in;

import com.rods.magicreator.domain.models.Character;
import com.rods.magicreator.domain.models.House;
import lombok.Getter;
import org.springframework.core.NestedCheckedException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface IDisplayHouses {
    List<House> findHouses() throws CouldNotSearchHousesException;

    class CouldNotSearchHousesException extends NestedCheckedException {
        public CouldNotSearchHousesException(Throwable cause) {
            super("Could not search houses", cause);
        }
    }
}
