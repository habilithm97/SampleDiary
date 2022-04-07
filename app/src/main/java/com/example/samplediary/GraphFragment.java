package com.example.samplediary;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.samplediary.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GraphFragment extends Fragment {

    PieChart pieChart;
    BarChart barChart;
    LineChart lineChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_graph, container, false);

        initUi(rootView);

        return rootView;
    }

    private void initUi(ViewGroup rootView) {
        pieChart = rootView.findViewById(R.id.pieChart);
        pieChart.setUsePercentValues(true); // ??
        pieChart.getDescription().setEnabled(false); // 그래프 설명 표시하지 않음
        pieChart.setCenterText("기분별 비율"); // 중앙 원 제목
        pieChart.setHoleRadius(70f); // 중앙 원 반지름
        pieChart.setDrawCenterText(true); // 중앙 원에 있는 텍스트 보이기

        pieChart.setTransparentCircleColor(Color.WHITE); // 중앙 원 테두리 색
        pieChart.setTransparentCircleAlpha(110); // ??
        pieChart.setTransparentCircleRadius(40f); // ??
        pieChart.setHighlightPerTapEnabled(false); // ??

        Legend legend1 = pieChart.getLegend();
        legend1.setEnabled(false); // 속성 표시 안함
        pieChart.setEntryLabelColor(Color.WHITE); // ??
        pieChart.setEntryLabelTextSize(12f); // ??
        setData1(); // 넣을 데이터 설정

        // -------------------------------------------------------------------------
        barChart = rootView.findViewById(R.id.barChart);
        barChart.setDrawValueAboveBar(true); // 속성 아이콘을 바 위에 배치함
        barChart.getDescription().setEnabled(false); // 그래프 설명 표시하지 않음

        XAxis xAxis = barChart.getXAxis();
        xAxis.setEnabled(false); // x좌표 표시 안함

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setLabelCount(6, true); // 6개의 눈금으로 나눔
        leftAxis.setAxisMinimum(0.0f); // 최소값은 0부터
        leftAxis.setGranularityEnabled(true); // ?
        leftAxis.setGranularity(1f); // ?

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false); // 오른쪽 y좌표는 표시하지 않음

        Legend legend2 = barChart.getLegend();
        legend2.setEnabled(false); // 속성 표시하지 않음

        barChart.animateXY(1500, 1500); // 바가 올라오는 애니메이션 속도

        setData2();

        // -------------------------------------------------------------------------
        /*
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);

        // 차트 배경색 변경
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setViewPortOffsets(0, 0, 0, 0);

        // get the legend (only possible after setting data)
        Legend legend3 = lineChart.getLegend();
        legend3.setEnabled(false);

        XAxis xAxis3 = lineChart.getXAxis();
        xAxis3.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis3.setTextSize(10f);
        xAxis3.setTextColor(Color.WHITE);
        xAxis3.setDrawAxisLine(false);
        xAxis3.setDrawGridLines(true);
        xAxis3.setTextColor(Color.rgb(255, 192, 56));
        xAxis3.setCenterAxisLabels(true);
        xAxis3.setGranularity(1f);
        xAxis3.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("MM-DD", Locale.KOREA);

            @Override
            public String getFormattedValue(float value) {

                long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });

        YAxis leftAxis3 = lineChart.getAxisLeft();
        leftAxis3.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis3.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis3.setDrawGridLines(true);
        leftAxis3.setGranularityEnabled(true);
        leftAxis3.setAxisMinimum(0f);
        leftAxis3.setAxisMaximum(170f);
        leftAxis3.setYOffset(-9f);
        leftAxis3.setTextColor(Color.rgb(255, 192, 56));

        YAxis rightAxis3 = lineChart.getAxisRight();
        rightAxis3.setEnabled(false);

        setData3(); */
    }

    private void setData1() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        // 각각의 속성에 추가함(비율, 속성 이름, 속성 아이콘)
        entries.add(new PieEntry(20.0f, "", getResources().getDrawable(R.drawable.smile1_48)));
        entries.add(new PieEntry(20.0f, "", getResources().getDrawable(R.drawable.smile2_48)));
        entries.add(new PieEntry(20.0f, "", getResources().getDrawable(R.drawable.smile3_48)));
        entries.add(new PieEntry(20.0f, "", getResources().getDrawable(R.drawable.smile4_48)));
        entries.add(new PieEntry(20.0f, "", getResources().getDrawable(R.drawable.smile5_48)));

        PieDataSet dataSet = new PieDataSet(entries, "기분별 비율");

        dataSet.setDrawIcons(true); // 속성 아이콘 표시
        dataSet.setSliceSpace(5f); // 속성 간 간격
        dataSet.setIconsOffset(new MPPointF(0, -40)); // 속성 아이콘 위치
        dataSet.setSelectionShift(10f); // 그래프 크기?

        ArrayList<Integer> colors = new ArrayList<>();

        for(int color : ColorTemplate.JOYFUL_COLORS) {
            colors.add(color);
        }
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(22.0f); // 비율 텍스트 크기
        data.setValueTextColor(Color.BLACK); // 비율 텍스트 색

        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void setData2() {

        ArrayList<BarEntry> entries = new ArrayList<>();

        // 바의 위치와 높이, 아이콘 설정
        entries.add(new BarEntry(1.0f, 20.0f, getResources().getDrawable(R.drawable.smile1_48)));
        entries.add(new BarEntry(2.0f, 40.0f, getResources().getDrawable(R.drawable.smile2_48)));
        entries.add(new BarEntry(3.0f, 60.0f, getResources().getDrawable(R.drawable.smile3_48)));
        entries.add(new BarEntry(4.0f, 30.0f, getResources().getDrawable(R.drawable.smile4_48)));
        entries.add(new BarEntry(5.0f, 90.0f, getResources().getDrawable(R.drawable.smile5_48)));

        BarDataSet dataSet2 = new BarDataSet(entries, "요일별 기분");
        dataSet2.setColor(Color.rgb(240, 120, 124)); // ??

        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : ColorTemplate.JOYFUL_COLORS) {
            colors.add(color);
        }
        dataSet2.setColors(colors);
        dataSet2.setIconsOffset(new MPPointF(0, -10)); // 속성 아이콘 위치

        BarData data = new BarData(dataSet2);
        data.setValueTextSize(10f);
        data.setDrawValues(false); // 바 위에 값 표시하지 않음
        data.setBarWidth(0.8f); // 바 너비

        barChart.setData(data);
        barChart.invalidate();
    }

    /*
    private void setData3() {

        ArrayList<Entry> values = new ArrayList<>();
        values.add(new Entry(24f, 20.0f));
        values.add(new Entry(48f, 50.0f));
        values.add(new Entry(72f, 30.0f));
        values.add(new Entry(96f, 70.0f));
        values.add(new Entry(120f, 90.0f));

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "DataSet 1");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(1.5f);
        set1.setDrawCircles(true);
        set1.setDrawValues(false);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(false);

        // create a data object with the data sets
        LineData data = new LineData(set1);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        // set data
        lineChart.setData(data);
        lineChart.invalidate();
    } */
}