package com.google.firebase.codelab.friendlychat;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFirebaseAdapter extends FirebaseRecyclerAdapter<FriendlyMessage, ChatFirebaseAdapter.CustomMessageViewHolder> {

    public ChatFirebaseAdapter(@NonNull FirebaseRecyclerOptions<FriendlyMessage> options, String username, Context context) {
        super(options);
        this.username = username;
        this.mContext = context;
    }

    private String username;
    private Context mContext;

    @Override
    protected void onBindViewHolder(@NonNull CustomMessageViewHolder customMessageViewHolder, int i, @NonNull FriendlyMessage friendlyMessage) {

        if (friendlyMessage.getName().equals(username)) {
            //show received message in left side
            customMessageViewHolder.chat_left_msg_layout.setVisibility(View.VISIBLE);
            customMessageViewHolder.chat_right_msg_layout.setVisibility(View.GONE);
            if (friendlyMessage.getText() != null) {
                customMessageViewHolder.leftMessageTextView.setText(friendlyMessage.getText());
                customMessageViewHolder.leftMessageTextView.setVisibility(TextView.VISIBLE);
                customMessageViewHolder.leftMessageImageView.setVisibility(ImageView.GONE);
            } else if (friendlyMessage.getImageUrl() != null) {
                String imageUrl = friendlyMessage.getImageUrl();
                if (imageUrl.startsWith("gs://")) {
                    StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(imageUrl);
                    storageReference.getDownloadUrl().addOnCompleteListener(
                            new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        String downloadUrl = task.getResult().toString();
                                        Glide.with(customMessageViewHolder.leftMessageImageView.getContext())
                                                .load(downloadUrl)
                                                .into(customMessageViewHolder.leftMessageImageView);
                                    } else {
                                        Log.w("Adapter", "Getting download url was not `successful.",
                                                task.getException());
                                    }
                                }
                            });
                } else {
                    Glide.with(customMessageViewHolder.leftMessageImageView.getContext())
                            .load(friendlyMessage.getImageUrl())
                            .into(customMessageViewHolder.leftMessageImageView);
                }
                customMessageViewHolder.leftMessageImageView.setVisibility(ImageView.VISIBLE);
                customMessageViewHolder.leftMessageTextView.setVisibility(TextView.GONE);
            }

            customMessageViewHolder.leftMessengerTextView.setText(friendlyMessage.getName());
            if (friendlyMessage.getPhotoUrl() == null) {
                customMessageViewHolder.leftMessengerImageView.setImageDrawable(ContextCompat.getDrawable(mContext,
                        R.drawable.ic_account_circle_black_36dp));
            } else {
                Glide.with(mContext)
                        .load(friendlyMessage.getPhotoUrl())
                        .into(customMessageViewHolder.leftMessengerImageView);
            }
        } else {
            //show received message in right side
            customMessageViewHolder.chat_left_msg_layout.setVisibility(View.GONE);
            customMessageViewHolder.chat_right_msg_layout.setVisibility(View.VISIBLE);
            if (friendlyMessage.getText() != null) {
                customMessageViewHolder.rightMessageTextView.setText(friendlyMessage.getText());
                customMessageViewHolder.rightMessageTextView.setVisibility(TextView.VISIBLE);
                customMessageViewHolder.rightMessageImageView.setVisibility(ImageView.GONE);
            } else if (friendlyMessage.getImageUrl() != null) {
                String imageUrl = friendlyMessage.getImageUrl();
                if (imageUrl.startsWith("gs://")) {
                    StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(imageUrl);
                    storageReference.getDownloadUrl().addOnCompleteListener(
                            new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        String downloadUrl = task.getResult().toString();
                                        Glide.with(customMessageViewHolder.rightMessageImageView.getContext())
                                                .load(downloadUrl)
                                                .into(customMessageViewHolder.rightMessageImageView);
                                    } else {
                                        Log.w("Adapter", "Getting download url was not successful.",
                                                task.getException());
                                    }
                                }
                            });
                } else {
                    Glide.with(customMessageViewHolder.rightMessageImageView.getContext())
                            .load(friendlyMessage.getImageUrl())
                            .into(customMessageViewHolder.rightMessageImageView);
                }
                customMessageViewHolder.rightMessageImageView.setVisibility(ImageView.VISIBLE);
                customMessageViewHolder.rightMessageTextView.setVisibility(TextView.GONE);
            }

            customMessageViewHolder.rightMessengerTextView.setText(friendlyMessage.getName());
            if (friendlyMessage.getPhotoUrl() == null) {
                customMessageViewHolder.rightMessengerImageView.setImageDrawable(ContextCompat.getDrawable(mContext,
                        R.drawable.ic_account_circle_black_36dp));
            } else {
                Glide.with(mContext)
                        .load(friendlyMessage.getPhotoUrl())
                        .into(customMessageViewHolder.rightMessengerImageView);
            }

        }
    }

    @NonNull
    @Override
    public CustomMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new CustomMessageViewHolder(inflater.inflate(R.layout.custom_item_message, parent, false));
    }


    class CustomMessageViewHolder extends RecyclerView.ViewHolder {

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

}
