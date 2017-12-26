package com.cyanbirds.momo.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.base.BaseActivity;
import com.cyanbirds.momo.config.AppConstants;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.eventtype.UserEvent;
import com.cyanbirds.momo.listener.ModifyUserInfoListener;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.net.request.DownloadFileRequest;
import com.cyanbirds.momo.net.request.OSSImagUploadRequest;
import com.cyanbirds.momo.net.request.UpdateUserInfoRequest;
import com.cyanbirds.momo.ui.widget.ClearEditText;
import com.cyanbirds.momo.utils.FileAccessorUtils;
import com.cyanbirds.momo.utils.FileUtils;
import com.cyanbirds.momo.utils.Md5Util;
import com.cyanbirds.momo.utils.ProgressDialogUtils;
import com.cyanbirds.momo.utils.ToastUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Cloudsoar(wangyb)
 * @datetime 2016-01-08 21:17 GMT+8
 * @email 395044952@qq.com
 */
public class ModifyUserInfoNewActivity extends BaseActivity implements ModifyUserInfoListener.OnModifyUserSignatureListener {

	@BindView(R.id.portrait_photo)
	SimpleDraweeView mPortraitPhoto;
	@BindView(R.id.nick_name)
	TextView mNickName;
	@BindView(R.id.nick_name_lay)
	RelativeLayout mNickNameLay;
	@BindView(R.id.sex)
	TextView mSex;
	@BindView(R.id.sex_lay)
	RelativeLayout mSexLay;
	@BindView(R.id.age)
	TextView mAge;
	@BindView(R.id.age_lay)
	RelativeLayout mAgeLay;
	@BindView(R.id.signature)
	TextView mSignature;
	@BindView(R.id.signature_lay)
	RelativeLayout mSignatureLay;
	@BindView(R.id.occupation)
	TextView mOccupation;
	@BindView(R.id.occupation_type_lay)
	RelativeLayout mOccupationTypeLay;
	@BindView(R.id.education)
	TextView mEducation;
	@BindView(R.id.education_type_lay)
	RelativeLayout mEducationTypeLay;
	@BindView(R.id.constellation)
	TextView mConstellation;
	@BindView(R.id.constellation_lay)
	RelativeLayout mConstellationLay;
	@BindView(R.id.tall)
	TextView mTall;
	@BindView(R.id.tall_lay)
	RelativeLayout mTallLay;
	@BindView(R.id.weight)
	TextView mWeight;
	@BindView(R.id.weight_lay)
	RelativeLayout mWeightLay;
	@BindView(R.id.married)
	TextView mMarried;
	@BindView(R.id.married_lay)
	RelativeLayout mMarriedLay;


	private String mPhotoPath;
	private File mPhotoFile;
	private Uri mPhotoOnSDCardUri;
	private Uri mPortraitUri;
	private File mCutFile;


	private ClientUser clientUser;
	private String currentFaceUrl;//用于判断用户是否只是修改了头像就返回
	private List<String> mVals = null;

