/*
 * Copyright (c) 2018 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.shared.I405TollRateSignItem;
import gov.wa.wsdot.android.wsdot.shared.I405TripItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class I405TollRatesFragment extends BaseFragment
        implements SwipeRefreshLayout.OnRefreshListener, Injectable {
	
    private static final String TAG = I405TollRatesFragment.class.getSimpleName();

    private static I405TollRatesItemAdapter mAdapter;
    private View mEmptyView;
    private static SwipeRefreshLayout swipeRefreshLayout;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

	@Inject
	ViewModelProvider.Factory viewModelFactory;
	I405TollRatesViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new I405TollRatesItemAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        mRecyclerView.setPadding(0,0,0,100);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        swipeRefreshLayout = root.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);

        mEmptyView = root.findViewById(R.id.empty_list_view);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(I405TollRatesViewModel.class);

        viewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        swipeRefreshLayout.setRefreshing(true);
                        break;
                    case SUCCESS:
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case ERROR:
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(this.getContext(), "connection error", Toast.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getTollRateItems().observe(this, tollRateSignItems -> {
            if (tollRateSignItems != null) {
                if (tollRateSignItems.size() == 0) {
                    TextView t = (TextView) mEmptyView;
                    t.setText("toll rates unavailable.");
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyView.setVisibility(View.GONE);
                }
                mAdapter.setData(new ArrayList<>(tollRateSignItems));
            }
        });

        viewModel.refresh();

        return root;
    }


    /**
     * Custom adapter for items in recycler view.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class I405TollRatesItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private Context context;

        private ArrayList<I405TollRateSignItem> mData = new ArrayList<>();

        private List<RecyclerView.ViewHolder> mItems = new ArrayList<>();

        public I405TollRatesItemAdapter(Context context) {
            this.context = context;
        }

        public void setData(ArrayList<I405TollRateSignItem> data){
            mData = data;
            this.notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_travel_time_group, null);
            ViewHolder viewholder = new ViewHolder(view);
            view.setTag(viewholder);
            mItems.add(viewholder);
            return viewholder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

            ViewHolder viewholder = (ViewHolder) viewHolder;

            I405TollRateSignItem tollRateSignItem = mData.get(position);

            final String title = tollRateSignItem.getStartLocationName().concat(" Entrance");
            viewholder.title.setText(title);
            viewholder.title.setTypeface(tfb);

            viewholder.travel_times_layout.removeAllViews();

            for (I405TripItem trip: tollRateSignItem.getTrips()) {

                View tripView = makeTripView(trip, getContext());

                // remove the line from the last trip
                if (tollRateSignItem.getTrips().indexOf(trip) == tollRateSignItem.getTrips().size() - 1){
                    tripView.findViewById(R.id.line).setVisibility(View.GONE);
                }

                viewholder.travel_times_layout.addView(tripView);
            }

            /* TODO: favorites
            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            viewholder.star_button.setOnCheckedChangeListener(null);
            viewholder.star_button
                    .setChecked(travelTimeGroup.trip.getIsStarred() != 0);

            viewholder.star_button.setOnCheckedChangeListener((buttonView, isChecked) -> {

                Snackbar added_snackbar = Snackbar
                        .make(getView(), R.string.add_favorite, Snackbar.LENGTH_SHORT);

                Snackbar removed_snackbar = Snackbar
                        .make(getView(), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

                if (isChecked){
                    added_snackbar.show();
                }else{
                    removed_snackbar.show();
                }

                viewModel.setIsStarredFor(title, isChecked ? 1 : 0);
            });
            */
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout travel_times_layout;
            public TextView title;
            public CheckBox star_button;

            public ViewHolder(View view) {
                super(view);
                travel_times_layout = view.findViewById(R.id.travel_times_linear_layout);
                title = view.findViewById(R.id.title);
                star_button = view.findViewById(R.id.star_button);
            }
        }
    }

    public static View makeTripView(I405TripItem tripItem, Context context) {

        Typeface tfb = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
        LayoutInflater li = LayoutInflater.from(context);
        View cv = li.inflate(R.layout.trip_view, null);

        // set end location label
        ((TextView) cv.findViewById(R.id.title)).setText(tripItem.getEndLocationName().concat(" Exit"));

        // set updated label
        ((TextView) cv.findViewById(R.id.updated)).setText(tripItem.getUpdatedAt());

        // set toll
        TextView currentTimeTextView = cv.findViewById(R.id.current_value);
        currentTimeTextView.setTypeface(tfb);
        currentTimeTextView.setText("$".concat(String.valueOf(tripItem.getToll()/100)));

        // set message if there is one
        if (tripItem.getMessage().equals("null")){
            ((TextView) cv.findViewById(R.id.subtitle)).setText("");
        } else {
            ((TextView) cv.findViewById(R.id.subtitle)).setText(tripItem.getMessage());
        }

        return cv;
    }

    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        viewModel.refresh();
    }
}