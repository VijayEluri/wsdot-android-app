package gov.wa.wsdot.android.wsdot.ui.ferries.departures.vesselwatch;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.database.ferries.WeatherReportEntity;
import gov.wa.wsdot.android.wsdot.repository.VesselWatchRepository;
import gov.wa.wsdot.android.wsdot.repository.WeatherReportRepository;
import gov.wa.wsdot.android.wsdot.shared.VesselWatchItem;
import gov.wa.wsdot.android.wsdot.shared.WeatherItem;
import gov.wa.wsdot.android.wsdot.shared.livedata.WeatherItemLiveData;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

import static java.lang.System.currentTimeMillis;

public class VesselWatchViewModel extends ViewModel {

    private static String TAG = VesselWatchViewModel.class.getSimpleName();

    private MutableLiveData<ResourceStatus> mVesselStatus;
    private MutableLiveData<ResourceStatus> mWeatherStatus;

    private VesselWatchRepository vesselWatchRepo;
    private WeatherReportRepository weatherReportRepo;

    private WeatherItemLiveData weatherItemLiveData;

    @Inject
    VesselWatchViewModel(VesselWatchRepository vesselWatchRepo, WeatherReportRepository weatherReportRepo) {
        this.mVesselStatus = new MutableLiveData<>();
        this.mWeatherStatus = new MutableLiveData<>();
        this.vesselWatchRepo = vesselWatchRepo;
        this.weatherReportRepo = weatherReportRepo;

        Date startDate = new Date();

        // only get the last hours worth of reports
        Date endDate = new Date(currentTimeMillis() - 3200 * 1000);
        weatherItemLiveData = new WeatherItemLiveData(weatherReportRepo.getReportsInRange(startDate, endDate, mWeatherStatus));

    }

    public LiveData<ResourceStatus> getVesselResourceStatus() { return this.mVesselStatus; }
    public LiveData<ResourceStatus> getWeatherResourceStatus() { return this.mWeatherStatus; }

    public MutableLiveData<List<VesselWatchItem>> getVessels(){
        return vesselWatchRepo.getVessels();
    }


    public LiveData<List<WeatherItem>> getWeatherReports(){
        return weatherItemLiveData;
    }

    public void refreshVessels() {
        vesselWatchRepo.refreshData(this.mVesselStatus);
    }

    public void refreshWeatherReports() { weatherReportRepo.refreshData(this.mWeatherStatus, true);}
}
