package com.example.renhanfei.mylocationmap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by renhanfei on 17/3/26.
 */

public class LocationPlotView extends View {

    // database and convert paras
    LocationRecordsDB db;
    private double[] originCoordinates;
    private double kilometerPerLat = 111;
    private double kilometerPerLng;

    // points
    final int pointNum = 25;
    public int[][] east = new int[pointNum][2];
    public int[][] south = new int[pointNum][2];
    public int[][] west = new int[pointNum][2];
    public int[][] north = new int[pointNum][2];

    // drawing parameters
    private final int xStart = 50;
    private final int xStop = 650;
    private final int yStart = 50;
    private final int yStop = 450;
    private final int gridSize = 25;
    private final int pointRadius = 5;
    public final int[] originPoint = {(xStart + xStop) / 2, (yStart + yStop) / 2};

    public LocationPlotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        db = new LocationRecordsDB(context);

        ArrayList<String[]> myRecords = getAllRecords();

        originCoordinates = getOriginCoordinates(myRecords);
//        1.2984759980000005  103.778429864

//        "True" Center Coordinates
//        originCoordinates[0] = 1.29866;
//        originCoordinates[1] = 103.7784;

        kilometerPerLng = 111 * Math.cos(originCoordinates[0] * Math.PI / 180);

        setGeoCoordinates(myRecords);
        // TODO Auto-generated constructor stub
    }

    public ArrayList<String[]> getAllRecords() {

        db.open();

        Cursor c = db.getAllRecords();

        ArrayList<String[]> records = new ArrayList<String[]>();

        if (c.moveToFirst()) {
            do {
                String[] record = {c.getString(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5), "\n"};
                // 0:id 1:Time 2:Position 3:latitude 4:longitude 5:address
                records.add(record);
            } while (c.moveToNext());
        }
        db.close();
        return records;
    }

    private double[] getOriginCoordinates(ArrayList<String[]> myRecords) {

        double lat = 0;
        double lng = 0;

        double[] center = new double[2];

        for (int i = 0; i < myRecords.size(); i++) {
            String[] myRecord = myRecords.get(i);
            lat += Double.valueOf(myRecord[3]);
            lng += Double.valueOf(myRecord[4]);
        }

        center[0] = lat / myRecords.size();
        center[1] = lng / myRecords.size();

        return center;

    }

    public void setGeoCoordinates(ArrayList<String[]> myRecords) {

        double[] geoCoordinates = new double[2];
        int[] tempPoint;

        int eastCursor = 0;
        int southCursor = 0;
        int westCursor = 0;
        int northCursor = 0;

        for (int i = 0; i < myRecords.size(); i++) {

            String[] myRecord = myRecords.get(i);
            geoCoordinates[0] = Double.valueOf(myRecord[3]);
            geoCoordinates[1] = Double.valueOf(myRecord[4]);
            tempPoint = convertCoordinates(geoCoordinates);

            if (myRecord[2].equalsIgnoreCase("east")) {
                east[eastCursor][0] = tempPoint[0];
                east[eastCursor][1] = tempPoint[1];
                eastCursor++;
            } else if (myRecord[2].equalsIgnoreCase("south")) {
                south[southCursor][0] = tempPoint[0];
                south[southCursor][1] = tempPoint[1];
                southCursor++;
            } else if (myRecord[2].equalsIgnoreCase("west")) {
                west[westCursor][0] = tempPoint[0];
                west[westCursor][1] = tempPoint[1];
                westCursor++;
            } else { // north
                north[northCursor][0] = tempPoint[0];
                north[northCursor][1] = tempPoint[1];
                northCursor++;
            }
        }
    }

    public int[] convertCoordinates(double[] geoCoordinates) {

        int[] rectCoordinates = new int[2]; // 0.5m/unit
        double latDiff, lngDiff;

        latDiff = (originCoordinates[0] - geoCoordinates[0]) * kilometerPerLat;
        lngDiff = (geoCoordinates[1] - originCoordinates[1]) * kilometerPerLng;

        rectCoordinates[0] = (int) (originPoint[0] + latDiff * 2000);
        rectCoordinates[1] = (int) (originPoint[1] + lngDiff * 2000);

        return rectCoordinates;
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);

        // background rectangle
        Paint screenPaint = new Paint();
        screenPaint.setStyle(Paint.Style.FILL);
        screenPaint.setColor(getResources().getColor(R.color.colorBackground));
        canvas.drawRect(xStart, yStart, xStop, yStop, screenPaint);


        // draw grids
        Paint netPaint = new Paint();
        netPaint.setStyle(Paint.Style.FILL);
        netPaint.setColor(getResources().getColor(R.color.colorGrid));
        // horizontal
        for (int i = 1; i < (yStop - yStart) / gridSize; i++) {
            canvas.drawLine(xStart, yStop - gridSize * i, xStop, yStop - gridSize * i, netPaint);
        }
        // vertical
        for (int i = 1; i < (xStop - xStart) / gridSize; i++) {
            canvas.drawLine(xStart + gridSize * i, yStop, xStart + gridSize * i, yStart, netPaint);
        }

        // draw axis
        Paint xyPaint = new Paint();
        xyPaint.setStyle(Paint.Style.STROKE);
        xyPaint.setColor(getResources().getColor(R.color.colorAxis));
        xyPaint.setStrokeWidth(3); //bolder
        canvas.drawLine(xStart, yStop / 2 + yStart / 2, xStop, yStop / 2 + yStart / 2, xyPaint);//X axis
        canvas.drawLine(xStop / 2 + xStart / 2, yStart, xStop / 2 + xStart / 2, yStop, xyPaint);//Y axis

        // draw points
        // draw outlines
        int[] tempPoint;
        Paint outlinePaint = new Paint();
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(getResources().getColor(R.color.colorOutline));
        outlinePaint.setAntiAlias(true);

        //draw east
        Paint eastPaint = new Paint();
        eastPaint.setStyle(Paint.Style.FILL);
        eastPaint.setColor(getResources().getColor(R.color.colorEast));
        eastPaint.setAntiAlias(true);
        for (int i = 0; i < east.length; i++) {
            tempPoint = east[i];
            canvas.drawCircle(tempPoint[0], tempPoint[1], pointRadius, eastPaint);
            canvas.drawCircle(tempPoint[0], tempPoint[1], pointRadius, outlinePaint);
        }

        // draw south
        Paint southPaint = new Paint();
        southPaint.setStyle(Paint.Style.FILL);
        southPaint.setColor(getResources().getColor(R.color.colorSouth));
        southPaint.setAntiAlias(true);
        for (int i = 0; i < south.length; i++) {
            tempPoint = south[i];
            canvas.drawCircle(tempPoint[0], tempPoint[1], pointRadius, southPaint);
            canvas.drawCircle(tempPoint[0], tempPoint[1], pointRadius, outlinePaint);
        }

        // draw west
        Paint westPaint = new Paint();
        westPaint.setStyle(Paint.Style.FILL);
        westPaint.setColor(getResources().getColor(R.color.colorWest));
        westPaint.setAntiAlias(true);
        for (int i = 0; i < west.length; i++) {
            tempPoint = west[i];
            canvas.drawCircle(tempPoint[0], tempPoint[1], pointRadius, westPaint);
            canvas.drawCircle(tempPoint[0], tempPoint[1], pointRadius, outlinePaint);
        }

        // draw north
        Paint northPaint = new Paint();
        northPaint.setStyle(Paint.Style.FILL);
        northPaint.setColor(getResources().getColor(R.color.colorNorth));
        northPaint.setAntiAlias(true);
        for (int i = 0; i < north.length; i++) {
            tempPoint = north[i];
            canvas.drawCircle(tempPoint[0], tempPoint[1], pointRadius, northPaint);
            canvas.drawCircle(tempPoint[0], tempPoint[1], pointRadius, outlinePaint);
        }


    }


}