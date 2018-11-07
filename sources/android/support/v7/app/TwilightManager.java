package android.support.v7.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import java.util.Calendar;

class TwilightManager {
    private static final int SUNRISE = 6;
    private static final int SUNSET = 22;
    private static final String TAG = "TwilightManager";
    private static TwilightManager sInstance;
    private final Context mContext;
    private final LocationManager mLocationManager;
    private final TwilightState mTwilightState = new TwilightState();

    private static class TwilightState {
        boolean isNight;
        long nextUpdate;
        long todaySunrise;
        long todaySunset;
        long tomorrowSunrise;
        long yesterdaySunset;

        TwilightState() {
        }
    }

    static TwilightManager getInstance(@NonNull Context context) {
        if (sInstance == null) {
            context = context.getApplicationContext();
            sInstance = new TwilightManager(context, (LocationManager) context.getSystemService("location"));
        }
        return sInstance;
    }

    @VisibleForTesting
    static void setInstance(TwilightManager twilightManager) {
        sInstance = twilightManager;
    }

    @VisibleForTesting
    TwilightManager(@NonNull Context context, @NonNull LocationManager locationManager) {
        this.mContext = context;
        this.mLocationManager = locationManager;
    }

    boolean isNight() {
        TwilightState state = this.mTwilightState;
        if (isStateValid()) {
            return state.isNight;
        }
        Location location = getLastKnownLocation();
        if (location != null) {
            updateState(location);
            return state.isNight;
        }
        boolean z;
        Log.i(TAG, "Could not get last known location. This is probably because the app does not have any location permissions. Falling back to hardcoded sunrise/sunset values.");
        int hour = Calendar.getInstance().get(11);
        if (hour >= 6) {
            if (hour < 22) {
                z = false;
                return z;
            }
        }
        z = true;
        return z;
    }

    @SuppressLint({"MissingPermission"})
    private Location getLastKnownLocation() {
        Location coarseLoc = null;
        Location fineLoc = null;
        if (PermissionChecker.checkSelfPermission(this.mContext, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            coarseLoc = getLastKnownLocationForProvider("network");
        }
        if (PermissionChecker.checkSelfPermission(this.mContext, "android.permission.ACCESS_FINE_LOCATION") == 0) {
            fineLoc = getLastKnownLocationForProvider("gps");
        }
        if (fineLoc == null || coarseLoc == null) {
            return fineLoc != null ? fineLoc : coarseLoc;
        }
        return fineLoc.getTime() > coarseLoc.getTime() ? fineLoc : coarseLoc;
    }

    @RequiresPermission(anyOf = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"})
    private Location getLastKnownLocationForProvider(String provider) {
        try {
            if (this.mLocationManager.isProviderEnabled(provider)) {
                return this.mLocationManager.getLastKnownLocation(provider);
            }
            return null;
        } catch (Exception e) {
            Log.d(TAG, "Failed to get last known location", e);
        }
    }

    private boolean isStateValid() {
        return this.mTwilightState.nextUpdate > System.currentTimeMillis();
    }

    private void updateState(@NonNull Location location) {
        long nextUpdate;
        TwilightState state;
        TwilightState state2 = this.mTwilightState;
        long now = System.currentTimeMillis();
        TwilightCalculator calculator = TwilightCalculator.getInstance();
        TwilightCalculator twilightCalculator = calculator;
        twilightCalculator.calculateTwilight(now - 86400000, location.getLatitude(), location.getLongitude());
        long yesterdaySunset = calculator.sunset;
        twilightCalculator.calculateTwilight(now, location.getLatitude(), location.getLongitude());
        boolean z = true;
        if (calculator.state != 1) {
            z = false;
        }
        boolean isNight = z;
        long todaySunrise = calculator.sunrise;
        long todaySunset = calculator.sunset;
        long j = now + 86400000;
        long yesterdaySunset2 = yesterdaySunset;
        yesterdaySunset = todaySunset;
        TwilightState state3 = state2;
        long todaySunrise2 = todaySunrise;
        double latitude = location.getLatitude();
        boolean isNight2 = isNight;
        calculator.calculateTwilight(j, latitude, location.getLongitude());
        long tomorrowSunrise = calculator.sunrise;
        if (todaySunrise2 != -1) {
            if (yesterdaySunset != -1) {
                long nextUpdate2;
                if (now > yesterdaySunset) {
                    nextUpdate2 = 0 + tomorrowSunrise;
                } else if (now > todaySunrise2) {
                    nextUpdate2 = 0 + yesterdaySunset;
                } else {
                    nextUpdate2 = 0 + todaySunrise2;
                }
                j = nextUpdate2 + 60000;
                nextUpdate = j;
                state = state3;
                state.isNight = isNight2;
                state.yesterdaySunset = yesterdaySunset2;
                state.todaySunrise = todaySunrise2;
                state.todaySunset = yesterdaySunset;
                state.tomorrowSunrise = tomorrowSunrise;
                state.nextUpdate = nextUpdate;
            }
        }
        j = now + 43200000;
        nextUpdate = j;
        state = state3;
        state.isNight = isNight2;
        state.yesterdaySunset = yesterdaySunset2;
        state.todaySunrise = todaySunrise2;
        state.todaySunset = yesterdaySunset;
        state.tomorrowSunrise = tomorrowSunrise;
        state.nextUpdate = nextUpdate;
    }
}
