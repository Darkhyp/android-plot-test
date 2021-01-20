package com.example.plottests.ui.fuctionplot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.plottests.R;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.plottests.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.Function;


public class FunctionPlotFragment extends Fragment {
    final int N_FUNCTION_POINTS = 100;
    private View root;
    private int count=100;
    protected LineChart lineChart;
    private EditText functionExpression;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_functionplot, container, false);

        Toast.makeText(getActivity(), "MP chart plot test", Toast.LENGTH_SHORT).show();

        // TextView for reading mathematical expression
        functionExpression = root.findViewById(R.id.functionInputText);

        // MPAndroidChart example
        initChart(root);

        Button btnChart = root.findViewById(R.id.btnPlotMPchart);
        btnChart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Plot function", Toast.LENGTH_SHORT).show();
                // get function expression
                String strExpression = functionExpression.getText().toString();

                Function f = new Function("f", strExpression,"x");
                Expression mathExpression0 = new Expression("f(0)", f),
                        mathExpression1 = new Expression("f(1)", f);
                if (mathExpression0.checkSyntax() || mathExpression1.checkSyntax())
                    plotMPChart(f);
                else
                    Toast.makeText(getActivity(), "Wrong expression as function of x. Check it!", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    public void plotMPChart(Function f){
        ArrayList<String> xAxisLabel = new ArrayList<>();
        ArrayList<Entry> yAxis = new ArrayList<>();
        float x;
        for(int i=0; i<N_FUNCTION_POINTS; i++){
            // function argument
            x = (((float)i-(float)N_FUNCTION_POINTS/2)/20);

            xAxisLabel.add(i, String.valueOf(x));
            // function calculation for current x
            yAxis.add(new Entry(i, (float) f.calculate(x)));
        }

        LineDataSet lineDataSet1 = new LineDataSet(yAxis,f.getFunctionExpressionString());
        lineDataSet1.setFillAlpha(110);
        lineDataSet1.setDrawCircles(true);
        lineDataSet1.setLineWidth(3f);
        lineDataSet1.setColor(Color.GREEN);

        ArrayList<ILineDataSet> data = new ArrayList<>();
        data.add(lineDataSet1);

    /*
    // add limit line
    LimitLine upper_limit = new LimitLine(0.75f,"danger");
    upper_limit.setLineColor(Color.RED);
    upper_limit.setLineWidth(4f);
    upper_limit.enableDashedLine(30f,30f,0f);
    upper_limit.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
    upper_limit.setTextSize(16f);
    //add limit to y-axis
    YAxis left_yaxis = lineChart.getAxisLeft();
    left_yaxis.removeAllLimitLines();
    left_yaxis.addLimitLine(upper_limit);
    left_yaxis.setDrawLimitLinesBehindData(true);

     */

        //format x-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        // add x-axis labels
        String[] xLabel = new String[xAxisLabel.size()];
        for(int i=0; i<xAxisLabel.size(); i++){
            xLabel[i] = xAxisLabel.get(i);
        }
        xAxis.setValueFormatter(new ValueFormatter(){
            @Override
            public String getFormattedValue(float value) {
                return xLabel[(int)value];
            }
        });
//        xAxis.setTextSize(16f);

        // hide right axis
        lineChart.getAxisRight().setEnabled(false);

        // add data to chart
        lineChart.setData(new LineData(data));
//        lineChart.setVisibleXRangeMaximum(65f);

        // update chart
//        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private void initChart(@NotNull View root){
        lineChart = root.findViewById(R.id.MP_chart_view);
//        lineChart.setOnChartGestureListener(this);
//        lineChart.setOnChartValueSelectedListener(this);

        // description text
        lineChart.getDescription().setEnabled(true);
        lineChart.getDescription().setText("MP chart test");
        // enable touch gestures
//        lineChart.setTouchEnabled(true);
//        lineChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);

//        lineChart.setDrawGridBackground(true);
//        lineChart.setHighlightPerDragEnabled(true);

        // set an alternative background color
//        lineChart.setBackgroundColor(Color.WHITE);
//        lineChart.setViewPortOffsets(0f, 0f, 0f, 0f);

    }

}