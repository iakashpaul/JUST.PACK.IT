package com.akashpaul.justpackit;

import android.app.Activity;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.skjolber.packing.Box;
import com.github.skjolber.packing.BoxItem;
import com.github.skjolber.packing.Container;
import com.github.skjolber.packing.LargestAreaFitFirstPackager;
import com.github.skjolber.packing.Packager;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
//import com.github.skjolber.3d-bin-container-packing;

public class MeasureActivity extends Activity {
    private static final String TAG = MeasureActivity.class.getSimpleName();

    private TextView mTextView;
    private TextView mSideTextView;
    private TextView mBoxTextView;
    private GLSurfaceView mSurfaceView;
    private MainRenderer mRenderer;

    private boolean mUserRequestedInstall = true;

    private Session mSession;
    private Config mConfig;

    private List<float[]> mPoints = new ArrayList<float[]>();

    private float mLastX;
    private float mLastY;
    private boolean mPointAdded = false;

    public int counter=1;
    public double distance;
    public double side_length_1;
    public double side_length_2;
    public double side_length_3;

    public int lowest_index=-1;
    public int lowest_volume=0;

    public int width=0;
    public int depth=0;
    public int height=0;
    //save Check
    private Boolean isSaveClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarAndTitleBar();
        setContentView(R.layout.activity_measure);

        mSideTextView = (TextView) findViewById(R.id.side_txt_dist);
        mBoxTextView = (TextView) findViewById(R.id.box_txt_dist);

        mTextView = (TextView) findViewById(R.id.txt_dist);
        mSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);

//        Bin packing
        // initialization
//        List<Container> containers = new ArrayList<Container>();
////        containers.add(new Container("box",1, 100, 100, 10000)); // x y z and weight
//        containers.add(new Container("box",100, 100, 100, 10000)); // x y z and weight


//        boolean rotate3d = true;
//        Packager packager = new LargestAreaFitFirstPackager(containers, rotate3d, true, true);


//        products.add(new BoxItem(new Box("Leg", 4, 10, 1, 25), 1));
//        products.add(new BoxItem(new Box("Arm", 4, 10, 2, 50), 1));

