package com.example.renhanfei.phonelocusdetermination;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by renhanfei on 17/4/12.
 */

public class LocusPlotView extends View {

    LocusDB db;

    private long recordID;
    private int pixelPerMeter = 3;

    // points
    final int pointNum = 4096;
    public int[][] rawData = new int[pointNum][2];
    public int[][] filteredData = new int[pointNum][2];

    // drawing parameters
    private final int xStart = 50;
    private final int xStop = 650;
    private final int yStart = 50;
    private final int yStop = 650;
    private final int gridSize = 25;
    private final int filteredRadius = 3;
    private final int rawRadius = 6;
    public final int[] originPoint = {(xStart + xStop) / 2, (yStart + yStop) / 2};

//    TextView debugView;

    public LocusPlotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        db = new LocusDB(context);
//        recordID = ((PlotActivity)getRootView().getContext()).recordID;
        recordID = ((PlotActivity) context).recordID;
//        Log.i("viewRecordID", String.valueOf(recordID));

        ArrayList<String[]> myItems = getRecordItems();
        setCoordinates(myItems);

//        for (int i = 0; i < 50; i++) {
//            Log.i("baseData", "raw0X: " + myItems.get(i)[8]
//                    + " raw0Y: " + myItems.get(i)[9]
//                    + "\nfiltered0X: " + myItems.get(i)[10]
//                    + " filtered0Y: " + myItems.get(i)[11]);
//        }

        for (int i = 0; i < 30; i++) {
            Log.i("viewData", "raw0X: " + String.valueOf(rawData[i][0])
                    + " raw0Y: " + String.valueOf(rawData[i][1])
                    + "\nfiltered0X: " + String.valueOf(filteredData[i][0]
                    + " filtered0Y: " + String.valueOf(filteredData[i][1])));
        }


        // TODO Auto-generated constructor stub

    }

    public ArrayList<String[]> getRecordItems() {

        db.open();

        Cursor c = db.getItems(recordID);

        ArrayList<String[]> items = new ArrayList<String[]>();

        if (c.moveToFirst()) {
            do {
                String[] item = {c.getString(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5), c.getString(6),
                        c.getString(7), c.getString(8), c.getString(9), c.getString(10),
                        c.getString(11), c.getString(12), c.getString(13), "\n"};
                // 0:id 1:TimeStamp 2:Time 3:Lat 4:Lng 5:X 6:Y 7:record id
                // 8:positionX 9:positionY 10:filteredX 11:filteredY 12:vx 13:vy
                items.add(item);
            } while (c.moveToNext());
        }
        db.close();
        return items;
    }

    public void setCoordinates(ArrayList<String[]> myItems) {

        for (int i = 0; i < myItems.size(); i++) {

            // 0:id 1:TimeStamp 2:Time 3:Lat 4:Lng 5:X 6:Y 7:record id
            // 8:positionX 9:positionY 10:filteredX 11:filteredY 12:vx 13:vy

            String[] myItem = myItems.get(i);
            rawData[i][0] = (int) (Double.valueOf(myItem[8]) * pixelPerMeter + originPoint[0]);
            rawData[i][1] = (int) (Double.valueOf(myItem[9]) * pixelPerMeter + originPoint[1]);
            filteredData[i][0] = (int) (Double.valueOf(myItem[10]) * pixelPerMeter + originPoint[0]);
            filteredData[i][1] = (int) (Double.valueOf(myItem[11]) * pixelPerMeter + originPoint[1]);

        }

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

//        // draw axis
//        Paint xyPaint = new Paint();
//        xyPaint.setStyle(Paint.Style.STROKE);
//        xyPaint.setColor(getResources().getColor(R.color.colorAxis));
//        xyPaint.setStrokeWidth(3); //bolder
//        canvas.drawLine(xStart, yStop / 2 + yStart / 2, xStop, yStop / 2 + yStart / 2, xyPaint);//X axis
//        canvas.drawLine(xStop / 2 + xStart / 2, yStart, xStop / 2 + xStart / 2, yStop, xyPaint);//Y axis

        // draw points
        // draw outlines
        int[] tempPoint;
        Paint outlinePaint = new Paint();
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(getResources().getColor(R.color.colorOutline));
        outlinePaint.setAntiAlias(true);

        //draw raw
        Paint rawPaint = new Paint();
        rawPaint.setStyle(Paint.Style.FILL);
        rawPaint.setColor(getResources().getColor(R.color.colorRaw));
        rawPaint.setAntiAlias(true);
        for (int i = 0; i < rawData.length; i++) {
            tempPoint = rawData[i];
            canvas.drawCircle(tempPoint[0], tempPoint[1], rawRadius, rawPaint);
//            canvas.drawCircle(tempPoint[0], tempPoint[1], pointRadius, outlinePaint);
        }

        // draw filtered
        Paint filteredPaint = new Paint();
        filteredPaint.setStyle(Paint.Style.FILL);
        filteredPaint.setColor(getResources().getColor(R.color.colorFiltered));
        filteredPaint.setAntiAlias(true);
        for (int i = 0; i < filteredData.length; i++) {
            tempPoint = filteredData[i];
            canvas.drawCircle(tempPoint[0], tempPoint[1], filteredRadius, filteredPaint);
//            canvas.drawCircle(tempPoint[0], tempPoint[1], pointRadius, outlinePaint);
        }


    }
}
