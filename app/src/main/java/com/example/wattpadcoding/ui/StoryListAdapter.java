package com.example.wattpadcoding.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wattpadcoding.R;
import com.example.wattpadcoding.data.DataManager;
import com.example.wattpadcoding.data.Story;
import com.example.wattpadcoding.data.User;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StoryListAdapter extends RecyclerView.Adapter<StoryListAdapter.StoryViewHolder> {
    List<Story> storyList = new ArrayList<>();
    List<User> userList = new ArrayList<>();
    Context context;
    DataManager dataManager;

    /**
     * Adpter which takes the stories and users to display in recycler view list
     * @param stories : stories to display, this only contains id for user
     * @param users : user info to display from id in stories
     * @param context : context
     * @param dataManager: datamanager
     */
    public StoryListAdapter(List<Story> stories, List<User> users, Context context,
                            DataManager dataManager) {
        this.storyList.addAll(stories);
        this.userList.addAll(users);
        this.context = context;
        this.dataManager = dataManager;
    }

    /**
     * Inflate the view and return the view holder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public StoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.story_view_holder, parent, false);
        return new StoryViewHolder(view);
    }

    /**
     * Binds data on view holder at position with story at position in list,
     * this also calls load bitmap from storage
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(
            @NonNull StoryListAdapter.StoryViewHolder holder,
            int position) {
        Story story = storyList.get(position);
        User user = getUserFromList(story.getUserId());
        setBitmapToImageView(holder.getStoryCover(), story.getId());
        holder.getStoryTitle().setText(story.getTitle());
        holder.getUserName().append(user.getName());
        holder.getUserFullName().append(user.getFullName());
        setBitmapToImageView(holder.getUserAvatar(), user.getId());
    }

    /**
     * Get user matching id
     * @param userId user id
     * @return user object
     */
    private User getUserFromList(String userId) {
        for (User user: userList) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return new User();
    }

    /**
     * Loads image from storage on background thread and sets it to image view
     * @param imageView imageview
     * @param fileName file for storage
     */
    private void setBitmapToImageView(ImageView imageView, String fileName) {
        dataManager.loadBitmap(fileName, imageView::setImageBitmap);
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    /**
     * Updates lists once new data is fetched from internet to allow recycler list to be repopulated
     * @param stories
     * @param users
     */
    public void updateLists(List<Story> stories,
                            List<User> users) {
        storyList.clear();
        storyList.addAll(stories);
        userList.clear();
        userList.addAll(users);
        notifyDataSetChanged();
    }

    /**
     * Story view holder that is used by recycler view to implement the list
     */
    public static class StoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView storyCover;
        private final ImageView userAvatar;
        private final TextView storyTitle;
        private final TextView userName;
        private final TextView userFullName;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            storyCover = itemView.findViewById(R.id.story_cover);
            userAvatar = itemView.findViewById(R.id.user_avatar);
            storyTitle = itemView.findViewById(R.id.story_title);
            userName = itemView.findViewById(R.id.user_name);
            userFullName = itemView.findViewById(R.id.user_full_name);
        }

        public ImageView getStoryCover() {
            return storyCover;
        }

        public ImageView getUserAvatar() {
            return userAvatar;
        }

        public TextView getStoryTitle() {
            return storyTitle;
        }

        public TextView getUserName() {
            return userName;
        }

        public TextView getUserFullName() {
            return userFullName;
        }
    }

    /**
     * Image fetched callback
     */
    public interface ImageFetchedFromStorage {
        void onImageFetched(Bitmap bitmap);
    }
}
