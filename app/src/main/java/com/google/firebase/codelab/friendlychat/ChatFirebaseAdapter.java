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
import com.google.firebase.codelab.friendlychat.callback.OnMessageImageCallback;
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
import timber.log.Timber;

public class ChatFirebaseAdapter extends FirebaseRecyclerAdapter<FriendlyMessage, CustomMessageViewHolder> {

    private OnMessageImageCallback onMessageImageCallback;

    public ChatFirebaseAdapter(@NonNull FirebaseRecyclerOptions<FriendlyMessage> options, String username, Context context, OnMessageImageCallback onMessageImageCallback) {
        super(options);
        this.username = username;
        this.mContext = context;
        this.onMessageImageCallback = onMessageImageCallback;
    }

    private String username;
    private Context mContext;

    @Override
    protected void onBindViewHolder(@NonNull CustomMessageViewHolder customMessageViewHolder, int i, @NonNull FriendlyMessage friendlyMessage) {

        if (friendlyMessage.getName().equals(username)) {
            showMessageLeft(customMessageViewHolder, friendlyMessage);
        } else {
            showMessageRight(customMessageViewHolder, friendlyMessage);

        }
    }

    private void showMessageLeft(CustomMessageViewHolder customMessageViewHolder, @NonNull FriendlyMessage friendlyMessage) {
        //show received message in left side
        customMessageViewHolder.chat_left_msg_layout.setVisibility(View.VISIBLE);
        customMessageViewHolder.chat_right_msg_layout.setVisibility(View.GONE);
        if (friendlyMessage.hasText()) {
            customMessageViewHolder.leftMessageTextView.setText(friendlyMessage.getText());
            customMessageViewHolder.leftMessageTextView.setVisibility(TextView.VISIBLE);
            customMessageViewHolder.leftMessageImageView.setVisibility(ImageView.GONE);
        } else if (friendlyMessage.hasImage()) {
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
                                    Timber.d(task.getException());
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
            customMessageViewHolder.leftMessageImageView.setOnClickListener(v -> {
                if (onMessageImageCallback != null) {
                    onMessageImageCallback.onMessageImageClicked(imageUrl);
                }
            });
        } else if (friendlyMessage.hasFile()) {
            customMessageViewHolder.leftMessengerImageView.setImageDrawable(ContextCompat.getDrawable(mContext,
                    R.mipmap.ic_pdf));

            String fileUrl = friendlyMessage.getFileUrl();
            if (fileUrl.startsWith("gs://")) {
                StorageReference storageReference = FirebaseStorage.getInstance()
                        .getReferenceFromUrl(fileUrl);
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
                                    Timber.d(task.getException());
                                }
                            }
                        });
            }

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
    }


    private void showMessageRight(CustomMessageViewHolder customMessageViewHolder, @NonNull FriendlyMessage friendlyMessage) {
        //show received message in right side
        customMessageViewHolder.chat_left_msg_layout.setVisibility(View.GONE);
        customMessageViewHolder.chat_right_msg_layout.setVisibility(View.VISIBLE);
        if (friendlyMessage.hasText()) {
            customMessageViewHolder.rightMessageTextView.setText(friendlyMessage.getText());
            customMessageViewHolder.rightMessageTextView.setVisibility(TextView.VISIBLE);
            customMessageViewHolder.rightMessageImageView.setVisibility(ImageView.GONE);
        } else if (friendlyMessage.hasImage()) {
            String imageUrl = friendlyMessage.getImageUrl();
            if (imageUrl.startsWith("gs://")) {
                StorageReference storageReference = FirebaseStorage.getInstance()
                        .getReferenceFromUrl(imageUrl);
                storageReference.getDownloadUrl().addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                String downloadUrl = task.getResult().toString();
                                Glide.with(customMessageViewHolder.rightMessageImageView.getContext())
                                        .load(downloadUrl)
                                        .into(customMessageViewHolder.rightMessageImageView);
                            } else {
                                Timber.d(task.getException());
                            }
                        });
            } else {
                Glide.with(customMessageViewHolder.rightMessageImageView.getContext())
                        .load(friendlyMessage.getImageUrl())
                        .into(customMessageViewHolder.rightMessageImageView);
            }

            customMessageViewHolder.rightMessageImageView.setOnClickListener(v -> {
                if (onMessageImageCallback != null) {
                    onMessageImageCallback.onMessageImageClicked(imageUrl);
                }
            });
            customMessageViewHolder.rightMessageImageView.setVisibility(ImageView.VISIBLE);
            customMessageViewHolder.rightMessageTextView.setVisibility(TextView.GONE);
        } else if (friendlyMessage.hasFile()) {
            customMessageViewHolder.rightMessageImageView.setImageDrawable(ContextCompat.getDrawable(mContext,
                    R.mipmap.ic_pdf));

            String fileUrl = friendlyMessage.getFileUrl();
            if (fileUrl.startsWith("gs://")) {
                StorageReference storageReference = FirebaseStorage.getInstance()
                        .getReferenceFromUrl(fileUrl);
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
                                    Timber.d(task.getException());
                                }
                            }
                        });
            }
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

    @NonNull
    @Override
    public CustomMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new CustomMessageViewHolder(inflater.inflate(R.layout.custom_item_message, parent, false));
    }

}
