package com.kai.lktMode.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kai.lktMode.R;

public class TerminalDialog extends AlertDialog {
    private Context context;
    private String codeing="";
    private TextView terminal;
    private View view;
    private TextView positive;
    private TextView negtive;
    private ScrollView scrollView;
    public TerminalDialog(Context context) {
        super(context,R.style.AppDialog);
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view=View.inflate(context, R.layout.dialog_terminal,null);
        scrollView=view.findViewById(R.id.scrollview);
        terminal=view.findViewById(R.id.terminal);
        terminal.setMovementMethod(ScrollingMovementMethod.getInstance());
        positive=view.findViewById(R.id.positve);
        negtive=view.findViewById(R.id.negtive);
        setContentView(view);
    }

    public void addText(String add){
        terminal.append(add+"\n");
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }


    public void setPositive(String p, View.OnClickListener clickListener){
        positive.setText(p);
        positive.setOnClickListener(clickListener);
    }
    public void setNegtive(String n, View.OnClickListener clickListener){
        negtive.setText(n);
        negtive.setOnClickListener(clickListener);
    }


}
