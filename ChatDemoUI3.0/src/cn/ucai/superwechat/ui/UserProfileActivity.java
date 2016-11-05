package cn.ucai.superwechat.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.bean.User;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.utils.EaseImageUtils;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.NetDao;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;
import cn.ucai.superwechat.video.util.Utils;

public class UserProfileActivity extends BaseActivity implements OnClickListener {

    private static final int REQUESTCODE_PICK = 1;
    private static final int REQUESTCODE_CUTTING = 2;
    @Bind(R.id.iv_profile_avatar)
    ImageView ivProfileAvatar;
    @Bind(R.id.tv_profile_nickname)
    TextView tvProfileNickname;
    @Bind(R.id.tv_profile_weixinhao)
    TextView tvProfileWeixinhao;
    private ProgressDialog dialog;
    private RelativeLayout rlNickName;


    User user;

    @Override
    protected void onCreate(Bundle arg0) {
        setContentView(R.layout.em_activity_user_profile);
        super.onCreate(arg0);
        ButterKnife.bind(this);
        initView();
        initData();
        initListener();
    }

    private void initData() {
        user=EaseUserUtils.getCurrenttAppUserInfo();
    }

    private void initView() {
    }

    private void initListener() {
        EaseUserUtils.setCurrentAppUserAvatar(this,ivProfileAvatar);
        EaseUserUtils.setCurrentAppUserNick(tvProfileNickname);
        EaseUserUtils.setCurrentAppUserName(tvProfileWeixinhao);
    }

    public void asyncFetchUserInfo(String username) {
        SuperWeChatHelper.getInstance().getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<EaseUser>() {

            @Override
            public void onSuccess(EaseUser user) {
                if (user != null) {
                    SuperWeChatHelper.getInstance().saveContact(user);
                    if (isFinishing()) {
                        return;
                    }

					/*User user1 = SuperWeChatHelper.getInstance().getUser();
					tvProfileNickname.setText(user1.getMUserNick());*/

                    tvProfileNickname.setText(user.getNick());

                    if (!TextUtils.isEmpty(user.getAvatar())) {
                        Glide.with(UserProfileActivity.this).load(user.getAvatar()).placeholder(R.drawable.default_avatar_1).into(ivProfileAvatar);
                    } else {
                        Glide.with(UserProfileActivity.this).load(R.drawable.default_avatar_1).into(ivProfileAvatar);
                    }
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
            }
        });
    }


