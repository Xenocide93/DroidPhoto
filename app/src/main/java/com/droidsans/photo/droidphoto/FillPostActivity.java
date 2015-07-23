package com.droidsans.photo.droidphoto;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.droidsans.photo.droidphoto.util.retrofit.CountingTypedFile;
import com.droidsans.photo.droidphoto.util.Devices;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.retrofit.PostService;
import com.droidsans.photo.droidphoto.util.retrofit.ProgressListener;
import com.droidsans.photo.droidphoto.util.retrofit.UploadResponseModel;
import com.github.nkzawa.emitter.Emitter;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class FillPostActivity extends AppCompatActivity {

    Map<String, String> states;
    Map<String, String> mapLocale;

    private ImageView photo;
    private Bitmap imageBitmap;
    private EditText caption, vendor, model;
    private Button uploadBtn;
    private CheckBox isEnhanced, isAccept, useLocation;
    private ExifInterface mExif;
    private String mCurrentPhotoPath;
    private String mImageFrom;
    private static String postURL = "/photo";
    private static int MAX_THUMBNAIL_SIZE = 500;

    Metadata metadata;
    ExifSubIFDDirectory exifDirectory;
    ExifIFD0Directory orientationDirectory;
    GpsDirectory gpsDirectory;

    private boolean isResolvedVendor = false;
    private boolean isResolvedModel = false;
    private String outputVendor, outputModel;

    private Toolbar toolbar;

    private Handler delayAction = new Handler();

    private Runnable loopCheckForValidGps;
    private int loopCount = 0;
    private LocationListener locationListener;
    private String resolvedLocation;
    private String resolvedLocalizedLocation;
    LocationManager locationManager;

    private Double gpsLat;
    private Double gpsLong;
    private float gpsAcc = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_post);

        initialize();
    }

    private void initStateHashMap() {
        //REF: http://stackoverflow.com/questions/11005751/is-there-a-util-to-convert-us-state-name-to-state-code-eg-arizona-to-az
        states = new HashMap<>();
        states.put("Alabama","AL");
        states.put("Alaska","AK");
        states.put("Alberta","AB");
        states.put("American Samoa","AS");
        states.put("Arizona","AZ");
        states.put("Arkansas","AR");
        states.put("Armed Forces (AE)","AE");
        states.put("Armed Forces Americas","AA");
        states.put("Armed Forces Pacific","AP");
        states.put("British Columbia","BC");
        states.put("California","CA");
        states.put("Colorado","CO");
        states.put("Connecticut","CT");
        states.put("Delaware","DE");
        states.put("District Of Columbia","DC");
        states.put("Florida","FL");
        states.put("Georgia","GA");
        states.put("Guam","GU");
        states.put("Hawaii","HI");
        states.put("Idaho","ID");
        states.put("Illinois","IL");
        states.put("Indiana","IN");
        states.put("Iowa","IA");
        states.put("Kansas","KS");
        states.put("Kentucky","KY");
        states.put("Louisiana","LA");
        states.put("Maine","ME");
        states.put("Manitoba","MB");
        states.put("Maryland","MD");
        states.put("Massachusetts","MA");
        states.put("Michigan","MI");
        states.put("Minnesota","MN");
        states.put("Mississippi","MS");
        states.put("Missouri","MO");
        states.put("Montana","MT");
        states.put("Nebraska","NE");
        states.put("Nevada","NV");
        states.put("New Brunswick","NB");
        states.put("New Hampshire","NH");
        states.put("New Jersey","NJ");
        states.put("New Mexico","NM");
        states.put("New York","NY");
        states.put("Newfoundland","NF");
        states.put("North Carolina","NC");
        states.put("North Dakota","ND");
        states.put("Northwest Territories","NT");
        states.put("Nova Scotia","NS");
        states.put("Nunavut","NU");
        states.put("Ohio","OH");
        states.put("Oklahoma","OK");
        states.put("Ontario","ON");
        states.put("Oregon","OR");
        states.put("Pennsylvania","PA");
        states.put("Prince Edward Island","PE");
        states.put("Puerto Rico","PR");
        states.put("Quebec","PQ");
        states.put("Rhode Island","RI");
        states.put("Saskatchewan","SK");
        states.put("South Carolina","SC");
        states.put("South Dakota","SD");
        states.put("Tennessee","TN");
        states.put("Texas","TX");
        states.put("Utah","UT");
        states.put("Vermont","VT");
        states.put("Virgin Islands","VI");
        states.put("Virginia","VA");
        states.put("Washington","WA");
        states.put("West Virginia","WV");
        states.put("Wisconsin","WI");
        states.put("Wyoming","WY");
        states.put("Yukon Territory","YT");
    }

    private void initMapLocaleHashMap() {
        //GOD REFERENCE: http://wiki.openstreetmap.org/wiki/Multilingual_names <3
        //subtag REFERENCE: http://www.iana.org/assignments/language-subtag-registry/language-subtag-registry

        mapLocale = new HashMap<>();
        mapLocale.put("BY", "ru"); //Belarus -> russian
//        mapLocale.put("BE", "multi"); //Belgium -> ??? || Dutch is spoken in Flanders, French in Wallonia, German in the East-Kantons. | Brussel : French - Dutch
        mapLocale.put("BG", "bg"); //Bulgaria -> Bulgarian
        mapLocale.put("CN", "zh"); //China -> Chinese
        mapLocale.put("HR", "hr"); //Croatia -> Croatian
        mapLocale.put("FI", "fi"); //Finland -> Finnish
        mapLocale.put("FR", "fr"); //France -> French
//        mapLocale.put("FR", "multi"); //France -> ??? || br (Breton), ca (Catalan), co (Corsican), oc (Occitan), eu (Euskara), vls (West Flemish)
        mapLocale.put("DE", "de"); //Germany -> German
        mapLocale.put("GR", "el"); //Greece -> Greek
        mapLocale.put("HT", "fr"); //Haiti -> French
        mapLocale.put("HK", "zh"); //Hong Kong -> Chinese
        mapLocale.put("IR", "fa"); //Iran -> Persian
        mapLocale.put("IE", "ga"); //Ireland -> Irish
        mapLocale.put("IM", "gv"); //Isle of Man -> Manx
//        mapLocale.put("IT", "multi"); //Italy -> ???
        mapLocale.put("JP", "ja"); //Japan -> Japanese
        mapLocale.put("KR", "ko"); //Korea -> Korean
//        mapLocale.put("LB", "ar"); //Lebanon -> Arabic
        mapLocale.put("LB", "fr"); //Lebanon -> French
        mapLocale.put("LU", "fr"); //Luxembourg -> French
//        mapLocale.put("MA", "ar"); //Morocco -> Arabic
        mapLocale.put("MA", "fr"); //Morocco -> French
        mapLocale.put("RO", "ro"); //Romania -> Romanian
        mapLocale.put("RU", "ru"); //Russian Federation -> Russian
        mapLocale.put("RS", "sr"); //Serbia -> Serbian
        mapLocale.put("SK", "sk"); //Slovakia -> Slovak
//        mapLocale.put("ES", "multi") //Spain -> ??? || Català (ca), Galego (ga) and Euskera (eu)
//        mapLocale.put("CH", "multi"); //Switzerland -> ??? || German (de), French (fr), Italian(it)
        mapLocale.put("TW", "zh"); //Taiwan -> Chinese
        mapLocale.put("TH", "th"); //Thailand -> Thai
        mapLocale.put("TN", "ar"); //Tunisia -> Arabic
        mapLocale.put("TN", "fr"); //Tunisia -> French
        mapLocale.put("UA", "uk"); //Ukraine -> Ukrainian
        mapLocale.put("UZ", "uz"); //Uzbekistan -> Uzbek (uz-Latn)
    }

    private void initialize() {
        findAllById();
        initStateHashMap();
        initMapLocaleHashMap();
        getStringExtra();
        setupToolbar();
        setupListener();
        setDefaultUseLocationText();
        applyUserSettings();
        setThumbnailImage();
        setVendorAndModel();
    }

    private void applyUserSettings() {
        useLocation.setChecked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.settings_use_location), true));
    }

    private void getStringExtra() {
        Intent previousIntent = getIntent();
        mCurrentPhotoPath = previousIntent.getStringExtra("photoPath");
        mImageFrom = previousIntent.getStringExtra("imageFrom");
    }

    private void setDefaultUseLocationText() {
        switch (mImageFrom) {
            case "Picture Picker":
                useLocation.setText(getString(R.string.fill_post_checkbox_use_exif));
                break;
            case "Camera":
                useLocation.setText(getString(R.string.fill_post_checkbox_use_gps));
                break;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setVendorAndModel(){
        resolveDeviceVendorModel();
        vendor.setText(outputVendor);
        model.setText(outputModel);
    }

    private void resolveDeviceVendorModel(){
        File file = new File(getApplicationContext().getExternalFilesDir(null), getString(R.string.csvFileName));
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            reader.readLine(); //

            while ((line = reader.readLine()) != null) {
                String[] csv = line.split(",");
                if(csv[1].trim().contains(Build.MODEL)){
                    isResolvedVendor = true;
                    outputVendor = csv[2].trim();
                    if (csv[0].trim().contains(Build.DEVICE)){
                        isResolvedModel = true;
                        outputModel = csv[3].trim();
                        break;
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(!isResolvedModel){
            outputModel = Build.MODEL;
            Log.d("droidphoto", "not resolve model: " + outputModel);
        } else {
            Log.d("droidphoto", "do resolve model: " + outputModel);
        }
        if(!isResolvedVendor){
            outputVendor = Build.DEVICE;
            Log.d("droidphoto", "not resolve vendor: " + outputVendor);
        } else {
            Log.d("droidphoto", "do resolve vendor: " + outputVendor);
        }
    }

    private void setThumbnailImage() {
        try {
            metadata = ImageMetadataReader.readMetadata(new File(mCurrentPhotoPath));

            gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            orientationDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            mExif = new ExifInterface(mCurrentPhotoPath);
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(exifDirectory == null || orientationDirectory == null) {
//            Toast.makeText(getApplicationContext(), "Error: selected image must have exif", Toast.LENGTH_LONG).show();
//            Snackbar.make(null, "Error: selected image must have exif", Snackbar.LENGTH_LONG).show();
            Intent returnIntent = new Intent();
            returnIntent.putExtra("return code", "no exif");
            setResult(RESULT_CANCELED, returnIntent);
            finish();
            return;
        }

        if(mImageFrom.equals("Picture Picker")) {
//            if(!orientationDirectory.hasTagName(ExifIFD0Directory.TAG_MAKE) || !orientationDirectory.hasTagName(ExifIFD0Directory.TAG_MODEL)) {
//                Intent returnIntent = new Intent();
//                returnIntent.putExtra("return code", "cannot detect photo owner");
//                setResult(RESULT_CANCELED, returnIntent);
//                finish();
//            }
            Log.d("droidphoto", "IFD0 make: " + orientationDirectory.getString(ExifIFD0Directory.TAG_MAKE));
            Log.d("droidphoto", "IFD0 model: " + orientationDirectory.getString(ExifIFD0Directory.TAG_MODEL));
            Log.d("droidphoto", "phone make: " + Build.MANUFACTURER);
            Log.d("droidphoto", "phone model: " + Build.MODEL);

            if(!orientationDirectory.getString(ExifIFD0Directory.TAG_MAKE).trim().equalsIgnoreCase(Build.MANUFACTURER.trim()) ||
                    !Build.MODEL.toLowerCase().trim().contains(orientationDirectory.getString(ExifIFD0Directory.TAG_MODEL).toLowerCase().trim())) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("return code", "not your photo");
                setResult(RESULT_CANCELED, returnIntent);
                finish();
                return;
            }
        }
//        Log.d("droidphoto","subIFD make: " + exifDirectory.getString(ExifSubIFDDirectory.TAG_MAKE));
//        Log.d("droidphoto","subIFD model: " + exifDirectory.getString(ExifSubIFDDirectory.TAG_MODEL));
//        Log.d("droidphoto", "aperture (drewnoakes) :" + exifDirectory.getString(ExifSubIFDDirectory.TAG_APERTURE) + " | max : " + exifDirectory.getString(ExifSubIFDDirectory.TAG_MAX_APERTURE));
//        Log.d("droidphoto", "aperture (exifint.) :" + mExif.getAttribute(ExifInterface.TAG_APERTURE));

        Glide.with(getApplicationContext())
                .load(mCurrentPhotoPath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(photo);

        /*
        boolean isSampledDown = false;
        //get width/height without load bitmap into memory
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, opt);

        //downsamples
        if(opt.outWidth > 5000 || opt.outHeight > 5000) {
            opt.inSampleSize = 4;
            isSampledDown = true;
        } else if(opt.outWidth > 2500 || opt.outHeight > 2500) {
            opt.inSampleSize = 2;
            isSampledDown = true;
        }
        opt.inJustDecodeBounds = false;
        imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, opt);
        Log.d("droidphoto", "outwidth: " + opt.outWidth + "|outheight: " + opt.outHeight);
        Log.d("droidphoto", "file size: " + imageBitmap.getByteCount() + " bytes");

        if(isSampledDown) {
            boolean isNormal = false;
            android.graphics.Matrix matrix = new android.graphics.Matrix();

            //old (will be deprecated)
//            switch (mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)) {
//                case ExifInterface.ORIENTATION_NORMAL:
//                    isNormal = true;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    matrix.postRotate(90);
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    matrix.postRotate(180);
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    matrix.postRotate(270);
//                    break;
//                default:
//                    Log.d("droidphoto", "what!!?? NO ROTATION!!?");
//                    break;
//            }
            //new
            if(orientationDirectory != null && orientationDirectory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                try {
                    Log.d("droidphoto", "orientation:" + orientationDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION));
                    switch (orientationDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION)) {
                        case 1:                     isNormal = true;                        break; //ExifInterface.ORIENTATION_NORMAL
                        case 2:                                 matrix.postScale(-1, 1);    break; //ExifInterface.ORIENTATION_FLIP_HORIZONTAL
                        case 3:     matrix.postRotate(180);                                 break; //ExifInterface.ORIENTATION_ROTATE_180
                        case 4:     matrix.postRotate(180);     matrix.postScale(-1, 1);    break; //ExifInterface.ORIENTATION_FLIP_VERTICAL
                        case 5:     matrix.postRotate(90);      matrix.postScale(-1, 1);    break; //ExifInterface.ORIENTATION_TRANSPOSE
                        case 6:     matrix.postRotate(90);                                  break; //ExifInterface.ORIENTATION_ROTATE_90
                        case 7:     matrix.postRotate(270);     matrix.postScale(-1, 1);    break; //ExifInterface.ORIENTATION_TRANSVERSE
                        case 8:     matrix.postRotate(270);                                 break; //ExifInterface.ORIENTATION_ROTATE_270

                        default:
                            Log.d("droidphoto", "what!!?? NO ROTATION!!?");
                            break;
                    }
                } catch (MetadataException e) {
                    e.printStackTrace();
                }
                if(!isNormal) {
                    imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
                }
            }
        }
        photo.setImageBitmap(imageBitmap);
        */
    }

    private void reGeocode(Double lat, Double lng) {
//        if(false) { //debug
//            useLocation.setText("Location : " + resolvedLocation + " (cache) " + gpsLat + " " + gpsLong);
        if(resolvedLocation != null)  {
            useLocation.setText(getString(R.string.fill_post_checkbox_location_prefix) + resolvedLocation);
            if(resolvedLocalizedLocation != null) {
                useLocation.append(" (" + resolvedLocalizedLocation + ")");
            }
        } else {
            useLocation.setClickable(false);
            useLocation.setText(getString(R.string.fill_post_checkbox_location_resolving));
            Double[] params = {lat, lng};
            new reverseGeocode().execute(params);
        }
    }

    private void setLocation(Location l) {
        if(this.gpsAcc < l.getAccuracy()) {
            this.gpsLat = l.getLatitude();
            this.gpsLong = l.getLongitude();
            this.gpsAcc = l.getAccuracy();
        }
    }

    private void resetLocation() {
        this.gpsLat = null;
        this.gpsLong = null;
        this.gpsAcc = 0.0f;
        this.loopCount = 0;
    }

    private void initLoopForGps() {
        loopCheckForValidGps = new Runnable() {
            @Override
            public void run() {
                if(loopCount % 3 == 0) useLocation.setText("working .");
                else useLocation.append(".");
                loopCount++;
                if (loopCount < 60) {
//                    Log.d("droidphoto", "location loop (" + loopCount + "x) | gpsAcc = " + gpsAcc);
                    if (gpsAcc > 0) {
                        locationManager.removeUpdates(locationListener);
//                        Log.d("droidphoto", "location from manager:" + gpsLat + " " + gpsLong);
                        reGeocode(gpsLat, gpsLong);
                    } else {
                        startLoopForGps();
                    }
                } else {
                    useLocation.setChecked(false);
                    useLocation.setText(Html.fromHtml(getString(R.string.fill_post_checkbox_error_gps_timeout) + " <font color='" + getResources().getColor(R.color.link) + getString(R.string.fill_post_checkbox_location_retry)));
                }
            }
        };
    }

    private void startLoopForGps() {
        if(loopCheckForValidGps == null) initLoopForGps();
        delayAction.postDelayed(loopCheckForValidGps, 500);
    }

    private class reverseGeocode extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground(Double... params) {
            if(params[0] == null || params[1] == null) return "error getting location :(";
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.US);
            List<Address> addressList = null;
            try {
                addressList = geocoder.getFromLocation(params[0], params[1], 1);
            } catch (IOException e) {
                e.printStackTrace();
                return "error resolving location :(";
            }

            publishProgress();

            if(addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
//                Log.d("droidphoto", "reverse geocode: " + address.toString());
//                Log.d("droidphoto", "lat=" + params[0] + "|long=" + params[1]);

                resolvedLocation = ""; //init
                resolvedLocation += (address.getLocality() == null) ? "" : (address.getLocality() + ", ");
                if(address.getAdminArea() != null) {
                    if (address.getCountryCode().equals("US") || address.getCountryCode().equals("CA")) {
                        resolvedLocation += states.get(address.getAdminArea()) + ", ";
                    } else if (address.getCountryCode().equals("JP") && address.getAdminArea().contains("Prefecture")) {
                        resolvedLocation += address.getAdminArea().substring(0, address.getAdminArea().indexOf(" ")) + ", ";
                    } else {
                        resolvedLocation += address.getAdminArea() + ", ";
                    }
                }
                resolvedLocation += address.getCountryCode();

                publishProgress();

                //get locale only for countries in TH, JP, CN, KR, TW, SG, HK,

                String lc = mapLocale.get(address.getCountryCode());
                if(lc != null) {
                    publishProgress();
//                    Log.d("droidphoto", "locale : " + localized.toString());
                    geocoder = new Geocoder(getApplicationContext(), new Locale(lc, address.getCountryCode()));
                    address = null;
                    addressList = null;
                    try {
                        addressList = geocoder.getFromLocation(params[0], params[1], 1);
                    } catch (IOException e) {
                        e.printStackTrace();
//                        return resolvedLocation;
                        return "error resolving localized location :(";
                    }
                    publishProgress();

                    if(addressList != null && addressList.size() > 0) {
                        address = addressList.get(0);
//                        Log.d("droidphoto", "localized reverse geocode: " + address.toString());
//                        Log.d("droidphoto", "localized lat=" + params[0] + "|long=" + params[1]);

                        resolvedLocalizedLocation = "";
                        resolvedLocalizedLocation += (address.getLocality() == null)? "":address.getLocality() + ", ";
                        resolvedLocalizedLocation += (address.getAdminArea() == null)? "":address.getAdminArea() + ", ";
                        resolvedLocalizedLocation += address.getCountryName();

                        return resolvedLocation + " (" + resolvedLocalizedLocation + ")";
                    }
                }
                return resolvedLocation;
            }
            return "error no location found";
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            useLocation.append(".");
        }

        @Override
        protected void onPostExecute(String s) {
            switch (s) {
                case "error getting location :(":
                    useLocation.setChecked(false);
                    useLocation.setText(Html.fromHtml(getString(R.string.fill_post_checkbox_error_location_get) + " <font color='" + getResources().getColor(R.color.link) + getString(R.string.fill_post_checkbox_location_retry)));
                    useLocation.setClickable(true);
                    resetLocation();
                    break;
                case "error resolving location :(":
                    useLocation.setChecked(false);
                    useLocation.setText(Html.fromHtml(getString(R.string.fill_post_checkbox_error_location_resolve) + " <font color='" + getResources().getColor(R.color.link) + getString(R.string.fill_post_checkbox_location_retry)));
                    useLocation.setClickable(true);
                    resetLocation();
                    break;
                case "error resolving localized location :(":
                    useLocation.setChecked(false);
                    useLocation.setText(Html.fromHtml(getString(R.string.fill_post_checkbox_error_localize_resolve) + " <font color='" + getResources().getColor(R.color.link) + getString(R.string.fill_post_checkbox_location_retry)));
                    useLocation.setClickable(true);
                    resetLocation();
                    break;
                case "error no location found":
                    useLocation.setChecked(false);
                    useLocation.setText(getString(R.string.fill_post_checkbox_error_location_notfound));
                    useLocation.setTextColor(getResources().getColor(R.color.light_gray));
                    resetLocation();
                    useLocation.setEnabled(false);
                    break;
                default: //has location data
                    useLocation.setText(getString(R.string.fill_post_checkbox_location_prefix) + s);
                    useLocation.setClickable(true);
                    break;
            }
        }
    }

    private void setupListener() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setLocation(location);
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };

        useLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    //debug
//                    reGeocode(Double.parseDouble(vendor.getText().toString()),Double.parseDouble(model.getText().toString()));
                    //real

                    String toastText = "location checked | ";
                    //try read location from exif first
                    if(gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
//                    if(false) { //debug
                        Log.d("droidphoto", "location from exif:" + gpsDirectory.getGeoLocation().toString());
                        useLocation.setText("reading location from exif ...");
                        gpsLat = gpsDirectory.getGeoLocation().getLatitude();
                        gpsLong = gpsDirectory.getGeoLocation().getLongitude();
                        reGeocode(gpsLat, gpsLong);
                        toastText += "get location from exif";
//                    } else {
                    } else if(mImageFrom.equals("Camera")) {
                        if(gpsAcc > 0.0f) {
                            reGeocode(gpsLat, gpsLong);
                        } else {
                            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                                useLocation.setText("working");
                                Criteria locationCriteria = new Criteria();
                                locationCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
                                locationCriteria.setSpeedAccuracy(Criteria.ACCURACY_LOW);
                                locationCriteria.setHorizontalAccuracy(Criteria.ACCURACY_LOW);
                                locationCriteria.setVerticalAccuracy(Criteria.ACCURACY_LOW);
                                locationCriteria.setPowerRequirement(Criteria.POWER_LOW);
                                Log.d("droidphoto", "provider: " + locationManager.getBestProvider(locationCriteria, true));
                                locationManager.requestLocationUpdates(locationManager.getBestProvider(locationCriteria, true), 490, 0, locationListener);
//                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 490, 0, locationListener);
//                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 990, 0, locationListener);
                                startLoopForGps();
                                toastText += "get location from GPS";
                            } else {
                                useLocation.setText(getString(R.string.fill_post_checkbox_location_off));
                            }
                        }
                    } else {
                        useLocation.setText(getString(R.string.fill_post_checkbox_no_location_exif));
                        useLocation.setTextColor(getResources().getColor(R.color.light_gray));
                        useLocation.setChecked(false);
                        useLocation.setEnabled(false);
                    }

//                    Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(getApplicationContext(), "location unchecked", Toast.LENGTH_SHORT).show();
                    if(locationManager != null) {
                        delayAction.removeCallbacks(loopCheckForValidGps);
                        locationManager.removeUpdates(locationListener);
                        locationManager = null;
                        loopCount = 0;
                    }

                    setDefaultUseLocationText();
                }
            }
        });
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAccept.isChecked()) {
//                    Toast.makeText(getApplicationContext(), "Please accpet our term of service", Toast.LENGTH_LONG).show();
                    Snackbar.make(photo, getString(R.string.snackbar_fill_post_accept_tos), Snackbar.LENGTH_LONG).show();
                    return;
                }
