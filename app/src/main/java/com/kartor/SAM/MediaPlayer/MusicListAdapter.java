package com.kartor.SAM.MediaPlayer;

import static com.kartor.SAM.MediaPlayer.MediaPlayerService.convertFormat;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.kartor.SAM.DB.Audio;
import com.kartor.SAM.R;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicViewHolder> {

    // Holds on to the cursor to display the waitlist
    private Cursor mCursor;
    private Context mContext;

    //click listener per recycler view
    private final ForecastAdapterOnClickHandler mClickHandler;

    public interface ForecastAdapterOnClickHandler {
        void onClick();
    }
    public MusicListAdapter(Cursor mCursor, Context mContext, ForecastAdapterOnClickHandler mClickHandler) {
        this.mCursor = mCursor;
        this.mContext = mContext;
        this.mClickHandler = mClickHandler;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.music_item, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        if(mCursor.moveToNext()) {
            String title = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String duration = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            //String playlist = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.PL));
            holder.nameMusicTextView.setText(title);
            holder.durationTextView.setText(convertFormat(Integer.parseInt(duration)));
            //holder.playlistTextView.setText();
        }
    }

    @Override
    public int getItemCount() {
        if(this.mCursor == null) return 0;
        return mCursor.getCount();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    /**
     * Swaps the Cursor currently held in the adapter with a new one
     * and triggers a UI refresh
     *
     * @param newCursor the new cursor that will replace the existing one
     */
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

    class MusicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Will display the music title
        private TextView nameMusicTextView;
        // Will display music's duration
        private TextView durationTextView;

        //private TextView playlistTextView;

        //private int playlist = -1;

        public MusicViewHolder(View itemView) {
            super(itemView);
            nameMusicTextView = itemView.findViewById(R.id.music_title_text_view);
            durationTextView = itemView.findViewById(R.id.music_duration_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            /*switch (playlist) {
                case -1:{
                    v.setBackgroundColor(Color.parseColor("#38c172"));
                    this.playlist += 1;
                    break;
                }
                case 0:{
                    v.setBackgroundColor(Color.parseColor("#fbb117"));
                    this.playlist += 1;
                    break;
                }
                case 1:{
                    v.setBackgroundColor(Color.parseColor("#e3342f"));
                    this.playlist += 1;
                    break;
                }
                case 2:{
                    v.setBackgroundColor(Color.parseColor("#000000"));
                    this.playlist = -1;
                    break;
                }
            }*/

            mClickHandler.onClick();
        }
    }
}
