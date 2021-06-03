package com.rods.magicreator.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Domain model for working with a Harry Potter House
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class House{
    public String school;
    public List<String> colors;
    public String founder;
    public String houseGhost;
    public String headOfHouse;
    public String name;
    public String mascot;
    public List<String> values;
    public String id;
}
