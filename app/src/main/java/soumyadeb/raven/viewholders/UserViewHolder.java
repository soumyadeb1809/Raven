package soumyadeb.raven.viewholders;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import soumyadeb.raven.R;

/**
 * Created by Soumya Deb on 11-07-2017.
 */

public class UserViewHolder extends RecyclerView.ViewHolder {
    public View mView;
    public UserViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }
    public void setName(String name){
        TextView textName = (TextView) mView.findViewById(R.id.text_display_name);
        textName.setText(name);
    }

    public void setStatus(String status){
        TextView textStatus = (TextView) mView.findViewById(R.id.text_status);
        textStatus.setText(status);
    }

    public void setThumbImage(final String thumb_image, String online){
        final CircleImageView imgProfile = (CircleImageView) mView.findViewById(R.id.img_profile);

        if(online.equals("true")) {
            imgProfile.setBorderWidth(10);
            imgProfile.setBorderColor(Color.GREEN);
        }
        else {
            imgProfile.setBorderWidth(0);
        }

        if(thumb_image.equals("default")){
            imgProfile.setImageResource(R.drawable.ic_circle_profile_img);
        }
        else {
            Picasso.with(mView.getContext()).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.ic_circle_profile_img).into(imgProfile, new Callback() {
                @Override
                public void onSuccess() {
                    // Do nothing
                }

                @Override
                public void onError() {
                    Picasso.with(mView.getContext()).load(thumb_image).placeholder(R.drawable.ic_circle_profile_img)
                            .into(imgProfile);
                }
            });
        }
    }
}