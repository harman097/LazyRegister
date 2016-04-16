package com.brent.harman.lazyregister;

import android.support.v4.app.Fragment;

/**
 * Created by Brent on 6/4/15.
 */
public interface OnCustomActionBarChangeRequests {
    public void requestActionBarChange(Fragment callingFragment);
    public void notifyOnDetach(Fragment callingFragment);
}
