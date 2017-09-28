package soumyadeb.raven.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import soumyadeb.raven.R;
import soumyadeb.raven.models.User;
import soumyadeb.raven.ui.ProfileActivity;

/**
 * Created by Soumya Deb on 01-07-2017.
 */

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {
    private ArrayList<User> userList;
    private Activity activity;
    private int userItem;

    public UsersAdapter(Activity activity, ArrayList<User> userList, int userItem){
        this.activity = activity;
        this.userList = userList;
        this.userItem = userItem;

    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView mName, mStatus;
        public CircleImageView mImgProfile;
        public Button mChat, mAccept;
        public View mView;

        public MyViewHolder(View view) {
            super(view);
            mView = view;
            mName = (TextView) view.findViewById(R.id.text_display_name);
            mStatus = (TextView) view.findViewById(R.id.text_status);
            mImgProfile = (CircleImageView) view.findViewById(R.id.img_profile);
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(userItem, parent, false);
        MyViewHolder mViewHold = new MyViewHolder(mView);
        return mViewHold;
    }

    @Override
    public void onBindViewHolder(final UsersAdapter.MyViewHolder holder, int position) {
        final User user = userList.get(position);

        String name = user.getName();
        String status = user.getStatus();
        final String thumb_image = user.getThumb_image();
        final String userId = user.getKey();



        if(thumb_image.equals("default")){
            holder.mImgProfile.setImageResource(R.drawable.ic_circle_profile_img);
        }
        else {
            Picasso.with(holder.mView.getContext()).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.ic_circle_profile_img).into(holder.mImgProfile, new Callback() {
                @Override
                public void onSuccess() {
                    // Do nothing

                }

                @Override
                public void onError() {
                    Picasso.with(holder.mView.getContext()).load(thumb_image).placeholder(R.drawable.ic_circle_profile_img)
                            .into(holder.mImgProfile);
                }
            });
        }

        holder.mName.setText(name);
        holder.mStatus.setText(status);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(activity, ProfileActivity.class);
                profileIntent.putExtra("userId",userId);
                activity.startActivity(profileIntent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }
}
