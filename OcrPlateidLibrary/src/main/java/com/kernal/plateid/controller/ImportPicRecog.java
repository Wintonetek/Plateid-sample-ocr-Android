package com.kernal.plateid.controller;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.kernal.plateid.CoreSetup;
import com.kernal.plateid.PlateCfgParameter;
import com.kernal.plateid.PlateRecognitionParameter;
import com.kernal.plateid.RecogService;

import java.io.File;
import java.lang.ref.SoftReference;

/***
 * @author user
 * 导入图像识别类
 */
public class ImportPicRecog {

    private volatile static ImportPicRecog importPicRecog;

    public static ImportPicRecog getInstance(Context context) {
        if (null == importPicRecog) {
            synchronized (ImportPicRecog.class) {
                if (null == importPicRecog) {
                    importPicRecog = new ImportPicRecog(context);
                }
            }
        }
        return importPicRecog;
    }

    private SoftReference<Context> mContext;
    private CoreSetup coreSetup;
    private int iInitPlateIDSDK;
    private RecogService.MyBinder recogBinder;
    private String[] recogResult;
    private PlateRecognitionParameter prp;
    private Point picPoint;
    private int nRet;
    private boolean isBind;
    Handler handler = new Handler(Looper.getMainLooper());

    public ImportPicRecog(Context context) {
        nRet = -2;
        iInitPlateIDSDK = -1;
        prp = new PlateRecognitionParameter();
        if (recogBinder == null) {
            coreSetup = new CoreSetup();
            this.mContext = new SoftReference<>(context);
            //快速、导入、拍照识别模式参数(0:快速、导入、拍照识别模式-----2:精准识别模式)
            RecogService.recogModel = 0;
            //启动识别服务
            Intent recogIntent = new Intent(this.mContext.get(),
                    RecogService.class);
            isBind = this.mContext.get().bindService(recogIntent, recogConn,
                    Service.BIND_AUTO_CREATE);
        }
    }

    public interface ResultCallBack {
        void getRecognizePlateResult(String[] result);
    }

    class RecognizePlateByPathRunnable implements Runnable {
        private String recognizePicPath;
        private ResultCallBack resultCallBack;

        RecognizePlateByPathRunnable(String recognizePicPath, ResultCallBack resultCallBack) {
            this.recognizePicPath = recognizePicPath;
            this.resultCallBack = resultCallBack;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            setRecogCoreParameter();
            picPoint = new Point();
            recogResult = new String[14];
            if (recogBinder != null) {
                if (obtainPicSize(recognizePicPath)) {
                    // 图像高度
                    prp.height = picPoint.y;
                    // 图像宽度
                    prp.width = picPoint.x;
                    // 图像文件
                    prp.pic = recognizePicPath;
                    recogResult = recogBinder.doRecogDetail(prp);
                    nRet = recogBinder.getnRet();
                    if (nRet != 0) {
                        recogResult = new String[]{"错误码:" + String.valueOf(nRet) + "，请查阅开发手册寻找解决方案", String.valueOf(nRet)};
                    }
                } else {
                    nRet = -1;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        resultCallBack.getRecognizePlateResult(recogResult);
                    }
                });
                releaseService();
            }
        }
    }

    /**
     * 识别获取结果
     *
     * @param recogPicPath 传入识别图像的路径
     * @return 识别结果
     */
    public void recogPicResults(String recogPicPath, ResultCallBack resultCallBack) {
        ThreadManager.getInstance().execute(new RecognizePlateByPathRunnable(recogPicPath, resultCallBack));
    }

    /***
     * 释放核心
     * 连续识别不需要重复初始化、释放
     */
    public void releaseService() {
        if (isBind && null != recogBinder) {
            importPicRecog = null;
            recogBinder.UninitPlateIDSDK();
            this.mContext.get().unbindService(recogConn);
            isBind = false;
        }
    }

    private ServiceConnection recogConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            recogConn = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            recogBinder = (RecogService.MyBinder) service;
        }
    };

    /***
     * 设置车牌识别核心参数
     *
     */
    private void setRecogCoreParameter() {
        iInitPlateIDSDK = recogBinder.getInitPlateIDSDK();
        if (iInitPlateIDSDK != 0) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext.get(), "错误码：" + iInitPlateIDSDK, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // 导入图片方式参数
            int imageformat = 0;
            PlateCfgParameter cfgparameter = new PlateCfgParameter();
            //识别阈值(取值范围0-9, 0:最宽松的阈值, 9:最严格的阈值, 5:默认阈值)
            cfgparameter.nOCR_Th = coreSetup.nOCR_Th;
            // 定位阈值(取值范围0-9, 0:最宽松的阈值9, :最严格的阈值, 5:默认阈值)
            cfgparameter.nPlateLocate_Th = coreSetup.nPlateLocate_Th;
            // 省份顺序,例:cfgparameter.szProvince = "京津沪";最好设置三个以内，最多五个。
            cfgparameter.szProvince = coreSetup.szProvince;

            // 是否开启个性化车牌:0开启；1关闭
            cfgparameter.individual = coreSetup.individual;
            // 双层黄色车牌是否开启:开启；3关闭
            cfgparameter.tworowyellow = coreSetup.tworowyellow;
            // 单层武警车牌是否开启:4开启；5关闭
            cfgparameter.armpolice = coreSetup.armpolice;
            // 双层军队车牌是否开启:6开启；7关闭
            cfgparameter.tworowarmy = coreSetup.tworowarmy;
            // 农用车车牌是否开启:8开启；9关闭
            cfgparameter.tractor = coreSetup.tractor;
            // 使馆车牌是否开启:12开启；13关闭
            cfgparameter.embassy = coreSetup.embassy;
            // 双层武警车牌是否开启:16开启；17关闭
            cfgparameter.armpolice2 = coreSetup.armpolice2;
            //厂内车牌是否开启     18:开启  19关闭
            cfgparameter.Infactory = coreSetup.Infactory;
            //民航车牌是否开启  20开启 21 关闭
            cfgparameter.civilAviation = coreSetup.civilAviation;
            //领事馆车牌开启   22开启   23关闭
            cfgparameter.consulate = coreSetup.consulate;
            //新能源车牌开启  24开启  25关闭
            cfgparameter.newEnergy = coreSetup.newEnergy;
            //厂车牌是否开启 26开启 27关闭
            cfgparameter.changche = coreSetup.changche;
            //应急救援车牌是否开启 28开启 29关闭
            cfgparameter.emergency = coreSetup.emergency;

            recogBinder.setRecogArgu(cfgparameter, imageformat);
            // 图像高度
            prp.devCode = coreSetup.Devcode;
            prp.plateIDCfg.isModifyRecogMode = true;
            prp.plateIDCfg.bShadow = 1;
        }
    }

    /***
     * 动态获取图像的宽高
     * @param recogPicPath 识别图像路径
     */
    private boolean obtainPicSize(String recogPicPath) {
        File file = new File(recogPicPath);
        BitmapFactory.Options options = null;
        if (file.exists()) {
            try {
                options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(recogPicPath, options);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //目前最大支持到4096*4096的图片
            if (options != null) {
                int picMaxWH = 4096;
                if (options.outWidth <= picMaxWH && options.outHeight <= picMaxWH) {
                    picPoint.set(options.outWidth, options.outHeight);
                    return true;
                } else {
                    recogResult[0] = "读取文件错误，图片超出识别限制4096*4096";
                }
            }
        } else {
            recogResult[0] = "读取文件不存在";
        }
        return false;
    }
}
