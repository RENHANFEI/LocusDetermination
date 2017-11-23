package com.example.renhanfei.phonelocusdetermination;

/**
 * Created by renhanfei on 17/4/5.
 */

public class KalmanStatus {
    public double[] advanceEstimation;
    public double[] estimation;
    public double[][] advanceError; // P-
    public double[][] error; // P
    public double[] measurement;
    public double[][] kalmanGain;
    public double time; // from start unit:s

//    public KalmanStatus(double[] measurement) {
//        measurement = measurement.clone();
//    }

    public KalmanStatus() {
        advanceEstimation = new double[4];
        estimation = new double[4];
        advanceError = new double[4][4];
        error = new double[4][4];
        measurement = new double[4];
        kalmanGain = new double[4][4];
    }

}
