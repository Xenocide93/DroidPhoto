package com.droidsans.photo.droidphoto;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {
    ListView mainList;
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_about, container, false);
        mainList = (ListView) rootView.findViewById(R.id.about_list);

        mainList.addHeaderView(((LayoutInflater)getActivity().getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_about_header, null, false));
//        mainList.setAdapter();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Snackbar.make(mainList, "position : " + position, Snackbar.LENGTH_SHORT).show();

                switch (position) {
                    case 1: //app info
                        startActivity(new Intent(getActivity().getApplicationContext(), PlaceholderActivity.class));
                        break;
                    case 2: //pp
                        startActivity(new Intent(getActivity().getApplicationContext(), PlaceholderActivity.class));
                        break;
                    case 3: //tos
                        startActivity(new Intent(getActivity().getApplicationContext(), PlaceholderActivity.class));
                        break;
                    case 4: //osl
                        startActivity(new Intent(getActivity().getApplicationContext(), OpenSourceLicensesActivity.class));
                        break;
                    case 5: //team
                        startActivity(new Intent(getActivity().getApplicationContext(), PlaceholderActivity.class));
                        break;
                }
            }
        });

    }

}
