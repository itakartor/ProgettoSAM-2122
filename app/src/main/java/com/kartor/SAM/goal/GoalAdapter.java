package com.kartor.SAM.goal;

import static com.kartor.SAM.MediaPlayer.MediaPlayerService.convertFormat;
import static com.kartor.SAM.SummaryActivity.df;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kartor.SAM.R;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder>{
    private Cursor mCursor;
    private Context mContext;

    public GoalAdapter(Cursor mCursor, Context mContext) {
        this.mCursor = mCursor;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.goal_item, parent, false);
        return new GoalViewHolder(view);
    }

    // cosa inserisco nel ViewHolder
    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        if(mCursor.moveToNext()) {
            String timeSession = mCursor.getString(mCursor.getColumnIndex(mContext.getString(R.string.time_duration_column)));
            String maxAccValue = mCursor.getString(mCursor.getColumnIndex(mContext.getString(R.string.maxValueAccelerometer)));
            String minAccValue = mCursor.getString(mCursor.getColumnIndex(mContext.getString(R.string.minValueAccelerometer)));
            String steps = mCursor.getString(mCursor.getColumnIndex(mContext.getString(R.string.step_column)));
            String date = mCursor.getString(mCursor.getColumnIndex(mContext.getString(R.string.timestamp)));
            holder.maxAccValue.setText(df.format(Float.parseFloat(maxAccValue)));
            holder.minAccValue.setText(df.format(Float.parseFloat(minAccValue)));
            holder.steps.setText(steps);
            holder.date.setText(date);
            holder.timeSession.setText(convertFormat(Integer.parseInt(timeSession)));
        }
    }

    @Override
    public int getItemCount() {
        if(this.mCursor == null) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return;
        }
        if (newCursor != null) {
            mCursor = newCursor;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, getItemCount());
            mCursor = null;
        }
    }
    // classe del view Holder
    class GoalViewHolder extends RecyclerView.ViewHolder {

        private TextView maxAccValue;
        private TextView minAccValue;
        private TextView steps;
        private TextView timeSession;
        private TextView date;

        public GoalViewHolder(View itemView) {
            super(itemView);
            maxAccValue = itemView.findViewById(R.id.max_acc_item);
            minAccValue = itemView.findViewById(R.id.min_acc_item);
            steps = itemView.findViewById(R.id.steps);
            date = itemView.findViewById(R.id.date);
            timeSession = itemView.findViewById(R.id.time_session);
        }
    }
}
