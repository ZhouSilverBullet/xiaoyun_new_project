package com.sdxxtop.robotproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ainirobot.coreservice.client.Definition;
import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.coreservice.client.listener.TextListener;
import com.bumptech.glide.Glide;
import com.sdxxtop.robotproject.adapter.GridAdapter;
import com.sdxxtop.robotproject.camera.CustomCameraActivity;
import com.sdxxtop.robotproject.control.MessageManager;
import com.sdxxtop.robotproject.control.RobotPersonInfo;
import com.sdxxtop.robotproject.global.App;
import com.sdxxtop.robotproject.global.Constants;
import com.sdxxtop.robotproject.presenter.iview.SkillView;
import com.sdxxtop.robotproject.skill.NavigationSkill;
import com.sdxxtop.robotproject.skill.SpeechSkill;
import com.sdxxtop.robotproject.utils.FuzzyUtils;
import com.sdxxtop.robotproject.utils.MessageParser;
import com.sdxxtop.robotproject.widget.SiriView;

public class MainActivity extends BaseActivity implements SkillView, Handler.Callback {

    public static final String TAG = "MainActivity";

    public static final int CONTROL_SLEEP_VISIBILITY = 101;
    //控制开启识别人脸唤醒
    public static final int START_SEARCH_PEOPLE = 105;

    public static final int SLEEP_CURRENT_TIME = 10000;

    private GridView gridview;
    private GridAdapter adapter;
    //    private MainPresenter mainPresenter;
    private View sleepLayout;
    private ImageView sleepImage;
    private View speakingLayout;
    private ImageView speakingImage;
    private SiriView siriNoSpeakingView;
    private SiriView siriSpeakingView;
    private TextView rainbowTextView;
    private MainActivity mContext;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
//        if (!SystemUtils.isServiceRunning(ModuleService.class, getApplicationContext())) {
//            Log.d(TAG, "onCreate: ");
//            Intent intent = new Intent(this, ModuleService.class);
//            startService(intent);
//        }
        handler = new Handler(this);
//        handler.sendEmptyMessage(100);

