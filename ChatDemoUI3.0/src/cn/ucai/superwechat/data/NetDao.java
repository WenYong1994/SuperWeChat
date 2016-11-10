package cn.ucai.superwechat.data;

import android.content.Context;

import java.io.File;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.bean.ResultContact;
import cn.ucai.superwechat.utils.MD5;

/**
 * Created by Wy on 2016/11/1.
 */
public class NetDao {
    /**
     * 注册账号
     * @param context
     * @param userName
     * @param nickName
     * @param password
     * @param listener
     */
    public static void register(Context context, String userName, String nickName, String password, OkHttpUtils.OnCompleteListener<Result> listener) {
        OkHttpUtils<Result> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_REGISTER)
                .addParam(I.User.USER_NAME, userName)
                .addParam(I.User.NICK, nickName)
                .addParam(I.User.PASSWORD, MD5.getMessageDigest(password))
                .targetClass(Result.class)
                .post()
                .execute(listener);
    }

    public static void unRegister(Context context, String userName, OkHttpUtils.OnCompleteListener<Result> listener) {
        OkHttpUtils<Result> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_REGISTER)
                .addParam(I.User.USER_NAME, userName)
                .targetClass(Result.class)
                .execute(listener);
    }

    /**
     * 用户登录
     * @param context
     * @param userName
     * @param password
     * @param listener
     */
    public static void login(Context context, String userName, String password, OkHttpUtils.OnCompleteListener<String> listener) {
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_LOGIN)
                .addParam(I.User.USER_NAME, userName)
                .addParam(I.User.PASSWORD, MD5.getMessageDigest(password))
                .targetClass(String.class)
                .execute(listener);
    }

    /**
     * 更新用户昵称
     * @param context
     * @param userName
     * @param nick
     * @param listener
     */
    public static void updateNick(Context context, String userName, String nick, OkHttpUtils.OnCompleteListener<String> listener) {
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_UPDATE_USER_NICK)
                .addParam(I.User.USER_NAME, userName)
                .addParam(I.User.NICK, nick)
                .targetClass(String.class)
                .execute(listener);
    }

    /**
     * 跟新用户头像
     * @param context
     * @param userName
     * @param file
     * @param listener
     */
    public static void updateAvatar(Context context, String userName, File file, OkHttpUtils.OnCompleteListener<String> listener) {
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_UPDATE_AVATAR)
                .addParam(I.NAME_OR_HXID, userName)
                .addParam(I.AVATAR_TYPE,I.AVATAR_TYPE_USER_PATH)
                .addFile2(file)
                .targetClass(String.class)
                .post()
                .execute(listener);
    }


    /**
     * 更新用户信息
     * @param context
     * @param userName
     * @param listener
     */
    public static void syncUserInfo(Context context, String userName, OkHttpUtils.OnCompleteListener<String> listener) {
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_FIND_USER)
                .addParam(I.User.USER_NAME, userName)
                .targetClass(String.class)
                .execute(listener);
    }


    public static void findUserByUserName(Context context,String userName,OkHttpUtils.OnCompleteListener<Result> listener){
        OkHttpUtils<Result> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_FIND_USER)
                .addParam(I.User.USER_NAME, userName)
                .targetClass(Result.class)
                .execute(listener);
    }

    public static void addContact(Context context,String userName,String currUserName,OkHttpUtils.OnCompleteListener<Result> listener){
        OkHttpUtils<Result> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_ADD_CONTACT)
                .addParam(I.Contact.USER_NAME, userName)
                .addParam(I.Contact.CU_NAME,currUserName)
                .targetClass(Result.class)
                .execute(listener);
    }

    public static void deleteContact(Context context,String userName,String currUserName,OkHttpUtils.OnCompleteListener<Result> listener){
        OkHttpUtils<Result> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_DELETE_CONTACT)
                .addParam(I.Contact.USER_NAME, userName)
                .addParam(I.Contact.CU_NAME,currUserName)
                .targetClass(Result.class)
                .execute(listener);
    }


    public static void getContactList(Context context, String userName, OkHttpUtils.OnCompleteListener<ResultContact> listener) {
        OkHttpUtils<ResultContact> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_DOWNLOAD_CONTACT_ALL_LIST)
                .addParam(I.Contact.USER_NAME, userName)
                .targetClass(ResultContact.class)
                .execute(listener);
    }

    public static void createGroup(Context context,String hunxinId, String groupName,String desc,String owner,boolean isPublic,boolean isAllowInvites,File file, OkHttpUtils.OnCompleteListener<String> listener) {
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_CREATE_GROUP)
                .addParam(I.Group.HX_ID,hunxinId)
                .addParam(I.Group.NAME,groupName)
                .addParam(I.Group.DESCRIPTION,desc)
                .addParam(I.Group.OWNER,owner)
                .addParam(I.Group.IS_PUBLIC,isPublic+"")
                .addParam(I.Group.ALLOW_INVITES,isAllowInvites+"")
                .addFile2(file)
                .targetClass(String.class)
                .post()
                .execute(listener);
    }

    public static void createGroup(Context context,String hunxinId, String groupName,String desc,String owner,boolean isPublic,boolean isAllowInvites, OkHttpUtils.OnCompleteListener<String> listener) {
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_CREATE_GROUP)
                .addParam(I.Group.HX_ID,hunxinId)
                .addParam(I.Group.NAME,groupName)
                .addParam(I.Group.DESCRIPTION,desc)
                .addParam(I.Group.OWNER,owner)
                .addParam(I.Group.IS_PUBLIC,isPublic+"")
                .addParam(I.Group.ALLOW_INVITES,isAllowInvites+"")
                .targetClass(String.class)
                .post()
                .execute(listener);
    }


}
