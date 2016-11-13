/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.platform.comapi.map.E;
import com.google.gson.Gson;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager.EMGroupOptions;
import com.hyphenate.chat.EMGroupManager.EMGroupStyle;
import com.hyphenate.easeui.bean.Group;
import com.hyphenate.easeui.utils.EaseImageUtils;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.exceptions.HyphenateException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.bean.ResultContact;
import cn.ucai.superwechat.data.NetDao;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;

public class NewGroupActivity extends BaseActivity {
    private static final int REQUESTCODE_PICK = 1;
    private static final int REQUESTCODE_CUTTING = 2;
    Bitmap bitmap;

    @Bind(R.id.m_New_Group_Avatar)
    ImageView mNewGroupAvatar;
    private EditText groupNameEditText;
    private ProgressDialog progressDialog;
    private EditText introductionEditText;
    private CheckBox publibCheckBox;
    private CheckBox memberCheckbox;
    private TextView secondTextView;



    String groupId;
    String groupName;
    String desc;
    EMGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.em_activity_new_group);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        groupNameEditText = (EditText) findViewById(R.id.edit_group_name);
        introductionEditText = (EditText) findViewById(R.id.edit_group_introduction);
        publibCheckBox = (CheckBox) findViewById(R.id.cb_public);
        memberCheckbox = (CheckBox) findViewById(R.id.cb_member_inviter);
        secondTextView = (TextView) findViewById(R.id.second_desc);

        publibCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    secondTextView.setText(R.string.join_need_owner_approval);
                } else {
                    secondTextView.setText(R.string.Open_group_members_invited);
                }
            }
        });
    }

    /**
     * @param v
     */
    public void save(View v) {
        String name = groupNameEditText.getText().toString();
        if (TextUtils.isEmpty(name)) {
            new EaseAlertDialog(this, R.string.Group_name_cannot_be_empty).show();
        } else {
            // select from contact list
            startActivityForResult(new Intent(this, GroupPickContactsActivity.class).putExtra("groupName", name), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String st1 = getResources().getString(R.string.Is_to_create_a_group_chat);
        final String st2 = getResources().getString(R.string.Failed_to_create_groups);
        switch (requestCode){
            case 0:
                if (resultCode == RESULT_OK) {
                    //new group
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage(st1);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            groupName = groupNameEditText.getText().toString().trim();
                            desc = introductionEditText.getText().toString();
                            String[] members = data.getStringArrayExtra("newmembers");
                            try {
                                EMGroupOptions option = new EMGroupOptions();
                                option.maxUsers = 200;

                                String reason = NewGroupActivity.this.getString(R.string.invite_join_group);
                                reason = EMClient.getInstance().getCurrentUser() + reason + groupName;

                                if (publibCheckBox.isChecked()) {
                                    option.style = memberCheckbox.isChecked() ? EMGroupStyle.EMGroupStylePublicJoinNeedApproval : EMGroupStyle.EMGroupStylePublicOpenJoin;
                                } else {
                                    option.style = memberCheckbox.isChecked() ? EMGroupStyle.EMGroupStylePrivateMemberCanInvite : EMGroupStyle.EMGroupStylePrivateOnlyOwnerInvite;
                                }
                                group = EMClient.getInstance().groupManager().createGroup(groupName, desc, members, reason, option);
                                groupId = group.getGroupId();

                                //下面有耗时操作，所以放到新线程里面去
                                new Thread(){
                                    @Override
                                    public void run() {
                                        createAppGroup(groupId,groupName,desc,
                                                EMClient.getInstance().getCurrentUser(),publibCheckBox.isChecked(),
                                                memberCheckbox.isChecked());
                                    }
                                }.start();
                            } catch (final HyphenateException e) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }).start();
                }
                break;
            case REQUESTCODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case REQUESTCODE_CUTTING:
                if (data != null) {
                    //upDataAppUserAvatar(data);
                    upDataAppGroupAvatar(data);
                }
                break;
            default:
                break;
        }
    }

    private void createAppGroup(final String groupId, String groupName, String desc, String currentUser, boolean checked, boolean checked1) {
        //这里最好放到新的线程里面去
        String filePath= EaseImageUtils.getImagePath(groupId+ I.AVATAR_SUFFIX_JPG);
        L.e("filePath:"+filePath);
        File file =savaBitMapFile(bitmap,filePath);
        if(file!=null){
            L.e("FileAbsPath"+file.getAbsolutePath());
        }
        if(file==null){
            NetDao.createGroup(this, groupId, groupName, desc, currentUser, checked, checked1, new OkHttpUtils.OnCompleteListener<String>() {
                @Override
                public void onSuccess(String s) {
                    progressDialog.dismiss();
                    L.e("wenyong",s.toString());
                    Gson gson = new Gson();
                    ResultContact resultContact = gson.fromJson(s,ResultContact.class);
                    if(resultContact!=null&&resultContact.isRetMsg()){
                        createGroupOnscucess();
                    }else {
                        createGroupFinal(groupId);
                    }

                }

                @Override
                public void onError(String error) {
                    try {
                        CommonUtils.showShortToast(error+"");
                        EMClient.getInstance().groupManager().destroyGroup(groupId);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                finish();
                            }
                        });
                    } catch (HyphenateException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                finish();
                            }
                        });
                        e.printStackTrace();
                    }
                }
            });
            return;
        }
        NetDao.createGroup(this, groupId, groupName, desc, currentUser, checked, checked1, file, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                progressDialog.dismiss();
                CommonUtils.showShortToast("创建群主成功");
                L.e("wenyong",s.toString());
                Gson gson = new Gson();
                ResultContact resultContact = gson.fromJson(s,ResultContact.class);
                if(resultContact!=null&&resultContact.isRetMsg()) {
                    if(group!=null&&group.getMembers()!=null&&group.getMembers().size()>1){
                        addGroupMember();
                    }else {
                        createGroupOnscucess();
                    }
                    String jsonstr = resultContact.getRetData().toString();
                    Group group = gson.fromJson(jsonstr,Group.class);
                    L.e("wenyong",group.toString());

                }else {
                    CommonUtils.showShortToast("创建群组失败");
                    createGroupFinal(groupId);
                }
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                CommonUtils.showShortToast(error+"");
                try {
                    EMClient.getInstance().groupManager().destroyGroup(groupId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }
        });

    }

    private void addGroupMember() {
        NetDao.addGooupMeMeber(this, group, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                Gson gson = new Gson();
                if(s!=null){
                    ResultContact resultContact =gson.fromJson(s,ResultContact.class);
                    if(resultContact!=null&&resultContact.getRetData()!=null){
                        createGroupOnscucess();
                    }
                }
            }

            @Override
            public void onError(String error) {
                CommonUtils.showShortToast("添加群成员失败");
                progressDialog.dismiss();
                MFGT.finish(NewGroupActivity.this);
            }
        });
    }

    private void createGroupFinal(String groupId) {
        try {
            EMClient.getInstance().groupManager().destroyGroup(groupId);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }

    private void createGroupOnscucess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void upDataAppGroupAvatar(Intent data) {
        ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));
        dialog.show();
        //String filePath= EaseImageUtils.getImagePath(user.getMUserName()+ I.AVATAR_SUFFIX_PNG);
//        L.e("filePath:"+filePath);
//        File file =savaBitMapFile(data,filePath) ;
//        L.e("FileAbsPath"+file.getAbsolutePath());
        Bundle extra = data.getExtras();
        bitmap = extra.getParcelable("data");
        mNewGroupAvatar.setImageBitmap(bitmap);
        dialog.dismiss();
    }

    public void back(View view) {
        finish();
    }

    @OnClick(R.id.m_New_Group_Avatar_Lin)
    public void onClick() {
        UserProfileActivity activity = new UserProfileActivity();
        uploadHeadPhoto();
    }

    private void uploadHeadPhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dl_title_upload_photo);
        builder.setItems(new String[]{getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Toast.makeText(NewGroupActivity.this, getString(R.string.toast_no_support),
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

    public File savaBitMapFile(Bitmap bitmap1,String absPath){
        if(true){
            Bitmap bitmap =bitmap1;
            File file = new File(absPath);//要保存图片的路径
            BufferedOutputStream bos=null;
            if(bitmap==null){
                return null;
            }
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




}
