package com.example.plottests.ui.sensors;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.plottests.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SensorsFragment extends Fragment implements SensorEventListener {
    private static final int MAX_POINTS_DISPLAYED = 50;
    private Button btnSensorMeasurement;
    private TextView textSensorValue;

    private LineChart lineChart;
    private LineData data;
    private ArrayList<Integer> colorList =  new ArrayList<Integer>() {{
        add(Color.BLUE);
        add(Color.GREEN);
        add(Color.RED);
    }};
    private ArrayList<String> labelList =  new ArrayList<String>() {{
        add("x-axis");
        add("y-axis");
        add("z-axis");
    }};
    private int numberOfMeasuredPoints = 0;

    private List<Sensor> sensorsList;
    private SensorManager mSensorManager;
    private Sensor mSensor = null;
    private Thread thread = null;
    // if sensor is readable
    boolean sensorRead = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sensors, container, false);

        Toast.makeText(getActivity(), "Sensor test", Toast.LENGTH_SHORT).show();


        // init sensors manager
        mSensorManager = (SensorManager) inflater.getContext().getSystemService(inflater.getContext().SENSOR_SERVICE);
        // get sensors list
        sensorsList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        // init spinner (dropdown menu) of the available sensor list
        initSensorList(root, inflater.getContext());

        // initialize graphic engine
        initChart(root);
        data = new LineData();
        data.setValueTextColor(Color.WHITE);
        lineChart.setData(data);

        // text field for displaying sensor data
        textSensorValue = root.findViewById(R.id.textSensorValue);

        // switch mode for continuous or by button measurements
        Switch switchMeasurementMode = root.findViewById(R.id.switchMeasurementMode);
        switchMeasurementMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSensor==null) return;
                if (isChecked){
                    // set button for single measurement inactive
                    btnSensorMeasurement.setEnabled(false);
                    // run thread for plotting
                    startPlot();
                }
                else{
                    // set button for single measurement inactive
                    btnSensorMeasurement.setEnabled(true);
                    if (thread!=null) {
                        // stop thread for plotting
                        thread.interrupt();
                    }
                }
            }
        });

        btnSensorMeasurement = root.findViewById(R.id.btnSensorMesurement);
        btnSensorMeasurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSensor!=null) {
                    Toast.makeText(root.getContext(), "Sensor data read", Toast.LENGTH_SHORT).show();
                    sensorRead = true;
                }
            }
        });


        return root;
    }

    private void initSensorList(View root, Context context) {
        //get the spinner from the xml.
        Spinner dropdown = root.findViewById(R.id.sensorlist);

        //create a list of items (sensor names) for the spinner.
        List<String> items = new ArrayList<>();
        for (Sensor sensor : sensorsList) {
            items.add(sensor.getName().toString());
        }
//        String[] items = new String[]{"1", "2", "three"};

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(root.getContext(), android.R.layout.simple_spinner_dropdown_item, items);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(root.getContext(), android.R.layout.simple_spinner_item, items);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context,parent.getItemAtPosition(position)+" is selected",Toast.LENGTH_SHORT).show();
                initSensor(context, sensorsList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initSensor(Context context, @NotNull Sensor sensor) {
        mSensor = mSensorManager.getDefaultSensor(sensor.getType());
        if(mSensor !=null){
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void startPlot() {
        if(thread!=null)
            thread.interrupt();
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()){
                    sensorRead = true;
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }
                sensorRead = false;
            }
        });

        thread.start();
    }

    @Override
    public void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDestroyView() {
        if(thread!=null)
            thread.interrupt();
        mSensorManager.unregisterListener(this);

        super.onDestroyView();
    }

    @Override
    public void onPause() {
        if(thread!=null)
            thread.interrupt();
        mSensorManager.unregisterListener(this);

        super.onPause();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(sensorRead){
            sensorRead = false;
            addEntry(event);
            textSensorValue.setText(String.format("Sensor: %g, %g, %g",event.values[0],event.values[1],event.values[2]));
        }
    }

    @NotNull
    private LineDataSet createSet(int color, String seriesLabel) {
        LineDataSet set = new LineDataSet(null, seriesLabel);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(color);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }
    @NotNull
    private ILineDataSet addDataSet(@NotNull int dataSeries){
        // add if not exist or return dataset
        ILineDataSet set = data.getDataSetByIndex(dataSeries);
        if (set==null){
            set = createSet(colorList.get(dataSeries), labelList.get(dataSeries));
            data.addDataSet(set);
        }
        return set;
    }
    private void addEntry(SensorEvent event) {
        if(data!=null){
            numberOfMeasuredPoints++;
            for (int dataSeries = 0; dataSeries < 3; dataSeries++) {
                ILineDataSet set = addDataSet(dataSeries);
                if(set.getEntryCount()>MAX_POINTS_DISPLAYED)
                    set.removeFirst();
                data.addEntry(new Entry(numberOfMeasuredPoints, event.values[dataSeries]), dataSeries);
//                    data.addXValue(String.valueOf(System.currentTimeMillis()));
            }
            data.notifyDataChanged();
            lineChart.moveViewToX(data.getEntryCount());
//                lineChart.setMaxVisibleValueCount(10);
            lineChart.notifyDataSetChanged();
            lineChart.invalidate();
        }
    }


    private void initChart(@NotNull View root){
        lineChart = root.findViewById(R.id.sensor_chart_view);
//        lineChart.setOnChartGestureListener(this);
//        lineChart.setOnChartValueSelectedListener(this);

        // description text
        lineChart.getDescription().setEnabled(true);
        lineChart.getDescription().setText("Real time sensor plot");
        // enable touch gestures
//        lineChart.setTouchEnabled(false);
//        lineChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
//        lineChart.setDragEnabled(false);
//        lineChart.setScaleEnabled(false);

//        lineChart.setDrawGridBackground(false);
//        lineChart.setHighlightPerDragEnabled(true);

//        lineChart.setPinchZoom(false);

        // set an alternative background color
        lineChart.setBackgroundColor(Color.WHITE);
//        lineChart.setViewPortOffsets(0f, 0f, 0f, 0f);

        lineChart.setMaxVisibleValueCount(10);
//
//        // legend
//        Legend legend = lineChart.getLegend();
//        legend.setForm(Legend.LegendForm.LINE);
//        legend.setTextColor(Color.WHITE);
//        // x-axis
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setTextColor(Color.WHITE);
//        xAxis.setDrawGridLines(true);
//        xAxis.setAvoidFirstLastClipping(true);
//        xAxis.setEnabled(true);
//
//        YAxis leftAxis = lineChart.getAxisLeft();
//        leftAxis.setTextColor(Color.WHITE);
//        leftAxis.setDrawGridLines(false);
//        leftAxis.setAxisMaximum(10f);
//        leftAxis.setAxisMinimum(0f);
//        leftAxis.setDrawGridLines(true);
//
//        YAxis rightAxis = lineChart.getAxisRight();
//        rightAxis.setEnabled(false);
//
//        lineChart.getXAxis().setDrawGridLines(false);
//        lineChart.getAxisLeft().setDrawGridLines(false);
//        lineChart.setDrawBorders(false);

    }
}