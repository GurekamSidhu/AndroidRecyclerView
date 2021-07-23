package com.example.wattpadcoding.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wattpadcoding.R;
import com.example.wattpadcoding.data.Story;
import com.example.wattpadcoding.data.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StoryListAdapter extends RecyclerView.Adapter<StoryListAdapter.StoryViewHolder> {
    List<Story> storyList = new ArrayList<>();
    List<User> userList = new ArrayList<>();
    Context context;

    public StoryListAdapter(List<Story> stories, List<User> users, Context context) {
        this.storyList.addAll(stories);
        this.userList.addAll(users);
        this.context = context;
    }

    @Override
    public StoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.story_view_holder, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StoryListAdapter.StoryViewHolder holder,
            int position) {
        Story story = storyList.get(position);
        User user = getUserFromList(story.getUserId());
        holder.getStoryCover().setImageBitmap(loadBitmap(story.getId()));
        holder.getStoryTitle().setText(story.getTitle());
        holder.getUserName().append(user.getName());
        holder.getUserFullName().append(user.getFullName());
        holder.getUserAvatar().setImageBitmap(loadBitmap(user.getId()));
    }

    private User getUserFromList(String userId) {
        for (User user: userList) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return new User();
    }

    private Bitmap loadBitmap(String fileName) {
        String filePath = context.getFilesDir() + "/" + fileName + ".jpeg";
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(new File(filePath));
            Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
            fileInputStream.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public void updateLists(List<Story> stories,
                            List<User> users) {
        storyList.clear();
        storyList.addAll(stories);
        userList.clear();
        userList.addAll(users);
        notifyDataSetChanged();
    }

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
}
