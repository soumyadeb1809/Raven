package soumyadeb.raven.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import soumyadeb.raven.ui.ChatsFragment;
import soumyadeb.raven.ui.FriendsFragment;
import soumyadeb.raven.ui.RequestsFragment;

/**
 * Created by Soumya Deb on 14-06-2017.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {
    public static final int ITEM_REQUESTS = 0;
    public static final int ITEM_CHATS = 1;
    public static final int ITEM_FRIENDS = 2;

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case ITEM_REQUESTS: return new RequestsFragment();
            case ITEM_CHATS: return new ChatsFragment();
            case ITEM_FRIENDS: return new FriendsFragment();
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        super.getPageTitle(position);
        switch (position){
            case 0: return "REQUESTS";
            case 1: return "CHATS";
            case 2: return "FRIENDS";
            default: return null;
        }
    }

}
