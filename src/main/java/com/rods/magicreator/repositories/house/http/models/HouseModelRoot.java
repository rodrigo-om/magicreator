package com.rods.magicreator.repositories.house.http.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Just a wrapper because HousesAPI returns {@link com.rods.magicreator.repositories.house.http.models.HouseModel Houses} inside
 * a named json array called "houses"
 */
@Getter
@Setter
@JsonDeserialize(builder = HouseModelRoot.HouseModelRootBuilder.class)
@Builder(builderClassName = "HouseModelRootBuilder")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HouseModelRoot {
    public List<HouseModel> houses;

    @JsonPOJOBuilder(withPrefix = "")
    public static class HouseModelRootBuilder {

    }
}
