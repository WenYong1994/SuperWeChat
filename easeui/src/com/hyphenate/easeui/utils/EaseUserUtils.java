package com.hyphenate.easeui.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Engine;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.bean.User;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.controller.EaseUI.EaseUserProfileProvider;
import com.hyphenate.easeui.domain.EaseUser;

public class EaseUserUtils {
    
    static EaseUserProfileProvider userProvider;
    static {
        userProvider = EaseUI.getInstance().getUserProfileProvider();
    }
    
    /**
     * get EaseUser according username
     * @param username
     * @return
     */
    public static EaseUser getUserInfo(String username){
        if(userProvider != null)
            return userProvider.getUser(username);
        
        return null;
    }

    /**
     * get EaseUser according username
     * @param username
     * @return
     */
    public static User getAppUserInfo(String username){
        if(userProvider != null)
            return userProvider.getAppUser(username);

        return null;
    }

    public static User getCurrenttAppUserInfo(){
        String username = EMClient.getInstance().getCurrentUser();
        if(username==null|"".equals(username)){
            return null;
        }
        if(userProvider != null)
            return userProvider.getAppUser(username);

        return null;
    }

    /**
     * set user avatar
     * @param username
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView){
    	EaseUser user = getUserInfo(username);
        if(user != null && user.getAvatar() != null){
            try {
                int avatarResId = Integer.parseInt(user.getAvatar());
                Glide.with(context).load(avatarResId).into(imageView);
            } catch (Exception e) {
                //use default avatar
                Glide.with(context).load(user.getAvatar()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ease_default_avatar).into(imageView);
            }
        }else{
            Glide.with(context).load(R.drawable.ease_default_avatar).into(imageView);
        }
    }
    
    /**
     * set user's nickname
     */
    public static void setAppUserNick(String username,TextView textView){
        if(textView != null){
        	User user = getAppUserInfo(username);

            Log.e("SuperWeChat",user.toString());

            if(user != null && user.getMUserNick() != null){
        		textView.setText(user.getMUserNick());
        	}else{
        		textView.setText(username);
        	}
        }
    }

    /**
     * set user avatar
     * @param username
     */
    public static void setAppUserAvatar(Context context, String username, ImageView imageView){
        User user = getAppUserInfo(username);
        if(user != null && user.getAvatar() != null){
            try {
                int avatarResId = Integer.parseInt(user.getAvatar());
                Glide.with(context).load(avatarResId).into(imageView);
            } catch (Exception e) {
                //use default avatar
                Log.e("SuperWeChat",user.getAvatar());
//                Glide.with(context).load(user.getAvatar()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.default_avatar_1).into(imageView);
                //下面这一句取消缓存
                Glide.clear(imageView);
                Glide.with(context).load(user.getAvatar()).placeholder(R.drawable.default_avatar_1).into(imageView);
                Log.e("SuperWeChat",user.getAvatar());
            }
        }else{
            Glide.with(context).load(R.drawable.default_avatar_1).into(imageView);
        }
    }

    /**
     * set user's nickname
     */
    public static void setUserNick(String username,TextView textView){
        if(textView != null){
            EaseUser user = getUserInfo(username);
            if(user != null && user.getNick() != null){
                textView.setText(user.getNick());
            }else{
                textView.setText(username);
            }
        }
    }


    public static void setCurrentAppUserAvatar(FragmentActivity activity, ImageView ivPrifileAvatar) {
        String userName = EMClient.getInstance().getCurrentUser();
        setAppUserAvatar(activity,userName,ivPrifileAvatar);
    }

    public static void setCurrentAppUserNick(TextView tvProfileNickname) {
        String userName = EMClient.getInstance().getCurrentUser();
        setAppUserNick(userName,tvProfileNickname);
    }

    public static void setCurrentAppUserNameWithNo(TextView tvProfileUsername) {
        String userName = EMClient.getInstance().getCurrentUser();
        setAppUserName("微信号：",userName,tvProfileUsername);
    }
    public static void setCurrentAppUserName(TextView tvProfileUsername) {
        String userName = EMClient.getInstance().getCurrentUser();
        setAppUserName("",userName,tvProfileUsername);
    }

    private static void setAppUserName(String suffix,String userName,TextView tvProfileUsername) {
        tvProfileUsername.setText(suffix+userName);
    }
}
