package com.google.firebase.codelab.friendlychat;

import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CustomMessageViewHolder extends RecyclerView.ViewHolder {

    @BindView(R2.id.chat_left_msg_layout)
    LinearLayout chat_left_msg_layout;

    @BindView(R2.id.leftMessageTextView)
    AppCompatTextView leftMessageTextView;

    @BindView(R2.id.leftMessageImageView)
    AppCompatImageView leftMessageImageView;

    @BindView(R2.id.leftMessengerTextView)
    AppCompatTextView leftMessengerTextView;

    @BindView(R2.id.leftMessengerImageView)
    CircleImageView leftMessengerImageView;

    @BindView(R2.id.chat_right_msg_layout)
    LinearLayout chat_right_msg_layout;

    @BindView(R2.id.rightMessageTextView)
    AppCompatTextView rightMessageTextView;

    @BindView(R2.id.rightMessageImageView)
    AppCompatImageView rightMessageImageView;

    @BindView(R2.id.rightMessengerTextView)
    AppCompatTextView rightMessengerTextView;

    @BindView(R2.id.rightMessengerImageView)
    CircleImageView rightMessengerImageView;


    public CustomMessageViewHolder(View v) {
        super(v);
        ButterKnife.bind(this, v);
    }
}