	/**
	 * 拍照返回
	 */
	public final static int CAMERA_RESULT = 101;
	/**
	 * 相册返回
	 */
	public final static int ALBUMS_RESULT = 102;
	/**
	 * 剪裁图片返回
	 */
	public final static int PHOTO_CUT_RESULT = 106;
	/**
	 * 读写文件夹
	 */
	private final int REQUEST_PERMISSION_WRITE = 1000;
	/**
	 * 跳转设置界面
	 */
	private final int REQUEST_PERMISSION_SETTING = 10001;
	/**
	 * 是否拥有读写权限
	 */
	private boolean isWritePersimmion = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_userinfo_new);
		ButterKnife.bind(this);
		Toolbar toolbar = getActionBarToolbar();
		if (toolbar != null) {
			toolbar.setNavigationIcon(R.mipmap.ic_up);
		}
		setupViews();
		setupEvent();
		setupData();
	}

	private void setupViews() {
	}

	private void setupEvent() {
		ModifyUserInfoListener.getInstance().setModifyUserSignatureListener(this);
	}

	private void setupData() {
		mVals = new ArrayList<>();
		setUserInfo();
	}

	private void setUserInfo() {
		clientUser = AppManager.getClientUser();
		if (clientUser != null) {
			currentFaceUrl = clientUser.face_url;
			if (!TextUtils.isEmpty(clientUser.face_local) && new File(clientUser.face_local).exists()) {
				mPortraitPhoto.setImageURI(Uri.parse("file://" + clientUser.face_local));
			} else if (!TextUtils.isEmpty(clientUser.face_url)) {
				mPortraitPhoto.setImageURI(Uri.parse(clientUser.face_url));
				new DownloadPortraitTask().request(clientUser.face_url,
						FileAccessorUtils.getImagePathName().getAbsolutePath(),
						Md5Util.md5(AppManager.getClientUser().face_url) + ".jpg");
			}
			if (!TextUtils.isEmpty(clientUser.user_name)) {
				mNickName.setText(clientUser.user_name);
			}
			if (!TextUtils.isEmpty(clientUser.sex)) {
				mSex.setText(clientUser.sex);
			}
			if (!TextUtils.isEmpty(String.valueOf(clientUser.age))) {
				mAge.setText(String.valueOf(clientUser.age));
			}
			if (!TextUtils.isEmpty(clientUser.signature)) {
				mSignature.setText(clientUser.signature);
			}
			if (!TextUtils.isEmpty(clientUser.occupation)) {
				mOccupation.setText(clientUser.occupation);
			}
			if (!TextUtils.isEmpty(clientUser.education)) {
				mEducation.setText(clientUser.education);
			}
			if (!TextUtils.isEmpty(clientUser.state_marry)) {
				mMarried.setText(clientUser.state_marry);
			}
			if (!TextUtils.isEmpty(clientUser.tall)) {
				mTall.setText(clientUser.tall);
			}
			if (!TextUtils.isEmpty(clientUser.weight)) {
				mWeight.setText(clientUser.weight);
			}
			if (!TextUtils.isEmpty(clientUser.constellation)) {
				mConstellation.setText(clientUser.constellation);
			}
		}
	}

	@OnClick({R.id.portrait_lay, R.id.nick_name_lay, R.id.sex_lay, R.id.age_lay, R.id.signature_lay,
			R.id.occupation_type_lay, R.id.education_type_lay, R.id.constellation_lay, R.id.tall_lay, R.id.weight_lay,
			R.id.married_lay})
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.portrait_lay:
				showPortraitDialog();
				break;
			case R.id.nick_name_lay:
				showEditDialog(mNickName, R.string.nick_name);
				break;
			case R.id.sex_lay:
				break;
			case R.id.age_lay:
				showSelectAgeDialog();
				break;
			case R.id.signature_lay:
				Intent intent = new Intent(ModifyUserInfoNewActivity.this, ModifySignatureActivity.class);
				intent.putExtra(ValueKey.SIGNATURE, mSignature.getText().toString().trim());
				startActivity(intent);
				break;
			case R.id.occupation_type_lay:
				showSelectOccupationDialog();
				break;
			case R.id.education_type_lay:
				showSelectEducationDialog();
				break;
			case R.id.constellation_lay:
				showConstellationDialog();
				break;
			case R.id.tall_lay:
				showTallDialog();
				break;
			case R.id.weight_lay:
				showWeightDialog();
				break;
			case R.id.married_lay:
				showMarriedSelectDialog();
				break;
		}
	}

	/**
	 * 下载头像
	 */
	class DownloadPortraitTask extends DownloadFileRequest {
		@Override
		public void onPostExecute(String s) {
			ClientUser clientUser = AppManager.getClientUser();
			clientUser.face_local = s;
			AppManager.setClientUser(clientUser);
		}

		@Override
		public void onErrorExecute(String error) {
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.save_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() ==  R.id.save) {
			ProgressDialogUtils.getInstance(this).show(R.string.dialog_save_data);
			AppManager.setClientUser(clientUser);
			AppManager.saveUserInfo();
			EventBus.getDefault().post(new UserEvent());
			if (clientUser != null) {
				new UpdateUserInfoTask().request(clientUser);
			}
		} else {
			finish();
		}

		return true;
	}

	class UpdateUserInfoTask extends UpdateUserInfoRequest {
		@Override
		public void onPostExecute(String s) {
			ToastUtil.showMessage(R.string.save_success);
			ProgressDialogUtils.getInstance(ModifyUserInfoNewActivity.this).dismiss();
			finish();
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(R.string.save_fail);
			ProgressDialogUtils.getInstance(ModifyUserInfoNewActivity.this).dismiss();
		}
	}


	/**
	 * 头像
	 */
	private void showPortraitDialog() {
		Builder builder = new Builder(this);
		builder.setTitle(getResources().getString(R.string.img_from));
		builder.setNegativeButton(getResources().getString(R.string.cancel),
				null);
		builder.setItems(
				new String[]{getResources().getString(R.string.photograph),
						getResources().getString(R.string.albums)},
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								if (AppManager.checkPermission(ModifyUserInfoNewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PERMISSION_WRITE)) {
									isWritePersimmion = true;
									if (AppManager.checkPermission(ModifyUserInfoNewActivity.this, Manifest.permission.CAMERA, CAMERA_RESULT)) {
										openCamera();
									}
								}
								break;
							case 1:
								openAlbums();
								break;
						}
						dialog.dismiss();
					}
				});
		builder.show();
	}

	/**
	 * 年龄
	 */
	private void showSelectAgeDialog(){
		final String[] array = getResources().getStringArray(R.array.age);
		Builder builder = new Builder(this);
		builder.setTitle("年龄");
		builder.setSingleChoiceItems(array, 7, null);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
				clientUser.age = Integer.parseInt(array[selectedPosition]);
				mAge.setText(array[selectedPosition]);
			}
		});
		builder.show();
	}

	/**
	 * 职业
	 */
	private void showSelectOccupationDialog(){
		final String[] array = getResources().getStringArray(R.array.occupation);
		Builder builder = new Builder(this);
		builder.setSingleChoiceItems(array, 1, null);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
				clientUser.occupation = array[selectedPosition];
				mOccupation.setText(array[selectedPosition]);
			}
		});
		builder.show();
	}

	/**
	 * 学历
	 */
	private void showSelectEducationDialog(){
		final String[] array = getResources().getStringArray(R.array.education);
		Builder builder = new Builder(this);
		builder.setSingleChoiceItems(array, 5, null);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
				clientUser.education = array[selectedPosition];
				mEducation.setText(array[selectedPosition]);
			}
		});
		builder.show();
	}

	/**
	 * 情感状态选择Dialog
	 */
	private void showMarriedSelectDialog() {

		Builder builder = new Builder(this);
		builder.setTitle(R.string.married_state);
		final String[] marryArray = getResources().getStringArray(R.array.marry_state);
		builder.setSingleChoiceItems(marryArray, 10, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mMarried.setText(marryArray[which]);
				clientUser.state_marry = marryArray[which];
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.show();
	}

	/**
	 * 身高
	 */
	private void showTallDialog() {
		Builder builder = new Builder(this);
		builder.setTitle(R.string.tall);
		final String[] tallArray = getResources().getStringArray(R.array.tall);
		builder.setSingleChoiceItems(tallArray, 10, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mTall.setText(tallArray[which]);
				clientUser.tall = tallArray[which];
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.show();
	}

	/**
	 * 体重
	 */
	private void showWeightDialog() {
		Builder builder = new Builder(this);
		builder.setTitle(R.string.weight);
		final String[] weightArray = getResources().getStringArray(R.array.weight);
		builder.setSingleChoiceItems(weightArray, 10, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mWeight.setText(weightArray[which]);
				clientUser.weight = weightArray[which];
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.show();
	}

	/**
	 * 星座
	 */
	private void showConstellationDialog() {
		Builder builder = new Builder(this);
		builder.setTitle(R.string.constellation);
		final String[] constellationArray = getResources().getStringArray(R.array.constellation);
		builder.setSingleChoiceItems(constellationArray, 10, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mConstellation.setText(constellationArray[which]);
				clientUser.constellation = constellationArray[which];
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.show();
	}


	private void showEditDialog(final TextView mText, final int resourceId) {
		Builder builder = new Builder(this);
		builder.setTitle(getResources().getString(resourceId));
		final ClearEditText input = new ClearEditText(ModifyUserInfoNewActivity.this);
		input.setText(mText.getText().toString());
		input.setSelection(mText.getText().toString().length());
		LayoutParams lp = new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		input.setLayoutParams(lp);
		builder.setNegativeButton(getResources().getString(R.string.cancel),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.setPositiveButton(getResources().getString(R.string.ok),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!TextUtils.isEmpty(input.getText().toString().trim())) {
							mText.setText(input.getText().toString());
						}
						if (resourceId == R.string.nick_name) {
							if (!TextUtils.isEmpty(input.getText().toString().trim())) {
								clientUser.user_name = input.getText().toString();
							}
						} else if (resourceId == R.string.weixin) {
							if (!TextUtils.isEmpty(input.getText().toString().trim())) {
								clientUser.weixin_no = input.getText().toString();
							}
						} else {
							if (!TextUtils.isEmpty(input.getText().toString().trim())) {
								clientUser.qq_no = input.getText().toString();
							}
						}

					}
				});
		builder.setView(input);
		builder.show();
	}

	@Override
	public void notifyUserSignatureChanged(String signature) {
		mSignature.setText(signature);
		clientUser.signature = signature;
	}


	/**
	 * 打开相机
	 */
	private void openCamera() {
		hideSoftKeyboard();
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (intent.resolveActivity(getPackageManager())!=null){
			String mPhotoDirPath = Environment
					.getExternalStoragePublicDirectory(
							Environment.DIRECTORY_DCIM).getPath();
			File mPhotoDirFile = new File(mPhotoDirPath);
			if (!mPhotoDirFile.exists()) {
				mPhotoDirFile.mkdir();
			}
			mPhotoPath = mPhotoDirPath + File.separator + getPhotoFileName();
			mPhotoFile = new File(mPhotoPath);
			if (!mPhotoFile.exists()) {
				try {
					mPhotoFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (mPhotoFile != null) {
				//FileProvider 是一个特殊的 ContentProvider 的子类，
				//它使用 content:// Uri 代替了 file:/// Uri. ，更便利而且安全的为另一个app分享文件
				mPhotoOnSDCardUri = FileProvider.getUriForFile(this,
						"com.cyanbirds.momo.fileProvider",
						mPhotoFile);
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoOnSDCardUri);
				startActivityForResult(intent, CAMERA_RESULT);
			}
		}
	}

	/**
	 * 返回图片文件名
	 *
	 * @return
	 */
	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}

	/**
	 * 打开相册
	 */
	private void openAlbums() {
		Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
		openAlbumIntent.setType("image/*");
		startActivityForResult(openAlbumIntent, ALBUMS_RESULT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == CAMERA_RESULT) {
			Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
					mPhotoOnSDCardUri);
			sendBroadcast(intent);
			if (mPhotoOnSDCardUri != null) {
				File file = new File(mPhotoPath);
				if (file.exists()) {
					cutPhoto(file);
				}
			}
		} else if (resultCode == RESULT_OK && requestCode == ALBUMS_RESULT) {
			Uri originalUri = data.getData();
			if (!TextUtils.isEmpty(FileUtils.getPath(this, originalUri))) {
				cutPhoto(new File(FileUtils.getPath(this, originalUri)));
			}
		} else if (resultCode == RESULT_OK && requestCode == PHOTO_CUT_RESULT) {
			mPortraitUri = data.getData();
			if (mPortraitUri == null && mCutFile != null) {
				mPortraitUri = Uri.parse(mCutFile.getPath());
			}
			if (mPortraitUri != null
					&& new File(mPortraitUri.getPath()).exists()) {
				ProgressDialogUtils.getInstance(this).show(R.string.dialog_request_uploda);
				new OSSImgUploadTask().request(AppManager.getFederationToken().bucketName,
						AppManager.getOSSFacePath(), mPortraitUri.getPath());
			}
		}
	}

	/**
	 * 上传图片至OSS
	 */
	class OSSImgUploadTask extends OSSImagUploadRequest {
		@Override
		public void onPostExecute(String s) {
			ProgressDialogUtils.getInstance(ModifyUserInfoNewActivity.this).dismiss();
			clientUser.face_url = AppConstants.OSS_IMG_ENDPOINT + s;
			clientUser.face_local = mPortraitUri.getPath();
//			mPortraitPhoto.setImageURI(Uri.parse("file://" + mPortraitUri.getPath()));
			mPortraitPhoto.setImageURI(clientUser.face_url);
			AppManager.setClientUser(clientUser);
			AppManager.saveUserInfo();
			EventBus.getDefault().post(new UserEvent());
			new UpdateUserTask().request(clientUser);
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(error);
		}
	}

	class UpdateUserTask extends UpdateUserInfoRequest {
		@Override
		public void onPostExecute(String s) {
			ToastUtil.showMessage(R.string.save_success);
			ProgressDialogUtils.getInstance(ModifyUserInfoNewActivity.this).dismiss();
		}

		@Override
		public void onErrorExecute(String error) {
			ToastUtil.showMessage(R.string.save_fail);
			ProgressDialogUtils.getInstance(ModifyUserInfoNewActivity.this).dismiss();
		}
	}

	/**
	 * 剪切图片
	 *
	 * @param file
	 */
	private void cutPhoto(File file) {
		mCutFile = new File(FileAccessorUtils.getImagePathName(),
				"cutphoto.png");
		if (!mCutFile.exists()) {
			try {
				mCutFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		Uri imageUri=FileProvider.getUriForFile(this, "com.cyanbirds.momo.fileProvider", file);//通过FileProvider创建一个content类型的Uri
		Uri imageUri = Uri.fromFile(file);
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.setDataAndType(imageUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("outputX", 500);
		intent.putExtra("outputY", 500);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCutFile));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		startActivityForResult(intent, PHOTO_CUT_RESULT);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getName());
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getName());
		MobclickAgent.onPause(this);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == CAMERA_RESULT) {
			// 拒绝授权
			if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
				// 勾选了不再提示
				if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
					showOpenCameraDialog();
				} else {
				}
			} else if (isWritePersimmion) {
				openCamera();
			}
		} else if (requestCode == REQUEST_PERMISSION_WRITE) {//读写文件夹权限
			// 拒绝授权
			if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
				// 勾选了不再提示
				if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
					showWriteDialog();
				}
			} else {
				isWritePersimmion = true;
				AppManager.checkPermission(this, Manifest.permission.CAMERA, CAMERA_RESULT);
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	private void showOpenCameraDialog(){
		Builder builder = new Builder(this);
		builder.setMessage(R.string.open_camera_permission);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", getPackageName(), null);
				intent.setData(uri);
				startActivityForResult(intent, REQUEST_PERMISSION_SETTING);

			}
		});
		builder.show();
	}

	private void showWriteDialog(){
		Builder builder = new Builder(this);
		builder.setMessage(R.string.open_write_external);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				Uri uri = Uri.fromParts("package", getPackageName(), null);
				intent.setData(uri);
				startActivityForResult(intent, REQUEST_PERMISSION_SETTING);

			}
		});
		builder.show();
	}
}
