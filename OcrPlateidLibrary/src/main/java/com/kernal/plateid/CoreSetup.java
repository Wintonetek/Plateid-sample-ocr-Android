package com.kernal.plateid;

import java.io.Serializable;

/**
 * @author user
 *根据注释设置参数
 */
public class CoreSetup implements Serializable{
    /***
     * 授权方式
     */
    public String Devcode = "QMVPAMLUZYBXAW5";//项目授权，开发码设置
    public String Sn = "";//序列号授权方式，序列号
    /**
     * 识别模式
     */
    public boolean takePicMode = false;//拍照识别:true,视频流自动识别:false
    public boolean accurateRecog = true;//精准识别:true,快速识别:false（推荐使用精准识别，识别准确率高。设备配置太低的推荐快速模式）
    /***
     * 保存图片
     */
    public boolean onlySaveOnePicture = false;//只保存一张图片，新的图片替换老的图片:true,保存每一张图片:false

    /**
     * 识别车牌参数设置
     */
    public int preWidth = 1280; //640;  //480;  //1920; //1080;//预览分辨率设置
    public int preHeight = 720; //480; //320; //1080; //

    public int nOCR_Th = 2;//识别阈值(取值范围0-9, 0:最宽松的阈值, 9:最严格的阈值, 5:默认阈值)
    public int nPlateLocate_Th = 5;// 定位阈值(取值范围0-9, 0:最宽松的阈值9, :最严格的阈值, 5:默认阈值)
    public String szProvince = "京";// 省份顺序,例:public int szProvince = "京津沪";最好设置三个以内，最多五个。

    public int individual = 1;// 个性化车牌是否开启：0开启；1关闭
    public int tworowyellow = 2;// 双层黄色车牌是否开启：开启；3关闭
    public int armpolice = 4;// 单层武警车牌是否开启：4开启；5关闭
    public int tworowarmy = 6;// 双层军队车牌是否开启：6开启；7关闭
    public int tractor = 8;// 农用车车牌是否开启：8开启；9关闭
    public int embassy = 12;// 使馆车牌是否开启：12开启；13关闭
    public int armpolice2 = 16;// 双层武警车牌是否开启：16开启；17关闭
    public int consulate = 22;// 领事馆车牌开启：22开启；23关闭
    public int newEnergy = 24;// 新能源车牌开启：24开启；25关闭
    public int changche = 27;//厂车牌是否开启：26开启；27关闭
    public int emergency = 28;//应急救援车牌是否开启：28开启；29关闭
    public int Infactory = 19;//厂内车牌是否开启：18:开启；19关闭
    public int civilAviation = 21;//民航车牌是否开启：20开启；21关闭
}
