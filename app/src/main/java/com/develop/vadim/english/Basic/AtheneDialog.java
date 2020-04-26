package com.develop.vadim.english.Basic;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.develop.vadim.english.R;

public class AtheneDialog extends Dialog {
    private TextView messageTextView;
    private TextView cancelTextView;
    private TextView accessTextView;
    private EditText userAnswerEditText;

    public interface Listener {
        void onClick(TextView textView);
    }

    public static final int SIMPLE_MESSAGE_TYPE = 0;
    public static final int TWO_OPTIONS_TYPE = 1;
    public static final int EDIT_TEXT_SIMPLE_MESSAGE_TYPE = 2;
    public static final int EDIT_TEXT_TWO_OPTIONS_TYPE = 3;

    private int type =  SIMPLE_MESSAGE_TYPE;

    private Listener positiveClickListener;
    private Listener negativeClickListener;

    public AtheneDialog(@NonNull Context context, int type) {
        super(context);
        this.type = type;

        View v = getLayoutInflater().inflate(R.layout.fragment_add_new_category, null, false);
        setContentView(v);

        this.getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);

        messageTextView = v.findViewById(R.id.messageTextView);
        accessTextView = v.findViewById(R.id.acceptTextView);
        cancelTextView = v.findViewById(R.id.cancelTextView);
        userAnswerEditText = v.findViewById(R.id.addNewCategoryEditText);

        switch(type) {
            case SIMPLE_MESSAGE_TYPE:
                break;
            case TWO_OPTIONS_TYPE:
                cancelTextView.setVisibility(View.VISIBLE);

                break;
            case EDIT_TEXT_SIMPLE_MESSAGE_TYPE:
                userAnswerEditText.setVisibility(View.VISIBLE);

                break;
            case EDIT_TEXT_TWO_OPTIONS_TYPE:
                userAnswerEditText.setVisibility(View.VISIBLE);
                cancelTextView.setVisibility(View.VISIBLE);

                break;
        }
    }

    public void setMessageText(String messageText) {
        this.messageTextView.setText(messageText);
    }

    public void setPositiveText(String positiveText) {
        this.accessTextView.setText(positiveText);
    }

    public void setNegativeText(String negativeText) {
        if(type == TWO_OPTIONS_TYPE || type == EDIT_TEXT_TWO_OPTIONS_TYPE) {
            this.cancelTextView.setText(negativeText);
        }
    }

    public void setNegativeClickListener(TextView.OnClickListener listener) {
        if(type == TWO_OPTIONS_TYPE || type == EDIT_TEXT_TWO_OPTIONS_TYPE) {
            this.cancelTextView.setOnClickListener(listener);
        }
    }

    public void setPositiveClickListener(TextView.OnClickListener listener) {
        this.accessTextView.setOnClickListener(listener);
    }

    public void setHint(String hint) {
        this.userAnswerEditText.setHint(hint);
    }

    public EditText getUserAnswerEditText() {
        if(type == EDIT_TEXT_TWO_OPTIONS_TYPE || type == EDIT_TEXT_SIMPLE_MESSAGE_TYPE) {
            return userAnswerEditText;
        }

        return null;
    }

    public TextView getAccessTextView() {
        return  accessTextView;
    }

    public TextView getCancelTextView() {
        if(type == TWO_OPTIONS_TYPE || type == EDIT_TEXT_TWO_OPTIONS_TYPE) {
            return cancelTextView;
        }

        return null;
    }
}
