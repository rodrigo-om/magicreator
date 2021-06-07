package com.rods.magicreator.controller;

import com.rods.magicreator.domain.models.House;
import com.rods.magicreator.domain.ports.in.IDisplayHouses;
import com.rods.magicreator.domain.ports.in.IDisplayHouses.CouldNotSearchHousesException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class HousesController {

    private final IDisplayHouses housesService;

    public HousesController(IDisplayHouses housesService) {
        this.housesService = housesService;
    }

    @GetMapping("/houses")
    public List<House> findAll() {
        try {
            return housesService.findHouses();
        } catch (CouldNotSearchHousesException e) {
            if (e.contains(IllegalArgumentException.class))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMostSpecificCause().getMessage(), e);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMostSpecificCause().getMessage(), e);
        }
    }
}


