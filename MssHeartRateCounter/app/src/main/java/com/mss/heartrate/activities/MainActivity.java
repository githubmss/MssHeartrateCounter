package com.mss.heartrate.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mss.heartrate.ImageProcessing;
import com.mss.heartrate.R;
import com.mss.heartrate.SharedArrayList;
import com.mss.heartrate.services.GPSTracker;
import com.mss.heartrate.utils.AppPreferences;
import com.mss.heartrate.utils.Constants;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private static Timer timer = new Timer();
    Toolbar toolbar;
    private TimerTask mTask;
    private static int mGx;
    private static int mJ;
    private static CountDownTimer mCdTimer;
    private static double FLAG = 1;
    private Handler handler;
    static Handler mHandler;
    private String mTitle = "pulse";
    private XYSeries mSeries;
    private XYMultipleSeriesDataset mDataset;
    private GraphicalView chart;
    private XYMultipleSeriesRenderer mRenderer;
    private Context mContext;
    private int mAddX = -1;
    double mAddY;
    int aa = 1;
    int[] xv = new int[300];
    int[] yv = new int[300];
    int[] hua = new int[]{9, 10, 11, 12, 13, 14, 13, 12, 11, 10, 9, 8, 7, 6,
            7, 8, 9, 10, 11, 10, 10};

    private static final AtomicBoolean processing = new AtomicBoolean(false);

    private static SurfaceView preview = null;
    private static View image = null;
    private static SurfaceHolder previewHolder = null;

    private static Camera camera = null;
    // private static View image = null;
    private static TextView mTV_Heart_Rate = null;
    private static TextView mTV_Avg_Pixel_Values = null;
    private static TextView mTV_pulse = null;
    private static WakeLock wakeLock = null;
    private static int averageIndex = 0;
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];
    static int mbeatsAvg;
    private static TextView txtDate;
    private static TextView txtTime;
    private static TextView txtfinger;
    private static TextView txtDetect;
    private static ImageView imgHearton;
    private GPSTracker gps;
    private AppPreferences mSession;

    public static enum TYPE {
        GREEN, RED
    }

    ;

    private static TYPE currentType = TYPE.GREEN;

    public static TYPE getCurrent() {
        return currentType;
    }

    private static int beatsIndex = 0;
    private static final int beatsArraySize = 3;
    private static final int[] beatsArray = new int[beatsArraySize];
    private static double beats = 0;
    private static long startTime = 0;
    SharedArrayList SAl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        ImageView imgIcon = (ImageView) findViewById(R.id.mss);
        imgIcon.setImageResource(R.drawable.app_icon);
        initConfig();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.toolbar_title);
        mSession = new AppPreferences(this);
        title.setText("Heart Rate");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            gps = new GPSTracker(mContext, MainActivity.this);
            // Check if GPS enabled
            if (gps.canGetLocation()) {
                double latitude = gps.getLatitude();
                mSession.setPrefrenceString(Constants.CURRENT_LATITUDE, "" + latitude);
                double longitude = gps.getLongitude();
                mSession.setPrefrenceString(Constants.CURRENT_LONGITUDE, "" + longitude);
            } else {
                gps.showSettingsAlert();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    gps = new GPSTracker(mContext, MainActivity.this);
                    if (gps.canGetLocation()) {
                        double latitude = gps.getLatitude();
                        mSession.setPrefrenceString(Constants.CURRENT_LATITUDE, "" + latitude);
                        double longitude = gps.getLongitude();
                        mSession.setPrefrenceString(Constants.CURRENT_LONGITUDE, "" + longitude);
                    } else {
                        gps.showSettingsAlert();
                    }
                } else {
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.contact_us) {
            Intent intent = new Intent(MainActivity.this, ContactUsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.about_us) {
            Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings({"deprecation", "static-access"})
    private void initConfig() {

        mContext = getApplicationContext();
        new AppPreferences(getApplicationContext())
                .init(getApplicationContext());
        new SharedArrayList(getApplicationContext());

        AppPreferences.saveArray("Mylist", new ArrayList<Integer>());
        AppPreferences.setPrefrenceBoolean("Timertask", true);
        AppPreferences.setPrefrenceBoolean("fingerRemoved", false);
        SAl = new SharedArrayList(getApplicationContext());
        // Get here layout main interface, the following chart will draw in the
        // layout inside
        LinearLayout layout = (LinearLayout) findViewById(R.id.id_linearLayout_graph);
        // layout.setVisibility(View.GONE);
        // This class is used to place all points on the curve, is a collection
        // of points, draw curves based on these points
        mSeries = new XYSeries(mTitle);
        // Create an instance of a data set, the data set will be used to create
        // the chart
        mDataset = new XYMultipleSeriesDataset();
        // Add this dataset to set point
        mDataset.addSeries(mSeries);
        // The following are the curves and style attributes, etc. settings,
        // mRenderer to do the equivalent of a chart used to handle rendering
        int color = Color.WHITE;
        PointStyle style = PointStyle.CIRCLE;
        mRenderer = buildRenderer(color, style, true);
        // Set the graph style
        setChartSettings(mRenderer, "X", "Y", 0, 300, 4, 16, Color.WHITE,
                Color.WHITE);

        // Generate charts
        chart = ChartFactory.getLineChartView(mContext, mDataset, mRenderer);

        // Add to the layout to chart
        layout.addView(chart, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        // Intent ii = new Intent(MainActivity.this,ShowBPMActivity.class);
        // Handler instances where the Timer with the following examples,
        // complete features regularly updated chart

        preview = (SurfaceView) findViewById(R.id.id_preview);
        // preview.setVisibility(View.GONE);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Typeface myTypeface = Typeface.createFromAsset(this.getAssets(),
                "digital.ttf");
        //	mTV_Heart_Rate = (TextView) findViewById(R.id.id_tv_heart_rate);
        //	mTV_Heart_Rate.setTypeface(myTypeface);
        //	mTV_Avg_Pixel_Values = (TextView) findViewById(R.id.id_tv_Avg_Pixel_Values);
        //	mTV_pulse = (TextView) findViewById(R.id.id_tv_pulse);
        //	image = findViewById(R.id.image);
        txtDate = (TextView) findViewById(R.id.txt_date);
        txtTime = (TextView) findViewById(R.id.txt_time);
        txtfinger = (TextView) findViewById(R.id.txt_placefinger);
        txtDetect = (TextView) findViewById(R.id.txt_detect);
        imgHearton = (ImageView) findViewById(R.id.img_hearton_off);
        txtDetect.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(Color.parseColor("#7280ce"));
        }

        setdate();
        runonUIthread();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm
                .newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                updateChart();

                super.handleMessage(msg);
            }
        };

        mTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }

        };

        timer.schedule(mTask, 1, 20);
        // Get SurfaceView control

        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {

                Showbpm(mbeatsAvg);
                super.handleMessage(msg);
            }

        };
    }


    // Curve

    @Override
    protected void onStop() {

        super.onStop();
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();

        if (mCdTimer != null) {

            mCdTimer.cancel();
        }

    }

    @Override
    public void onDestroy() {
        // Timer switch off when the program ends
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();

        if (mCdTimer != null) {

            mCdTimer.cancel();
        }

        super.onDestroy();
    }

    ;

    /**
     * Create a chart
     */
    protected XYMultipleSeriesRenderer buildRenderer(int color,
                                                     PointStyle style, boolean fill) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        // Set the chart curve itself styles, including color, point size and
        // line thickness, etc.
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(Color.WHITE);
        r.setLineWidth(4);
        renderer.addSeriesRenderer(r);
        return renderer;
    }

    protected void setChartSettings(XYMultipleSeriesRenderer renderer,
                                    String xTitle, String yTitle, double xMin, double xMax,
                                    double yMin, double yMax, int axesColor, int labelsColor) {
        // About rendering of the chart can be found in the documentation api
//		mRenderer.setChartTitle(mTitle);
//		mRenderer.setXTitle(xTitle);
//		mRenderer.setYTitle(yTitle);
//		mRenderer.setXAxisMin(xMin);
//		mRenderer.setXAxisMax(xMax);
//		mRenderer.setYAxisMin(yMin);
//		mRenderer.setYAxisMax(yMax);
//		mRenderer.setAxesColor(axesColor);
//		mRenderer.setLabelsColor(labelsColor);
//		mRenderer.setShowGrid(true);
//
//		mRenderer.setGridColor(R.color.gridcolor);
//		mRenderer.setXLabels(20);
//		mRenderer.setYLabels(10);
//		mRenderer.setXTitle("Time");
//		mRenderer.setYTitle("mmHg");
//		mRenderer.setYLabelsAlign(Align.RIGHT);
//		mRenderer.setPointSize((float) 3);
//		mRenderer.setShowLegend(false);
        ///////////////////////////////////////////////////


        renderer.setChartTitle(mTitle);
        renderer.setXTitle("");
        renderer.setYTitle("");
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(R.color.gridcolor);
        renderer.setLabelsColor(R.color.gridcolor);
        renderer.setShowGrid(false);

        renderer.setGridColor(R.color.gridcolor);
        renderer.setXLabels(0);
        renderer.setYLabels(0);
        renderer.setXTitle("");
        renderer.setYTitle("");
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setPointSize((float) 3);
        renderer.setShowLegend(false);
    }

    /**
     * Information update icon
     */
    private void updateChart() {
        // Set up the need to increase next node
        if (FLAG == 1) {
            mAddY = 10;
        } else {
            FLAG = 1;
            if (mGx < 200) {
                if (hua[20] > 1) {


                    txtfinger.setText("Please Place Your Finger on The Camera lens");
//					Toast toast = Toast
//							.makeText(
//									MainActivity.this,
//									"Please use your fingertips to cover the camera lens!",
//									Toast.LENGTH_SHORT);
//					toast.setGravity(Gravity.TOP, 0, 0);
//					toast.show();


                    if (timer != null) {
                        timer.cancel();
                    }
                    timer = new Timer();
                    if (mCdTimer != null) {

                        mCdTimer.cancel();
                    }

                    AppPreferences.setPrefrenceBoolean("Timertask", true);
                    AppPreferences.setPrefrenceBoolean("fingerRemoved", true);
                    //	mTV_Heart_Rate.setText("Time Reamining :- ");
                    // mCdTimer.cancel();

                    hua[20] = 0;
                }
                hua[20]++;
                return;
            } else {
                hua[20] = 10;
                AppPreferences.setPrefrenceBoolean("fingerRemoved", false);
            }
            mJ = 0;
        }
        if (mJ < 20) {
            mAddY = hua[mJ];
            mJ++;
        }

        // Remove the old data set point set
        mDataset.removeSeries(mSeries);

        // Determine the current point of focus in the end the number of points,
        // because the screen can only accommodate a total of 100, so when more
        // than 100 points, the length is always 100
        int length = mSeries.getItemCount();
        int bz = 0;
        // mAddX = length;
        if (length > 300) {
            length = 300;
            bz = 1;
        }
        mAddX = length;
        // The value of the old centralized point x and y taken out into the
        // backup, and the value of x plus 1, causing the curve to the right
        // panning effect
        for (int i = 0; i < length; i++) {
            xv[i] = (int) mSeries.getX(i) - bz;
            yv[i] = (int) mSeries.getY(i);
        }

        // Set of points to empty, in order to make a new set of points and
        // prepare
        mSeries.clear();
        mDataset.addSeries(mSeries);
        // The first point of the new generation of the focus point is added,
        // and then a mSeries of points in the coordinate loop body are converted
        // to rejoin the focus point
        // Here you can experiment with the order reversed what effect, which is
        // to run the loop body, and then add a new generation of point
        mSeries.add(mAddX, mAddY);
        for (int k = 0; k < length; k++) {
            mSeries.add(xv[k], yv[k]);
        }
        // Add a new set of points in the dataset
        // mDataset.addSeries(mSeries);

        // Update the view, without this step, the curve will not render dynamic
        // If the main non-UI thread, call postInvalidate (), with particular
        // reference to api
        chart.invalidate();
    } // curve

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        camera = Camera.open();
        startTime = System.currentTimeMillis();
        timer = new Timer();

        // mCdTimer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        // timer.cancel();
        wakeLock.release();
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    /**
     * Camera preview method This method dynamically updated interface UI
     * features, Get parameter by mobile phone camera in real time to
     * dynamically calculate the average pixel value, the number of pulses so
     * that real-time dynamic calculation of heart rate.
     */
    private static PreviewCallback previewCallback = new PreviewCallback() {
        int timecount = 10;

        public void onPreviewFrame(byte[] data, Camera cam) {
            if (data == null) {
                throw new NullPointerException();
            }
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) {
                throw new NullPointerException();
            }
            if (!processing.compareAndSet(false, true)) {
                return;
            }
            int width = size.width;
            int height = size.height;

            // Image Processing
            int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(),
                    height, width);
            mGx = imgAvg;
            // ///////////////////////*************************************/////////////////////////
            // mTV_Avg_Pixel_Values.setText("Avg RGB value"
            // + String.valueOf(imgAvg));
            // //////////////////=======******************==========//////////////////////////////////////
            if (imgAvg == 0 || imgAvg == 255) {
                processing.set(false);
                return;
            }
            // Calculate the average
            int averageArrayAvg = 0;
            int averageArrayCnt = 0;
            for (int i = 0; i < averageArray.length; i++) {
                if (averageArray[i] > 0) {
                    averageArrayAvg += averageArray[i];
                    averageArrayCnt++;
                }
            }

            // Calculate the average
            int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt)
                    : 0;
            TYPE newType = currentType;
            if (imgAvg < rollingAverage) {
                newType = TYPE.RED;
                if (newType != currentType) {
                    beats++;
                    FLAG = 0;
                    //	mTV_pulse.setText("Pulse" + String.valueOf(beats));
                }
            } else if (imgAvg > rollingAverage) {
                newType = TYPE.GREEN;
            }

            if (averageIndex == averageArraySize) {
                averageIndex = 0;
            }
            averageArray[averageIndex] = imgAvg;
            averageIndex++;

            if (newType != currentType) {
                currentType = newType;
            }

            // Get the system end time (ms)
            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - startTime) / 1000d;
            if (totalTimeInSecs >= 2) {
                double bps = (beats / totalTimeInSecs);
                int dpm = (int) (bps * 60d);
                if (dpm < 30 || dpm > 180 || imgAvg < 200) {
                    // Get the system start time (ms)
                    startTime = System.currentTimeMillis();
                    // Beats Total heartbeat
                    beats = 0;
                    processing.set(false);
                    return;
                }

                if (beatsIndex == beatsArraySize) {
                    beatsIndex = 0;
                }
                beatsArray[beatsIndex] = dpm;
                beatsIndex++;

                int beatsArrayAvg = 0;
                int beatsArrayCnt = 0;
                for (int i = 0; i < beatsArray.length; i++) {
                    if (beatsArray[i] > 0) {
                        beatsArrayAvg += beatsArray[i];
                        beatsArrayCnt++;
                    }
                }
                final int beatsAvg = (beatsArrayAvg / beatsArrayCnt);
                // mTV_Heart_Rate.setText("HeartRate...."+String.valueOf(beatsAvg)
                // +
                // "  ֵ:" + String.valueOf(beatsArray.length) +
                // "    " + String.valueOf(beatsIndex) +
                // "    " + String.valueOf(beatsArrayAvg) +
                // "    " + String.valueOf(beatsArrayCnt));
                // ////////////////********************************************///////////////////////////////////
                // mTV_Heart_Rate.setText("HeartRate in bpm :-"
                // + String.valueOf(beatsAvg));
                // /////////////////////////************************************//////////////////////////////////
                // ArrayList<Integer> beat = new ArrayList<Integer>();

                //	txtfinger.setText("Detecting...");
                txtDetect.setVisibility(View.GONE);
                ArrayList<Integer> sharedArrayList = AppPreferences
                        .getArray("Mylist");
                sharedArrayList.add(beatsAvg);

                // beat.add(beatsAvg);

                AppPreferences.setPreferenceRelaod("Beatavg", beatsAvg);
                AppPreferences.saveArray("Mylist", sharedArrayList);
                // setPreferenceNotificationCount("Beatavg", beatsAvg,
                // MainActivity.this);

                if (mGx > 200) {
                    txtDetect.setVisibility(View.VISIBLE);
                    txtDetect.setText("Measuring...");
                    if (AppPreferences.getPrefrenceBoolean("Timertask") == true) {

                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                mbeatsAvg = beatsAvg;
                                Message message = new Message();
                                message.what = 1;
                                mHandler.sendMessage(message);
                            }
                        };

                        timer.schedule(task, 10000);

                        mCdTimer = new CountDownTimer(11000, 1000) {

                            @Override
                            public void onTick(long millisUntilFinished) {

                                if (AppPreferences
                                        .getPrefrenceBoolean("fingerRemoved") == true) {

                                    timecount = 10;
                                    txtDetect.setVisibility(View.GONE);
                                    AppPreferences.setPrefrenceBoolean(
                                            "fingerRemoved", false);
                                }

                                if (mGx > 200) {

                                    Log.e("Pakistan: ", timecount + "");

                                    if (timecount > 0) {
//										mTV_Heart_Rate
//												.setText("Time Reamining : "
//														+ timecount + " sec");

                                        txtfinger.setText("Time Reamining : " + timecount + " sec");
                                    } else {
                                        timecount = 10;
//										mTV_Heart_Rate
//												.setText("Time Reamining : "
//														+ timecount + " sec");
                                        txtfinger.setText("Time Reamining : " + timecount + " sec");
                                        txtDetect.setVisibility(View.GONE);
                                    }
                                    timecount--;

                                } else {

                                    timecount = 10;
                                    txtfinger.setText("Time Reamining : " + timecount + " sec");
                                    txtDetect.setVisibility(View.GONE);
//									mTV_Heart_Rate.setText("Time Reamining : "
//											+ timecount + " sec");
                                    if (timer != null) {
                                        timer.cancel();
                                    }
                                    timer = new Timer();
                                }
                            }

                            @Override
                            public void onFinish() {
                                timecount = 10;
                            }
                        };
                        mCdTimer.start();

                        AppPreferences.setPrefrenceBoolean("Timertask", false);

                    }

                }

                startTime = System.currentTimeMillis();
                beats = 0;
            }
            processing.set(false);
        }
    };

    /**
     * Preview callback interface
     */
    private static SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Exception t) {
//				Log.e("PreviewDemo-surfaceCallback",
//						"Exception in setPreviewDisplay()", t);
            }
        }

        // When the preview time for a change this callback method
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    /**
     * Get a preview of the smallest size camera
     */
    private static Camera.Size getSmallestPreviewSize(int width, int height,
                                                      Camera.Parameters parameters) {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea < resultArea) {
                        result = size;
                    }
                }
            }
        }
        return result;
    }

    void Showbpm(int mbeatsAvg2) {

        String value = String.valueOf(mbeatsAvg2);
        System.out.println("" + value);
        Intent ii = new Intent(MainActivity.this, ShowBPMActivity.class);
        ii.putExtra("Beatavg", value);

        startActivity(ii);
        finish();
    }

    static void setPreferenceNotificationCount(String key, int value,
                                               Context context) {

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    static int getPreferenceNotificationCount(String key, int defaultValue,
                                              Context context) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getInt(key, defaultValue);
    }

    private void runonUIthread() {

        new Thread() {
            @Override
            public void run() {
                super.run();

                while (true) {

                    try {

                        runOnUiThread(new Runnable() {
                            public void run() {

                                if (aa == 1) {

                                    imgHearton.setImageResource(R.drawable.heartbeatoff);
                                    aa = 0;

                                } else if (aa == 0) {

                                    imgHearton.setImageResource(R.drawable.heartbeaton);
                                    aa = 1;
                                }

                            }
                        });
                        Thread.sleep(300);
                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                }

            }

        }.start();

    }


    public void setdate() {

        Calendar cal = Calendar.getInstance();

        int Month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int year = cal.get(Calendar.YEAR);

        int hour = cal.get(Calendar.HOUR);
        int min = cal.get(Calendar.MINUTE);
        int amPm = cal.get(Calendar.AM_PM);

        String AMPM = getamPm(amPm);


        String Monthcurrent = getmonth(Month);


        txtDate.setText(Monthcurrent + " " + day + ", " + year + "");
        txtTime.setText(hour + " : " + min + " " + AMPM + "");


    }

    public String getmonth(int month) {
        String MMonth = null;

        if (month == 1) {

            MMonth = "January";
        } else if (month == 2) {

            MMonth = "February";
        } else if (month == 3) {

            MMonth = "March";
        } else if (month == 4) {
            MMonth = "April";

        } else if (month == 5) {
            MMonth = "May";
        } else if (month == 6) {
            MMonth = "June";
        } else if (month == 7) {
            MMonth = "July";
        } else if (month == 8) {
            MMonth = "August";
        } else if (month == 9) {
            MMonth = "September";
        } else if (month == 10) {

            MMonth = "October";
        } else if (month == 11) {
            MMonth = "November";
        } else if (month == 12) {
            MMonth = "December";
        } else {
            MMonth = "";
        }


        return MMonth;

    }

    public String getamPm(int ampm) {
        String AMPM = null;
        if (ampm == 0) {

            AMPM = "am";
        } else if (ampm == 1) {
            AMPM = "pm";
        } else {
            AMPM = "";
        }

        return AMPM;
    }
}