package com.sdxxtop.robotproject.control;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.coreservice.client.listener.ActionListener;
import com.ainirobot.coreservice.client.listener.CommandListener;
import com.ainirobot.coreservice.client.listener.Person;
import com.ainirobot.coreservice.client.listener.PersonInfoListener;
import com.ainirobot.coreservice.client.listener.TextListener;
import com.sdxxtop.robotproject.R;
import com.sdxxtop.robotproject.global.App;
import com.sdxxtop.robotproject.global.Constants;
import com.sdxxtop.robotproject.skill.FaceSkill;
import com.sdxxtop.robotproject.skill.SpeechSkill;
import com.sdxxtop.robotproject.utils.FuzzyUtils;
import com.sdxxtop.robotproject.utils.MessageParser;
import com.sdxxtop.robotproject.utils.NameUtils;
import com.xuxin.entry.ChatWordBean;
import com.xuxin.http.IRequestListener;
import com.xuxin.http.Params;
import com.xuxin.http.RequestCallback;
import com.xuxin.http.RequestExe;

import java.util.ArrayList;
import java.util.List;


/**
 * 处理由ModuleCall回调的消息
 *
 * @author Orion
 * @time 2018/9/14
 */
public class MessageManager {

    private List<MessageCallBack> callbackList;
    private static final String TAG = "MessageManager";

    boolean isSpeaking;
    private PersonInfoListener askPersonInfoListener;
    private PersonInfoListener wakeUpPersonInfoListener;

    private MessageManager() {
        callbackList = new ArrayList<>();
    }

    public static MessageManager getInstance() {
        return SingleHolder.INSTANCE;
    }

    private static class SingleHolder {
        public static final MessageManager INSTANCE = new MessageManager();
    }

