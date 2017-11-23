package com.example.renhanfei.mylocationmap;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

public class MeasurementAnalysisActivity extends Activity {

    private int scale = 8;
    private double zero = 0.000000000000000000;

    LocationRecordsDB gpsDB;
    AccelerometerDB accDB;

    double noiseX2 = 0, noiseY2 = 0, noiseXY = 0;
    double noiseVX2 = 0, noiseVY2 = 0, noiseVXVY = 0;

    // Got from the first task
    private double centerLat = 1.29866;
    private double centerLng = 103.7784;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement_analysis);

        gpsDB = new LocationRecordsDB(this);
        accDB = new AccelerometerDB(this);

        getGpsError();
        getAccError();

        showNoiseMatrix();
    }

    public void getGpsError() {

        double varFlyawayThreshold = 30;
        double meanFlyawayThreshold = 50;

        int pointNum = 25;
        double[][] east = new double[pointNum][2];
        double[][] south = new double[pointNum][2];
        double[][] west = new double[pointNum][2];
        double[][] north = new double[pointNum][2];

        int eastCursor = 0;
        int southCursor = 0;
        int westCursor = 0;
        int northCursor = 0;

        // 0: lat mean, 1: lng mean, 2: lat variance, 3: lng variance
        double[] eastAnalysis = {0, 0, 0, 0};
        double[] southAnalysis = {0, 0, 0, 0};
        double[] westAnalysis = {0, 0, 0, 0};
        double[] northAnalysis = {0, 0, 0, 0};

        double[] tempPoint = new double[2];

        ArrayList<String[]> myRecords = getAllGpsRecords();

        for (int k = 0; k < myRecords.size(); k++) {

            String[] myRecord = myRecords.get(k);
            // 0:id 1:Time 2:Position 3:latitude 4:longitude 5:address

            tempPoint[0] = Double.valueOf(myRecord[3]);
            tempPoint[1] = Double.valueOf(myRecord[4]);

            tempPoint = convertCoordinates(tempPoint).clone();

            if (myRecord[2].equalsIgnoreCase("east")) {
                eastAnalysis[0] += tempPoint[0];
                eastAnalysis[1] += tempPoint[1];
                east[eastCursor][0] = tempPoint[0];
                east[eastCursor][1] = tempPoint[1];
                eastCursor++;
            } else if (myRecord[2].equalsIgnoreCase("south")) {
                southAnalysis[0] += tempPoint[0];
                southAnalysis[1] += tempPoint[1];
                south[southCursor][0] = tempPoint[0];
                south[southCursor][1] = tempPoint[1];
                southCursor++;
            } else if (myRecord[2].equalsIgnoreCase("west")) {
                westAnalysis[0] += tempPoint[0];
                westAnalysis[1] += tempPoint[1];
                west[westCursor][0] = tempPoint[0];
                west[westCursor][1] = tempPoint[1];
                westCursor++;
            } else { // north
                northAnalysis[0] += tempPoint[0];
                northAnalysis[1] += tempPoint[1];
                north[northCursor][0] = tempPoint[0];
                north[northCursor][1] = tempPoint[1];
                northCursor++;
            }

        }

        if (eastCursor > 0) {
            eastAnalysis[0] /= eastCursor;
            eastAnalysis[1] /= eastCursor;
        }

        if (southCursor > 0) {
            southAnalysis[0] /= southCursor;
            southAnalysis[1] /= southCursor;
        }

        if (westCursor > 0) {
            westAnalysis[0] /= westCursor;
            westAnalysis[1] /= westCursor;
        }

        if (northCursor > 0) {
            northAnalysis[0] /= northCursor;
            northAnalysis[1] /= northCursor;
        }

        // Calculate Mean Again to get more precise values

        int eastXMeanValid = 0, eastYMeanValid = 0,
                southXMeanValid = 0, southYMeanValid = 0,
                westXMeanValid = 0, westYMeanValid = 0,
                northXMeanValid = 0, northYMeanValid = 0;

        double eastXMean = 0, eastYMean = 0,
                southXMean = 0, southYMean = 0,
                westXMean = 0, westYMean = 0,
                northXMean = 0, northYMean = 0;

        for (int k = 0; k < eastCursor; k++) {
            if (Math.abs(east[k][0] - eastAnalysis[0]) < meanFlyawayThreshold) {
                eastXMean += east[k][0];
                eastXMeanValid++;
            }
            if (Math.abs(east[k][1] - eastAnalysis[1]) < meanFlyawayThreshold) {
                eastYMean += east[k][1];
                eastYMeanValid++;
            }
        }

        for (int k = 0; k < southCursor; k++) {
            if (Math.abs(south[k][0] - southAnalysis[0]) < meanFlyawayThreshold) {
                southXMean += south[k][0];
                southXMeanValid++;
            }
            if (Math.abs(south[k][1] - southAnalysis[1]) < meanFlyawayThreshold) {
                southYMean += south[k][1];
                southYMeanValid++;
            }
        }

        for (int k = 0; k < westCursor; k++) {
            if (Math.abs(west[k][0] - westAnalysis[0]) < meanFlyawayThreshold) {
                westXMean += west[k][0];
                westXMeanValid++;
            }
            if (Math.abs(west[k][1] - westAnalysis[1]) < meanFlyawayThreshold) {
                westYMean += west[k][1];
                westYMeanValid++;
            }
        }

        for (int k = 0; k < northCursor; k++) {
            if (Math.abs(north[k][0] - northAnalysis[0]) < meanFlyawayThreshold) {
                northXMean += north[k][0];
                northXMeanValid++;
            }
            if (Math.abs(north[k][1] - northAnalysis[1]) < meanFlyawayThreshold) {
                northYMean += north[k][1];
                northYMeanValid++;
            }
        }

        eastXMean /= eastXMeanValid;
        eastYMean /= eastYMeanValid;
        southXMean /= southXMeanValid;
        southYMean /= southYMeanValid;
        westXMean /= westXMeanValid;
        westYMean /= westYMeanValid;
        northXMean /= northXMeanValid;
        northYMean /= northYMeanValid;

        // update
        eastAnalysis[0] = eastXMean;
        eastAnalysis[1] = eastYMean;
        southAnalysis[0] = southXMean;
        southAnalysis[1] = southYMean;
        westAnalysis[0] = westXMean;
        westAnalysis[1] = westYMean;
        northAnalysis[0] = northXMean;
        northAnalysis[1] = northYMean;



//        Log.i("GPSerror", "east: " + String.valueOf(east[0][0] - eastAnalysis[0]) +
//                "\nsouth: " + String.valueOf(south[0][0] - southAnalysis[0]) +
//                "\nwest: " + String.valueOf(west[0][0] - westAnalysis[0]) +
//                "\nnorth: " + String.valueOf(north[0][0] - northAnalysis[0]));


        int eastXVarValid = 0, eastYVarValid = 0,
                southXVarValid = 0, southYVarValid = 0,
                westXVarValid = 0, westYVarValid = 0,
                northXVarValid = 0, northYVarValid = 0;


        for (int k = 0; k < eastCursor; k++) {
            if (Math.abs(east[k][0] - eastAnalysis[0]) < varFlyawayThreshold) {
                eastAnalysis[2] += (east[k][0] - eastAnalysis[0]) * (east[k][0] - eastAnalysis[0]);
                eastXVarValid++;
            }
            if (Math.abs(east[k][1] - eastAnalysis[1]) < varFlyawayThreshold) {
                eastAnalysis[3] += (east[k][1] - eastAnalysis[1]) * (east[k][1] - eastAnalysis[1]);
                eastYVarValid++;
            }
//            Log.i("GPSError", "eastY: " + String.valueOf(east[k][1] - eastAnalysis[1]) +
//                    "\nsouthY: " + String.valueOf(south[k][1] - southAnalysis[1]) +
//                    "\nwestY: " + String.valueOf(west[k][1] - westAnalysis[1]) +
//                    "\nnorthY: " + String.valueOf(north[k][1] - northAnalysis[1]));
        }

        for (int k = 0; k < southCursor; k++) {
            if (Math.abs(south[k][0] - southAnalysis[0]) < varFlyawayThreshold) {
                southAnalysis[2] += (south[k][0] - southAnalysis[0]) * (south[k][0] - southAnalysis[0]);
                southXVarValid++;
            }
            if (Math.abs(south[k][1] - southAnalysis[1]) < varFlyawayThreshold) {
                southAnalysis[3] += (south[k][1] - southAnalysis[1]) * (south[k][1] - southAnalysis[1]);
                southYVarValid++;
            }
        }

        for (int k = 0; k < westCursor; k++) {
            if (Math.abs(west[k][0] - westAnalysis[0]) < varFlyawayThreshold) {
                westAnalysis[2] += (west[k][0] - westAnalysis[0]) * (west[k][0] - westAnalysis[0]);
                westXVarValid++;
            }
            if (Math.abs(west[k][1] - westAnalysis[1]) < varFlyawayThreshold) {
                westAnalysis[3] += (west[k][1] - westAnalysis[1]) * (west[k][1] - westAnalysis[1]);
                westYVarValid++;
            }
        }

        for (int k = 0; k < northCursor; k++) {
            if (Math.abs(north[k][0] - northAnalysis[0]) < varFlyawayThreshold) {
                northAnalysis[2] += (north[k][0] - northAnalysis[0]) * (north[k][0] - northAnalysis[0]);
                northXVarValid++;
            }
            if (Math.abs(north[k][1] - northAnalysis[1]) < varFlyawayThreshold) {
                northAnalysis[3] += (north[k][1] - northAnalysis[1]) * (north[k][1] - northAnalysis[1]);
                northYVarValid++;
            }
        }

        noiseX2 = (eastAnalysis[2] + southAnalysis[2] + westAnalysis[2] + northAnalysis[2]) /
                (eastXVarValid + southXVarValid + westXVarValid + northXVarValid);

        noiseY2 = (eastAnalysis[3] + southAnalysis[3] + westAnalysis[3] + northAnalysis[3]) /
                (eastYVarValid + southYVarValid + westYVarValid + northYVarValid);

        int eastCovValid = 0, southCovValid = 0, westCovValid = 0, northCovValid = 0;
        for (int k = 0; k < eastCursor; k++)
            if (Math.abs(east[k][0] - eastAnalysis[0]) < varFlyawayThreshold &&
                    Math.abs(east[k][1] - eastAnalysis[1]) < varFlyawayThreshold) {
                noiseXY += (east[k][0] - eastAnalysis[0]) * (east[k][1] - eastAnalysis[1]);
                eastCovValid++;
            }

        for (int k = 0; k < southCursor; k++)
            if (Math.abs(south[k][0] - southAnalysis[0]) < varFlyawayThreshold &&
                    Math.abs(south[k][1] - southAnalysis[1]) < varFlyawayThreshold) {
                noiseXY += (south[k][0] - southAnalysis[0]) * (south[k][1] - southAnalysis[1]);
                southCovValid++;
            }

        for (int k = 0; k < westCursor; k++)
            if (Math.abs(west[k][0] - westAnalysis[0]) < varFlyawayThreshold &&
                    Math.abs(west[k][1] - westAnalysis[1]) < varFlyawayThreshold) {
                noiseXY += (west[k][0] - westAnalysis[0]) * (west[k][1] - westAnalysis[1]);
                westCovValid++;
            }

        for (int k = 0; k < northCursor; k++)
            if (Math.abs(north[k][0] - northAnalysis[0]) < varFlyawayThreshold &&
                    Math.abs(north[k][1] - northAnalysis[1]) < varFlyawayThreshold) {
                noiseXY += (north[k][0] - northAnalysis[0]) * (north[k][1] - northAnalysis[1]);
                northCovValid++;
            }


        noiseXY /= (eastCovValid + southCovValid + westCovValid + northCovValid);

    }

    public void getAccError() {

        double[][] xPlusError = new double[999][2];
        double[][] xMinusError = new double[999][2];
        double[][] yPlusError = new double[999][2];
        double[][] yMinusError = new double[999][2];
        // 0:main direction 1:the other direction

        double xPlusErrorMean = 0;
        double xMinusErrorMean = 0;
        double yPlusErrorMean = 0;
        double yMinusErrorMean = 0;
        double xPlusErrorVar = 0;
        double xMinusErrorVar = 0;
        double yPlusErrorVar = 0;
        double yMinusErrorVar = 0;

        double xPlusOtherErrorMean = 0;
        double xMinusOtherErrorMean = 0;
        double yPlusOtherErrorMean = 0;
        double yMinusOtherErrorMean = 0;

        double xPlusErrorCov = 0;
        double xMinusErrorCov = 0;
        double yPlusErrorCov = 0;
        double yMinusErrorCov = 0;

        int xPlusCursor = 0;
        int xMinusCursor = 0;
        int yPlusCursor = 0;
        int yMinusCursor = 0;

        double xErrorMean = 0;
        double yErrorMean = 0;
        double xErrorVar = 0;
        double yErrorVar = 0;


        ArrayList<String[]> myRecords = getAllAccRecords();

        for (int i = 0; i < myRecords.size(); i++) {
            String[] myRecord = myRecords.get(i);

            String orientation = myRecord[4];
            double error = Double.valueOf(myRecord[5]);
            double otherError = Double.valueOf(myRecord[6]);

            if (orientation.equalsIgnoreCase("xplus")) {
                xPlusError[xPlusCursor++][0] = error;
                xPlusError[xPlusCursor++][1] = otherError;
                xPlusErrorMean += error;
            } else if (orientation.equalsIgnoreCase("xminus")) {
                xMinusError[xMinusCursor++][0] = error;
                xMinusError[xMinusCursor++][1] = otherError;
                xMinusErrorMean += error;
            } else if (orientation.equalsIgnoreCase("yplus")) {
                yPlusError[yPlusCursor++][0] = error;
                yPlusError[yPlusCursor++][1] = otherError;
                yPlusErrorMean += error;
            } else { // yminus
                yMinusError[yMinusCursor++][0] = error;
                yMinusError[yMinusCursor++][1] = otherError;
                yMinusErrorMean += error;
            }

        }

        // get mean error
        if (xPlusCursor > 0) {
            xPlusErrorMean /= xPlusCursor;
            xPlusOtherErrorMean /= xPlusCursor;
        }
        if (xMinusCursor > 0) {
            xMinusErrorMean /= xMinusCursor;
            xMinusOtherErrorMean /= xMinusCursor;
        }
        if (yPlusCursor > 0) {
            yPlusErrorMean /= yPlusCursor;
            yPlusOtherErrorMean /= yPlusCursor;
        }
        if (yMinusCursor > 0) {
            yMinusErrorMean /= yMinusCursor;
            yMinusOtherErrorMean /= yMinusCursor;
        }

        // get variance
        for (int i = 0; i < xPlusCursor; i++) {
            xPlusErrorVar += (xPlusError[i][0] - xPlusErrorMean)
                    * (xPlusError[i][0] - xPlusErrorMean);
            xPlusErrorCov += (xPlusError[i][0] - xPlusErrorMean)
                    * (xPlusError[i][1] - xPlusOtherErrorMean);
        }
        for (int i = 0; i < xMinusCursor; i++) {
            xMinusErrorVar += (xMinusError[i][0] - xMinusErrorMean)
                    * (xMinusError[i][0] - xMinusErrorMean);
            xMinusErrorCov += (-xMinusError[i][0] + xMinusErrorMean)
                    * (xMinusError[i][1] - xMinusOtherErrorMean);

        }
        for (int i = 0; i < yPlusCursor; i++) {
            yPlusErrorVar += (yPlusError[i][0] - yPlusErrorMean)
                    * (yPlusError[i][0] - yPlusErrorMean);
            yPlusErrorCov += (yPlusError[i][0] - yPlusErrorMean)
                    * (yPlusError[i][1] - yPlusOtherErrorMean);
        }
        for (int i = 0; i < yMinusCursor; i++) {
            yMinusErrorVar += (yMinusError[i][0] - yMinusErrorMean)
                    * (yMinusError[i][0] - yMinusErrorMean);
            yMinusErrorCov += (-yMinusError[i][0] + yMinusErrorMean)
                    * (yMinusError[i][1] - yMinusOtherErrorMean);
        }

        // get 2 directions' analysis data
        // variance
        if (xPlusCursor + xMinusCursor > 0) {
            xErrorVar = (xPlusErrorVar * xPlusCursor + xMinusErrorVar * xMinusCursor)
                    / (xPlusCursor + xMinusCursor);
        }
        if (yPlusCursor + yMinusCursor > 0) {
            yErrorVar = (yPlusErrorVar * yPlusCursor + yMinusErrorVar * yMinusCursor)
                    / (yPlusCursor + yMinusCursor);
        }

        double xyErrorCov = (xMinusErrorCov * xMinusCursor + xPlusErrorCov * xPlusCursor +
                yMinusErrorCov * yMinusCursor + yPlusErrorCov * yPlusCursor) /
                (xMinusCursor + xPlusCursor + yMinusCursor + yPlusCursor);

        noiseVX2 = xErrorVar * xErrorVar;
        noiseVY2 = yErrorVar * yErrorVar;
        noiseVXVY = xyErrorCov * xyErrorCov;

    }

    public void showNoiseMatrix() {

        TextView mView = (TextView) findViewById(R.id.R);

        double scaleX2 = significand(noiseX2, scale);
        double scaleY2 = significand(noiseY2, scale);
        double scaleXY = significand(noiseXY, scale);
        double scaleVX2 = significand(noiseVX2, scale);
        double scaleVY2 = significand(noiseVY2, scale);
        double scaleVXVY = significand(noiseVXVY, scale);
        double scaleZero = significand(zero, scale);

        String m =
                String.valueOf(scaleX2) + ",  " + String.valueOf(scaleXY) + ",  " +
                        String.valueOf(scaleZero) + ",  " + String.valueOf(scaleZero) + "\n" +
                        String.valueOf(scaleXY) + ",  " + String.valueOf(scaleY2) + ",  " +
                        String.valueOf(scaleZero) + ",  " + String.valueOf(scaleZero) + "\n" +
                        String.valueOf(scaleZero) + ",  " + String.valueOf(scaleZero) + ",  " +
                        String.valueOf(scaleVX2) + ",  " + String.valueOf(scaleVXVY) + ",  " + "\n" +
                        String.valueOf(scaleZero) + ",  " + String.valueOf(scaleZero) + ",  " +
                        String.valueOf(scaleVXVY) + ",  " + String.valueOf(scaleVY2) + ",  " + "\n";

        mView.setText(m);
    }


    public static double significand(double oldDouble, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "scale指定的精度为非负值");
        }
        /**
         * RoundingMode：舍入模式
         * UP：远离零方向舍入的舍入模式；
         * DOWN：向零方向舍入的舍入模式；
         * CEILING： 向正无限大方向舍入的舍入模式；
         * FLOOR：向负无限大方向舍入的舍入模式；
         * HALF_DOWN：向最接近数字方向舍入的舍入模式，如果与两个相邻数字的距离相等，则向下舍入；
         * HALF_UP：向最接近数字方向舍入的舍入模式，如果与两个相邻数字的距离相等，则向上舍入；
         * HALF_EVEN：向最接近数字方向舍入的舍入模式，如果与两个相邻数字的距离相等，则向相邻的偶数舍入;(在重复进行一系列计算时,此舍入模式可以将累加错误减到最小)
         * UNNECESSARY：用于断言请求的操作具有精确结果的舍入模式，因此不需要舍入。
         */
        RoundingMode rMode = null;
        //rMode=RoundingMode.FLOOR;
        //下面这种情况，其实和FLOOR一样的。
        if (oldDouble > 0) {
            rMode = RoundingMode.DOWN;
        } else {
            rMode = RoundingMode.UP;
        }
        //此处的scale表示的是，几位有效位数
        BigDecimal b = new BigDecimal(Double.toString(oldDouble), new MathContext(scale, rMode));
        return b.doubleValue();
    }

    public double[] convertCoordinates(double[] geoCoordinates) {

        double kilometerPerLat = 111;
        double kilometerPerLng = 111 * Math.cos(centerLat * Math.PI / 180);

        double[] rectCoordinates = new double[2];
        double latDiff, lngDiff;

        latDiff = (centerLat - geoCoordinates[0]) * kilometerPerLat;
        lngDiff = (geoCoordinates[1] - centerLng) * kilometerPerLng;

        rectCoordinates[0] = latDiff * 1000;
        rectCoordinates[1] = lngDiff * 1000;

        return rectCoordinates;
    }

    public ArrayList<String[]> getAllGpsRecords() {

        gpsDB.open();

        Cursor c = gpsDB.getAllRecords();

        ArrayList<String[]> records = new ArrayList<String[]>();

        if (c.moveToFirst()) {
            do {
                String[] record = {c.getString(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5), "\n"};
                // 0:id 1:Time 2:Position 3:latitude 4:longitude 5:address
                records.add(record);
            } while (c.moveToNext());
        }
        gpsDB.close();
        return records;
    }

    public ArrayList<String[]> getAllAccRecords() {

        accDB.open();

        Cursor c = accDB.getAllRecords();

        ArrayList<String[]> records = new ArrayList<String[]>();

        if (c.moveToFirst()) {
            do {
                String[] record = {c.getString(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5), c.getString(6), "\n"};
                // 0:id 1:TimeStamp 2:Time 3:Address 4: Orientation 5:Error 6:otherError
                records.add(record);
            } while (c.moveToNext());
        }
        accDB.close();
        return records;
    }
}
