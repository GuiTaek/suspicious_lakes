package com.gmail.guitaekm.enderlakes;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;

import java.util.List;

@Modmenu(modId = "enderlakes")
@Config(name = "enderlakes", wrapperName = "Config")
public class ConfigModel {
    public int nrLakes = 7499981;
    @RangeConstraint(min = 2, max = 4)
    public double powerDistance = 1.5;
    public List<Integer> cycleWeights = List.of(
            0, 5, 5, 3, 3, 1, 1
    );
    public int minimumDistance = 1;
}
