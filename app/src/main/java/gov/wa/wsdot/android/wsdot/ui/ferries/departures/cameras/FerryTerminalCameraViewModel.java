package gov.wa.wsdot.android.wsdot.ui.ferries.departures.cameras;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import gov.wa.wsdot.android.wsdot.database.cameras.CameraEntity;
import gov.wa.wsdot.android.wsdot.repository.CameraRepository;
import gov.wa.wsdot.android.wsdot.shared.CameraItem;
import gov.wa.wsdot.android.wsdot.ui.camera.MapCameraViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.helpers.FerryHelper;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;

public class FerryTerminalCameraViewModel extends ViewModel {

    private static String TAG = MapCameraViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mStatus;

    private MediatorLiveData<List<CameraItem>> terminalCameras;

    private AppExecutors appExecutors;
    private CameraRepository cameraRepo;

    @Inject
    FerryTerminalCameraViewModel(CameraRepository cameraRepo, AppExecutors appExecutors) {
        this.mStatus = new MutableLiveData<>();
        this.terminalCameras = new MediatorLiveData<>();
        this.cameraRepo = cameraRepo;
        this.appExecutors = appExecutors;
    }

    public LiveData<ResourceStatus> getResourceStatus() { return this.mStatus; }

    public MediatorLiveData<List<CameraItem>> getTerminalCameras() {
        return this.terminalCameras;
    }

    public void loadTerminalCameras(Integer terminalId, String roadName){
        terminalCameras.addSource(cameraRepo.getCamerasForRoad(roadName, mStatus), cameras -> {
            processTerminalCameras(terminalId, cameras);
        });
    }

    public void processTerminalCameras(Integer terminalId, List<CameraEntity> cameras){

        appExecutors.taskIO().execute(() -> {
            List<CameraItem> terminalCameraItems = new ArrayList<>();

            for (CameraEntity cameraEntity: cameras) {

                int distance = FerryHelper.getDistanceFromTerminal(terminalId, cameraEntity.getLatitude(), cameraEntity.getLongitude());

                // If less than 3 miles from terminal, and labeled as a ferries camera, show it
                if (distance < 15840 && cameraEntity.getRoadName().toLowerCase(Locale.US).equals("ferries")) { // in feet
                    CameraItem camera = new CameraItem();
                    camera.setCameraId(cameraEntity.getCameraId());
                    camera.setTitle(cameraEntity.getTitle());
                    camera.setImageUrl(cameraEntity.getUrl());
                    camera.setLatitude(cameraEntity.getLatitude());
                    camera.setLongitude(cameraEntity.getLongitude());
                    camera.setDistance(distance);
                    terminalCameraItems.add(camera);
                }
            }
            terminalCameras.postValue(terminalCameraItems);
        });


    }

}
