package com.yuwei.face.register.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yuwei.face.R;
import com.yuwei.face.util.YuweiFaceUtil;

public class RegisterLeftPromptView extends RelativeLayout {
    private Context mContext;
    private ImageView mMotionNotify;
    private TextView mRegisterTextNotify;
    public RegisterLeftPromptView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView(context);
    }
    private void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.left_register_prompt, this);
        mMotionNotify = (ImageView)findViewById(R.id.motion_notify);
        mRegisterTextNotify = (TextView)findViewById(R.id.motion_notify_text);
    }

    /**设置注册过程中相关提示的图片和文字说明
     * @param step:注册进行到第几步
     */
    public void setRegisterNotifyValue(int step){
        switch (step){
            case 1:
                YuweiFaceUtil.showTopMessage(mContext,"请对准摄像头");
                mMotionNotify.setImageResource(R.drawable.yuwei_face_register_open_mouth);
                mRegisterTextNotify.setText(" ");
                break;
            case 2:
                YuweiFaceUtil.showTopMessage(mContext,"请左转");
                mMotionNotify.setImageResource(R.drawable.yuwei_face_register_left_turn);
                mRegisterTextNotify.setText("请左转");
                break;
            case 3:
                YuweiFaceUtil.showTopMessage(mContext,"请右转");
                mMotionNotify.setImageResource(R.drawable.yuwei_face_register_right_turn);
                mRegisterTextNotify.setText("请右转");
                break;
        }
    }
}
