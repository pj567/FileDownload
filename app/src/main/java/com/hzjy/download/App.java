package com.hzjy.download;

import android.app.Application;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;

/**
 * pj567
 * 2019/9/17
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AutoSizeConfig.getInstance().getUnitsManager().setSupportDP(false).setSupportSubunits(Subunits.PT);
    }
}
