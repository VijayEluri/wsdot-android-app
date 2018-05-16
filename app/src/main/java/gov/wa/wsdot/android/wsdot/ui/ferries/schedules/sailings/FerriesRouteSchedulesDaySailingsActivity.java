/*
 * Copyright (c) 2017 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.ui.ferries.schedules.sailings;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityEvent;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.ferries.FerrySchedulesViewModel;
import gov.wa.wsdot.android.wsdot.util.MyLogger;

public class FerriesRouteSchedulesDaySailingsActivity extends BaseActivity {

	private final String TAG = FerriesRouteSchedulesDaySailingsActivity.class.getSimpleName();

	private boolean mIsStarred;
	private int mId;
	private Toolbar mToolbar;
    private String mTitle;

	static final private int MENU_ITEM_STAR = 0;

    private static FerrySchedulesViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ferries_route_schedules_day_sailings);

		Bundle args = getIntent().getExtras();
		mId = args.getInt("id");

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FerrySchedulesViewModel.class);
        viewModel.init(mId);

        viewModel.getFerrySchedule().observe(this, schedule -> {
            if (schedule != null) {
                mTitle = schedule.getTitle();
                mIsStarred = schedule.getIsStarred() != 0;
                mToolbar.setTitle(mTitle);
            } else {
                mId = -1;
            	mTitle = "Schedule Unavailable";
            	mToolbar.setTitle(mTitle);
			}
        });

		mToolbar = findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
		MyLogger.crashlyticsLog("Ferries", "Screen View", "FerriesRouteSchedulesDaySailingsActivity: Route " + String.valueOf(mId), 1);
        enableAds(getString(R.string.ferries_ad_target));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

	    if (mId != -1) {
            MenuItem menuItem_Star = menu.add(0, MENU_ITEM_STAR, menu.size(), R.string.description_star);
            MenuItemCompat.setShowAsAction(menuItem_Star, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

            if (mIsStarred) {
                menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star_on);
                menu.getItem(MENU_ITEM_STAR).setTitle("Favorite checkbox, checked");
            } else {
                menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star);
                menu.getItem(MENU_ITEM_STAR).setTitle("Favorite checkbox, not checked");
            }
        }

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
	    case android.R.id.home:
	    	finish();
	    	return true;
		case MENU_ITEM_STAR:
			toggleStar(item);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void toggleStar(MenuItem item) {

		Snackbar added_snackbar = Snackbar
				.make(findViewById(R.id.day_sailings), "Added to favorites", Snackbar.LENGTH_SHORT);

		Snackbar removed_snackbar = Snackbar
				.make(findViewById(R.id.day_sailings), "Removed from favorites", Snackbar.LENGTH_SHORT);

		added_snackbar.addCallback(new Snackbar.Callback() {
			@Override
			public void onShown(Snackbar snackbar) {
				super.onShown(snackbar);
				snackbar.getView().setContentDescription("added to favorites");
				snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
			}
		});

		removed_snackbar.addCallback(new Snackbar.Callback() {
			@Override
			public void onShown(Snackbar snackbar) {
				super.onShown(snackbar);
				snackbar.getView().setContentDescription("removed from favorites");
				snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
			}
		});

		if (mIsStarred) {
			item.setIcon(R.drawable.ic_menu_star);
			item.setTitle("Favorite checkbox, not checked");
            viewModel.setIsStarredFor(mId, 0);
            removed_snackbar.show();
            mIsStarred = false;
		} else {
			item.setIcon(R.drawable.ic_menu_star_on);
			item.setTitle("Favorite checkbox, checked");
            viewModel.setIsStarredFor(mId, 1);
            added_snackbar.show();
            mIsStarred = true;
		}
	}
}