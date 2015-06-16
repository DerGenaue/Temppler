package com.temppler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

/**
 * Created by Daniel on 11.06.2015.
 */
public class GraphView extends View {

    private double[] vals;
    private double scale = -1;

    public GraphView(Context c, AttributeSet s){
        super(c, s);
    }

    public synchronized void setVals(double[] v){
        vals = v;
    }


    public synchronized void draw(Canvas c){
        if(vals == null || c == null)
            return;
        double max = scale;
        if(max <= 0)
            for (int i = 0; i < vals.length; i++)
                if (vals[i] > max)
                    max = vals[i];

        float w = this.getWidth(), h = this.getHeight()-1,
                x = w/vals.length,
                y = (float) (h/max);
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setStrokeWidth(1);

        for (int i = 1; i < vals.length; i++) {
            c.drawLine((i-1)*x, h-(float)(vals[i-1]*y), i*x, h-(float)(vals[i]*y), p);
        }

        c.drawLine(0, 0, 0, h, p);
        c.drawLine(0, h, w, h, p);
    }



    public void setScale(double s){
        scale = s;
    }
}
