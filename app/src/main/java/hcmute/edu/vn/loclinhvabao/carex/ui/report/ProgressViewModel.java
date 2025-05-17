package hcmute.edu.vn.loclinhvabao.carex.ui.report;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaSession;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.YogaRepository;

@HiltViewModel
public class ProgressViewModel extends ViewModel {
    private final YogaRepository yogaRepository;

    private final MutableLiveData<String> timeRange = new MutableLiveData<>("week");
    private final MutableLiveData<Date> startDate = new MutableLiveData<>();
    private final MutableLiveData<Date> endDate = new MutableLiveData<>(new Date());

    private final LiveData<List<YogaSession>> filteredSessions;

    @Inject
    public ProgressViewModel(YogaRepository yogaRepository) {
        this.yogaRepository = yogaRepository;

        // Set default time range to this week
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        startDate.setValue(calendar.getTime());

        // Get sessions between selected dates
        filteredSessions = Transformations.switchMap(startDate, start ->
                Transformations.switchMap(endDate, end ->
                        yogaRepository.getSessionsBetweenDates(start, end)
                )
        );
    }

    public LiveData<List<YogaSession>> getFilteredSessions() {
        return filteredSessions;
    }
    public void setTimeRange(String range) {
        if (!range.equals(timeRange.getValue())) {
            timeRange.setValue(range);

            Calendar calendar = Calendar.getInstance();
            Date end = calendar.getTime();
            endDate.setValue(end);

            // Tính ngày bắt đầu dựa vào timeRange
            switch (range) {
                case "week":
                    calendar.add(Calendar.DAY_OF_YEAR, -7);
                    break;
                case "month":
                    calendar.add(Calendar.MONTH, -1);
                    break;
                case "year":
                    calendar.add(Calendar.YEAR, -1);
                    break;
                case "all":
                    calendar.add(Calendar.YEAR, -10); // Default to 10 years back for "all time"
                    break;
            }
            startDate.setValue(calendar.getTime());
        }
    }

    public String getTimeRange() {
        return timeRange.getValue();
    }

    public void setCustomDateRange(Date start, Date end) {
        startDate.setValue(start);
        endDate.setValue(end);
        timeRange.setValue("custom");
    }
}