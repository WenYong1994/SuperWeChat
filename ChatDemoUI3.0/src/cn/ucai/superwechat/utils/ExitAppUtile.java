package cn.ucai.superwechat.utils;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Wy on 2016/11/5.
 */
public class ExitAppUtile {
    List<Activity> mActivityList = new LinkedList<>();
    private static ExitAppUtile instance =new ExitAppUtile();


    private ExitAppUtile(){};


    public static ExitAppUtile getInstance(){
        return instance;
    }

    public void addActivity(Activity activity){
        mActivityList.add(activity);
    }

    public void delActivtity(Activity activity){
        mActivityList.remove(activity);
    }

    public  void exit(){
        if(this.mActivityList==null){
            return;
        }
         for(Activity activity:mActivityList){
                activity.finish();
         }
    }

}
