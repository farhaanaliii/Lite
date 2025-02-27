package com.farhanali.lite.fragment;

import androidx.preference.PreferenceFragmentCompat;
import android.os.Bundle;
import androidx.preference.PreferenceScreen;
import com.farhanali.lite.R;

public class AboutFragment extends PreferenceFragmentCompat{
    @Override
    public void onCreatePreferences(Bundle savedInstance, String rootKey) {
        setPreferencesFromResource(R.xml.activity_about, rootKey);
    }
}