    private void uploadHeadPhoto() {
        Builder builder = new Builder(this);
        builder.setTitle(R.string.dl_title_upload_photo);
        builder.setItems(new String[]{getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Toast.makeText(UserProfileActivity.this, getString(R.string.toast_no_support),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, REQUESTCODE_PICK);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }


    private void updateRemoteNick(final String nickName) {
        dialog = ProgressDialog.show(this, getString(R.string.dl_update_nick), getString(R.string.dl_waiting));
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean updatenick = SuperWeChatHelper.getInstance().getUserProfileManager().updateCurrentUserNickName(nickName);
                if (UserProfileActivity.this.isFinishing()) {
                    return;
                }
                if (!updatenick) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT)
                                    .show();
                            dialog.dismiss();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //现在更新我们自己服务器的数据
                            upDataAppUserNick(nickName);
                        }
                    });
                }
            }
        }).start();
    }

    private void upDataAppUserNick(String nickName) {
        NetDao.updateNick(this, user.getMUserName(), nickName, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                L.e(s.toString());
                Gson gson = new Gson();
                if(s==null){
                    CommonUtils.showShortToast("更新昵称失败");
                    dialog.dismiss();
                    return;
                }
                Result result = gson.fromJson(s,Result.class);
                if(!result.isRetMsg()){
                    CommonUtils.showShortToast("更新昵称失败");
                    dialog.dismiss();
                    return;
                }
                if(result.getRetData()==null){
                    CommonUtils.showShortToast("更新昵称失败");
                    dialog.dismiss();
                    return;
                }
                String userJson =  result.getRetData().toString();
                User user = gson.fromJson(userJson,User.class);
                if(user==null){
                    dialog.dismiss();
                    CommonUtils.showShortToast("更新昵称失败");
                    return;
                }
                updataLocaUser(user);
            }

            @Override
            public void onError(String error) {
                dialog.dismiss();
                CommonUtils.showShortToast("更新昵称失败");
            }
        });
    }
    private void updataLocaUser(User user) {
        SuperWeChatHelper.getInstance().saveAppContact(user);
        EaseUserUtils.setCurrentAppUserNick(tvProfileNickname);
        this.user=user;
        dialog.dismiss();
        CommonUtils.showShortToast("更新昵称成功");
    }
    private void updataLocaUserAvatar(User user) {
        SuperWeChatHelper.getInstance().saveAppContact(user);
        this.user=user;
        dialog.dismiss();
        CommonUtils.showShortToast("更新头像成功");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case REQUESTCODE_CUTTING:
                if (data != null) {
                    upDataAppUserAvatar(data);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void upDataAppUserAvatar(final Intent pidData) {
        dialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));
        dialog.show();
        String filePath= EaseImageUtils.getImagePath(user.getMUserName()+ I.AVATAR_SUFFIX_PNG);
        L.e("filePath:"+filePath);
        File file =savaBitMapFile(pidData,filePath) ;
        L.e("FileAbsPath"+file.getAbsolutePath());
        NetDao.updateAvatar(this, user.getMUserName(), file, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                L.e("修改头像"+s);
                Gson gson = new Gson();
                if(s==null){
                    CommonUtils.showShortToast("上传头像失败");
                    dialog.dismiss();
                    return;
                }
                Result result = gson.fromJson(s,Result.class);
                if(result==null){
                    CommonUtils.showShortToast("上传头像失败");
                    dialog.dismiss();
                    return;
                }
                if(!result.isRetMsg()){
                    CommonUtils.showShortToast("上传头像失败");
                    dialog.dismiss();
                    return;
                }
                if(result.getRetData()==null){
                    CommonUtils.showShortToast("上传头像失败");
                    dialog.dismiss();
                    return;
                }
                String userJson =  result.getRetData().toString();
                User user = gson.fromJson(userJson,User.class);
                if(user==null){
                    dialog.dismiss();
                    CommonUtils.showShortToast("上传头像失败");
                    return;
                }
                L.e("修改头像"+user.toString());
                updataLocaUserAvatar(user);
                setPicToView(pidData);

            }

            @Override
            public void onError(String error) {
                L.e(error);
                CommonUtils.showShortToast("上传头像失败");
            }
        });
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    /**
     * save the picture data
     *
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(getResources(), photo);
            ivProfileAvatar.setImageDrawable(drawable);
            uploadUserAvatar(Bitmap2Bytes(photo));
            L.e("setPicToView");
            dialog.dismiss();
        }
    }

    private void uploadUserAvatar(final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String avatarUrl = SuperWeChatHelper.getInstance().getUserProfileManager().uploadUserAvatar(data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        if (avatarUrl != null) {
                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_success),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_fail),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        }).start();
    }


    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    @OnClick({R.id.m_UserProfile_Titile_back, R.id.layout_title, R.id.layout_profile_nick, R.id.layout_weixinhao})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.m_UserProfile_Titile_back:
                MFGT.finish(this);
                break;
            case R.id.layout_title:
                uploadHeadPhoto();
                break;
            case R.id.layout_profile_nick:
                final EditText editText = new EditText(this);
                editText.setText(user.getMUserNick());
                new Builder(this).setTitle(R.string.setting_nickname).setIcon(android.R.drawable.ic_dialog_info).setView(editText)
                        .setPositiveButton(R.string.dl_ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String nickString = editText.getText().toString();
                                if (TextUtils.isEmpty(nickString)) {
                                    Toast.makeText(UserProfileActivity.this, getString(R.string.toast_nick_not_isnull), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if(user.getMUserNick().equals(nickString)){
                                    CommonUtils.showShortToast("昵称未修改");
                                    return;
                                }
                                updateRemoteNick(nickString);
                            }
                        }).setNegativeButton(R.string.dl_cancel, null).show();
                break;
            case R.id.layout_weixinhao:
                CommonUtils.showShortToast("账号不能被修改");
                break;
        }
    }




    public File savaBitMapFile(Intent data,String absPath){
        Bundle extra = data.getExtras();
        if(extra!=null){
            Bitmap bitmap = extra.getParcelable("data");
            File file = new File(absPath);//要保存图片的路径
            BufferedOutputStream bos=null;
            try {
                bos= new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.PNG,100,bos);
                bos.close();
                bos.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(bos!=null){
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return file;
        }
        return null;
    }


    @Override
    protected void onDestroy() {
        if(dialog!=null){
            dialog.dismiss();
        }
        super.onDestroy();
    }
}
