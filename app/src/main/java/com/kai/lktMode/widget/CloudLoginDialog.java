package com.kai.lktMode.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.kai.lktMode.R;

public class CloudLoginDialog extends AlertDialog {
    private Context context;
    private OnLoginClick onLoginClick;
    private OnCancelClick onCancelClick;
    private OnRegistClick onRegistClick;
    private Button regist;
    private Button login;
    private EditText username;
    private EditText password;
    private ImageButton cancel;
    private View view;
    public CloudLoginDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view=View.inflate(context, R.layout.dialog_cloud,null);
        username=(EditText)view.findViewById(R.id.username);
        password=(EditText)view.findViewById(R.id.password);
        password.requestFocus();
        username.requestFocus();
        CloudLoginDialog.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        cancel=(ImageButton)view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloudLoginDialog.this.dismiss();
            }
        });
        regist=(Button)view.findViewById(R.id.regist);
        regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRegistClick.onregist();
            }
        });
        login=(Button)view.findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLoginClick.onclick(username.getText().toString(),password.getText().toString());
            }
        });
        setContentView(view);
    }



    public void setOnCancelClick(OnCancelClick onCancelClick) {
        this.onCancelClick = onCancelClick;
    }

    public void setOnLoginClick(OnLoginClick onLoginClick) {
        this.onLoginClick = onLoginClick;
    }

    public void setOnRegistClick(OnRegistClick onRegistClick) {
        this.onRegistClick = onRegistClick;
    }

    public interface OnLoginClick{
        void onclick(String user,String password);
    }
    public interface OnRegistClick{
        void onregist();
    }
    public interface OnCancelClick{
        void oncancel();
    }
}
