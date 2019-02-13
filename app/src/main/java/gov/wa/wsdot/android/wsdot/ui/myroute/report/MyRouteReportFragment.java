package gov.wa.wsdot.android.wsdot.ui.myroute.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.camera.CameraListFragment;
import gov.wa.wsdot.android.wsdot.ui.myroute.myroutealerts.MyRouteAlertsListFragment;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.alertsinarea.HighwayAlertListFragment;
import gov.wa.wsdot.android.wsdot.ui.traveltimes.TravelTimesFragment;

public class MyRouteReportFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_tabs, null);

        // Setting ViewPager for each Tabs
        ViewPager viewPager = root.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = root.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(viewPager);


        return root;
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {

        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new HighwayAlertListFragment(), "Alerts");
        adapter.addFragment(new TravelTimesFragment(), "Travel Times");
        adapter.addFragment(new CameraListFragment(), "Cameras");
        viewPager.setAdapter(adapter);

    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
