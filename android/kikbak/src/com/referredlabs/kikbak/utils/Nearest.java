
package com.referredlabs.kikbak.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.location.Location;

import com.referredlabs.kikbak.data.MerchantLocationType;
import com.referredlabs.kikbak.service.LocationFinder;

public class Nearest {
  private String mMerchantName;
  private MerchantLocationType[] mLocations;
  private MerchantLocationType mNearestLocation;
  private float mDistanceToNearest;

  public Nearest(String merchantName, MerchantLocationType[] locations) {
    mMerchantName = merchantName;
    mLocations = locations;

    Location l = LocationFinder.getLastLocation();
    if (l != null) {
      determineNearestLocation(l.getLatitude(), l.getLongitude());
    } else {
      mNearestLocation = mLocations[0];
      mDistanceToNearest = -1.0f;
    }
  }

  public Nearest(String merchantName, MerchantLocationType[] locations, double latitude,
      double longitude) {
    mMerchantName = merchantName;
    mLocations = locations;
    determineNearestLocation(latitude, longitude);
  }

  private void determineNearestLocation(double latitude, double longitude) {
    float[] results = new float[1];
    float distance = Float.MAX_VALUE;
    MerchantLocationType nearest = null;

    for (MerchantLocationType loc : mLocations) {
      Location.distanceBetween(latitude, longitude, loc.latitude, loc.longitude, results);
      if (results[0] < distance) {
        distance = results[0];
        nearest = loc;
      }
    }

    mNearestLocation = nearest;
    mDistanceToNearest = distance;
  }

  public float getDistance() {
    return mDistanceToNearest;
  }

  public MerchantLocationType get() {
    return mNearestLocation;
  }

  public String getMerchantName() {
    return mMerchantName;
  }

  public static void sortByDistance(ArrayList<MerchantLocationType> locations, double latitude,
      double longitude) {
    float[] results = new float[1];

    final HashMap<MerchantLocationType, Float> distances =
        new HashMap<MerchantLocationType, Float>(2 * locations.size());
    for (MerchantLocationType l : locations) {
      Location.distanceBetween(latitude, longitude, l.latitude, l.longitude, results);
      distances.put(l, results[0]);
    }

    Collections.sort(locations, new Comparator<MerchantLocationType>() {
      @Override
      public int compare(MerchantLocationType lhs, MerchantLocationType rhs) {
        return Float.compare(distances.get(lhs), distances.get(rhs));
      }
    });
  }

}
