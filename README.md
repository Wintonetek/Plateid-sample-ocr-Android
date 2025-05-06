# plateid_sample-Android


接口说明顺序就是调用顺序
序列号和TF卡授权方式要先加上授权服务
顺序	接口	作用	参数说明

// 2:精准识别0:快速/导入/拍照识别

1	RecogService.recogModel = 2;	设置识别模式	

2	Intent recogIntent=new Intent(activity, RecogService.class);

//	启动识别服务	启动识别服务，调用核心设置接口、识别接口

activity.bindService(recogIntent,recogConn,Service.BIND_AUTO_CREATE);

//获取初始化结果	0：初始化成功  其他：失败

3	recogBinder.getInitPlateIDSDK();	

//设置核心初始化参数	参考demo中的设置参数

4	recogBinder.setRecogArgu(cfgparameter, imageformat);	

	//识别并获取结果	参考demo中的设置参数
 
5	recogBinder.doRecogDetail(prp);

说明：以上是完整的识别接口调用顺序，方便自定义界面用户查看。具体参数太多，可以参考demo中的设置，并配有详细的参数说明