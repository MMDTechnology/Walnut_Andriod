package maojian.android.walnut;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MainActivity extends FragmentActivity {
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */


	private int current_tab;

	private  FragmentManager fragmentManager;

	private Home fragment_home;
	private Discover fragment_discover;
	private Device fragment_device;
	private iMessage fragment_message;
	private  Me fragment_me;

	private RadioGroup rg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		FacebookSdk.sdkInitialize(getApplicationContext());
//		AppEventsLogger.activateApp(this);
		super.onCreate(savedInstanceState);
//		TwitterAuthConfig authConfig = new TwitterAuthConfig("u9uyYYnlumipcGUU6YJSlPtno", "kc1UL9CPPI3FDVbGMlYN6PVrCeRzw7cISrJzOA2cShV1T2TzFu");
//		Fabric.with(MainActivity.this, new Twitter(authConfig));
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		MapboxAccountManager.start(this, "pk.eyJ1IjoiZHJhZ21lcGx6IiwiYSI6ImNpaWp6eDEweTAxOTF0cGtwZmwwaDhmcXMifQ.QMz7SFg6hGGmfo48w6eC8Q");



		setContentView(R.layout.activity_main);


			if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				//具有拍照权限，直接调用相机
				//具体调用代码
				Log.e("cameradebug", "checkSelfPermission");

			} else {
				//不具有拍照权限，需要进行权限申请
				ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,Manifest.permission.BLUETOOTH,Manifest.permission.INSTALL_LOCATION_PROVIDER}, 1);
			}


//		TwitterAuthConfig authConfig = new TwitterAuthConfig("u9uyYYnlumipcGUU6YJSlPtno", "kc1UL9CPPI3FDVbGMlYN6PVrCeRzw7cISrJzOA2cShV1T2TzFu");
//		Fabric.with(this, new Twitter(authConfig));

		final AVInstallation currentInstall = AVInstallation.getCurrentInstallation();
		//currentInstall.add("owner",AVUser.getCurrentUser().getObjectId());

		currentInstall.put("owner", AVUser.getCurrentUser());

		currentInstall.saveInBackground(new SaveCallback() {
			public void done(AVException e) {
				if (e == null) {
					// 保存成功
					String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
					Log.e("abc", "install sucess " + currentInstall.get("owner").toString());
					//String owner = AVUser.getCurrentUser().getObjectId();
					// 关联  installationId 到用户表等操作……
				} else {
					// 保存失败，输出错误信息
				}
			}
		});
		PushService.setDefaultPushCallback(this, MainActivity.class);


//		AVPush push = new AVPush();
//		JSONObject object = new JSONObject();
//		object.put("alert", "push message to android device directly");
//		push.setPushToAndroid(true);
//		push.setData(object);
//		push.sendInBackground(new SendCallback() {
//			@Override
//			public void done(AVException e) {
//				if (e == null) {
//					Log.e("abc","push success");
//					// push successfully.
//				} else {
//					Log.e("abc","push fail "+e);
//					// something wrong.
//				}
//			}
//		});



//		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration
//				.Builder(this)
//				.memoryCacheExtraOptions(480, 800) // max width, max height，即保存的每个缓存文件的最大长宽
//				//.discCacheExtraOptions(480, 800, Bitmap.CompressFormat.JPEG, 75, null) // Can slow ImageLoader, use it carefully (Better don't use it)/设置缓存的详细信息，最好不要设置这个
//				.threadPoolSize(3)//线程池内加载的数量
//				.threadPriority(Thread.NORM_PRIORITY - 2)
//				.denyCacheImageMultipleSizesInMemory()
//				.memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
//				.memoryCacheSize(2 * 1024 * 1024)
//				.discCacheSize(50 * 1024 * 1024)
//				.discCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候的URI名称用MD5 加密
//				.tasksProcessingOrder(QueueProcessingType.LIFO)
//				.discCacheFileCount(100) //缓存的文件数量
//				.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
//				.imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
//				.writeDebugLogs() // Remove for release app
//				.build();//开始构建

		ImageLoaderConfiguration configuration = ImageLoaderConfiguration
				.createDefault(this);

		//Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(configuration);

		fragmentManager = getFragmentManager();
		setTabSelection(0);

		// Set up the action bar.
		rg = (RadioGroup) findViewById(R.id.bottom_tabbar);

		RadioButton rb = (RadioButton) findViewById(R.id.bottom_tabbar_rb_0);

		rb.setChecked(true);

		rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.bottom_tabbar_rb_0:
						LogUtil.log.e("home");
						current_tab = 0;
						setTabSelection(0);
						break;
					case R.id.bottom_tabbar_rb_1:
						LogUtil.log.e("discover");
						current_tab = 1;
						setTabSelection(1);
						break;
					case R.id.bottom_tabbar_rb_2:
