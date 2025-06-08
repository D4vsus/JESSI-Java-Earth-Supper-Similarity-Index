package org.D4vsus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * <h1>Main</h1>
 * <p>Run a simple example to run the model</p>
 *
 * @author D4vsus
 */
public class Main {

    public static EsiModel model = new EsiModel();

    public static void main(String[] args) {
        try {
            List<String> dataset = Files.readAllLines(Paths.get(args[1]));
            dataset.removeFirst();

            dataset.forEach(System.out::println);

            final Double[][] X = new Double[dataset.size()][];
            final Double[]   Y = new Double[dataset.size()];

            int row = 0;
            for (String record : dataset){
                String[] recordSplit = record.split(",");
                Double[] xRecord = new Double[7];
                for (int i = 0; i < 7; i++) {
                    try {
                        xRecord[i] = Double.parseDouble(recordSplit[i + 3]);
                    } catch (NumberFormatException exception){
                        xRecord[i] = Double.NaN;
                    }
                }
                X[row] = xRecord;
                Y[row] = Double.parseDouble(recordSplit[recordSplit.length - 1]);
                row++;
            }

            double sumAvg;
            double numbElements;
            double avg;


            for (int j = 0; j < 7; j++) {
                sumAvg = 0.0;
                numbElements = 0.0;

                for (Double[] x : X){
                    if (!x[j].isNaN()){
                        sumAvg += x[j];
                        numbElements++;
                    }
                }

                avg = sumAvg/numbElements;

                for (int i = 0; i < X.length; i++) {
                    if (X[i][j].isNaN()){
                        X[i][j] = avg;
                    }
                }

            }

            System.out.println();
            Arrays.stream(X).forEach(record -> Arrays.stream(record).forEach(System.out::println));
            System.out.println();

            Double[][] xTrain = Arrays.copyOf(X, 56);
            Double[]   yTrain = Arrays.copyOf(Y, 56);
            Double[][] xTest = Arrays.copyOfRange(X, 56, X.length - 1);
            Double[]   yTest = Arrays.copyOfRange(Y, 56, Y.length - 1);

            model.fit(xTrain, yTrain, xTest, yTest,0.5e-2,1000,1.0e-1);

            System.out.println();
            System.out.println("Weights: " + "Mass(Ground_Uds), Radius(Earth-radius), Solar Irradiation, Temp Kelvin, Orbital Period (days),Earth Distance (Lightyears), Age");
            System.out.println("Weights: " + Arrays.toString(model.getWeights()));
            System.out.println();

            System.out.println("inst#,actual,predicted,error");
            System.out.println("============================");
            for (int i = 0; i < X.length; i++){
                double result = model.prediction(X[i]);
                System.out.println((i + 1) + "|" + new DecimalFormat("0.000").format(Y[i]) + "|" + new DecimalFormat("0.000").format(result) + "|" + new DecimalFormat("0.000").format(result - Y[i]));
            }
            System.out.println();
            double quadraticError = model.computeMSE(xTest,yTest);
            System.out.println("Quadratic error: " + quadraticError);
            if (quadraticError < 0.006){
                //model.saveModel(Paths.get("/"));
            }

/*            model.loadModel(Paths.get("/"));
            quadraticError = model.computeMSE(xTest,yTest);
            System.out.println("Quadratic error: " + quadraticError);
            System.out.println("inst#,actual,predicted,error");
            System.out.println("============================");
            for (int i = 0; i < X.length; i++){
                double result = model.prediction(X[i]);
                System.out.println((i + 1) + "|" + new DecimalFormat("0.000").format(Y[i]) + "|" + new DecimalFormat("0.000").format(result) + "|" + new DecimalFormat("0.000").format(result - Y[i]));
            }
            System.out.println("Quadratic error: " + quadraticError);
            System.out.println("Weights: " + "Mass(Ground_Uds), Radius(Earth-radius), Solar Irradiation, Temp Kelvin, Orbital Period (days),Earth Distance (Lightyears), Age");
            System.out.println("Weights: " + Arrays.toString(model.getWeights()));*/


        } catch (IOException ex){
            System.out.println("Not able to read the file");
        }
    }
}