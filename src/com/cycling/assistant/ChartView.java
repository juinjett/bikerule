package com.cycling.assistant;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class ChartView extends View{
	
	private int height = 1024;
	private int width = 768;
	
    private int XPoint=100;   //原点的X坐标
    private int YPoint=520;   //原点的Y坐标
    private int XScale=110;   //X的刻度长度
    private int YScale=80;    //Y的刻度长度
    private int XLength=760;  //X轴的长度
    private int YLength=440;  //Y轴的长度
    private String[] XLabel;  //X的刻度
    private String[] YLabel;  //Y的刻度
    private String[] Data;    //数据
    private String Title;     //显示的标题
    public ChartView(Context context)
    {
        super(context);
    }
    public void SetInfo(int height, int width, String[] XLabels, String[] YLabels, String[] AllData, String strTitle)
    {
    	this.height = height;
    	this.width = width;
        this.XLabel = XLabels;
        this.YLabel = YLabels;
        this.XPoint = width/8;
        this.YPoint = height*2/5;
        this.XScale = width/8;
        this.YScale = height/16;
        this.XLength = (this.XScale)*(XLabel.length - 1);
        this.YLength = (this.YScale)*(YLabel.length - 1);
        this.Data = AllData;
        this.Title = strTitle;
    }
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        Paint paint= new Paint();
        //paint.setStyle(Paint.Style.STROKE);
        //paint.setAntiAlias(true);
        paint.setColor(Color.rgb(52, 171, 219));
        paint.setStrokeWidth(3);
        int calibrationLength = width/30;
        int characterLength = width/40;
        int characterLengthL = width/20;
        int characterLengthTitle = width/15;
        paint.setTextSize(characterLength);
        
        // Y-Axis
        canvas.drawLine(XPoint, YPoint-YLength, XPoint, YPoint, paint);   // Y-Axis
        for(int i=0;i*YScale<YLength ;i++)               
        {
            canvas.drawLine(XPoint,YPoint-i*YScale, XPoint+calibrationLength, YPoint-i*YScale, paint);  // Calibration
            try
            {
                canvas.drawText(YLabel[i] , XPoint-characterLength*2, YPoint-i*YScale+characterLength/2, paint);
            }
            catch(Exception e)
            {
            }
        }
        canvas.drawText("Miles", XPoint-characterLength*2, YPoint-YLength-calibrationLength, paint);
        //canvas.drawLine(XPoint,YPoint-YLength,XPoint-6,YPoint-YLength+6,paint);  // Arrow
        //canvas.drawLine(XPoint,YPoint-YLength,XPoint+6,YPoint-YLength+6,paint);           
        // X-Axis
        canvas.drawLine(XPoint,YPoint,XPoint+XLength,YPoint,paint);   //轴线
        for(int i=0;i*XScale<XLength;i++)   
        {
            canvas.drawLine(XPoint+i*XScale, YPoint, XPoint+i*XScale, YPoint-calibrationLength, paint);  // Calibration
            try
            {
                canvas.drawText(XLabel[i] , XPoint+i*XScale-characterLength/2, YPoint+characterLength, paint);
                //data
                    if(i>0&&YCoord(Data[i-1])!=-9999&&YCoord(Data[i])!=-9999)  // Valid data
                        canvas.drawLine(XPoint+(i-1)*XScale, YCoord(Data[i-1]), XPoint+i*XScale, YCoord(Data[i]), paint);
                    canvas.drawCircle(XPoint+i*XScale,YCoord(Data[i]), 2, paint);
           }
            catch(Exception e)
            {
            }
        }
        canvas.drawText("Time", XPoint+XLength+calibrationLength, YPoint, paint);
        //canvas.drawLine(XPoint+XLength,YPoint,XPoint+XLength-12,YPoint-6,paint);    // Arrow
        //canvas.drawLine(XPoint+XLength,YPoint,XPoint+XLength-12,YPoint+6,paint); 
        paint.setColor(Color.rgb(247, 148, 29));
        paint.setTextSize(characterLengthTitle);
        canvas.drawText(Title, this.width/2 - characterLengthTitle*5/2, calibrationLength*3, paint);
        
        // Total Milage
        paint.setColor(Color.rgb(0x8e, 0x8f, 0x93));
        paint.setTextSize(characterLengthL);
        canvas.drawText("Total Milage:", this.XPoint, this.YPoint + this.YScale*3/2, paint);
        paint.setColor(Color.rgb(247, 148, 29));
        canvas.drawText("0", this.width*4/7, this.YPoint + this.YScale*3/2, paint);	
        paint.setColor(Color.rgb(0x8e, 0x8f, 0x93));
        canvas.drawText("miles", this.width*4/7 + characterLengthL*2, this.YPoint + this.YScale*3/2, paint);	
        
        // Total Calories
        canvas.drawText("Total Calories:", this.XPoint, this.YPoint + this.YScale*5/2, paint);
        paint.setColor(Color.rgb(247, 148, 29));
        canvas.drawText("0", this.width*4/7, this.YPoint + this.YScale*5/2, paint);	
        paint.setColor(Color.rgb(0x8e, 0x8f, 0x93));
        canvas.drawText("cal", this.width*4/7 + characterLengthL*2, this.YPoint + this.YScale*5/2, paint);	
        
        // Average Calories
        canvas.drawText("Average Calories:", this.XPoint, this.YPoint + this.YScale*7/2, paint);
        paint.setColor(Color.rgb(247, 148, 29));
        canvas.drawText("0", this.width*4/7, this.YPoint + this.YScale*7/2, paint);	
        paint.setColor(Color.rgb(0x8e, 0x8f, 0x93));
        canvas.drawText("cal/hour", this.width*4/7 + characterLengthL*2, this.YPoint + this.YScale*7/2, paint);	
        
        // Average Speed
        canvas.drawText("Average Speed:", this.XPoint, this.YPoint + this.YScale*9/2, paint);
        paint.setColor(Color.rgb(247, 148, 29));
        canvas.drawText("0", this.width*4/7, this.YPoint + this.YScale*9/2, paint);	
        paint.setColor(Color.rgb(0x8e, 0x8f, 0x93));
        canvas.drawText("mph", this.width*4/7 + characterLengthL*2, this.YPoint + this.YScale*9/2, paint);	
        
    }
    
    private int YCoord(String y0)
    {
        int y;
        try
        {
            y=Integer.parseInt(y0);
        }
        catch(Exception e)
        {
            return -9999;    //error 
        }
        try
        {
            return YPoint-y*YScale/Integer.parseInt(YLabel[1]);
        }
        catch(Exception e)
        {
        }
        return y;
    }
}