//						LogUtil.log.e("device");
						//setTabSelection(2);
						Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
						startActivity(intent);

						overridePendingTransition(R.anim.fade, R.anim.hold);

						break;
					case R.id.bottom_tabbar_rb_3:
						LogUtil.log.e("message");
						current_tab = 3;
						setTabSelection(3);
						break;
					case R.id.bottom_tabbar_rb_4:
						LogUtil.log.e("me");
						current_tab = 4;
						setTabSelection(4);
						break;
				}
			}
		});

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.

		Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.bottom_in,
				R.anim.bottom_out);

	}

	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		//Log.v("LH", "onSaveInstanceState"+outState);
		//super.onSaveInstanceState(outState);   //将这一行注释掉，阻止activity保存fragment的状态
	}

	@Override
	protected void onPause() {

		super.onPause();

	}
	@Override
	protected void onResume() {

		super.onResume();
		RadioButton rb;
		switch (current_tab){
			case 0:
				rb = (RadioButton) findViewById(R.id.bottom_tabbar_rb_0);
				rb.performClick();
				break;
			case 1:
				rb = (RadioButton) findViewById(R.id.bottom_tabbar_rb_1);
				rb.performClick();
				break;
			case 3:
				rb = (RadioButton) findViewById(R.id.bottom_tabbar_rb_3);
				rb.performClick();
				break;
			case 4:
				rb = (RadioButton) findViewById(R.id.bottom_tabbar_rb_4);
				rb.performClick();
				break;


		}



		//setTabSelection(0);

		Log.e("abc", "mainactivity resume");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_about_app:
			//Intent aboutIntent = new Intent(this, AboutAppActivity.class);
			//startActivity(aboutIntent);

//			Intent intent = new Intent(MainActivity.this, CameraActivity.class);
//			startActivity(intent);
			return true;
		case R.id.action_logout:
			new AlertDialog.Builder(this)
					.setTitle(
							this.getResources().getString(
									R.string.dialog_message_title))
					.setMessage(
							this.getResources().getString(
									R.string.action_logout_alert_message))
					.setNegativeButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									Logout();
								}
							})
					.setPositiveButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void Logout() {
    AVService.logout();
    Intent loginIntent = new Intent(this, LoginActivity.class);
		startActivity(loginIntent);
		this.finish();
	}

	/**
	 * 根据传入的index参数来设置选中的tab页。
	 *
	 */
	@SuppressLint("NewApi")
	private void setTabSelection(int index)
	{
		
		// 开启一个Fragment事务
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		// 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
		hideFragments(transaction);
		switch (index)
		{
			case 0:
				if (fragment_home == null)
				{
					// 如果MessageFragment为空，则创建一个并添加到界面上
					fragment_home = new Home();
					transaction.add(R.id.id_content, fragment_home);
				} else
				{
					// 如果MessageFragment不为空，则直接将它显示出来
					transaction.show(fragment_home);
				}
				break;
			case 1:
				if (fragment_discover == null)
				{
					// 如果MessageFragment为空，则创建一个并添加到界面上
					fragment_discover = new Discover();
					transaction.add(R.id.id_content, fragment_discover);
				} else
				{
					// 如果MessageFragment不为空，则直接将它显示出来
					transaction.show(fragment_discover);
				}
				break;
			case 2:
				if (fragment_device == null)
				{
					// 如果MessageFragment为空，则创建一个并添加到界面上
					fragment_device = new Device();
					transaction.add(R.id.id_content, fragment_device);
				} else
				{
					// 如果MessageFragment不为空，则直接将它显示出来
					transaction.show(fragment_device);
				}
				break;
			case 3:
				if (fragment_message == null)
				{
					// 如果MessageFragment为空，则创建一个并添加到界面上
					fragment_message = new iMessage();
					transaction.add(R.id.id_content, fragment_message);
				} else
				{
					// 如果MessageFragment不为空，则直接将它显示出来
					transaction.show(fragment_message);
				}
				break;
			case 4:
				if (fragment_me == null)
				{
					// 如果MessageFragment为空，则创建一个并添加到界面上
					fragment_me = new Me();
					transaction.add(R.id.id_content, fragment_me);
				} else
				{
					// 如果MessageFragment不为空，则直接将它显示出来
					transaction.show(fragment_me);
				}
				break;
		}
		transaction.commit();
	}


	/**
	 * 将所有的Fragment都置为隐藏状态。
	 *
	 * @param transaction
	 *            用于对Fragment执行操作的事务
	 */
	@SuppressLint("NewApi")
	private void hideFragments(FragmentTransaction transaction)
	{
		if (fragment_home != null)
		{
			transaction.hide(fragment_home);
		}
		if (fragment_discover != null)
		{
			transaction.hide(fragment_discover);
		}
		if (fragment_device != null)
		{
			transaction.hide(fragment_device);
		}
		if (fragment_message != null)
		{
			transaction.hide(fragment_message);
		}
		if (fragment_me != null)
		{
			transaction.hide(fragment_me);
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1) {
			if (grantResults.length >= 1) {
				int cameraResult = grantResults[0];//相机权限
				boolean cameraGranted = cameraResult == PackageManager.PERMISSION_GRANTED;//拍照权限
				if (cameraGranted) {

					Log.e("cameradebug1", "checkSelfPermission");

				} else {
					//不具有相关权限，给予用户提醒，比如Toast或者对话框，让用户去系统设置-应用管理里把相关权限开启
				}
			}
		}
	}

}
