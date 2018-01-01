package com.at.alerttrader;

import android.app.Application;

/**
 * Created by lenovo on 25-12-2017.
 */

public class ApplicationConstant extends Application {
    private long nextUpdate;

    public long getNextUpdate() {
        return nextUpdate;
    }

    public void setNextUpdate(long nextUpdate) {
        this.nextUpdate = nextUpdate;
    }
}
