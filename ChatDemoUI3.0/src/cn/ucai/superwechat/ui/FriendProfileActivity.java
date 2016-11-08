package cn.ucai.superwechat.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.bean.User;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseAlertDialog;

import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.db.UserDao;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;

public class FriendProfileActivity extends BaseActivity {

    public static final int IS_ONLINE=1;
    public static final int NOT_ONLINE=-1020;

    @Bind(R.id.m_Frient_Profile_Iv)
    ImageView mFrientProfileIv;
    @Bind(R.id.m_Frient_Profile_Nick)
    TextView mFrientProfileNick;
    @Bind(R.id.m_Frient_Profile_Add)
    LinearLayout mFrientProfileAdd;
    @Bind(R.id.m_Frient_Profile_SendMessage)
    LinearLayout mFrientProfileSendMessage;
    @Bind(R.id.m_Frient_Profile_MP4)
    LinearLayout mFrientProfileMP4;

    User mUser;
    @Bind(R.id.m_Frient_Profile_Name)
    TextView mFrientProfileName;

    Map<String,User> mContactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_friend_profile);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        initDate();
    }

    private void initDate() {
        Intent intent = getIntent();
        mUser = (User) intent.getSerializableExtra("user");
        if (mUser == null) {
            return;
        }
        EaseUserUtils.setAppUserNick(mUser.getMUserNick(), mFrientProfileNick);
        EaseUserUtils.setAppUserAvatar(this, mUser.getMUserName(), mFrientProfileIv, mUser);
        EaseUserUtils.setAppUserNameWithNo(mUser.getMUserName(),mFrientProfileName);
        if(!SuperWeChatHelper.getInstance().getContactList().containsKey(mUser.getMUserName())){
            //代表不是联系人也不是自己
            mFrientProfileAdd.setVisibility(View.VISIBLE);
        }else {
            //代表是联系人或者是自己
            mFrientProfileMP4.setVisibility(View.VISIBLE);
            mFrientProfileSendMessage.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.m_Frient_Profile_Title_Back, R.id.m_Frient_Profile_Add_Btn, R.id.m_Frient_Profile_SendMessage_Btn, R.id.m_Frient_Profile_MP4e_Btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.m_Frient_Profile_Title_Back:
                MFGT.finish(this);
                break;
            case R.id.m_Frient_Profile_Add_Btn:
                if(mUser.getMUserName().equals(SuperWeChatHelper.getInstance().getCurrentUsernName())){
                    CommonUtils.showShortToast("不能添加自己为好友");
                    return;
                }
                Intent intent = new Intent(this,SendAddFriendActivity.class);
                intent.putExtra("senduser",mUser);
                MFGT.startActivity(this,intent);
                MFGT.finish(this);
                break;
            case R.id.m_Frient_Profile_SendMessage_Btn:
                MFGT.finish(this);
                startActivity(new Intent(this, ChatActivity.class).putExtra("userId", mUser.getMUserName()));
                break;
            case R.id.m_Frient_Profile_MP4e_Btn:
                EMClient.getInstance().isConnected();
                Intent intent1 = new Intent(this,VideoCallActivity.class);
                intent1.putExtra("isComingCall",false);
                intent1.putExtra("username",mUser.getMUserName());
                startActivityForResult(intent1,IS_ONLINE);
                break;
        }
    }


    //这是为了判断对方是否在线
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case IS_ONLINE:
                if(resultCode==NOT_ONLINE){
                    CommonUtils.showShortToast("对方不在线");
                }else if(resultCode==RESULT_OK){
                    MFGT.finish(this);
                }
                break;
        }
    }
}
