package com.example.renhanfei.phonelocusdetermination;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;

/**
 * Created by renhanfei on 17/4/5.
 */

public class KalmanFilter {

    KalmanStatus preStatus, curStatus;

    private final double deltaT = 1; // unit: s

    private final double[][] stateTransitionM = {
            {1, 0, deltaT, 0},
            {0, 1, 0, deltaT},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
    };

    private final double[][] measurementM = {
            {1, 0, 0, 0},
            {0, 1, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
    };

    private final double[][] processNoiseCov = {
            {0.002, 0, 0, 0},
            {0, 0.003, 0, 0},
            {0, 0, 0.001, 0},
            {0, 0, 0, 0.001}
    };

    private final double[][] measureNoiseCov = {
            {38.688155, 11.441456, 0, 0},
            {11.441456, 61.841524, 0, 0},
            {0, 0, 7.732136e-5, 2.0560208e-5},
            {0, 0, 2.0560208e-5, 8.3577526e-4}
    };

//    private final double[][] measureNoiseCov = {
//            {175.74293, 4.0259166, 0, 0},
//            {4.0259116, 90.301509, 0, 0},
//            {0, 0, 7.732136e-5, 2.0560208e-5},
//            {0, 0, 2.0560208e-5, 8.3577526e-4}
//    };

//    private final double[][] measureNoiseCov = {
//            {3612.7783, 3562.07, 0, 0},
//            {3562.07, 7577.5699, 0, 0},
//            {0, 0, 7.732136e-5, 2.0560208e-5},
//            {0, 0, 2.0560208e-5, 8.3577526e-4}
//    };

    // control iput
    double accX = 0;
    double accY = 0;

    double accXMeanError = 0.0086473;
    double accYMeanError = 0.0171874;

    double[] controlVec = {deltaT * deltaT / 2, deltaT * deltaT / 2, deltaT, deltaT};
    double[][] controlM = {
            {accX - accXMeanError, 0, 0, 0},
            {0, accY - accYMeanError, 0, 0},
            {0, 0, accX - accXMeanError, 0},
            {0, 0, 0, accY - accYMeanError}
    };


    public KalmanFilter() {
    }

    public KalmanFilter(KalmanStatus pre, KalmanStatus cur) {
        preStatus = new KalmanStatus();
        curStatus = new KalmanStatus();
        preStatus = pre;
        curStatus = cur;
    }

    public void doFiltering() {

        String TAG = "Kalman";

        // 1 Compute estimation & error cov based on former data
        curStatus.advanceEstimation = addVectors(
                multiplyFourOne(stateTransitionM, preStatus.estimation),
                multiplyFourOne(controlM, controlVec));
        curStatus.advanceError = addMatrices(
                multiplyFourFour(
                        multiplyFourFour(stateTransitionM, preStatus.error),
                        transposeFour(stateTransitionM)),
                processNoiseCov);
        String info = "advanceEstimation: " + String.valueOf(curStatus.advanceEstimation[0])
                + "  advanceError" + String.valueOf(curStatus.advanceError[0][0]);
        Log.i(TAG, info);

        // 2 Compute kalman gain
        curStatus.kalmanGain = multiplyFourFour(
                multiplyFourFour(
                        inverseFour(curStatus.advanceError),
                        measurementM),
                inverseFour(addMatrices(
                        multiplyFourFour
                                (multiplyFourFour
                                                (measurementM,
                                                        inverseFour(curStatus.advanceError)),
                                        measurementM),
                        measureNoiseCov)));
        String info2 = "Kalmangain: " + String.valueOf(curStatus.kalmanGain[0][0]);
        Log.i(TAG, info2);

        // 3 Update estimation
        curStatus.estimation = addVectors(curStatus.advanceEstimation,
                multiplyFourOne(
                        curStatus.kalmanGain,
                        minusVectors(curStatus.measurement,
                                multiplyFourOne(measurementM,
                                        curStatus.advanceEstimation))));
        String info3 = "Estimation: " + String.valueOf(curStatus.estimation[0]);
        Log.i(TAG, info3);

        // 4 Update error var
        curStatus.error = minusMatrices(curStatus.advanceError,
                multiplyFourFour(
                        multiplyFourFour(
                                curStatus.kalmanGain,
                                measurementM),
                        curStatus.advanceError));

    }

    public double[] multiplyFourOne(double[][] a, double[] b) { // multiply 4*4 * 4*1
        double product[] = {0, 0, 0, 0};
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                product[i] += a[i][j] * b[j];
            }
        }
        return product;
    }

    public double[][] multiplyFourFour(double[][] a, double[][] b) {
        double[][] product = new double[4][4];
        for (int i = 0; i < 4; i++)
            Arrays.fill(product[i], 0);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    product[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return product;
    }

    public double[][] transposeFour(double[][] a) {
        double[][] transpose = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                transpose[i][j] = a[j][i];
            }
        }
        return transpose;
    }

    public double[][] inverseFour(double[][] a) {
        double[][] inverse = new double[4][4];
        double determinant = determinantFour(a);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                inverse[i][j] = adjointElem(a, i, j) / determinant;
            }
        }

        return inverse;
    }

    private double determinantTwo(double[][] a) {
        return a[0][0] * a[1][1] - a[0][1] * a[1][0];
    }

    private double determinantThree(double[][] a) {
        return a[0][0] * a[1][1] * a[2][2] + a[1][0] * a[2][1] * a[0][2]
                + a[0][1] * a[1][2] * a[2][0] - a[0][2] * a[1][1] * a[2][0]
                - a[0][1] * a[1][0] * a[2][2] - a[0][0] * a[1][2] * a[2][1];
    }

    private double determinantFour(double[][] a) {
        double[][] tempM = new double[3][3];
        double determinant = 0;
        int[][] index = {{1, 2, 3}, {0, 2, 3}, {0, 1, 3}, {0, 1, 2}};

        for (int i = 0; i < 4; i++) {
            // copy tempM
            for (int m = 0; m < 3; m++) {
                for (int n = 0; n < 3; n++) {
                    tempM[m][n] = a[m + 1][index[i][n]];
                }
            }
            determinant += a[0][i] * determinantThree(tempM) * Math.pow(-1, i);
        }
        return determinant;
    }

    private double adjointElem(double[][] a, int i, int j) { // get Aij
        double[][] tempM = new double[3][3];
        int[][] index = {{1, 2, 3}, {0, 2, 3}, {0, 1, 3}, {0, 1, 2}};
        for (int m = 0; m < 3; m++) {
            for (int n = 0; n < 3; n++) {
                tempM[m][n] = a[index[j][m]][index[i][n]];
            }
        }
        return determinantThree(tempM) * Math.pow(-1, i + j);
    }

    private double[] addVectors(double[] a, double[] b) {
        double[] sum = new double[4];
        for (int i = 0; i < 4; i++) {
            sum[i] = a[i] + b[i];
        }
        return sum;
    }

    private double[] minusVectors(double[] a, double[] b) {
        double[] diff = new double[4];
        for (int i = 0; i < 4; i++) {
            diff[i] = a[i] - b[i];
        }
        return diff;
    }

    private double[][] addMatrices(double[][] a, double[][] b) {
        double[][] sum = new double[4][4];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                sum[i][j] = a[i][j] + b[i][j];

        return sum;
    }

    private double[][] minusMatrices(double[][] a, double[][] b) {
        double[][] sum = new double[4][4];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                sum[i][j] = a[i][j] - b[i][j];

        return sum;
    }

    private double[][] covarianceM(double[] a) {
        double[][] cov = new double[4][4];
        double mean = 0;

        // get mean
        for (int i = 0; i < 4; i++)
            mean += a[i];
        mean /= 4;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {

            }
        }

        return cov;
    }
}
