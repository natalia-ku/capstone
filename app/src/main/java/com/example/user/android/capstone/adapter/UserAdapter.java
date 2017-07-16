
package com.example.user.android.capstone.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.user.android.capstone.R;
import com.example.user.android.capstone.activity.UserProfileActivity;
import com.example.user.android.capstone.model.User;

import java.util.List;


/**
 * Created by nataliakuleniuk on 6/26/17.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    List<User> users;
    Context context;


    public UserAdapter(Context context, List<User> users) {
        this.users = users;
        this.context = context;
    }


    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UserAdapter.ViewHolder holder, final int position) {
        holder.userNameTextView.setText(users.get(position).getName());
        String photo = users.get(position).getPhoto();

        Glide.with(context).load(photo).asBitmap().centerCrop().into(new BitmapImageViewTarget(holder.mUserPhoto) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                holder.mUserPhoto.setImageDrawable(circularBitmapDrawable);
            }
        });


        holder.userLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Class destinationClass = UserProfileActivity.class;
                Intent intentToStartEventInfoActivity = new Intent(context, destinationClass);
                intentToStartEventInfoActivity.putExtra("userEmail", users.get(position).getEmail());
                context.startActivity(intentToStartEventInfoActivity);
            }
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout userLinearLayout;
        TextView userNameTextView;
        ImageView mUserPhoto;

        public ViewHolder(View itemView) {
            super(itemView);
            userNameTextView = (TextView) itemView.findViewById(R.id.user_name);
            userLinearLayout = (LinearLayout) itemView.findViewById(R.id.layout_user);
            mUserPhoto = (ImageView) itemView.findViewById(R.id.user_photo_event_participants);
        }
    }


}
