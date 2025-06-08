package org.D4vsus;

import java.io.Serializable;

/**
 * <h1>EsiModelDTO</h1>
 * <p>Save the data of the model</p>
 *
 * @author D4vsus
 */
public class EsiModelDTO implements Serializable {
    private Double[] inputVector;
    private Double[] weights;
    private Double[] earth;

    public EsiModelDTO() {
    }

    public Double[] getInputVector() {
        return inputVector;
    }

    public void setInputVector(Double[] inputVector) {
        this.inputVector = inputVector;
    }

    public Double[] getWeights() {
        return weights;
    }

    public void setWeights(Double[] weights) {
        this.weights = weights;
    }

    public Double[] getEarth() {
        return earth;
    }

    public void setEarth(Double[] planet) {
        this.earth = planet;
    }
}
