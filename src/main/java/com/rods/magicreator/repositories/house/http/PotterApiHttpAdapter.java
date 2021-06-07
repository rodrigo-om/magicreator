package com.rods.magicreator.repositories.house.http;

import com.rods.magicreator.domain.models.House;
import com.rods.magicreator.domain.ports.out.IObtainHousesInfo;
import com.rods.magicreator.repositories.house.http.PotterApiClient.PotterApiCallException;
import com.rods.magicreator.repositories.house.http.models.HouseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.management.timer.Timer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PotterApiHttpAdapter implements IObtainHousesInfo {

    private final PotterApiClient api;

    public PotterApiHttpAdapter(PotterApiClient api) {
        this.api = api;
    }

    @Cacheable("houses")
    public Optional<House> getHouseById(String id) throws ErrorObtainingHousesException {
        try {
            List<House> houses = getHouses();
            return houses.stream().filter(h -> id.equals(h.getId())).findFirst();
        } catch (Exception e) {
            log.error("Error obtaining house - House id: {}", id, e);
            throw new ErrorObtainingHousesException(e);
        }
    }

    public List<House> getHouses() throws ErrorObtainingHousesException {
        try {
            return api.getHouses().getHouses().stream()
                    .map(this::toHouse)
                    .collect(Collectors.toList());
        } catch (PotterApiCallException e) {
            log.error("Error obtaining houses. Status Code: {}", e.getStatusCode(), e);
            throw new ErrorObtainingHousesException(e);
        }
    }

    private House toHouse(HouseModel houseModel) {
        return House.builder()
                .id(houseModel.getId())
                .name(houseModel.getName())
                .school(houseModel.getSchool())
                .build();
    }

    @Scheduled(fixedRate = Timer.ONE_HOUR)
    private void updateHouses() {
        try {
            getHouses().forEach(this::updateCache);
        } catch (ErrorObtainingHousesException e) {
            log.error("Error updating houses cache");
        }
    }

    @CachePut(value = "houses", key = "#house.id")
    //Method used exclusively for updating cache according to scheduled
    //method above
    public House updateCache(House house) { return house; }
}
