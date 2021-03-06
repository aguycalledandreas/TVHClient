package org.tvheadend.tvhclient.fragments;

import org.tvheadend.tvhclient.Constants;
import org.tvheadend.tvhclient.R;
import org.tvheadend.tvhclient.TVHClientApplication;
import org.tvheadend.tvhclient.model.Recording;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;

public class ScheduledRecordingListFragment extends RecordingListFragment {

    /**
     * Sets the correct tag. This is required for logging and especially for the
     * main activity so it knows what action shall be executed depending on the
     * recording fragment type.
     */
    public ScheduledRecordingListFragment() {
        TAG = ScheduledRecordingListFragment.class.getSimpleName();
    }

    @Override
    public void onResume() {
        super.onResume();
        TVHClientApplication app = (TVHClientApplication) activity.getApplication();
        app.addListener(this);
        if (!app.isLoading()) {
            populateList();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        TVHClientApplication app = (TVHClientApplication) activity.getApplication();
        app.removeListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Do not show the remove menu in single pane mode. No recording is
        // preselected so the behavior is undefined. In dual pane mode one
        // recording is also selected which is fine.
        if (!isDualPane) {
            (menu.findItem(R.id.menu_record_cancel)).setVisible(false);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (prefs.getBoolean("hideMenuCancelAllRecordingsPref", false)) {
            (menu.findItem(R.id.menu_record_cancel_all)).setVisible(false);
        }

        (menu.findItem(R.id.menu_record_remove)).setVisible(false);
        (menu.findItem(R.id.menu_record_remove_all)).setVisible(false);
        // Playing a scheduled recording is not possible 
        (menu.findItem(R.id.menu_play)).setVisible(false);
    }

    /**
     * Fills the list with the available recordings. Only the recordings that
     * are scheduled are added to the list.
     */
    private void populateList() {
        // Clear the list and add the recordings
        adapter.clear();
        TVHClientApplication app = (TVHClientApplication) activity.getApplication();
        for (Recording rec : app.getRecordingsByType(Constants.RECORDING_TYPE_SCHEDULED)) {
            adapter.add(rec);
        }
        // Show the newest scheduled recordings first 
        adapter.sort(Constants.RECORDING_SORT_DESCENDING);
        adapter.notifyDataSetChanged();
        
        // Shows the currently visible number of recordings of the type  
        if (actionBarInterface != null) {
            actionBarInterface.setActionBarTitle(getString(R.string.recordings), TAG);
            actionBarInterface.setActionBarSubtitle(adapter.getCount() + " " + getString(R.string.upcoming_recordings), TAG);
            actionBarInterface.setActionBarIcon(R.drawable.ic_launcher, TAG);
        }
        // Inform the listeners that the channel list is populated.
        // They could then define the preselected list item.
        if (fragmentStatusInterface != null) {
            fragmentStatusInterface.onListPopulated(TAG);
        }
    }
    
    /**
     * This method is part of the HTSListener interface. Whenever the HTSService
     * sends a new message the correct action will then be executed here.
     */
    @Override
    public void onMessage(String action, final Object obj) {
        if (action.equals(Constants.ACTION_LOADING)) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    boolean loading = (Boolean) obj;
                    if (loading) {
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                    } else {
                        populateList();
                    }
                }
            });
        } else if (action.equals(Constants.ACTION_DVR_ADD) 
                || action.equals(Constants.ACTION_DVR_DELETE)
                || action.equals(Constants.ACTION_DVR_UPDATE)) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    populateList();
                }
            });
        }
    }
}