    public void exeRequest(Message msg) {
        Bundle bundle = msg.getData();
        String type = bundle.getString(Constants.BUNDLE_REQUEST_TYPE);
        final String param = bundle.getString(Constants.BUNDLE_REQUEST_PARAM);
        String answerText = MessageParser.getAnswerText(param);
        String userText = MessageParser.getUserText(param);

        if (wakeUpInterruptListener != null) {
            wakeUpInterruptListener.onWakeUp();
        }

        switch (type) {
            case Constants.REQUEST_TYPE_SPEECH:
                SpeechSkill.getInstance().setPlaying(false);

                //你们请注意我们的实现逻辑   当喊他的时候  先头转  如果找到人   交点跟随  ，然后身体转动
                // FIXME: 2018/9/20 需要地盘同步转动  //relative absolute
                FaceSkill.getInstance().stopFocusFollow();
                RobotApi.getInstance().stopSearchPerson(0);
                RobotApi.getInstance().stopWakeUp(0);
                stopGetAllPersonInfo();

                final Integer angle = Integer.valueOf(param);
                Log.e(TAG, " exeRequest angle :" + angle);
                RobotApi.getInstance().wakeUp(Constants.REQUEST_ID_DEFAULT, angle, new ActionListener() {
                    @Override
                    public void onResult(int status, String responseString) throws RemoteException {
                        super.onResult(status, responseString);
                        switch (status) {
                            case 1:
                                RobotApi.getInstance().stopWakeUp(0);
                                PersonInfo.getInstance().startSearchPerson();
                                break;
                        }
                    }

                    @Override
                    public void onError(int errorCode, String errorString) throws RemoteException {
                        super.onError(errorCode, errorString);

                        Log.e(TAG, "wakeUp onError errorCode :" + errorCode + ", " + errorString);
                    }
                });

//                RobotApi.getInstance().wakeUp(Constants.REQUEST_ID_DEFAULT, angle, new ActionListener() {
//                    @Override
//                    public void onResult(int status, String responseString) throws RemoteException {
//                        super.onResult(status, responseString);
//                        Log.e(TAG, "status : " + status + " responseString: " + responseString);
//                        RobotApi.getInstance().startSearchPerson(1, 1, "", 5000, new ActionListener() {
//
//                            @Override
//                            public void onError(int errorCode, String errorString) throws RemoteException {
//                                super.onError(errorCode, errorString);
//                                RobotApi.getInstance().stopSearchPerson(0);
//                                Log.e(TAG, "startSearchPerson onError " + errorCode + ", " + errorString);
//                            }
//
//                            @Override
//                            public void onResult(int status, String responseString) throws RemoteException {
//                                super.onResult(status, responseString);
//                                Log.e(TAG, "startSearchPerson onResult " + status + ", " + responseString);
//                                RobotApi.getInstance().stopSearchPerson(0);
//                                switch (status) {
//                                    case 1:
//                                        stopGetAllPersonInfo();
//                                        wakeUpPersonInfoListener = new PersonInfoListener() {
//                                            @Override
//                                            public void onResult(int status, String responseString) {
//                                                super.onResult(status, responseString);
//                                                Log.e(TAG, "startGetAllPersonInfo status : " + status + " onStatusUpdate: " + responseString);
//                                                stopGetAllPersonInfo();
//                                            }
//
//                                            @Override
//                                            public void onData(int code, List<Person> data) {
//                                                super.onData(code, data);
//                                                for (Person datum : data) {
//                                                    int id = datum.getId();
//                                                    stopGetAllPersonInfo();
//                                                    FaceSkill.getInstance().startFocusFollow(id, new ActionListener() {
//                                                        @Override
//                                                        public void onResult(int status, String responseString) throws RemoteException {
//                                                            stopGetAllPersonInfo();
//                                                            super.onResult(status, responseString);
//                                                            Log.e(TAG, "onResult startFocusFollow" + status + ", " + responseString);
//                                                        }
//
//                                                        @Override
//                                                        public void onError(int errorCode, String errorString) throws RemoteException {
//                                                            stopGetAllPersonInfo();
//                                                            super.onError(errorCode, errorString);
//                                                            Log.e(TAG, "onError startFocusFollow" + errorCode + ", " + errorString);
//                                                        }
//                                                    });
//                                                }
//                                                Log.e(TAG, "1 startGetAllPersonInfo code : " + code + " onStatusUpdate: " + data.toString());
//                                            }
//                                        };
//                                        RobotApi.getInstance().startGetAllPersonInfo(0, wakeUpPersonInfoListener);
//                                        break;
//                                }
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onError(int errorCode, String errorString) throws RemoteException {
//                        super.onError(errorCode, errorString);
//                        Log.e(TAG, "WakeUp errorCode : " + errorCode + " errorString: " + errorString);
//                    }
//                });

                break;
            case Constants.REQUEST_TYPE_CRUISE:
                // 巡逻
                break;
            case Constants.REQUEST_TYPE_GUIDE:
                // 导航
//                String destination = MessageParser.getDestination(param);
//                NavigationSkill.getInstance().startNavigation(destination, callback);
                break;
            case Constants.REQUEST_TYPE_SET_LOCATION:
                // 设点
//                String location = MessageParser.getLocation(param);
//                NavigationSkill.getInstance().setLocation(location);
                break;
            case Constants.REQUEST_TYPE_ASK:
                FaceSkill.getInstance().stopFocusFollow();
                stopGetAllPersonInfo();
                isSpeaking = false;
                askPersonInfoListener = new PersonInfoListener() {
                    @Override
                    public void onResult(int status, String responseString) {
                        super.onResult(status, responseString);
                        stopGetAllPersonInfo();
                        Log.e(TAG, "startGetAllPersonInfo status : " + status + " onStatusUpdate: " + responseString);
                    }

                    @Override
                    public void onData(int code, List<Person> data) {
                        super.onData(code, data);
                        ArrayList<String> list = new ArrayList<>();
                        for (Person datum : data) {
                            list.add(datum.toGson());
                            final int id = datum.getId();
                            RobotApi.getInstance().getPictureById(0, id, 1, new CommandListener() {
                                @Override
                                public void onResult(int result, String message) {
                                    super.onResult(result, message);
                                    Log.e(TAG, "getPictureById result: " + result + " message: " + message);
                                    App.getInstance().getHandler().removeCallbacks(delayMessageRunable);
                                    isStart = false;
                                    switch (result) {
                                        case 1:
                                            stopGetAllPersonInfo();
                                            List<String> pictures = MessageParser.getPictures(message);
                                            RobotApi.getInstance().remoteDetect(0, "" + id, pictures, new CommandListener() {
                                                @Override
                                                public void onResult(int result, String message) {
                                                    super.onResult(result, message);
                                                    Log.e(TAG, "remoteDetect result: " + result + " message: " + message);
                                                    String personName = MessageParser.getPersonName(message);
                                                    boolean b = !TextUtils.isEmpty(personName);
                                                    FaceSkill.getInstance().startFocusFollow(id, new ActionListener() {
                                                        @Override
                                                        public void onResult(int status, String responseString) throws RemoteException {
                                                            super.onResult(status, responseString);
                                                            Log.e(TAG, " REQUEST_TYPE_ASK onResult startFocusFollow " + status + ", " + responseString);
                                                        }

                                                        @Override
                                                        public void onError(int errorCode, String errorString) throws RemoteException {
                                                            super.onError(errorCode, errorString);
                                                            Log.e(TAG, "REQUEST_TYPE_ASK onError startFocusFollow" + errorCode + ", " + errorString);
                                                        }
                                                    });
                                                    if (b) {
                                                        if (!isSpeaking) {
                                                            isSpeaking = true;
                                                            SpeechSkill.getInstance().getSkillApi().playText(NameUtils.getGetName(personName), new TextListener());
                                                        }
                                                    } else {
                                                        int personError = MessageParser.getPersonError(message);
                                                        if (personError == 1) {
                                                            SpeechSkill.getInstance().getSkillApi().playText("我还不认识你了，你能告诉我吗", new TextListener());
                                                        } else {
                                                            SpeechSkill.getInstance().getSkillApi().playText("您离我近一点，再来一次", new TextListener());
                                                        }
                                                    }
                                                }
                                            });
                                            break;
                                        default:
                                            SpeechSkill.getInstance().getSkillApi().playText("您离我近一点，再来一次", new TextListener());
                                            break;
                                    }
                                }
                            });
                        }

                        if (data.size() == 0 && !isStart) {
                            isStart = true;
                            App.getInstance().getHandler().postDelayed(delayMessageRunable, 3000);
                        }

                        Log.e(TAG, "2 startGetAllPersonInfo code : " + code + " onStatusUpdate: " + list.toString());
                    }
                };
                RobotApi.getInstance().startGetAllPersonInfo(0, askPersonInfoListener);
                break;
            case Constants.REQUEST_TYPE_REGISTER:
                RobotApi.getInstance().stopFocusFollow(0);
                String personName = MessageParser.parseRegisterName(param);
                Log.e(TAG, "startRegister personName: " + personName);
                RobotApi.getInstance().startRegister(Constants.REQUEST_ID_DEFAULT,
                        personName, 6 * 1000, 2, 1000,
                        new ActionListener() {
                            @Override
                            public void onResult(int status, String responseString) throws RemoteException {
                                Log.e(TAG, "startRegister onResult: " + status + ", " + responseString);
//                                ToastUtil.onResult(getActivity(), status, responseString);
                                RobotApi.getInstance().stopRegister(0);
                                switch (status) {
                                    case 1:
                                        String remoteName = MessageParser.getRegisterRemoteName(responseString);
                                        SpeechSkill.getInstance().playTxt(remoteName);
                                        break;
                                    default:
                                        SpeechSkill.getInstance().playTxt("录入失败，离我近一点，咱们再来一次");
                                        break;
                                }
                            }

                            @Override
                            public void onError(int errorCode, String errorString) throws RemoteException {
                                super.onError(errorCode, errorString);
                                RobotApi.getInstance().stopRegister(0);
                                SpeechSkill.getInstance().playTxt("录入失败");
                                Log.e(TAG, "startRegister onError: " + errorCode + ", " + errorString);
                            }
                        });
                break;
            default:
                break;
        }

//        if (callbackList.size() == 0) {
//            return;
//        }

        if (isPause) {
            return;
        }

        for (MessageCallBack messageCallBack : callbackList) {
            messageCallBack.exeRequest(type, param, answerText);
        }

        // 文字播报
        if (!TextUtils.isEmpty(answerText)) {
//            if (!TextUtils.isEmpty(answerText) && answerText.contains("豹小秘")) {
//                answerText = answerText.replace("豹小秘", getString(R.string.robot_name));
//            }
//
//            if (!TextUtils.isEmpty(answerText) && answerText.contains("小豹")) {
//                answerText = answerText.replace("小豹", getString(R.string.robot_name));
//            }
//
//            if (!TextUtils.isEmpty(answerText) && answerText.contains("猎豹移动")) {
//                answerText = answerText.replace("猎豹移动", getString(R.string.company_name));
//            }
//
//            if (!TextUtils.isEmpty(userText) && FuzzyUtils.contains1(userText)) {
//                answerText = getString(R.string.report_call);
//            }
//
//            if (!TextUtils.isEmpty(userText) && userText.contains("你叫什么名字")) {
//                answerText = "我叫小云，是知点云产品的智能助理";
//            }
//
//            if (!TextUtils.isEmpty(answerText) && answerText.contains("接待机器人")) {
//                if (answerText.contains("作为专业的接待机器人")) {
//                    answerText = answerText.replace("接待机器人", "智能助理");
//                } else {
//                    answerText = "知点云产品的智能助理";
//                }
//            }

            if ("打开合影".equals(userText)) {
                return;
            }

            if (FuzzyUtils.contains10(userText)) {
                return;
            }

            loadData(userText);
//            SpeechSkill.getInstance().playTxt(answerText, new TextListener() {
//                @Override
//                public void onComplete() {
//                    super.onComplete();
//
//                    for (MessageCallBack messageCallBack : callbackList) {
//                        messageCallBack.onComplete();
//                    }
//                }
//            });
        }
    }

    private void stopGetAllPersonInfo() {
        if (askPersonInfoListener != null || wakeUpPersonInfoListener != null) {
            if (askPersonInfoListener != null) {
                RobotApi.getInstance().stopGetAllPersonInfo(0, askPersonInfoListener);
                askPersonInfoListener = null;
            }

            if (wakeUpPersonInfoListener != null) {
                RobotApi.getInstance().stopGetAllPersonInfo(0, wakeUpPersonInfoListener);
                wakeUpPersonInfoListener = null;
            }
        } else {
            RobotApi.getInstance().stopGetAllPersonInfo(0, null);
        }
    }

    public synchronized void loadData(String value) {
        Params params = new Params();
        params.put("it", value);
        params.put("tp", 2);
        String data = params.getData();
        Log.e(TAG, "params data = " + data);
        RequestExe.createRequest().postChatXiaoYun(data).enqueue(new RequestCallback<>(new IRequestListener<ChatWordBean>() {
            @Override
            public void onSuccess(ChatWordBean chatWordBean) {
//                isRequesting = false;
                ChatWordBean.DataEntry data = chatWordBean.getData();
                Log.e(TAG, "data = " + data);
                if (data != null) {
                    String answer = data.getAnswer();
                    if (!TextUtils.isEmpty(answer)) {
                        payText(answer);
                    }
                }
            }

            @Override
            public void onFailure(int code, String errorMsg) {
//                isRequesting = false;
                Log.e(TAG, "code = " + code + " errorMsg = " + errorMsg);
            }
        }));
    }

    private void payText(String answerText) {
        SpeechSkill.getInstance().playTxt(answerText, new TextListener() {
            @Override
            public void onComplete() {
                super.onComplete();

                for (MessageCallBack messageCallBack : callbackList) {
                    messageCallBack.onComplete();
                }
            }
        });
    }

    private boolean isPause;

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public String getString(int stringId) {
        return App.getInstance().getString(stringId);
    }

    public synchronized void addCallBack(MessageCallBack skillCallback) {
        this.callbackList.add(skillCallback);
    }

    public synchronized void removeCallBack(MessageCallBack skillCallback) {
        this.callbackList.remove(skillCallback);
    }

    public void exeCMD(Message message) {
    }

    public void exeHWReport(Message message) {
    }

    public interface MessageCallBack {
        void exeRequest(String type, String param, String answerText);

        void onComplete();
    }

    private WakeUpInterruptListener wakeUpInterruptListener;

    public void setWakeUpInterruptListener(WakeUpInterruptListener wakeUpInterruptListener) {
        this.wakeUpInterruptListener = wakeUpInterruptListener;
    }

    public interface WakeUpInterruptListener {
        void onWakeUp();
    }

    private FindPersonListener findPersonListener;

    public void setFindPersonListener(FindPersonListener findPersonListener) {
        this.findPersonListener = findPersonListener;
    }

    public interface FindPersonListener {
        void findPeople();
    }

    boolean isStart;
    private Runnable delayMessageRunable = new Runnable() {
        @Override
        public void run() {
            SpeechSkill.getInstance().getSkillApi().playText("您离我近一点，我才能认识你", new TextListener());
        }
    };
}
