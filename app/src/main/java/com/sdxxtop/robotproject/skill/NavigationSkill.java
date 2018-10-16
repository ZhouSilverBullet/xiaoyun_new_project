package com.sdxxtop.robotproject.skill;

import android.os.RemoteException;
import android.util.Log;

import com.ainirobot.coreservice.client.Definition;
import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.coreservice.client.listener.ActionListener;
import com.ainirobot.coreservice.client.listener.CommandListener;
import com.ainirobot.coreservice.client.listener.TextListener;
import com.sdxxtop.robotproject.global.Constants;


/**
 * 导航相关
 *
 * @author Orion
 * @time 2018/9/12
 */
public class NavigationSkill extends BaseSkill {

    private static final String TAG = "NavigationSkill";

    private NavigationSkill() {
    }

    public static NavigationSkill getInstance() {
        return SingleHolder.INSTANCE;
    }

    private static class SingleHolder {
        public static final NavigationSkill INSTANCE = new NavigationSkill();
    }

    public void startNavigation(final String destination, final NavigationCallback callback) {
        RobotApi.getInstance().stopFocusFollow(0);
        RobotApi.getInstance().startNavigation(Constants.REQUEST_ID_DEFAULT, destination,
                Constants.COORDINATE_DEVIATION, Constants.START_NAVIGATION_TIME_OUT,
                new ActionListener() {
                    @Override
                    public void onError(int errorCode, String errorString) throws RemoteException {
                        Log.e(TAG, "onError: " + errorCode + ", " + errorString);
                        stopNavigation();
                    }

                    @Override
                    public void onResult(int status, String responseString) throws RemoteException {
                        Log.e(TAG, "onResult: " + status + ", " + responseString);
                        switch (status) {
                            case Definition.RESULT_OK:
                                if (callback != null) {
                                    callback.onNavigationSuccessStart();
                                }

                                SpeechSkill.getInstance().getSkillApi().playText("已经到达了" + destination, new TextListener() {
                                    @Override
                                    public void onComplete() {
                                        super.onComplete();
                                        if (callback != null) {
                                            callback.onNavigationSuccessEnd();
                                        }
                                    }
                                });
                                RobotApi.getInstance().resetHead(Constants.REQUEST_ID_DEFAULT, null);
                                stopNavigation();
                                break;
                            case Definition.ACTION_RESPONSE_STOP_SUCCESS:
                                stopNavigation();
                                break;
                            default:
                                break;
                        }
                    }

                    @Override
                    public void onStatusUpdate(int status, String data) throws RemoteException {
                        Log.e(TAG, "onResult: " + status + ", " + data);
                    }
                });
    }

    public void prepareStartNavigation(final String destination, final NavigationCallback callback) {
        if (callback != null) {
            callback.onNavigationStart();
        }
        SpeechSkill.getInstance().getSkillApi().playText("正在去往" + destination + "，跟我来吧！", new TextListener() {
            @Override
            public void onComplete() {
                super.onComplete();
                if (callback != null) {
                    callback.onNavigationEnd();
                }
                startNavigation(destination, callback);
            }
        });
    }

    public void stopNavigation() {
        RobotApi.getInstance().stopNavigation(Constants.REQUEST_ID_DEFAULT);
    }

    public void setLocation(String param) {
        RobotApi.getInstance().setLocation(Constants.REQUEST_ID_DEFAULT,
                param, new CommandListener() {
                    @Override
                    public void onResult(int result, String message) {
                        Log.d(TAG, "onResult: " + result + ", " + message);
                        if (result == Definition.RESULT_OK) {
                            SpeechSkill.getInstance().playTxt("设置成功");
                        } else {
                            SpeechSkill.getInstance().playTxt("设置失败");
                        }
                    }
                });
    }

    public void getLocation(String param, CommandListener listener) {
        RobotApi.getInstance().getLocation(Constants.REQUEST_ID_DEFAULT, param, listener);
    }

    public void goPosition(String position, CommandListener listener) {
        RobotApi.getInstance().goPosition(Constants.REQUEST_ID_DEFAULT, position, listener);
    }

    public void setPoseLocation(String param, CommandListener listener) {
        RobotApi.getInstance().setPoseLocation(Constants.REQUEST_ID_DEFAULT, param, listener);
    }

    public void setPoseEstimate(String param, CommandListener listener) {
        RobotApi.getInstance().setPoseEstimate(Constants.REQUEST_ID_DEFAULT, param, listener);
    }

    public void saveRobotEstimate(CommandListener listener) {
        RobotApi.getInstance().saveRobotEstimate(Constants.REQUEST_ID_DEFAULT, listener);
    }

    public void getPlaceName(String param, CommandListener listener) {
        RobotApi.getInstance().getPlace(Constants.REQUEST_ID_DEFAULT, param, listener);
    }

    public void getPlaceList(CommandListener listener) {
        RobotApi.getInstance().getPlaceList(Constants.REQUEST_ID_DEFAULT, listener);
    }

    public void isRobotInlocations(String param, CommandListener listener) {
        RobotApi.getInstance().isRobotInlocations(Constants.REQUEST_ID_DEFAULT, param, listener);
    }

    public void isRobotEstimate(CommandListener listener) {
        RobotApi.getInstance().isRobotEstimate(Constants.REQUEST_ID_DEFAULT, listener);
    }

    public interface NavigationCallback {
        void onNavigationStart();

        void onNavigationEnd();

        void onNavigationSuccessStart();

        void onNavigationSuccessEnd();
    }

}
