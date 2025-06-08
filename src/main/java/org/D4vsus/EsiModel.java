package org.D4vsus;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

/**
 * <h1>EsiModel</h1>
 * <p>Fits the model, make the predictions and brings stuff to work</p>
 *
 * @author D4vsus
 */
public class EsiModel {

    private EsiModelDTO modelDTO;

    public EsiModel() {
        this.modelDTO = new EsiModelDTO();

        Double[] weights = new Double[7];
        Arrays.fill(weights,0.0);

        this.modelDTO.setWeights(weights);

        //Mass(Ground_Uds), Radius(Earth-radius), Solar Irradiation, Temp Kelvin, Orbital Period (days),Earth Distance (Light-years), Age
        Double[] earthValues = new Double[7];
        Arrays.fill(earthValues,1.0);

        this.modelDTO.setEarth(earthValues);
    }

    /**
     * <h1>computeMSE()</h1>
     * <p>Return the mean square error</p>
     *
     * @param X {@link Double[][]}
     * @param Y {@link Double[]}
     * @return {@link Double}
     */
    public double computeMSE(Double[][] X, Double[] Y) {
        int m = Y.length;
        double sumErrors = 0.0;

        for (int i = 0; i < m ;i++){
            double yPredicted = prediction(X[i]);
            double error = yPredicted - Y[i];
            sumErrors += Math.pow(error,2);
        }

        return  sumErrors / (2.0 * m);
    }

    /**
     * <h1>computeMSE()</h1>
     * <p>Return the mean square error with the given weights</p>
     *
     * @param X {@link Double[][]}
     * @param Y {@link Double[]}
     * @param weights {@link Double[]}
     * @return {@link Double}
     */
    public double computeMSE(Double[][] X, Double[] Y, Double[] weights) {
        int m = Y.length;
        double sumErrors = 0.0;

        for (int i = 0; i < m ;i++){
            double yPredicted = prediction(X[i], weights);
            double error = yPredicted - Y[i];
            sumErrors += Math.pow(error,2);
        }

        return  sumErrors / (2.0 * m);
    }

    /**
     * <h1>esiFormula()</h1>
     * <p>Execute the esi formula</p>
     *
     * @param planet {@link Double[]}
     * @return {@link Double}
     */
    private Double esiFormula(Double[] planet){
        Double[] earth  = modelDTO.getEarth();
        int n = earth.length;
        Double[] weights = modelDTO.getWeights();
        double result = 1.0;

        for (int i = 0; i < n; i++) {
            double parameter_similarity = 1.0 - Math.abs((planet[i] - earth[i]) / (planet[i] + earth[i]));
            double weighted_similarity = Math.pow(parameter_similarity, (weights[i] / n) );
            result *= weighted_similarity;
        }
        return result;
    }

    /**
     * <h1>esiFormula()</h1>
     * <p>Execute the esi formula</p>
     *
     * @param planet {@link Double[]}
     * @return {@link Double}
     */
    private Double esiFormula(Double[] planet, Double[] weights){
        Double[] earth  = modelDTO.getEarth();
        int n = earth.length;
        double result = 1.0;

        for (int i = 0; i < n; i++) {
            double parameter_similarity = 1.0 - Math.abs((planet[i] - earth[i]) / (planet[i] + earth[i]));
            double weighted_similarity = Math.pow(parameter_similarity, (weights[i] / n) );
            result *= weighted_similarity;
        }
        return result;
    }

    /**
     * <h1>notWeightedEsiFormula()</h1>
     * <p>Not weighted formula to get</p>
     *
     * @param planet {@link Double}[]
     * @param arg int
     * @return {@link Double}
     */
    private Double notWeightedEsiFormula(Double[] planet,int arg){
        Double[] earth  = modelDTO.getEarth();
        int n = earth.length;
        double result;

        double parameter_similarity = 1.0 - Math.abs((planet[arg] - earth[arg]) / (planet[arg] + earth[arg]));
        result = Math.log(parameter_similarity)/n;

        return result;
    }

    /**
     * <h1>loadModel()</h1>
     * <p>Loads the model from a file to the EsiDTO</p>
     *
     * @param path {@link Path[]}
     */
    public void loadModel(Path path) {
        try {
            FileInputStream fileIn = new FileInputStream(path.toFile());
            ObjectInputStream in = new ObjectInputStream(fileIn);
            modelDTO = (EsiModelDTO) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            System.out.println("Error reading the file");
        } catch (ClassNotFoundException c) {
            System.out.println("EsiModelDTO class not found");
        }
    }

    /**
     * <h1>prediction()</h1>
     * <p>Performs a prediction with the given record</p>
     *
     * @param x {@link Double}[]
     * @return {@link Double}
     */
    public Double prediction(Double[] x) {
        //Normalize data
        Double mass = x[0];
        Double radius = x[1];
        Double solarIrradiation = x[2];
        Double temp = (x[3] / 255.0000);
        Double orbitalPeriod = (x[4] / 365.2711);
        Double earthDistance = x[5] + 1.0;
        Double age = x[6];

        return esiFormula(Arrays.asList(mass,radius,solarIrradiation,temp,orbitalPeriod,earthDistance,age).toArray(new Double[0]));
    }