//                if (mExif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME) == null ||
//                        mExif.getAttribute(ExifInterface.TAG_ISO) == null ||
//                        mExif.getAttribute(ExifInterface.TAG_APERTURE) == null) {
//                if(exifDirectory == null) {
//                    Toast.makeText(getApplicationContext(), "image has no exif", Toast.LENGTH_SHORT).show();
//                    return;
//                } else if(exifDirectory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME) == null) {
//                    Toast.makeText(getApplicationContext(), "image has no required exif", Toast.LENGTH_SHORT).show();
//                    return;
//                }


                new Thread(new Runnable() {
                    public void run() {
//                        Log.d("droidphoto", "uploading...");
                        //init value
                        FeedFragment.percentage = 0;
                        FeedFragment.isFailedToUpload = false;

                        //resize image save to cache
                        final File tempFile = new File(getCacheDir() + "/" + "uploadtemp.jpg");
                        final File originalFile = new File(mCurrentPhotoPath);
                        FileOutputStream out = null;
                        Bitmap bmp = null;
                        try {
                            InputStream in = new BufferedInputStream(new FileInputStream(originalFile));
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeStream(in, null, options);
                            if(options.outHeight > MAX_THUMBNAIL_SIZE || options.outWidth > MAX_THUMBNAIL_SIZE) {
                                // Calculate ratios of height and width to requested height and width
                                final int heightRatio = Math.round((float) options.outHeight / MAX_THUMBNAIL_SIZE);
                                final int widthRatio = Math.round((float) options.outWidth / MAX_THUMBNAIL_SIZE);

                                // Choose the smallest ratio as inSampleSize value, this will guarantee
                                // a final image with both dimensions larger than or equal to the
                                // requested height and width.
                                options.inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
                            }
                            Metadata metadata;
                            ExifIFD0Directory orientationDirectory = null;
                            try {
                                metadata = ImageMetadataReader.readMetadata(originalFile);
                                orientationDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                            } catch (ImageProcessingException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            android.graphics.Matrix matrix = new android.graphics.Matrix();
                            if(orientationDirectory != null && orientationDirectory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
//                            Log.d("droidphoto", "rotating...");
                                try {
                                    switch (orientationDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION)) {
                                        case 1:                                                             break; //ExifInterface.ORIENTATION_NORMAL
                                        case 2:                                 matrix.postScale(-1, 1);    break; //ExifInterface.ORIENTATION_FLIP_HORIZONTAL
                                        case 3:     matrix.postRotate(180);                                 break; //ExifInterface.ORIENTATION_ROTATE_180
                                        case 4:     matrix.postRotate(180);     matrix.postScale(-1, 1);    break; //ExifInterface.ORIENTATION_FLIP_VERTICAL
                                        case 5:     matrix.postRotate(90);      matrix.postScale(-1, 1);    break; //ExifInterface.ORIENTATION_TRANSPOSE
                                        case 6:     matrix.postRotate(90);                                  break; //ExifInterface.ORIENTATION_ROTATE_90
                                        case 7:     matrix.postRotate(270);     matrix.postScale(-1, 1);    break; //ExifInterface.ORIENTATION_TRANSVERSE
                                        case 8:     matrix.postRotate(270);                                 break; //ExifInterface.ORIENTATION_ROTATE_270

                                        default:
//                                        Log.d("droidphoto", "what!!?? NO ROTATION!!?");
                                            break;
                                    }
                                } catch (MetadataException e) {
                                    e.printStackTrace();
                                }
                            }

                            try {
                                in.close();
                                in = new BufferedInputStream(new FileInputStream(originalFile));
                                out = new FileOutputStream(tempFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            options.inJustDecodeBounds = false;
//                            Log.d("droidphoto", "has matrix");
//                            Bitmap.createBitmap(
//                                    BitmapFactory.decodeStream(in, null, options),
//                                    (options.outWidth > options.outHeight) ? ((options.outWidth / 2) - (options.outHeight / 2)):0,
//                                    (options.outWidth > options.outHeight) ? 0:(options.outHeight / 2 - options.outWidth / 2),
//                                    (options.outWidth > options.outHeight) ? options.outHeight:options.outWidth,
//                                    (options.outWidth > options.outHeight) ? options.outHeight:options.outWidth,
//                                    matrix,
//                                    true)
//                                    .compress(Bitmap.CompressFormat.JPEG, 80, out);
                            Bitmap.createScaledBitmap(Bitmap.createBitmap(
                                    BitmapFactory.decodeStream(in, null, options),
                                    (options.outWidth > options.outHeight) ? ((options.outWidth / 2) - (options.outHeight / 2)) : 0,
                                    (options.outWidth > options.outHeight) ? 0 : (options.outHeight / 2 - options.outWidth / 2),
                                    (options.outWidth > options.outHeight) ? options.outHeight : options.outWidth,
                                    (options.outWidth > options.outHeight) ? options.outHeight : options.outWidth,
                                    matrix, true), MAX_THUMBNAIL_SIZE, MAX_THUMBNAIL_SIZE, true).compress(Bitmap.CompressFormat.JPEG, 80, out);
                            if(out != null) {
                                try {
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        //create file
                        final long filesize = originalFile.length();

                        //create client
                        OkHttpClient okHttpClient = new OkHttpClient();
                        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);

                        //create rest adapter
                        RestAdapter restAdapter = new RestAdapter.Builder()
                                .setEndpoint(GlobalSocket.serverURL)
                                .setClient(new OkClient(okHttpClient))
                                .build();
                        PostService postService = restAdapter.create(PostService.class);
                        ProgressListener listener = new ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                FeedFragment.percentage = (int)((95 * num) / filesize);
                            }
                        };
                        postService.postPhoto(new CountingTypedFile("image/jpeg", originalFile, listener),
                                new TypedFile("image/jpeg", tempFile),
                                getSharedPreferences(getString(R.string.userdata), MODE_PRIVATE).getString(getString(R.string.token), ""),
                                new Callback<UploadResponseModel>() {
                                    @Override
                                    public void success(UploadResponseModel jsonObject, Response response) {
                                        tempFile.delete();
//                                        if(response.getStatus() == HttpURLConnection.HTTP_OK) {
//                                            Log.d("droidphoto", "success");
//                                            Log.d("droidphoto", "response body : " + response.getBody().toString());
//                                            Log.d("droidphoto", "jsonObject : " + jsonObject);
//                                        }
                                        try {
//                                            if (jsonObject.getBoolean("success")) {
                                            if(jsonObject.success) {
                                                final JSONObject photoDetailStuff = new JSONObject();
//                                                photoDetailStuff.put("photo_url", jsonObject.getString("filename"));
                                                photoDetailStuff.put("photo_url", jsonObject.filename);
                                                photoDetailStuff.put("caption", caption.getText().toString());
                                                photoDetailStuff.put("model", model.getText().toString());
                                                photoDetailStuff.put("vendor", vendor.getText().toString());
                                                photoDetailStuff.put("is_flash", exifDirectory.getString(ExifSubIFDDirectory.TAG_FLASH));
                                                photoDetailStuff.put("exp_time", exifDirectory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
                                                photoDetailStuff.put("tag_date", exifDirectory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
                                                photoDetailStuff.put("width", exifDirectory.getString(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH));
                                                photoDetailStuff.put("height", exifDirectory.getString(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT));
                                                photoDetailStuff.put("iso", exifDirectory.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
//                                           photoDetailStuff.put("aperture", exifDirectory.getString(ExifSubIFDDirectory.TAG_APERTURE));
                                                photoDetailStuff.put("aperture", mExif.getAttribute(ExifInterface.TAG_APERTURE)); //because drewnoakes' aperture sometimes missing
//                                            if(gpsDirectory != null) {
//                                                photoDetailStuff.put("gps_lat", gpsDirectory.getString(GpsDirectory.TAG_LATITUDE));
//                                                photoDetailStuff.put("gps_long", gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE));
//                                                photoDetailStuff.put("gps_lat_ref", gpsDirectory.getString(GpsDirectory.TAG_LATITUDE_REF));
//                                                photoDetailStuff.put("gps_long_ref", gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE_REF));
//                                            }
                                                if(resolvedLocation != null && useLocation.isChecked()) {
                                                    photoDetailStuff.put("gps_lat", gpsLat);
                                                    photoDetailStuff.put("gps_long", gpsLong);
                                                    photoDetailStuff.put("gps_location", resolvedLocation);
                                                    if(resolvedLocalizedLocation != null) {
                                                        photoDetailStuff.put("gps_localized", resolvedLocalizedLocation);
                                                    }
                                                }
                                                photoDetailStuff.put("_event", "photoupload_respond");
                                                photoDetailStuff.put("is_accept", isAccept.isChecked());
                                                photoDetailStuff.put("is_enhanced", isEnhanced.isChecked());

                                                FeedFragment.percentage = 98;
                                                if(!GlobalSocket.globalEmit("photo.upload", photoDetailStuff)) {
                                                    delayAction.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if(!GlobalSocket.globalEmit("photo.upload", photoDetailStuff)) {
                                                                //???
                                                                FeedFragment.isFailedToUpload = true;
//                                                                Toast.makeText(getApplicationContext(), "upload failed (on socket.io)", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }, 2000);
                                                }
                                            } else {
                                                FeedFragment.isFailedToUpload = true;
//                                                Toast.makeText(getApplicationContext(), "upload failed (on success check)", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        tempFile.delete();
                                        FeedFragment.isFailedToUpload = true;
                                        error.printStackTrace();
                                    }
                                });
/*
                        //old
                        /*
                        JSONObject respond = post(GlobalSocket.serverURL + postURL, mCurrentPhotoPath);
                        if(respond != null) {
                            Log.d("droidphoto", "respond: " + respond.toString());

                            try {
                                if (respond.getBoolean("success")) {
                                    final JSONObject photoDetailStuff = new JSONObject();
                                    photoDetailStuff.put("photo_url", respond.getString("filename"));
                                    photoDetailStuff.put("caption", caption.getText().toString());
                                    photoDetailStuff.put("model", model.getText().toString());
                                    photoDetailStuff.put("vendor", vendor.getText().toString());
                                    photoDetailStuff.put("is_flash", exifDirectory.getString(ExifSubIFDDirectory.TAG_FLASH));
                                    photoDetailStuff.put("exp_time", exifDirectory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
                                    photoDetailStuff.put("tag_date", exifDirectory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
                                    photoDetailStuff.put("width", exifDirectory.getString(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH));
                                    photoDetailStuff.put("height", exifDirectory.getString(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT));
                                    photoDetailStuff.put("iso", exifDirectory.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
//                                photoDetailStuff.put("aperture", exifDirectory.getString(ExifSubIFDDirectory.TAG_APERTURE));
                                    photoDetailStuff.put("aperture", mExif.getAttribute(ExifInterface.TAG_APERTURE)); //because drewnoakes' aperture sometimes missing
//                                if(gpsDirectory != null) {
//                                    photoDetailStuff.put("gps_lat", gpsDirectory.getString(GpsDirectory.TAG_LATITUDE));
//                                    photoDetailStuff.put("gps_long", gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE));
//                                    photoDetailStuff.put("gps_lat_ref", gpsDirectory.getString(GpsDirectory.TAG_LATITUDE_REF));
//                                    photoDetailStuff.put("gps_long_ref", gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE_REF));
//                                }
                                    if(resolvedLocation != null && useLocation.isChecked()) {
                                        photoDetailStuff.put("gps_lat", gpsLat);
                                        photoDetailStuff.put("gps_long", gpsLong);
                                        photoDetailStuff.put("gps_location", resolvedLocation);
                                        if(resolvedLocalizedLocation != null) {
                                            photoDetailStuff.put("gps_localized", resolvedLocalizedLocation);
                                        }
                                    }
                                    photoDetailStuff.put("_event", "photoupload_respond");
                                    photoDetailStuff.put("is_accept", isAccept.isChecked());
                                    photoDetailStuff.put("is_enhanced", isEnhanced.isChecked());

                                    if(!GlobalSocket.globalEmit("photo.upload", photoDetailStuff)) {
                                        delayAction.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(!GlobalSocket.globalEmit("photo.upload", photoDetailStuff)) {
                                                    //???
                                                }
                                            }
                                        }, 2000);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "upload failed", Toast.LENGTH_SHORT).show();
                        }
                        */
                    }
                }).start();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("caption", caption.getText().toString());
                returnIntent.putExtra("vendor", vendor.getText().toString());
                returnIntent.putExtra("model", model.getText().toString());
                returnIntent.putExtra("path", mCurrentPhotoPath);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        Emitter.Listener onPhotoUploadRespond = new Emitter.Listener() {
            @Override
            public void call(final Object[] args) {
                FillPostActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            if(data.getBoolean("success")){
                                Log.d("droidphoto", "upload success");
                                FeedFragment.percentage = 100;
                            } else {
                                FeedFragment.isFailedToUpload = true;
                                Log.d("droidphoto", "upload error: " + data.getString("msg"));
                            }
                        } catch (JSONException e) {
                            FeedFragment.isFailedToUpload = true;
                            e.printStackTrace();
                        }
                        finish();
                    }
                });
            }
        };

        if(!GlobalSocket.mSocket.hasListeners("photoupload_respond")){
            GlobalSocket.mSocket.on("photoupload_respond", onPhotoUploadRespond);
        }
    }
/*
    public JSONObject post(String url, String path) {
        byte[] data = fileToByteArray(path);
        String attachmentName = "image";
        String attachmentFileName = "image.jpg";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        HttpURLConnection httpUrlConnection = null;

        try {
            httpUrlConnection = (HttpURLConnection) (new URL(url)).openConnection();
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);

            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
            httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());
            //start request section
            request.writeBytes(twoHyphens + boundary + lineEnd);

            /////////////////////////////////////////////////////////////////////////////////////////////

            //body heading
            request.writeBytes("Content-Disposition: form-data; name=\"_token\"" + lineEnd);
            request.writeBytes("Content-Type: text/plain" + lineEnd);
            request.writeBytes("Content-Transfer-Encoding: base64" + lineEnd);

            //request body
//            request.writeBytes(lineEnd + GlobalSocket.getToken() + lineEnd);
            request.writeBytes(lineEnd + getSharedPreferences(getString(R.string.userdata), Context.MODE_PRIVATE).getString(getString(R.string.token), "") + lineEnd);

            //end of body section
            request.writeBytes(twoHyphens + boundary + lineEnd);

            /////////////////////////////////////////////////////////////////////////////////////////////

            //heading
            request.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + lineEnd);
            request.writeBytes("Content-Type: image/jpeg" + lineEnd);
            request.writeBytes("Content-Transfer-Encoding: base64" + lineEnd);

            //request file
            request.writeBytes(lineEnd);
            request.write(data);
            request.writeBytes(lineEnd);

            //end of file section
            request.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            request.flush();
            request.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        }

        //get respond
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream responseStream = new BufferedInputStream(httpUrlConnection.getInputStream());
            BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
            String line;
            while ((line = responseStreamReader.readLine()) != null)
            {
                stringBuilder.append(line).append("\n");
            }
            responseStreamReader.close();
//            httpUrlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            try {
                httpUrlConnection.disconnect();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        try {
            return new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
*/
    @Override
    protected void onDestroy() {
        if(imageBitmap != null) imageBitmap.recycle();
        super.onDestroy();
    }

    private byte[] fileToByteArray(String filePath){
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private void findAllById() {
        photo = (ImageView) findViewById(R.id.photo);
        caption = (EditText) findViewById(R.id.caption);
        vendor = (EditText) findViewById(R.id.vendor);
        model = (EditText) findViewById(R.id.model);
        uploadBtn = (Button) findViewById(R.id.upload_btn);
        isAccept = (CheckBox) findViewById(R.id.is_accept);
        isEnhanced = (CheckBox) findViewById(R.id.is_enhanced);
        useLocation = (CheckBox) findViewById(R.id.use_current_location);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fill_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
