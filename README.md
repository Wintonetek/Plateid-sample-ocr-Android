# plateid_sample-Android


The order of the interface instructions corresponds to the calling sequence
For serial number and TF card authorization, the authorization service must be added first
Order	Interface    Function	 Paramter Description

// Accurate recognition  0: Fast/Import/Photo recognition

1	RecogService.recogModel = 2;	Set recognition mode	

2	Intent recogIntent=new Intent(activity, RecogService.class);

//	Start recognition service. This launches the recognition service and calls the core setting and recognition interfaces.

activity.bindService(recogIntent,recogConn,Service.BIND_AUTO_CREATE);

//Get initialization result: 0 = success, others = failure

3	recogBinder.getInitPlateIDSDK();	

//Set core initialization parameters. Refer to the demo for parameter settings.

4	recogBinder.setRecogArgu(cfgparameter, imageformat);	

	//Perform recognition and get results. Refer to the demo for parameter settings.
 
5	recogBinder.doRecogDetail(prp);

Note: The above is the complete sequence of recognition interface calls, which is helpful for users customizing their UI. There are many parameters, so please refer to the demo for configuration, which includes detailed parameter descriptions.
