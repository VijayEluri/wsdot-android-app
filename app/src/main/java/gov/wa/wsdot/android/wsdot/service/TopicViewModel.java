package gov.wa.wsdot.android.wsdot.service;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.database.notifications.NotificationTopicEntity;
import gov.wa.wsdot.android.wsdot.repository.NotificationTopicsRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

public class TopicViewModel extends ViewModel {

    private static String TAG = TopicViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private NotificationTopicsRepository topicsRepo;

    private LiveData<List<NotificationTopicEntity>> topics;

    @Inject
    public TopicViewModel(NotificationTopicsRepository topicsRepo) {
        this.mStatus = new MutableLiveData<>();
        this.topicsRepo = topicsRepo;
    }

    public void init() {
        this.topics = topicsRepo.loadTopics(mStatus);
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public LiveData<List<NotificationTopicEntity>> getTopics() {
        return topics;
    }

}