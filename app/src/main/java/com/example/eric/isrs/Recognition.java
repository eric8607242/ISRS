package com.example.eric.isrs;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recognition extends AppCompatActivity {

    private final int REQUEST_PERMISSION_CAMERA = 100;

    private boolean reRecFlag = false;
    private int reRecNum = 1;
    private int mRecPicNum = 1;
    private int ListOffset = 1;

    private TextureView mTextureView = null;
    private SurfaceView mRectView = null;
    private LinearLayout mLinearLayout = null;

    private Size mPreviewSize = null;
    private CameraDevice mCameraDevice = null;
    private CaptureRequest.Builder mPreviewBuilder = null;
    private CameraCaptureSession mCameraPreviewCaptureSession = null;
    private CameraCaptureSession mCameraTakePicCaptureSession = null;

    private Button mTakePicture;
    private Button mBack;
    private Button mcheckBack;
    private Button mfailBack;
    private Button mreRec;
    private Button mResult;

    private TextView mCheckTitle;

    private ImageView mimageView;

    private View view_rec = null;
    private View view_result = null;
    private View view_fail = null;

    private List<LinearLayout> mlinearLayout = new ArrayList<LinearLayout>();
    private List<TextView> mtextList = new ArrayList<TextView>();
    private List<ProgressBar> mprogressList = new ArrayList<ProgressBar>();

    private int sheetID;
    private String sheetTitle;

    private String savePath;

    LinearLayout.LayoutParams layoutParams;
    LinearLayout.LayoutParams textParams;
    LinearLayout.LayoutParams progressParams;


    //當UI的TextureView建立時，會執行onSurfaceTextureAvailabe()
    private TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    if(askForPermissions()){
                        //檢查是否取得camera的權限
                        //開啟camera
                        openCamera();
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                    Canvas canvas = mRectView.getHolder().lockCanvas();
                    if(canvas != null){
                        canvas.drawColor(Color.TRANSPARENT , PorterDuff.Mode.CLEAR);

                        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setColor(Color.GREEN);
                        paint.setStrokeWidth(3);



                        canvas.save();

                        int density  = (int)getResources().getDisplayMetrics().density;


                        int btnHeight = mBack.getHeight();
                        int shotHeight = mTakePicture.getHeight();

                        int RectLeft = 30 * density;
                        int RectTop = btnHeight + 25 * density;
                        int RectRight = getScreenWidth() -30 * density;
                        int RectBottom = getScreenHeight()-shotHeight - 40 * density;

                        canvas.drawRect(RectLeft, RectTop, RectRight, RectBottom, paint);
                        canvas.restore();
                    }
                    mRectView.getHolder().unlockCanvasAndPost(canvas);
                }

            };



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);


        savePath = String.valueOf(getFilesDir());


        sheetID = getIntent().getIntExtra("SHEET_ID", 0);
        sheetTitle = getIntent().getStringExtra("SHEET_TITLES");

        LayoutInflater inflater = getLayoutInflater();
        view_rec = inflater.inflate(R.layout.activity_recognition, null);
        view_result = inflater.inflate(R.layout.activity_check_upload, null);
        view_fail = inflater.inflate(R.layout.activity_failed_pic, null);

        setContentView(view_rec);

        mTextureView = findViewById(R.id.rec_textureView);
        mRectView = (SurfaceView)findViewById(R.id.rec_surfaceView);
        mLinearLayout = (LinearLayout)view_result.findViewById(R.id.check_linearlayout);

        mCheckTitle = (TextView)view_result.findViewById(R.id.check_title);
        mCheckTitle.setText(sheetTitle);

        mRectView.setZOrderOnTop(true);
        mRectView.getHolder().setFormat(PixelFormat.TRANSPARENT);

        mTakePicture = (Button) findViewById(R.id.rec_shotbtn);
        mBack = (Button) findViewById(R.id.rec_backbtn);
        mResult = (Button) findViewById(R.id.rec_checkbtn);

        mcheckBack = (Button) view_result.findViewById(R.id.check_backbtn);

        mfailBack = (Button) view_fail.findViewById(R.id.fail_backbtn);
        mreRec = (Button) view_fail.findViewById(R.id.fail_rerecbtn);
        mimageView = (ImageView) view_fail.findViewById(R.id.fail_imageView);


        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mcheckBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(view_rec);
            }
        });
        mResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCamera();
                setContentView(view_result);
            }
        });
        mfailBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCamera();
                setContentView(view_result);
            }
        });
        mreRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reRecFlag = true;
                setContentView(view_rec);
            }
        });
        mTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    takePicture();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                100
        );

        textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );


        progressParams = new LinearLayout.LayoutParams(
                100,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        Reghttp http = new Reghttp(savePath+ "/" + sheetID+ "_0.jpeg", 0);
        http.execute();

        Toast.makeText(Recognition.this, "麻煩將問卷對準中間的框框拍攝\n", Toast.LENGTH_SHORT).show();
    }



    @Override
    protected void onResume(){
        //設定SurfaceTextureListener ，這時當SurfaceTexture準備好時
        //會啟動onSurfaceTextureAvailable()來啟動Camera
        super.onResume();

        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStop(){
        super.onStop();

        closeCamera();
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case REQUEST_PERMISSION_CAMERA:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) openCamera();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



    private boolean askForPermissions(){
        //App需要用的功能權限清單

        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};


        //檢查是否已經取得權限
        final List<String> listPermissionNeeded = new ArrayList<>();
        boolean bShowPermissionRationale = false;

        for(String p:permissions){
            int result = ContextCompat.checkSelfPermission(Recognition.this, p);
            if(result != PackageManager.PERMISSION_GRANTED){
                listPermissionNeeded.add(p);

                //檢查是否需要顯示說明
                if(ActivityCompat.shouldShowRequestPermissionRationale(Recognition.this, p))
                    bShowPermissionRationale = true;
            }
        }

        //像使用者徵詢還沒有許可的權限
        if (!listPermissionNeeded.isEmpty()) {
            if(bShowPermissionRationale){
                AlertDialog.Builder altDigBuilder = new AlertDialog.Builder(Recognition.this);
                altDigBuilder.setTitle("提示");
                altDigBuilder.setMessage("App 需要您的許可才能執行");
                altDigBuilder.setIcon(android.R.drawable.ic_dialog_info);
                altDigBuilder.setCancelable(false);
                altDigBuilder.setPositiveButton("確認",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(Recognition.this, listPermissionNeeded.toArray(
                                        new String[listPermissionNeeded.size()]), REQUEST_PERMISSION_CAMERA);
                            }
                        });
                altDigBuilder.show();
            }else {
                ActivityCompat.requestPermissions(Recognition.this, listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]), REQUEST_PERMISSION_CAMERA);
            }
            return false;
        }

        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera(){
        //取得CameraManager
        CameraManager camMgr = (CameraManager) getSystemService(CAMERA_SERVICE);

        try{
            //取得相機背後的camera
            String cameraId = camMgr.getCameraIdList()[0];
            CameraCharacteristics camChar = camMgr.getCameraCharacteristics(cameraId);


            //取得解析度
            StreamConfigurationMap map = camChar.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

            //啟動camera
            if(ContextCompat.checkSelfPermission(Recognition.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                //設定callback
                camMgr.openCamera(cameraId, mCameraStateCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    private CameraDevice.StateCallback mCameraStateCallback =
            new CameraDevice.StateCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onOpened(@NonNull CameraDevice cameraDevice) {
                    //當相機啟動完成時，才開始預覽
                    mCameraDevice = cameraDevice;
                    startPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {

                    Toast.makeText(Recognition.this, "無法使用camera", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Toast.makeText(Recognition.this, "Camera開啟錯誤", Toast.LENGTH_LONG).show();
                }
            };


    //Camera的CaptureSession狀態改變時執行
    private CameraCaptureSession.StateCallback mCameraCaptureSessionCallback =
            new CameraCaptureSession.StateCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    closeAllCameraCaptureSession();

                    //記下這個capture session，使用完要刪除
                    mCameraPreviewCaptureSession = cameraCaptureSession;

                    mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                    HandlerThread backgroundThread = new HandlerThread("CameraPreview");
                    backgroundThread.start();
                    Handler backgroundHandler = new Handler(backgroundThread.getLooper());

                    try{
                        mCameraPreviewCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
                    }catch (CameraAccessException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(Recognition.this, "Camera預覽錯誤", Toast.LENGTH_LONG).show();
                }
            };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startPreview(){
        //從UI元件的TextureView取得SurfaceTexture
        //依照camera的解析度，設定TextureView的解析度
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

        //依照TextureView的解析度建立一個surface給camera使用
        Surface surface = new Surface(surfaceTexture);

        //設定camera的CaptureRequest和CaptureSession
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mPreviewBuilder.addTarget(surface);

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), mCameraCaptureSessionCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }


    //建立新的Camera Capture Session之前
    //呼叫這個method，清除舊的Camera Capture Session
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closeAllCameraCaptureSession() {
        if (mCameraPreviewCaptureSession != null) {
            mCameraPreviewCaptureSession.close();
            mCameraPreviewCaptureSession = null;
        }

        if (mCameraTakePicCaptureSession != null) {
            mCameraTakePicCaptureSession.close();
            mCameraTakePicCaptureSession = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void takePicture() throws IOException {
        if(mCameraDevice == null){
            Toast.makeText(Recognition.this, "Camera錯誤", Toast.LENGTH_LONG).show();
            return;
        }

        final File folder = new File(savePath+"/ISRS/");

        if(!folder.exists()) {
            folder.mkdir();
        }

        final File file;

        //準備影像檔
        if(reRecFlag == false) {
            file = new File(folder, sheetID+ "_" + mRecPicNum + ".jpeg");
        }else{
            file = new File(folder, sheetID+ "_" + reRecNum + ".jpeg");
        }


//        Toast.makeText(Recognition.this,file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        if(file.exists()){
            file.delete();
        }
        file.createNewFile();

        //準備OnImageAvilableListener
        ImageReader.OnImageAvailableListener imgReaderOnImageAvilable =
                new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader imageReader) {
                        //把影像資料寫入檔案
                        Image image = null;

                        try{
                            image = imageReader.acquireLatestImage();

                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();

                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);

                            FileOutputStream output = null;
                            try{
                                output = new FileOutputStream(file);
                                output.write(bytes);
                            }finally {
                                if(output != null){
                                    output.flush();
                                    output.close();
                                }
                            }
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }catch (IOException e){
                            e.printStackTrace();
                        }finally {
                            if(image != null){
                                image.close();
                            }
                        }
                    }
                };

        //取得CameraManager
        CameraManager camMgr = (CameraManager) getSystemService(CAMERA_SERVICE);

        try{
            CameraCharacteristics camChar = camMgr.getCameraCharacteristics(mCameraDevice.getId());

            //設定拍照的解析度
            Size[] jpegSizes = null;
            if(camChar != null){
                jpegSizes = camChar.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }

            int picWidth = 640;
            int picHeight = 480;
            if(jpegSizes != null && jpegSizes.length > 0){
                picWidth = jpegSizes[0].getWidth();
                picHeight = jpegSizes[0].getHeight();
            }

            //儲存為影像檔 輸出給UI的TextureView顯示
            ImageReader imgReader = ImageReader.newInstance(picWidth, picHeight, ImageFormat.JPEG, 1);

            //準備拍照用的thread
            HandlerThread thread = new HandlerThread("CameraTakePicture");
            thread.start();

            final Handler backgroundHandler = new Handler(thread.getLooper());

            //把OnImageAvailableListener和thread設定給ImageReader
            imgReader.setOnImageAvailableListener(imgReaderOnImageAvilable, backgroundHandler);

            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(imgReader.getSurface());
            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imgReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //決定照片的方向(直的或是橫的)
            SparseIntArray PICTURE_ORIENTATIONS = new SparseIntArray();
            PICTURE_ORIENTATIONS.append(Surface.ROTATION_0, 90);
            PICTURE_ORIENTATIONS.append(Surface.ROTATION_90, 0);
            PICTURE_ORIENTATIONS.append(Surface.ROTATION_180, 270);
            PICTURE_ORIENTATIONS.append(Surface.ROTATION_270, 180);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, PICTURE_ORIENTATIONS.get(rotation));

            //準備拍照的callback
            final CameraCaptureSession.CaptureCallback camCaptureCallback =
                    new CameraCaptureSession.CaptureCallback(){
                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result){
                            super.onCaptureCompleted(session, request, result);

                            Reghttp http;

                            if(reRecFlag == false) {
                                UploadTextUpdate("問卷:"+mRecPicNum+"          " + "上傳中");

                                http = new Reghttp(file.getAbsolutePath(), mRecPicNum);
                                http.execute();

                                mRecPicNum++;
                            }else {
                                TextView tv = (TextView) mtextList.get(reRecNum-ListOffset);

                                tv.setCompoundDrawables(null, null, null, null);
                                tv.setText("問卷:"+reRecNum+"          " + "上傳中");

                                ProgressBar progressBar = new ProgressBar(Recognition.this,null,android.R.attr.progressBarStyleHorizontal);
                                progressBar.setIndeterminate(true);
                                progressBar.setVisibility(View.VISIBLE);

                                mprogressList.set(reRecNum - ListOffset, progressBar);

                                LinearLayout ll = mlinearLayout.get(reRecNum-ListOffset);

                                setBackground(ll, 0XFF0072e3);
                                ll.addView(progressBar, progressParams);

                                http = new Reghttp(file.getAbsolutePath(), reRecNum);
                                http.execute();

                                reRecFlag = false;
                            }
                            Toast.makeText(Recognition.this, "拍照完成，照片上傳中\n", Toast.LENGTH_SHORT).show();
                            startPreview();
                        }

                        @Override
                        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult){
                        }
                    };

            //建立Capture Session
            //然後啟動拍照
            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSessionsession) {
                    try{
                        closeAllCameraCaptureSession();

                        mCameraTakePicCaptureSession = cameraCaptureSessionsession;

                        cameraCaptureSessionsession.capture(captureBuilder.build(), camCaptureCallback, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(Recognition.this, "拍照起始錯誤", Toast.LENGTH_LONG).show();
                }
            },backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public class Reghttp extends AsyncHttp {

        private String filePath;
        private int picNum;

        public Reghttp(String path, int num){
            filePath = path;
            picNum = num;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(picNum == 0){
                return;
            }

            mprogressList.get(picNum-ListOffset).setVisibility(View.GONE);

            if(s.equals("success")){
                setBackground(mlinearLayout.get(picNum-ListOffset), 0XFF02c874);
                TextView tv = (TextView) mtextList.get(picNum-ListOffset);
                setTVIcon(tv, R.mipmap.done);
                tv.setText("問卷:"+picNum+"          " + "完成");

                tv.setOnClickListener(null);

                File delFile = new File(filePath);
                if(delFile.exists()) {
                    delFile.delete();
                }

            }else {
                setBackground(mlinearLayout.get(picNum-ListOffset), 0XFFff2d2d);

                TextView tv = (TextView) mtextList.get(picNum-ListOffset);
                if(s.equals("recognition_failed")){
                    tv.setText("問卷:"+picNum+"          " + "辨識失敗");
                }else if(s.equals("file_failed")){
                    tv.setText("問卷:"+picNum+"          " + "上傳失敗");
                }

                setTVIcon(tv, R.mipmap.error);
                tv.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(View v) {
                        reRecNum = picNum;

                        File imgFile = new File(filePath);
                        closeCamera();

                        setContentView(view_fail);
                        if(imgFile.exists()){
                            Bitmap bm = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            mimageView.setImageBitmap(bm);
                        }
                    }
                });
            }
        }


        @Override
        protected String doInBackground(Map<String, String>... maps) {

            SharedPreferences settings = getSharedPreferences("setting", MODE_PRIVATE);
            String mUserName = settings.getString("USERNAME", "failed");

            try {
                return super.uploadImage(filePath, String.valueOf(sheetID) +"_"+ String.valueOf(picNum), mUserName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "file_failed";
        }
    }

    public static int getScreenWidth(){
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(){
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    //設定 status of image uploaded text section
    public void UploadTextUpdate(String textContent){

        LinearLayout ll = new LinearLayout(this);
        ll.setGravity(Gravity.CENTER);

        setBackground(ll, 0XFF0072e3);

        TextView uploadStatus = new TextView(this);
        uploadStatus.setText(textContent);
        uploadStatus.setTextSize(20);
        uploadStatus.setGravity(Gravity.CENTER);

        ProgressBar progressBar = new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        mlinearLayout.add(ll);
        mtextList.add(uploadStatus);
        mprogressList.add(progressBar);

        ll.setPadding(0,0,20,0);
        ll.addView(uploadStatus, textParams);
        ll.addView(progressBar, progressParams);

        mLinearLayout.addView(ll, layoutParams);
    }

    //設定LinearLayout Background color
    public void setBackground(LinearLayout ll, int color){
        final GradientDrawable border = new GradientDrawable();
        border.setColor(color); //white background
        border.setStroke(1, Color.BLACK); //black border with full opacity

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            ll.setBackgroundDrawable(border);
        } else {
            ll.setBackground(border);
        }
    }


    //set textview icon with status
    public void setTVIcon(TextView tv, int icon){
        Drawable img = ContextCompat.getDrawable(getBaseContext(), icon);
        img.setBounds(0, 0, 100, 100);
        tv.setCompoundDrawables(null, null, img, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void closeCamera(){
        if(mCameraDevice != null){
            mCameraDevice.close();
            mCameraDevice= null;
        }
    }
}


