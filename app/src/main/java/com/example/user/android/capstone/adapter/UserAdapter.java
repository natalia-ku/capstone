
package com.example.user.android.capstone.adapter;

        import android.content.Context;
        import android.content.Intent;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.LinearLayout;
        import android.widget.TextView;

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


    public UserAdapter( Context context, List<User> users) {
        this.users = users;
        this.context = context;
    }


    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserAdapter.ViewHolder holder, final int position) {
        holder.userNameTextView.setText(users.get(position).getName());
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
        public ViewHolder(View itemView) {
            super(itemView);
            userNameTextView = (TextView) itemView.findViewById(R.id.user_name);
            userLinearLayout = (LinearLayout) itemView.findViewById(R.id.layout_user);
        }
    }



}
