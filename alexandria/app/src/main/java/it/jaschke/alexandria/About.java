package it.jaschke.alexandria;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class About extends Fragment {

    public About(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        try {
            String versionName = String.format(getString(R.string.version_by),
                    getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
            ((TextView) rootView.findViewById(R.id.version)).setText(versionName);
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
            return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.about);



    }

}
