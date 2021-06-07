package com.rods.magicreator.repositories.house.http.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import java.util.List;


@Data
@JsonDeserialize(builder = HouseModel.HouseModelBuilder.class)
@Builder(builderClassName = "HouseModelBuilder")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HouseModel {
    public String school;
    public List<String> colors;
    public String founder;
    public String houseGhost;
    public String headOfHouse;
    public String name;
    public String mascot;
    public List<String> values;
    public String id;

    @JsonPOJOBuilder(withPrefix = "")
    public static class HouseModelBuilder {

    }
}

