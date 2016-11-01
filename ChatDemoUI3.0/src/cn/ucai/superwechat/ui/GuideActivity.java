package cn.ucai.superwechat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.utils.MFGT;

public class GuideActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_guide);
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.btn_Login, R.id.btn_Register})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_Login:
                MFGT.gotoLoginActivity(this);
                break;
            case R.id.btn_Register:
                MFGT.gotoRegisterActivity(this);
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
