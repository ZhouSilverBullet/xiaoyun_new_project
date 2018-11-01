package com.sdxxtop.robotproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.coreservice.client.listener.TextListener;
import com.sdxxtop.robotproject.control.MessageManager;
import com.sdxxtop.robotproject.global.App;
import com.sdxxtop.robotproject.presenter.iview.SkillView;
import com.sdxxtop.robotproject.skill.MoveSkill;
import com.sdxxtop.robotproject.skill.SpeechSkill;

public class MoveActivity extends SecondBaseActivity {

    public static final String TAG = "MoveActivity";
    private MoveActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move);
        mContext = this;

        App.getInstance().addView(this);


//        findViewById(R.id.move_place1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MoveSkill.getInstance().goPosition(mContext, "大门");
//            }
//        });
//
//        findViewById(R.id.move_place2).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MoveSkill.getInstance().goPosition(mContext, "讲解台");
//            }
//        });
//        findViewById(R.id.move_place3).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MoveSkill.getInstance().goPosition(mContext, "展厅");
//            }
//        });
//        findViewById(R.id.move_place4).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MoveSkill.getInstance().goPosition(mContext, "待定");
//            }
//        });
//        findViewById(R.id.move_place5).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MoveSkill.getInstance().goPosition(mContext, "待定");
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getInstance().removeView(this);
        MessageManager.getInstance().setPause(false);
    }

    @Override
    public void onSpeechParResult(String speechMessage) {
        if (TextUtils.isEmpty(speechMessage)) {
            return;
        }
        super.onSpeechParResult(speechMessage);
        if (speechMessage.contains("带我去大门")) {
            SpeechSkill.getInstance().playTxt("正在去往大门，跟我来吧！", new TextListener() {
                @Override
                public void onComplete() {
                    super.onComplete();
                    MoveSkill.getInstance().goPosition(mContext, "大门");
                }
            });
        } else if (speechMessage.contains("带我去讲解台")) {
            SpeechSkill.getInstance().playTxt("正在去往讲解台，跟我来吧！", new TextListener() {
                @Override
                public void onComplete() {
                    super.onComplete();
                    MoveSkill.getInstance().goPosition(mContext, "讲解台");
                }
            });
        } else if (speechMessage.contains("带我去展厅")) {
            SpeechSkill.getInstance().playTxt("正在去往讲展厅，跟我来吧！", new TextListener() {
                @Override
                public void onComplete() {
                    super.onComplete();
                    MoveSkill.getInstance().goPosition(mContext, "展厅");
                }
            });
        } else if (speechMessage.contains("退出导航")) {
            SpeechSkill.getInstance().playTxt("退出导航成功", new TextListener() {
                @Override
                public void onComplete() {
                    super.onComplete();
                    RobotApi.getInstance().stopGoPosition(0);
                }
            });
        }
    }

    @Override
    public void onStartSkill() {

    }

    @Override
    public void onStopSkill() {

    }

    @Override
    public void onVolumeChange(int volume) {

    }

    @Override
    public void onQueryEnded(int query) {

    }

    @Override
    public void onSendRequest(String reqType, String reqText, String reqParam) {

    }

    @Override
    public void onComplete() {

    }
}
