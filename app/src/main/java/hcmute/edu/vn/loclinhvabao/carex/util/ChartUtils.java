package hcmute.edu.vn.loclinhvabao.carex.util;

import android.content.Context;
import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.DailyProgress;

public class ChartUtils {

    public static void setupLineChart(LineChart chart, Context context, List<DailyProgress> progressList, String[] labels) {
        if (chart == null || context == null) return;

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < progressList.size(); i++) {
            entries.add(new Entry(i, progressList.get(i).getDuration()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Duration (minutes)");
        dataSet.setColor(context.getColor(R.color.yogaColor));
        dataSet.setCircleColor(context.getColor(R.color.yogaColor));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Customize chart appearance
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        // X-axis customization
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        if (labels != null) {
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        }
        xAxis.setTextColor(context.getColor(R.color.textSecondary));

        // Y-axis customization
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawZeroLine(true);
        leftAxis.setTextColor(context.getColor(R.color.textSecondary));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.animateX(1000);
        chart.invalidate();
    }

    public static void setupPieChart(PieChart chart, Context context, Map<String, Integer> distribution) {
        if (chart == null || context == null) return;

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Generate colors for the pie chart
        int[] colorArray = new int[] {
                context.getColor(R.color.yogaColor),
                context.getColor(R.color.purple_500),
                context.getColor(R.color.teal_200),
                context.getColor(R.color.orange_500),
                context.getColor(R.color.blue_500)
        };

        int colorIndex = 0;
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            colors.add(colorArray[colorIndex % colorArray.length]);
            colorIndex++;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Yoga Style Distribution");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f min", value);
            }
        });

        chart.setData(pieData);

        // Customize chart appearance
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(context.getColor(R.color.cardBackground));
        chart.setHoleRadius(35f);
        chart.setTransparentCircleRadius(40f);
        chart.setTransparentCircleColor(context.getColor(R.color.cardBackground));
        chart.setTransparentCircleAlpha(100);

        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextColor(context.getColor(R.color.textPrimary));

        chart.animateY(1000);
        chart.invalidate();
    }
}