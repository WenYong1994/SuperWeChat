package cn.ucai.superwechat.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.bean.User;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.utils.MFGT;

public class SendAddFriendActivity extends AppCompatActivity {

    @Bind(R.id.m_Send_Friend_Edit)
    EditText mSendFriendEdit;
    //用来存储需要添加的用户
    User mUser;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_send_add_friend);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        initDate();
    }

    private void initDate() {

        mUser = (User) getIntent().getSerializableExtra("senduser");
        if (mUser == null) {
            return;
        }
        mSendFriendEdit.setText("我是" + SuperWeChatHelper.getInstance().getUser().getMUserNick());
    }

    @OnClick(R.id.m_Send_Friend_Btn)
    public void onClick() {
        progressDialog = new ProgressDialog(this);
        String stri = getResources().getString(R.string.Is_sending_a_request);
        progressDialog.setMessage(stri);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() {
            public void run() {

                try {
                    //demo use a hardcode reason here, you need let user to input if you like
                    String s = mSendFriendEdit.getText().toString();
                    EMClient.getInstance().contactManager().addContact(mUser.getMUserName(), s);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s1 = getResources().getString(R.string.send_successful);
                            Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
                            MFGT.finish(SendAddFriendActivity.this);
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();


    }

    @OnClick(R.id.m_Send_Friend_Btn)
    public void onTitleBack() {
        MFGT.finish(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MFGT.finish(this);
    }

    @OnClick(R.id.m_Send_Friend_Btn)
    public void onBtnBack() {
        MFGT.finish(this);
    }
}