    /**
     * <h1>prediction()</h1>
     * <p>Performs a prediction with the given record with the given weights</p>
     *
     * @param x {@link Double}[]
     * @return {@link Double}
     */
    public Double prediction(Double[] x, Double[] weights) {
        //Normalize data
        Double mass = x[0];
        Double radius = x[1];
        Double solarIrradiation = x[2];
        Double temp = (x[3] / 255.0000);
        Double orbitalPeriod = (x[4] / 365.2711);
        Double earthDistance = x[5] + 1.0;
        Double age = x[6];

        return esiFormula(Arrays.asList(mass,radius,solarIrradiation,temp,orbitalPeriod,earthDistance,age).toArray(new Double[0]), weights);
    }

    /**
     * <h1>fit()</h1>
     * <p>Fit the model searching for the most optimal weights</p>
     *
     * @param xTrain {@link Double}[][]
     * @param yTrain {@link Double}[]
     * @param learningRate {@link Double}
     * @param iterations {@link Integer}
     * @param noiseRate {@link Double}
     */
    public void fit(Double[][] xTrain, Double[] yTrain, Double learningRate, Integer iterations, Double noiseRate) {
        Double[] trainingWeights = this.modelDTO.getWeights();
        Double[] posibleTrainingWeights = this.modelDTO.getWeights();
        Double[] xRecord;
        Double   yRecord;
        Double yPrediction;
        Double error;
        int rowNum = xTrain.length;
        int randomRecord;

        for (int iteration = 0; iteration < iterations; iteration++) {
            randomRecord = new Random().nextInt(rowNum);
            xRecord = xTrain[randomRecord];
            yRecord = yTrain[randomRecord];

            yPrediction = prediction(xRecord);
            error = yPrediction - yRecord;

            for (int j = 0; j < xRecord.length; j++) {
                double noise = new Random().nextGaussian() * noiseRate;
                if (iteration < 1) {
                    posibleTrainingWeights[j] -= learningRate * error;
                } else {
                    posibleTrainingWeights[j] -= learningRate * error * notWeightedEsiFormula(xRecord,j) + noise;
                }
            }

            trainingWeights = posibleTrainingWeights;

            if (computeMSE(xTrain, yTrain) < 0.01) {
                break;
            }
        }

        this.modelDTO.setWeights(trainingWeights);
    }

    /**
     * <h1>fit()</h1>
     * <p>Fit the model searching for the most optimal weights with a test to make sure it don't overfit</p>
     *
     * @param xTrain {@link Double}[][]
     * @param yTrain {@link Double}[]
     * @param learningRate {@link Double}
     * @param iterations {@link Integer}
     * @param noiseRate {@link Double}
     */
    public void fit(Double[][] xTrain, Double[] yTrain, Double[][] xTest, Double[] yTest, Double learningRate, Integer iterations, Double noiseRate) {
        Double[] trainingWeights = this.modelDTO.getWeights();
        Double[] posibleTrainingWeights = this.modelDTO.getWeights();
        Double[] xRecord;
        Double   yRecord;
        Double yPrediction;
        Double error;
        int rowNum = xTrain.length;
        int randomRecord;

        for (int iteration = 0; iteration < iterations; iteration++) {
            randomRecord = new Random().nextInt(rowNum);
            xRecord = xTrain[randomRecord];
            yRecord = yTrain[randomRecord];

            yPrediction = prediction(xRecord);
            error = yPrediction - yRecord;

            for (int j = 0; j < xRecord.length; j++) {
                double noise = new Random().nextGaussian() * (noiseRate / Math.pow(iteration + 1, 2));
                if (iteration < 1) {
                    posibleTrainingWeights[j] -= learningRate * error * noise;
                } else {
                    posibleTrainingWeights[j] -= learningRate * error * notWeightedEsiFormula(xRecord,j) + noise;
                }
            }

            trainingWeights = Arrays.copyOf(posibleTrainingWeights, posibleTrainingWeights.length);

            if (computeMSE(xTest, yTest) < 1.0e-4) {
                break;
            }
        }

        this.modelDTO.setWeights(trainingWeights);

        computeMSE(xTrain, yTrain);
    }

    /**
     * <h1>saveModel()</h1>
     * <p>Save the model in a file</p>
     *
     * @param path {@link Path}
     */
    public void saveModel(Path path) {
        try {
            Path modelPath = Paths.get(path.toAbsolutePath() + File.separator + "ESI.model");
            Files.createFile(modelPath);
            FileOutputStream fileModel = new FileOutputStream(modelPath.toFile());
            ObjectOutputStream out = new ObjectOutputStream(fileModel);
            out.writeObject(modelDTO);
            out.close();
            fileModel.close();
            System.out.println("Serialized data is saved in ESI.model");
        } catch (IOException ex) {
            System.out.println("File can't be read");
        }
    }

    /**
     * <h1>getWeights()</h1>
     * <p>Return the weights of the model</p>
     *
     * @return {@link Double}
     */
    public Double[] getWeights(){
        return modelDTO.getWeights();
    }
}
