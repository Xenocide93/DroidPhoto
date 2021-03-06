package com.droidsans.photo.droidphoto.util.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.droidsans.photo.droidphoto.BrowseModelActivity;
import com.droidsans.photo.droidphoto.BrowseVendorActivity;
import com.droidsans.photo.droidphoto.MainActivity;
import com.droidsans.photo.droidphoto.ModelViewerActivity;
import com.droidsans.photo.droidphoto.R;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.view.FontTextView;

import org.json.JSONException;
import org.json.JSONObject;

public class ModelListAdapter extends ArrayAdapter<String> {
    private int resourceLayout;
    private LayoutInflater inflater;
    private AppCompatActivity activity;
    private int vendorNum;

    public ModelListAdapter(Context context, int resource, String[] objects, int vendorNum, AppCompatActivity parentActivity) {
        super(context, resource, objects);
        this.resourceLayout = resource;
        this.activity = parentActivity;
        this.vendorNum = vendorNum;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemHolder holder;

        if(convertView==null){
            if(inflater==null){
                inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            row = inflater.inflate(resourceLayout, parent, false);
            holder = new ItemHolder(row);
            row.setTag(holder);
        } else {
            holder = (ItemHolder) row.getTag();
        }

        holder.showModelInfo.setVisibility(
                activity.getSharedPreferences(activity.getString(R.string.userdata), Context.MODE_PRIVATE)
                        .getInt(activity.getString(R.string.user_priviledge), 1) > 1? ImageView.VISIBLE: ImageView.GONE);
//        holder.showModelInfo.setVisibility(ImageView.GONE);

        holder.showModelInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String message = "Vendor: " + BrowseVendorActivity.vendorName[vendorNum] + "\n";
//                message += "Model: " + BrowseModelActivity.modelName[vendorNum][position] + "\n";
//                message += "DEVICE (OS): " + BrowseModelActivity.buildDevice[vendorNum][position] + "\n";
//                message += "MODEL (OS): " + BrowseModelActivity.buildModel[vendorNum][position] + "\n";
//                new AlertDialog.Builder(activity)
//                        .setTitle("Report Incorrect Model ?")
//                        .setMessage(message)
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
////                                final JSONObject send = new JSONObject();
////                                try {
////                                    send.put("retail_vendor", BrowseVendorActivity.vendorName[vendorNum]);
////                                    send.put("retail_model", BrowseModelActivity.modelName[vendorNum][position]);
////                                    send.put("build_device", BrowseModelActivity.buildDevice[vendorNum][position]);
////                                    send.put("build_model", BrowseModelActivity.buildModel[vendorNum][position]);
////                                    send.put("_event", "report_respond");
////                                } catch (JSONException e) {
////                                    e.printStackTrace();
////                                }
////                                GlobalSocket.globalEmit("device.report", send);
////
////                                Snackbar.make(activity.findViewById(R.id.model_name),
////                                        "report: " + BrowseVendorActivity.vendorName[vendorNum] + " " +
////                                                BrowseModelActivity.modelName[vendorNum][position] + " submitted", Snackbar.LENGTH_SHORT).show();
//                                Snackbar.make(activity.findViewById(R.id.model_name), "currently disabled due to severe displacement bug.", Snackbar.LENGTH_SHORT).show();
//                            }
//                        })
//                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                            }
//                        })
//                        .show();

                Intent info = new Intent(activity.getApplicationContext(), ModelViewerActivity.class);
                info.putExtra(BrowseVendorActivity.VENDOR_NUM, vendorNum);
                info.putExtra(BrowseModelActivity.MODEL_NUM, position);
                activity.startActivity(info);
            }
        });

        holder.modelName.setText(getItem(position));

        return row;
    }

    private class ItemHolder {
        FontTextView modelName;
        ImageView showModelInfo;
        public ItemHolder(View view){
            modelName = (FontTextView) view.findViewById(R.id.model_name);
            showModelInfo = (ImageView) view.findViewById(R.id.model_show_info);
        }
    }
}
