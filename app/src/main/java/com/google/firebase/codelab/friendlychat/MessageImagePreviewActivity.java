package com.google.firebase.codelab.friendlychat;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageImagePreviewActivity extends AppCompatActivity {

    //TODO: add share options for that image....

    private static final String ARG_IMAGE_URL = "arg_image_url";

    @BindView(R2.id.messageImageView)
    AppCompatImageView messageImageView;

    String imageUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_image_preview);
        getPassData();
        ButterKnife.bind(this);
        loadMessageImage();
    }

    private void loadMessageImage() {
        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher))
                    .into(messageImageView);
        }
    }

    private void getPassData() {
        Bundle passData = getIntent().getExtras();
        if (passData != null) {
            imageUrl = passData.getString(ARG_IMAGE_URL);
        }

    }
}
