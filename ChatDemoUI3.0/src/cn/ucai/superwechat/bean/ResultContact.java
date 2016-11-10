package cn.ucai.superwechat.bean;

/**
 * Created by Wy on 2016/11/10.
 */
public class ResultContact {


    /**
     * retCode : 0
     * retMsg : true
     * retData : {"muserName":"a952702","muserNick":"彭鹏","mavatarId":74,"mavatarPath":"user_avatar","mavatarSuffix":".jpg","mavatarType":0,"mavatarLastUpdateTime":"1476284146171"}
     */

    private int retCode;
    private boolean retMsg;
    /**
     * muserName : a952702
     * muserNick : 彭鹏
     * mavatarId : 74
     * mavatarPath : user_avatar
     * mavatarSuffix : .jpg
     * mavatarType : 0
     * mavatarLastUpdateTime : 1476284146171
     */

    private Object retData;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public boolean isRetMsg() {
        return retMsg;
    }

    public void setRetMsg(boolean retMsg) {
        this.retMsg = retMsg;
    }

    public Object getRetData() {
        return retData;
    }

    public void setRetData(String retData) {
        this.retData = retData;
    }

    @Override
    public String toString() {
        return "Result{" +
                "retCode=" + retCode +
                ", retMsg=" + retMsg +
                ", userAvatar=" + retData.toString()+
                '}';
    }
}
