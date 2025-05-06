package com.plateid.demo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kernal.permission.PlateBaseActivity;
import com.kernal.plateid.CoreSetup;
import com.kernal.plateid.activity.PlateidCameraActivity;
import com.kernal.plateid.controller.CommonTools;
import com.kernal.plateid.controller.ImportPicRecog;
import com.kernal.plateid.controller.SNandTFAuth;

/**
 * @author user
 */
public class PlateMainActivity extends PlateBaseActivity implements View.OnClickListener {
    private ImportPicRecog importPicRecog;
    private LinearLayout startLinearLayout;
    private RelativeLayout endRelativieLayout;
    private ImageView plateImage;
    private TextView plateIdText;
    private TextView plateColorText;
    /**
     * 动态授权需要的权限
     */
    private String[] PERMISSION = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_plate);
        initView();
    }

    @Override
    public void onClick(View v) {
        if (R.id.activationButton == v.getId()) {
            /**
             * 序列号激活（项目授权不需要调用）
             */
            createViewToAuthService();
        } else if (R.id.takePicButton == v.getId()) {
            /**
             * 拍照识别
             */
            Intent cameraIntent = new Intent(PlateMainActivity.this, PlateidCameraActivity.class);
            CoreSetup coreSetup = new CoreSetup();
            coreSetup.takePicMode = true;
            cameraIntent.putExtra("coreSetup", coreSetup);
            startActivityForResult(cameraIntent, 1);
        } else if (R.id.automaticRecogButton == v.getId()) {
            /**
             * 扫描识别
             */
            Intent cameraIntent = new Intent(PlateMainActivity.this, PlateidCameraActivity.class);
            CoreSetup coreSetup = new CoreSetup();
            coreSetup.takePicMode = false;
            cameraIntent.putExtra("coreSetup", coreSetup);
            startActivityForResult(cameraIntent, 1);
        } else if (R.id.selectPicButton == v.getId()) {
            /**
             * 导入识别
             */
            importPicRecog = ImportPicRecog.getInstance(this);
            Intent selectIntent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Intent wrapperIntent = Intent.createChooser(selectIntent, getString(R.string.photo_album_title));
            startActivityForResult(wrapperIntent, 2);
        } else if (R.id.confirm == v.getId() || R.id.plate_back == v.getId()) {
            // 确定或者返回触发事件
            showMainPageLayout(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 扫描识别回调接口
         * 显示识别的车牌号码，颜色及车牌图片
         */
        if (data != null && requestCode == 1 && resultCode == RESULT_OK) {
            //获取到的识别结果
            String[] recogResult = data.getStringArrayExtra("RecogResult");
            //保存图片路径
            String savePicturePath = data.getStringExtra("savePicturePath");
            //是竖屏还是横屏
            int screenDirection = data.getIntExtra("screenDirection", 0);
            showMainPageLayout(false);
            //设置显示图片的区域
            if (recogResult != null && recogResult[0] != null && !"".equals(recogResult[0])) {
                showPlateImage(savePicturePath, Integer.valueOf(recogResult[7]), Integer.valueOf(recogResult[8]), Integer.valueOf(recogResult[9])
                        - Integer.valueOf(recogResult[7]), Integer.valueOf(recogResult[10])
                        - Integer.valueOf(recogResult[8]));
                setShowPlateInformation(recogResult[0], recogResult[1]);
            } else {
                CoreSetup coreSetup = new CoreSetup();
                if (screenDirection == 1 || screenDirection == 3) {
                    showPlateImage(savePicturePath, coreSetup.preHeight / 24, coreSetup.preWidth / 4, coreSetup.preHeight * 23 / 24, coreSetup.preWidth / 3);
                } else {
                    showPlateImage(savePicturePath, coreSetup.preWidth / 4, coreSetup.preHeight / 4, coreSetup.preWidth / 2, coreSetup.preHeight / 2);
                }
                setShowPlateInformation("null", "null");
            }
        }
        /**
         * 导入识别接口调用
         * 显示识别的车牌号码，颜色及车牌图片
         */
        else if (data != null && requestCode == 2 && resultCode == RESULT_OK && importPicRecog != null) {
            Uri uri = data.getData();
            final String picPathString = CommonTools.getPath(PlateMainActivity.this, uri);
            //初始化和识别接口要有一个时间段，所以将初始化放在了上面，这里要注意下
            //传入图片识别获取结果
            setShowPlateInformation(getString(R.string.recognizing), getString(R.string.recognizing));
            showMainPageLayout(false);
            Bitmap bitmap = BitmapFactory.decodeFile(picPathString);
            plateImage.setImageBitmap(bitmap);
            /**
             * 导入识别接口
             * result 识别结果
             */
            importPicRecog.recogPicResults(picPathString, new ImportPicRecog.ResultCallBack() {
                @Override
                public void getRecognizePlateResult(String[] result) {
                    setShowPlateInformation(result[0], result[1]);
                }
            });
            importPicRecog = null;
        }
        /**
         *  导入识别返回释放接口调用
         *  不释放进入到扫描识别会导致扫描使用导入识别的参数，错误率高
         */
        else if (requestCode == 2 && resultCode == RESULT_CANCELED && importPicRecog != null) {
            importPicRecog.releaseService();
            importPicRecog = null;
        }
    }

    private void showMainPageLayout(boolean showMainPage) {
        if (showMainPage) {
            endRelativieLayout.setVisibility(View.GONE);
            startLinearLayout.setVisibility(View.VISIBLE);
        } else {
            endRelativieLayout.setVisibility(View.VISIBLE);
            startLinearLayout.setVisibility(View.GONE);
        }
    }

    private void setShowPlateInformation(String plateId, String plateColor) {
        plateIdText.setText(plateId);
        plateColorText.setText(plateColor);
    }

    /**
     * 展示裁剪后的车牌图片
     *
     * @param savePicturePath 路径
     * @param left            车牌左上点横坐标
     * @param top             车牌左上点纵坐标
     * @param width           宽
     * @param height          高
     */
    private void showPlateImage(String savePicturePath, int left, int top, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeFile(savePicturePath);
        if (bitmap != null) {
            if (width - left >= bitmap.getWidth()) {
                left = 0;
                width = bitmap.getWidth();
            }
            if (height - top >= bitmap.getHeight()) {
                top = 0;
                height = bitmap.getHeight();
            }
            bitmap = Bitmap.createBitmap(bitmap, left, top, width, height);
            plateImage.setImageBitmap(bitmap);
        }
    }

    /**
     * 设置需要请求的权限
     */
    @Override
    public String[] getPermissions() {
        return PERMISSION;
    }

    /**
     * 序列号授权激活服务接口
     */
    public void createViewToAuthService() {
        final AlertDialog mDialog = new AlertDialog.Builder(PlateMainActivity.this).create();
        mDialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_dialog_background);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_name_dialog, null);
        Button mBtActivation = view.findViewById(R.id.mBtActivation);
        final EditText mEtSn = view.findViewById(R.id.mEtSn);
        mBtActivation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                }
                /**
                 * 在此传入序列号，激活
                 */
                new SNandTFAuth(PlateMainActivity.this, mEtSn.getText().toString().toUpperCase());
                mDialog.dismiss();
            }
        });
        mDialog.setView(view);
        mDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (endRelativieLayout.getVisibility() == View.VISIBLE) {
                showMainPageLayout(true);
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        startLinearLayout = (LinearLayout) findViewById(R.id.startLinearLayout);
        endRelativieLayout = (RelativeLayout) findViewById(R.id.endRelativieLayout);
        plateImage = (ImageView) findViewById(R.id.plateImage);
        plateIdText = (TextView) findViewById(R.id.plateId);
        plateColorText = (TextView) findViewById(R.id.plateColor);
    }
}
