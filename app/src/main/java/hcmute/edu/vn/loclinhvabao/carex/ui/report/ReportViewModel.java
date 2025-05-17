package hcmute.edu.vn.loclinhvabao.carex.ui.report;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.ProgressStats;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaSession;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.YogaRepository;

@HiltViewModel
public class ReportViewModel extends ViewModel {
    private final YogaRepository yogaRepository;

    private final MutableLiveData<ProgressStats> progressStats = new MutableLiveData<>();
    private final LiveData<List<YogaSession>> recentSessions;

    @Inject
    public ReportViewModel(YogaRepository yogaRepository) {
        this.yogaRepository = yogaRepository;
        this.recentSessions = yogaRepository.getRecentSessions(5);

        // Load initial stats
        loadProgressStats();
    }

    public LiveData<ProgressStats> getProgressStats() {
        return progressStats;
    }

    public LiveData<List<YogaSession>> getRecentSessions() {
        return recentSessions;
    }

    public void loadProgressStats() {
        new Thread(() -> {
            ProgressStats stats = yogaRepository.calculateStats();
            progressStats.postValue(stats);
        }).start();
    }

    public void refreshData() {
        loadProgressStats();
    }
}