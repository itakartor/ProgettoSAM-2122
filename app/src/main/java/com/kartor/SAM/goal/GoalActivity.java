package com.kartor.SAM.goal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationBarView;

import com.kartor.SAM.DB.SportSessionViewModel;
import com.kartor.SAM.MainActivity;
import com.kartor.SAM.MediaPlayer.ListTracesActivity;
import com.kartor.SAM.R;
import com.kartor.SAM.databinding.ActivityGoalBinding;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class GoalActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private Cursor mCursor;
    private ActivityGoalBinding binding;
    private GoalAdapter goalAdapter;
    private SportSessionViewModel sportSessionViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sportSessionViewModel = new ViewModelProvider(this).get(SportSessionViewModel.class);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerGoal.setLayoutManager(layoutManager);
        binding.recyclerGoal.setHasFixedSize(true);
        goalAdapter = new GoalAdapter(mCursor,this);
        binding.recyclerGoal.setAdapter(goalAdapter);
        binding.bottomNavigation.setOnItemSelectedListener(this);
        binding.bottomNavigation.getMenu().getItem(0).setChecked(true);
        loadGoalData();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.playlist_bottom_nav:
                intent = new Intent(getBaseContext(), ListTracesActivity.class);
                startActivity(intent);
                return true;
            case R.id.home:
                intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }
    private void showErrorMessage() {
        binding.recyclerGoal.setVisibility(View.INVISIBLE);
        binding.errorText.setVisibility(View.VISIBLE);
    }
    private void showGoalDataView() {
        binding.errorText.setVisibility(View.INVISIBLE);
        binding.recyclerGoal.setVisibility(View.VISIBLE);
    }
    private void showVoidListView(){
        binding.voidListText.setVisibility(View.VISIBLE);
        binding.recyclerGoal.setVisibility(View.INVISIBLE);
    }
    // recycler view
    private void loadGoalData() {
        showGoalDataView();
        new GoalActivity.getGoalsDataRecycler().execute();
    }
    private class getGoalsDataRecycler extends AsyncTask<Void,Void,Cursor> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.pbLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            sportSessionViewModel.getAllSportSession();
            try {
                return sportSessionViewModel.getCursorSportSession();
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            binding.pbLoadingIndicator.setVisibility(View.INVISIBLE);
            if(cursor != null) {
                if(cursor.getCount() > 0) {
                    showGoalDataView();
                } else {
                    showVoidListView();
                }
                goalAdapter.swapCursor(cursor);
            } else {
                showErrorMessage();
            }
        }
    }
}