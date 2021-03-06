package com.hhua.android.producthunt.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hhua.android.producthunt.ProductHuntApplication;
import com.hhua.android.producthunt.ProductHuntClient;
import com.hhua.android.producthunt.R;
import com.hhua.android.producthunt.activities.CollectionActivity;
import com.hhua.android.producthunt.activities.DetailsActivity;
import com.hhua.android.producthunt.activities.UserActivity;
import com.hhua.android.producthunt.adapters.NotificationsArrayAdapter;
import com.hhua.android.producthunt.models.Collection;
import com.hhua.android.producthunt.models.Notification;
import com.hhua.android.producthunt.models.Post;
import com.hhua.android.producthunt.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class NotificationsFragment extends Fragment {
    private ProductHuntClient client;
    private SwipeRefreshLayout swipeContainer;
    private NotificationsArrayAdapter notificationsArrayAdapter;
    private ListView lvNotifications;
    private List<Notification> notifications;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        client = ProductHuntApplication.getRestClient();

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.notificationsSwipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                refreshNotifications();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        lvNotifications = (ListView) view.findViewById(R.id.lvNotifications);
        lvNotifications.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Notification notification = notificationsArrayAdapter.getItem(position);
                Intent intent;

                if(notification.getType().equals("Collection")){
                    intent = new Intent(getContext(), CollectionActivity.class);
                    intent.putExtra(Collection.COLLECTION_ID_MESSAGE, notification.getReferenceId());
                }else if(notification.getType().equals("User")){
                    intent = new Intent(getContext(), UserActivity.class);
                    intent.putExtra(User.USER_ID_MESSAGE, notification.getReferenceId());
                }else{
                    intent = new Intent(getContext(), DetailsActivity.class);
                    intent.putExtra(Post.POST_ID_MESSAGE, notification.getReferenceId());
                }

                startActivity(intent);
            }
        });
        notifications = new ArrayList<Notification>();
        notificationsArrayAdapter = new NotificationsArrayAdapter(getContext(), notifications);
        lvNotifications.setAdapter(notificationsArrayAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        populateNotifications();
    }

    public void refreshNotifications(){
        client.getAllNotifications(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Notifications", response.toString());
                try{
                    List<Notification> notifications = Notification.fromJSONArray(response.getJSONArray("notifications"));
                    notificationsArrayAdapter.clear();
                    notificationsArrayAdapter.addAll(notifications);

                    swipeContainer.setRefreshing(false);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }
        });
    }

    public void populateNotifications(){
        client.getAllNotifications(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    List<Notification> notificationsList = Notification.fromJSONArray(response.getJSONArray("notifications"));
                    notificationsArrayAdapter.addAll(notificationsList);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }
        });
    }
}
