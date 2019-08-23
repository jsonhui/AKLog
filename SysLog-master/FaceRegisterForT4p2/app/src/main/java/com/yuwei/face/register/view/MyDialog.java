package com.yuwei.face.register.view;

import java.util.TimerTask;

import com.yuwei.face.register.R;
import com.yuwei.face.util.MyOwnTimer;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

    public class MyDialog extends Dialog {
        private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;
        private DialogInterface.OnClickListener positiveButtonClickListener;
        private DialogInterface.OnClickListener negativeButtonClickListener;
        private View layout;
        private TextView text;
        private LinearLayout linear;
        private DisplayMetrics dm;
        private int widthScreen;
        private LinearLayout mydialog_layout;
        private int size;
        private MyOwnTimer timer;
        public static int Dialog_Show = 0;
        public MyDialog(Context context,int style) {
            super(context,style);
            this.context = context;
            dm = context.getApplicationContext().getResources().getDisplayMetrics();
            widthScreen = dm.widthPixels;
        }    

        public MyDialog setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Set the Dialog message from resource
         * 
         * @param title
         * @return
         */
        public MyDialog setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        /**
         * Set the Dialog title from resource
         * 
         * @param title
         * @return
         */
        public void setTitle(int title) {
            this.title = (String) context.getText(title);
        }

        /**
         * Set the Dialog title from String
         * 
         * @param title
         * @return
         */

        public MyDialog setTitle(String title) {
            this.title = title;
            return this;
        }

        public MyDialog setView(View v) {
            this.contentView = v;
            return this;
        }
        
        public void showMe() {
            init(true);
            show();
        }
        
        public void showMeNotDisMiss() {
            init(false);
            show();
        }
        /**
         * Set the positive button resource and it's listener
         * 
         * @param positiveButtonText
         * @return
         */
        public MyDialog setPositiveButton(int positiveButtonText,DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context.getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        public MyDialog setPositiveButton(String positiveButtonText,DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public MyDialog setNegativeButton(int negativeButtonText,DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context.getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }

        public MyDialog setNegativeButton(String negativeButtonText,DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }
        
         public void setPadding(int left,int top,int right,int bottom){
             ((LinearLayout) layout.findViewById(R.id.mydialog_linear)).setPadding(left, top, right, bottom);
        }
         
         public MyDialog setTextSize(int _size){
             this.size = 35;
             return this;
         }
         
         public MyDialog setWidth(int _width){
             this.widthScreen = _width;
             return this;
         }
        
        private void init(boolean _autoDismiss) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            layout = inflater.inflate(R.layout.mydialog, null);

            text = ((TextView) layout.findViewById(R.id.mydialog_message));
            linear = (LinearLayout)layout.findViewById(R.id.mydialog_linear);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(3*widthScreen/6, LinearLayout.LayoutParams.WRAP_CONTENT);
            mydialog_layout = (LinearLayout)layout.findViewById(R.id.mydialog_layout);
            mydialog_layout.setLayoutParams(lp);
            // set the dialog title
            ((TextView) layout.findViewById(R.id.mydialog_title)).setText(title);
            // set the confirm button
            if (positiveButtonText != null) {
                ((TextView) layout.findViewById(R.id.mydialog_ok)).setText(positiveButtonText); 
                if (positiveButtonClickListener != null) {
                    ((TextView)layout.findViewById(R.id.mydialog_ok)).setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(MyDialog.this, DialogInterface.BUTTON_POSITIVE);
                                    stopTimer();
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.mydialog_ok).setVisibility(View.GONE);
                layout.findViewById(R.id.divider_line).setVisibility(View.GONE);
            }
            // set the cancel button
            if (negativeButtonText != null) {
                ((TextView) layout.findViewById(R.id.mydialog_cancel)).setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((TextView) layout.findViewById(R.id.mydialog_cancel)).setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    negativeButtonClickListener.onClick(MyDialog.this,DialogInterface.BUTTON_NEGATIVE);
                                    stopTimer();
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.mydialog_cancel).setVisibility(View.GONE);
                layout.findViewById(R.id.divider_line).setVisibility(View.GONE);
            }   
            if((positiveButtonText == null)&&(negativeButtonText == null)){
                layout.findViewById(R.id.mydialog_divider).setVisibility(View.GONE);
                layout.findViewById(R.id.mydialog_panel).setVisibility(View.GONE);
            }
            if((positiveButtonText != null)&&(negativeButtonText == null)){
                layout.findViewById(R.id.mydialog_ok).setBackgroundResource(R.drawable.yuwei_dialog_buttonclick);
            }
            
            // set the content message
            if (message != null) {
                   text.setText(message);
                   if(size > 0)
                       text.setTextSize(size);
            if(_autoDismiss)
                   startTimer(10000);
            } else if (contentView != null) {
                // if no message set
                text.setVisibility(View.GONE);
                linear.setVisibility(View.VISIBLE);
                ((LinearLayout) layout.findViewById(R.id.mydialog_linear)).addView(contentView, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
                if(_autoDismiss)
                    startTimer(60000);//
            }       
            setContentView(layout);
            setOnDismissListener(new OnDismissListener() {
                
                @Override
                public void onDismiss(DialogInterface dialog) {
                    // TODO Auto-generated method stub
                    stopTimer();
                }
            });
        }
         @Override
         public boolean dispatchTouchEvent(MotionEvent ev) {
             // 暂时注释 张柳 2018-9-17 10:46:39
             //Constants.restartCertificateAdTimer(ev);
             return super.dispatchTouchEvent(ev);
         }
        private void startTimer(int _time){
            stopTimer();
            timer = new MyOwnTimer();
            timer.scheduleOneShort(new TimerTask() {
                
                @Override
                public void run() {
                    dismiss();
                }
            }, _time);
        }
        
        private void stopTimer(){
            if(timer != null){
                timer.cancel();
                timer = null;
            }
        }
}