        initVariables();
        initView();
    }

    private void initView() {

        gridview = findViewById(R.id.gridview);
        adapter = new GridAdapter();
        gridview.setAdapter(adapter);

        adapter.setItemClickListener(new GridAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = null;
                switch (position) {
                    case 0:
                        intent = new Intent(MainActivity.this, ChatActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, MoveActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
//                        intent = new Intent(MainActivity.this, CustomCameraActivity.class);
                        intent = new Intent(MainActivity.this, ReserveActivity.class);
                        startActivity(intent);
                        break;
                    case 3:
//                        intent = new Intent(MainActivity.this, SingleImageActivity.class);
                        intent = new Intent(MainActivity.this, ReserveActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });


        sleepLayout = findViewById(R.id.sleep_layout);
        sleepImage = findViewById(R.id.sleep_image);

        speakingLayout = findViewById(R.id.speaking_layout);
        speakingImage = findViewById(R.id.speaking_image);

        siriNoSpeakingView = findViewById(R.id.siri_no_speaking_view);
        siriSpeakingView = findViewById(R.id.siri_speaking_view);
        rainbowTextView = findViewById(R.id.chat_text);

        Glide.with(this).load(R.drawable.main_normal).into(sleepImage);
        Glide.with(this).load(R.drawable.main_spking).into(speakingImage);

        MessageManager.getInstance().setWakeUpInterruptListener(new MessageManager.WakeUpInterruptListener() {
            @Override
            public void onWakeUp() {
                //只要导航被唤醒打断都不显示动嘴的笑脸
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        speakingLayout.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

        sleepLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeMessages(100);
                handler.sendEmptyMessageDelayed(100, SLEEP_CURRENT_TIME);
                sleepLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void initVariables() {

//        mainPresenter = new MainPresenter();

        //skill 是否连接成功
        if (App.getInstance().isRobotInitSuccess) {
            register();
        } else {
            App.getInstance().setRobotRunnable(new Runnable() {
                @Override
                public void run() {
                    register();
                }
            });
        }

        RobotPersonInfo.getInstance().setRobotWakeUpListener(new RobotPersonInfo.RobotWakeUpListener() {
            @Override
            public void wakeUp() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sleepLayout.setVisibility(View.GONE);
                        handler.removeMessages(100);
                        handler.removeMessages(START_SEARCH_PEOPLE);
                        handler.sendEmptyMessage(CONTROL_SLEEP_VISIBILITY);
                        handler.sendEmptyMessageDelayed(100, SLEEP_CURRENT_TIME);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.sendEmptyMessageDelayed(100, 5000);
        App.getInstance().addView(this);
        MessageManager.getInstance().setPause(false);
//        mainPresenter.addView(this);
    }

    @Override
    protected void onPause() {
        handler.removeMessages(100);
        super.onPause();
        App.getInstance().removeView(this);
        MessageManager.getInstance().setPause(true);
//        mainPresenter.removeView();
    }

    private void register() {
//        robotPresenter.registerModule();
    }

    private void speaking(boolean speaking) {
        if (speaking) {
            siriSpeakingView.setVisibility(View.VISIBLE);
            siriNoSpeakingView.setVisibility(View.INVISIBLE);
        } else {
            siriSpeakingView.setVisibility(View.INVISIBLE);
            siriNoSpeakingView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSpeechParResult(final String speechMessage) {
        Log.e(TAG, "onSpeechParResult thread " + Thread.currentThread());
        if (!TextUtils.isEmpty(speechMessage)) {
            Intent intent = null;
            if ("打开问答".equals(speechMessage) || "开启问答".equals(speechMessage)) {
                intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            } else if ("打开带路".equals(speechMessage) || "开启带路".equals(speechMessage)) {
                intent = new Intent(MainActivity.this, MoveActivity.class);
                startActivity(intent);
            } else if ("打开合影".equals(speechMessage) || "开启合影".equals(speechMessage)) {
                intent = new Intent(MainActivity.this, CustomCameraActivity.class);
                startActivity(intent);
            } else if ("打开美图".equals(speechMessage) || "开启美图".equals(speechMessage)) {
                intent = new Intent(MainActivity.this, SingleImageActivity.class);
                startActivity(intent);
            }
//            else if (!TextUtils.isEmpty(speechMessage) && FuzzyUtils.contains2(speechMessage)) {
//                String answerText = getString(R.string.report_luanshen);
//                SpeechSkill.getInstance().playTxt(answerText);
//            } else if (!TextUtils.isEmpty(speechMessage) && FuzzyUtils.contains6(speechMessage)) {
//                String answerText = getString(R.string.report_luanshen_jieshu);
//                SpeechSkill.getInstance().playTxt(answerText);
//            } else if (!TextUtils.isEmpty(speechMessage) && FuzzyUtils.contains8(speechMessage)) {
//                String answerText = getString(R.string.report_hui_yi_neirong);
//                SpeechSkill.getInstance().playTxt(answerText);
//            } else if (!TextUtils.isEmpty(speechMessage) && FuzzyUtils.contains5(speechMessage)) {
//                String answerText = getString(R.string.report_jieri);
//                SpeechSkill.getInstance().playTxt(answerText);
//            } else if (!TextUtils.isEmpty(speechMessage) && FuzzyUtils.contains9(speechMessage)) {
//                String answerText = getString(R.string.report_hui_yi_place);
//                SpeechSkill.getInstance().playTxt(answerText);
//            } else if (!TextUtils.isEmpty(speechMessage) && FuzzyUtils.contains10(speechMessage)) {
//                String answerText = getString(R.string.report_hui_yi_keqi);
//                SpeechSkill.getInstance().playTxt(answerText);
//            }
            else if (!TextUtils.isEmpty(speechMessage) && FuzzyUtils.contains3(speechMessage)) {
                String answerText = getString(R.string.report_go_position);
                SpeechSkill.getInstance().playTxt(answerText);
            }

            else if (!TextUtils.isEmpty(speechMessage) && FuzzyUtils.contains4(speechMessage)) {
                String answerText = getString(R.string.report_say_bye);
                SpeechSkill.getInstance().playTxt(answerText);
            }
            else if (!TextUtils.isEmpty(speechMessage) && FuzzyUtils.contains7(speechMessage)) {
                String answerText = getString(R.string.report_laoban);
                SpeechSkill.getInstance().playTxt(answerText);
            }
            else if (speechMessage.contains("带我去展厅") || speechMessage.contains("去展厅")) {
                navigation("展厅");
            }

////            //大门、展厅、电梯口
//            else if (speechMessage.contains("带我去大门")|| speechMessage.contains("去大门")) {
//                navigation("大门");
//            } else if (speechMessage.contains("带我去展厅") || speechMessage.contains("去展厅")) {
//                navigation("展厅");
//            } else if (speechMessage.contains("带我去电梯口")|| speechMessage.contains("去电梯口")) {
//                navigation("电梯口");
//            } else if (speechMessage.contains("退出导航")) {
//                SpeechSkill.getInstance().playTxt("退出导航成功", new TextListener() {
//                    @Override
//                    public void onComplete() {
//                        super.onComplete();
//                        RobotApi.getInstance().stopNavigation(0);
//                    }
//                });
//            }

        }

        if (sleepLayout.getVisibility() != View.VISIBLE) {
            if (speechMessage.contains("小豹")) {
                String tempSpeechMessage = speechMessage.replace("小豹", "小云");
                rainbowTextView.setText(tempSpeechMessage);
            } else {
                rainbowTextView.setText(speechMessage);
            }
        }
    }

    @Override
    public void onStartSkill() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                speaking(true);
            }
        });

        Log.e(TAG, "onStartSkill thread " + Thread.currentThread());
    }

    @Override
    public void onStopSkill() {
        Log.e(TAG, "onStopSkill thread " + Thread.currentThread());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                speaking(false);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handler.removeMessages(100);
                        handler.sendEmptyMessageDelayed(100, SLEEP_CURRENT_TIME);
                        rainbowTextView.setText("");
                    }
                }, 3000);
            }
        });
    }

    private void showSleepView() {
        String s = rainbowTextView.getText().toString();
        handler.removeMessages(START_SEARCH_PEOPLE);
        if (TextUtils.isEmpty(s)) {
            sleepLayout.setVisibility(View.VISIBLE);
            handler.sendEmptyMessageDelayed(START_SEARCH_PEOPLE, 5000);
        } else {
            sleepLayout.setVisibility(View.GONE);
        }
    }


    @Override
    public void onVolumeChange(int volume) {
        Log.e(TAG, "onVolumeChange thread " + Thread.currentThread());
    }

    @Override
    public void onQueryEnded(int query) {
        Log.e(TAG, "onQueryEnded thread " + Thread.currentThread() + " query: " + query);
    }

    @Override
    public void onSendRequest(String reqType, String reqText, String reqParam) {
        String userText = MessageParser.getUserText(reqText);

        if (!TextUtils.isEmpty(userText) && "打开合影".equals(userText)) {
            Intent intent = new Intent(MainActivity.this, CustomCameraActivity.class);
            startActivity(intent);
            return;
        }
        sleepLayout.setVisibility(View.GONE);
        switch (reqType) {
            case Definition.REQ_SPEECH_WAKEUP:
                if (sleepLayout.getVisibility() == View.INVISIBLE) {
                    gridview.setAdapter(adapter);
                }
                sleepLayout.setVisibility(View.GONE);
                break;
            case Definition.REQ_SPEECH_SLEEP:

                break;
            case Definition.REQ_CHAT:
                Log.e(TAG, "onSendRequest" + Definition.REQ_CHAT);
                break;
            case Definition.REQ_TELL_ME_WHY:

                break;
            case Constants.REQUEST_TYPE_GUIDE:
                // 导航
                if (!TextUtils.isEmpty(reqText)) {
//                    if (reqText.contains("大门") || reqText.contains("电梯口") || reqText.contains("展厅")) {
//                        String destination = MessageParser.getDestination(reqText);
//                        navigation(destination);
//                    }
                    if (reqText.contains("带我去大门") || reqText.contains("去大门")) {
                        navigation("大门");
                    } else if (reqText.contains("带我去展厅") || reqText.contains("去展厅")) {
                        navigation("展厅");
                    } else if (reqText.contains("带我去电梯口") || reqText.contains("去电梯口")) {
                        navigation("电梯口");
                    } else if (reqText.contains("退出导航")) {
                        handler.removeMessages(START_SEARCH_PEOPLE);
                        RobotApi.getInstance().stopNavigation(0);
                        SpeechSkill.getInstance().playTxt("退出导航成功", new TextListener() {
                            @Override
                            public void onComplete() {
                                super.onComplete();
                            }
                        });
                    }
                }
                break;
            default:
                Log.e(TAG, "onSendRequest default ");
                break;
        }
    }

    private void navigation(String destinationName) {
        handler.removeMessages(START_SEARCH_PEOPLE);
        NavigationSkill.getInstance().prepareStartNavigation(destinationName, new NavigationSkill.NavigationCallback() {
            @Override
            public void onNavigationStart() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        speakingLayout.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onNavigationEnd() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        speakingLayout.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onNavigationSuccessStart() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        speakingLayout.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onNavigationSuccessEnd() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        speakingLayout.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }

    @Override
    public void onComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rainbowTextView.setText("");
            }
        });
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case 100:
                showSleepView();
                handler.sendEmptyMessageDelayed(100, SLEEP_CURRENT_TIME);
                break;
            case CONTROL_SLEEP_VISIBILITY:
                sleepLayout.setVisibility(View.GONE);
                break;
            case START_SEARCH_PEOPLE:
                RobotPersonInfo.getInstance().startSearchPerson();
                break;
        }
        return true;
    }
}
