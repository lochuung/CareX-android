package hcmute.edu.vn.loclinhvabao.carex.ui.report;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaSession;
import hcmute.edu.vn.loclinhvabao.carex.data.repository.YogaRepository;

@HiltViewModel
public class SessionDetailViewModel extends ViewModel {
    private final YogaRepository yogaRepository;

    private final MutableLiveData<YogaSession> session = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public SessionDetailViewModel(YogaRepository yogaRepository) {
        this.yogaRepository = yogaRepository;
    }

    public LiveData<YogaSession> getSession() {
        return session;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadSession(String sessionId) {
        isLoading.setValue(true);

        new Thread(() -> {
            try {
                YogaSession yogaSession = yogaRepository.getSessionById(sessionId);
                session.postValue(yogaSession);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Failed to load session: " + e.getMessage());
                isLoading.postValue(false);
            }
        }).start();
    }

    public void updateSessionNotes(String notes) {
        YogaSession currentSession = session.getValue();
        if (currentSession != null) {
            currentSession.setNotes(notes);
            yogaRepository.updateSession(currentSession);
            session.setValue(currentSession);
        }
    }

    public void shareSession() {
        // Implement sharing functionality
    }
}