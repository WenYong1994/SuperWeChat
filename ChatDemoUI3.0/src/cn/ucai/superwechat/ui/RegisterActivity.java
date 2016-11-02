/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
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

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.LeadingMarginSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.data.NetDao;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MD5;
import cn.ucai.superwechat.utils.MFGT;

/**
 * register screen
 *
 */
public class RegisterActivity extends BaseActivity {
    @Bind(R.id.mRegister_UserName)
    EditText mRegisterUserName;
    @Bind(R.id.mRegister_UserNick)
    EditText mRegisterUserNick;
    @Bind(R.id.mRegister_PassWrod)
    EditText mRegisterPassWrod;
    @Bind(R.id.mRegister_Ok_PassWrod)
    EditText mRegisterOkPassWrod;

    ProgressDialog pd =null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.em_activity_register);
        super.onCreate(savedInstanceState);
    }

    public void register() {
        final String username = mRegisterUserName.getText().toString().trim();
        final String pwd = mRegisterPassWrod.getText().toString().trim();
        String confirm_pwd = mRegisterOkPassWrod.getText().toString().trim();
        String userNick = mRegisterUserNick.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, getResources().getString(R.string.User_name_cannot_be_empty), Toast.LENGTH_SHORT).show();
            mRegisterUserName.requestFocus();
            return;
        } else if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, getResources().getString(R.string.Password_cannot_be_empty), Toast.LENGTH_SHORT).show();
            mRegisterPassWrod.requestFocus();
            return;
        } else if (TextUtils.isEmpty(confirm_pwd)) {
            Toast.makeText(this, getResources().getString(R.string.Confirm_password_cannot_be_empty), Toast.LENGTH_SHORT).show();
            mRegisterOkPassWrod.requestFocus();
            return;
        } else if (!pwd.equals(confirm_pwd)) {
            Toast.makeText(this, getResources().getString(R.string.Two_input_password), Toast.LENGTH_SHORT).show();
            return;
        } else  if (!username.matches("[a-zA-Z]\\w{5,15}")) {
            mRegisterUserName.setError("非法账号，5-15个字符，并且使用字母开头");
            mRegisterUserName.requestFocus();
            return ;
        }else if(!pwd.equals(confirm_pwd)){
            mRegisterOkPassWrod.setError("两次密码必须相同");
            mRegisterOkPassWrod.requestFocus();
            return;
        }
        pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.Is_the_registered));
        pd.show();
        registerApp(username,pwd,userNick);
    }

    //注册本地用户
    private void registerApp(final String username, final String pwd, final String userNick) {
        NetDao.register(RegisterActivity.this, username, userNick, pwd, new OkHttpUtils.OnCompleteListener<Result>() {
            @Override
            public void onSuccess(Result result) {
                if(result.isRetMsg()){
                    registerHX(username,pwd);
                }else {
                    if(result.getRetCode()!=101){
                        CommonUtils.showShortToast("注册失败");
                       deteUserNameApp(username);
                    }
                    if(!RegisterActivity.this.isFinishing()){
                        pd.dismiss();
                        CommonUtils.showShortToast("注册失败，账号已存在");
                    }

                }
            }

            @Override
            public void onError(String error) {
                pd.dismiss();
                CommonUtils.showShortToast("注册失败");
            }
        });
    }

    //在环信注册用户
    private void registerHX(final String username, final String pwd) {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        // call method in SDK
                        EMClient.getInstance().createAccount(username, MD5.getMessageDigest(pwd));
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (!RegisterActivity.this.isFinishing())
                                    pd.dismiss();
                                // save current user
                                SuperWeChatHelper.getInstance().setCurrentUserName(username);
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), Toast.LENGTH_SHORT).show();
                                MFGT.gotoLoginActivity(RegisterActivity.this);
                                MFGT.finish(RegisterActivity.this);
                            }
                        });
                    } catch (final HyphenateException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                deteUserNameApp(username);
                                if (!RegisterActivity.this.isFinishing())
                                    pd.dismiss();
                                int errorCode = e.getErrorCode();
                                if (errorCode == EMError.NETWORK_ERROR) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
                                } else if (errorCode == EMError.USER_ALREADY_EXIST) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();

                                } else if (errorCode == EMError.USER_AUTHENTICATION_FAILED) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
                                } else if (errorCode == EMError.USER_ILLEGAL_ARGUMENT) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                }
            }).start();

        }
    }

    //这里删除自己的服务器里面的数据
    private void deteUserNameApp(String userName) {
        NetDao.unRegister(RegisterActivity.this, userName, new OkHttpUtils.OnCompleteListener<Result>() {
            @Override
            public void onSuccess(Result result) {
                pd.dismiss();
            }
            @Override
            public void onError(String error) {
                pd.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        MFGT.finish(this);
    }

    @OnClick({R.id.common_back, R.id.mRegister_Btn_Register})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.common_back:
                MFGT.finish(this);
                break;
            case R.id.mRegister_Btn_Register:
                register();
                break;
        }
    }
}
