package com.gmail.guitaekm.enderlakes;

/*
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;
*/

import java.util.List;

public class ConfigModel {
    public int nrLakes = 276_987_611;
    public double powerDistance = 1.7;
    public List<Integer> cycleWeights = List.of(
            0, 5, 5, 3, 3, 1, 1
    );
    public int[] factsPhi = new int[]{2, 5, 27698761};
    public int minimumDistance = 2;
}