// match a single container
//        Container match = packager.pack(products);
//        if(match != null){
//            Log.d("BIN_PACK", "Matches, "+match.getLevels().toString());
//        }
//        else{
//            Log.d("BIN_PACK", "No match found, ");
//        }




        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                    @Override
                public void onDisplayAdded(int displayId) {
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    synchronized (this) {
                        mRenderer.onDisplayChanged();
                    }
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                }
            }, null);
        }

        mRenderer = new MainRenderer(this, new MainRenderer.RenderCallback() {
            @Override
            public void preRender() throws CameraNotAvailableException {
                if (mRenderer.isViewportChanged()) {
                    Display display = getWindowManager().getDefaultDisplay();
                    int displayRotation = display.getRotation();
                    mRenderer.updateSession(mSession, displayRotation);
                }

                mSession.setCameraTextureName(mRenderer.getTextureId());

                Frame frame = mSession.update();
                if (frame.hasDisplayGeometryChanged()) {
                    mRenderer.transformDisplayGeometry(frame);
                }

                PointCloud pointCloud = frame.acquirePointCloud();
                mRenderer.updatePointCloud(pointCloud);
                pointCloud.release();

                if (mPointAdded) {
                    List<HitResult> results = frame.hitTest(mLastX, mLastY);
                    for (HitResult result : results) {
                        Pose pose = result.getHitPose();
                        float[] points = new float[]{ pose.tx(), pose.ty(), pose.tz() };
                        mPoints.add(points);
                        mRenderer.addPoint(points);
                        updateDistance();
                        break;
                    }
                    mPointAdded = false;
                }

                Camera camera = frame.getCamera();
                float[] projMatrix = new float[16];
                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f);
                float[] viewMatrix = new float[16];
                camera.getViewMatrix(viewMatrix, 0);

                mRenderer.setProjectionMatrix(projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);
            }
        });
        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(mRenderer);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSurfaceView.onPause();
        mSession.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (mSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    case INSTALLED:
                        mSession = new Session(this);
                        Log.d(TAG, "ARCore Session created.");
                        break;
                    case INSTALL_REQUESTED:
                        mUserRequestedInstall = false;
                        Log.d(TAG, "ARCore should be installed.");
                        break;
                }
            }
        }
        catch (UnsupportedOperationException e) {
            Log.e(TAG, e.getMessage());
        } catch (UnavailableApkTooOldException e) {
            e.printStackTrace();
        } catch (UnavailableDeviceNotCompatibleException e) {
            e.printStackTrace();
        } catch (UnavailableUserDeclinedInstallationException e) {
            e.printStackTrace();
        } catch (UnavailableArcoreNotInstalledException e) {
            e.printStackTrace();
        } catch (UnavailableSdkTooOldException e) {
            e.printStackTrace();
        }

        mConfig = new Config(mSession);
        if (!mSession.isSupported(mConfig)) {
            Log.d(TAG, "This device is not support ARCore.");
        }
        mSession.configure(mConfig);
        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }

        mSurfaceView.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();
                mPointAdded = true;
                break;
        }
        return true;
    }

    public void onRemoveButtonClick(View view){
        if (!mPoints.isEmpty()) {
            mPoints.remove(mPoints.size() - 1);
            mRenderer.removePoint();
            updateDistance();
        }
    }
    public void onExitButtonClick(View view){
        Intent intent = new Intent();
        intent.putExtra("stage_value", 0);
        setResult(RESULT_OK,intent);
        finish();
    }

    public void updateDistance() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(counter<4){
                mSideTextView.setText("Measure Edge Number "+(counter));
                }
                double totalDistance = 0.0;
                if (mPoints.size() <= 2)  {
                    for (int i = 0; i < mPoints.size() - 1; i++) {
                        float[] start = mPoints.get(i);
                        float[] end = mPoints.get(i + 1);

                         distance = Math.sqrt(
                                (start[0] - end[0]) * (start[0] - end[0])
                                        + (start[1] - end[1]) * (start[1] - end[1])
                                        + (start[2] - end[2]) * (start[2] - end[2]));
                        totalDistance += distance;
                    }
                }
                else {
//                    mTextView.setText("");

                    // Empty the points & save that counter to length of side
//                    while (mPoints.isEmpty()) {
                    mPoints.remove(mPoints.size() - 1);
                    mRenderer.removePoint();
                    mPoints.remove(mPoints.size() - 1);
                    mRenderer.removePoint();
                    mPoints.remove(mPoints.size() - 1);
                    mRenderer.removePoint();
//                        updateDistance();
//                    }

                        if (counter <= 3) {

//                            save to side variable
                            Log.d("BIN_PACK", "Distance, "+distance+" of Edge "+counter);
                            if(counter==1){
                                side_length_1=distance;
                            }
                            if(counter==2){
                                side_length_2=distance;
                            }
                            if(counter==3){



                                side_length_3=distance;
                                double volume=side_length_1*side_length_2*side_length_3*61023;// is cubic-inch now
                                mBoxTextView.setText("Volume "+String.format(Locale.getDefault(), "%.7f", volume)+"m3");
                                Toast.makeText(getApplicationContext(),"Volume "+String.format(Locale.getDefault(), "%.7f", volume)+"m3", Toast.LENGTH_LONG).show();

                                if(volume!=0){
                                    // calculations here
                                    // all meters are inches
                                    side_length_1=side_length_1*39.3701;
                                    side_length_2=side_length_2*39.3701;
                                    side_length_3=side_length_3*39.3701;
                                    List<BoxItem> products = new ArrayList<BoxItem>();
                                    products.add(new BoxItem(new Box("Foot",(int) Math.ceil(side_length_1), (int) Math.ceil(side_length_2), (int) Math.ceil(side_length_3), 1), 1));
                                    // loop through all boxes
                                    // box dimensions
                                    int[] box_1={18,18,18};
                                    int[] box_2={6,6,4};
                                    int[] box_3={11,8,12};
                                    int[] box_4={12,9,10};
                                    int[] box_5={17,11,10};
                                    int[] box_6={20,20,20};
                                    int[] box_7={24,24,24};
                                    int[] box_8={24,18,9};
                                    int[] box_9={22,15,13};
                                    int[] box_10={11,8,6};
                                    int[] box_11={12,9,6};
                                    int[] box_12={17,11,12};
                                    int[] box_13={18,12,12};
                                    int[] box_14={16,16,16};
                                    int[] box_15={14,14,14};
                                    int[] box_16={12,12,12};
                                    int[] box_17={10,8,6};
                                    int[] box_18={18,13,9};

                                    List<int[]> box_list = new ArrayList<int[]>();
                                    box_list.add(box_1);
                                    box_list.add(box_2);
                                    box_list.add(box_3);
                                    box_list.add(box_4);
                                    box_list.add(box_5);
                                    box_list.add(box_6);
                                    box_list.add(box_7);
                                    box_list.add(box_8);
                                    box_list.add(box_9);
                                    box_list.add(box_10);
                                    box_list.add(box_11);
                                    box_list.add(box_12);
                                    box_list.add(box_13);
                                    box_list.add(box_14);
                                    box_list.add(box_15);
                                    box_list.add(box_16);
                                    box_list.add(box_17);
                                    box_list.add(box_18);

                                    lowest_volume=5832;
                                    // loop through containers small volume to largest
                                    for (int i=0;i<box_list.size();i++){
                                        List<Container> containers_dynamic = new ArrayList<Container>();
//        containers.add(new Container("box",1, 100, 100, 10000)); // x y z and weight
                                        width=(int) box_list.get(i)[0];
                                        depth=(int) box_list.get(i)[1];
                                        height=(int) box_list.get(i)[2];
                                        containers_dynamic.add(new Container("box",width, depth, height, 10000)); // x y z and weight
                                        Log.d("BIN_PACK", "Trying for "+(i+1)+" WxDxH "+width+"x"+ depth+"x"+ height);
                                        boolean rotate3d = true;
                                        Packager packager = new LargestAreaFitFirstPackager(containers_dynamic, rotate3d, true, true);

                                        Container match = packager.pack(products);
                                        if(match != null){
                                            Log.d("BIN_PACK", "Matches for "+(i+1)+" with  WxDxH="+width+"x"+depth+"x"+height+" placement@"+match.getLevels().toString()+" volume="+match.getVolume());
                                            // Keep track of lowest volume box index
//                                            lowest_index = i;
//                                            lowest_volume=(int) match.getVolume();
                                            if(match.getVolume() < lowest_volume) {
                                                lowest_index = i;
                                                lowest_volume=(int) match.getVolume();
                                                Log.d("BIN_PACK", "Lowest Index yet, "+lowest_index);
                                            }
                                        }
                                        else{
                                            Log.d("BIN_PACK", "No match found");
                                        }
                                    }
                                }
                                Intent intent = new Intent();
                                intent.putExtra("stage_value", volume);
                                intent.putExtra("index", lowest_index);
                                intent.putExtra("width", width);
                                intent.putExtra("depth", depth);
                                intent.putExtra("height", height);

                                setResult(RESULT_OK,intent);
                                finish();
                            }



                            counter = counter + 1;

                        } else {
                            counter = 1;
                        }
                }
                    String distanceString = String.format(Locale.getDefault(), "%.2f", totalDistance)
                            + getString(R.string.txt_dist)+"\r\n"+"Tap again to measure next side";
                    mTextView.setText(distanceString);

                }
            });
        }

    private void hideStatusBarAndTitleBar(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
