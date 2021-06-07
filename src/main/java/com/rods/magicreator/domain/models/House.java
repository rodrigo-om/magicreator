package com.rods.magicreator.domain.models;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Domain model for working with a Harry Potter House
 */
@AllArgsConstructor
@Getter
@Setter
@With
@Builder(toBuilder = true)
public class House{
    public String id;
    public String name;
    public String school;
}
