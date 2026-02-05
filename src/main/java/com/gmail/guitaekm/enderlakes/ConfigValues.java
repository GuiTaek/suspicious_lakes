package com.gmail.guitaekm.enderlakes;

import java.util.List;

public class ConfigValues {
    public int nrLakes = 276_987_611; // correct: 84_890_419
    public double powerDistance = 1.7;
    public List<Integer> cycleWeights = List.of(
            0, 5, 5, 3, 3, 1, 1
    );
    public int[] factsPhi = new int[]{2, 5, 27698761}; // correct: new int[]{2, 3, 17, 41, 53, 383};
    public int minimumDistance = 2;
}
