package com.droidsans.photo.droidphoto;


import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.ReportPack;
import com.droidsans.photo.droidphoto.util.SpacesItemDecoration;
import com.droidsans.photo.droidphoto.util.adapter.ReportAdapter;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class EvaluateFragment extends Fragment {
    private RecyclerView evalulateRecyclerview;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Handler delayAction = new Handler();
    private ReportPack[] reportList;


    public EvaluateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_evaluate, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findAllById(getView());
        initialize();
    }

    private void initialize() {
        setupRecyclerview();
    }

    private void setupRecyclerview() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshEvaluate();
            }
        });

        evalulateRecyclerview.addItemDecoration(new SpacesItemDecoration(
                getActivity(),
                1,
                (int) getResources().getDimension(R.dimen.feed_recycleview_item_space),
                true, true, true, true
        ));

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false);
        evalulateRecyclerview.setLayoutManager(layoutManager);
        JSONObject data = new JSONObject();
        try {
            data.put("_event", "onGetEvaluateListRespond");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        GlobalSocket.globalEmit("device.getreportlist", data);

        //setup emitter  listener
        if(!GlobalSocket.mSocket.hasListeners("onGetEvaluateListRespond")){
            GlobalSocket.mSocket.on("onGetEvaluateListRespond", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    GlobalSocket.mSocket.off("onGetEvaluateListRespond");
                    JSONObject data = (JSONObject) args[0];
                    if(data.optBoolean("success")){
                        JSONArray reportArray = data.optJSONArray("reportList");
                        reportList = new ReportPack[reportArray.length()];
                        for(int i = 0; i < reportArray.length(); i++){
                            JSONObject reportItem = reportArray.optJSONObject(i);
                            switch (reportItem.optString("type")){
                                case "device":
                                    reportList[i] = new ReportPack(
                                            reportItem.optString("build_device"),
                                            reportItem.optString("build_model"),
                                            reportItem.optString("retail_vendor"),
                                            reportItem.optString("retail_model"),
                                            reportItem.optString("report_date"),
                                            reportItem.optInt("report_by")
                                    );
                                    break;
                                case "photo":
                                    reportList[i] = new ReportPack(
                                            reportItem.optInt("photo_id"),
                                            reportItem.optString("reason"),
                                            reportItem.optString("report_date"),
                                            reportItem.optBoolean("is_severe")
                                    );
                                    break;
                                case "user":
                                    reportList[i] = new ReportPack(
                                            reportItem.optInt("user_id"),
                                            reportItem.optString("reason"),
                                            reportItem.optString("report_date")
                                    );
                                    break;
                            }
                        }
                        ReportAdapter adapter = new ReportAdapter(getActivity(), reportList);
                        evalulateRecyclerview.setAdapter(adapter);
                    } else {
                        Snackbar.make(getView(), "Error: "+data.optString("msg"), Snackbar.LENGTH_SHORT ).show();
                    }
                }
            });
        }
    }

    private void refreshEvaluate(){
        delayAction.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 3000);
    }

    private void findAllById(View view){
        evalulateRecyclerview = (RecyclerView) view.findViewById(R.id.evaluate_recyclerview);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
    }

}
