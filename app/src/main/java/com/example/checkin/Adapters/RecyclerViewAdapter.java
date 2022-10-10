package com.example.checkin.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.checkin.Database.Logs;
import com.example.checkin.R;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Handler;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.VHolder> {

    Context context;
    List<Logs> logsList;
    Date date;
    MediaPlayer player;
    boolean playing = false, paused = false;
    int pausedPosition, currentPosition;
    long timerPosition;
    private VHolder currentHolder;


    public RecyclerViewAdapter(Context context, List<Logs> logsList) {
        this.context = context;
        this.logsList = logsList;
    }

    @NonNull
    @Override
    public VHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.single_log, parent, false);

        return new VHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull VHolder holder, int position) {

        holder.name.setText(logsList.get(position).getName());


        holder.date.setText(logsList.get(position).getDate());

        holder.time.setText(logsList.get(position).getTime());

        if (logsList.get(position).getImg() != null) {
            holder.img.setVisibility(View.VISIBLE);
            holder.img.setImageBitmap(StringToBitMap(logsList.get(position).getImg()));
        }


        if (logsList.get(position).isCheckedState()) {
            holder.in.setVisibility(View.VISIBLE);
            holder.out.setVisibility(View.GONE);
        } else {
            holder.out.setVisibility(View.VISIBLE);
            holder.in.setVisibility(View.GONE);
        }

        if (logsList.get(position).getRecord() != null) {
            holder.play.setVisibility(View.VISIBLE);
            //   holder.seekBar.setVisibility(View.VISIBLE);
            holder.play.setOnClickListener(v -> {
                holder.play.setVisibility(View.GONE);
                holder.pause.setVisibility(View.VISIBLE);
                if (currentPosition != position) {
                    stopAudio();
                    playAudio(position, holder);
                }else {
                    resumeAudio(holder);
                }
                holder.playTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        holder.seekBar.setProgress(player.getCurrentPosition());
                        Log.d("record", player.getCurrentPosition() + "" + "-----" + holder.playTimer.getBase());
                    }
                });

            });

            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        player.seekTo(progress);
                        holder.playTimer.setBase(SystemClock.elapsedRealtime() - seekBar.getProgress() - 1);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            holder.pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pauseAudio(holder);
                    holder.pause.setVisibility(View.GONE);
                    holder.play.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return logsList.size();
    }


    public class VHolder extends RecyclerView.ViewHolder {

        ImageView img, play, pause;
        TextView name, date, in, out, time;
        SeekBar seekBar;
        Chronometer playTimer, durationTime;

        public VHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.log_img);
            name = itemView.findViewById(R.id.log_name);
            date = itemView.findViewById(R.id.log_date);
            time = itemView.findViewById(R.id.log_time);
            in = itemView.findViewById(R.id.log_state_in);
            out = itemView.findViewById(R.id.log_state_out);
            play = itemView.findViewById(R.id.play_record);
            seekBar = itemView.findViewById(R.id.seekBar);
            pause = itemView.findViewById(R.id.pause_record);
            playTimer = itemView.findViewById(R.id.play_timer);
            durationTime = itemView.findViewById(R.id.duration_time);

        }
    }


    private void playAudio(int path, VHolder holder) {
        player = new MediaPlayer();

        currentPosition = path;
        currentHolder = holder;
        try {
            player.setDataSource(logsList.get(path).getRecord());
            player.prepare();
            holder.seekBar.setVisibility(View.VISIBLE);
            holder.playTimer.setVisibility(View.VISIBLE);
            holder.durationTime.setBase(SystemClock.elapsedRealtime() - player.getDuration());
            holder.playTimer.setBase(SystemClock.elapsedRealtime());
            holder.playTimer.setVisibility(View.VISIBLE);
            holder.seekBar.setMax(player.getDuration());

            player.start();
            holder.playTimer.start();
            playing = true;
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    holder.playTimer.stop();
                    holder.pause.setVisibility(View.GONE);
                    holder.play.setVisibility(View.VISIBLE);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void pauseAudio(VHolder holder) {
        if (playing) {
            player.pause();
            pausedPosition = player.getCurrentPosition();
            playing = false;
            paused = true;
            //holder.pause.setVisibility(View.GONE);
            //holder.play.setVisibility(View.VISIBLE);
            holder.playTimer.stop();
            timerPosition = SystemClock.elapsedRealtime() - holder.playTimer.getBase();

        }

    }

    private void resumeAudio(VHolder holder) {
        if (!player.isPlaying()) {
            player.seekTo(pausedPosition);
            player.start();
            holder.playTimer.setBase(SystemClock.elapsedRealtime() - holder.seekBar.getProgress());
            holder.playTimer.start();
            playing = true;
            paused = false;


        }

    }

    private void stopAudio( ) {
        if (player == null) return;
        if (player.isPlaying()) {
            player.stop();
            currentHolder.playTimer.stop();
            player = null;
            playing = false;
            paused = false;
            currentHolder.play.setVisibility(View.VISIBLE);
            currentHolder.pause.setVisibility(View.GONE);
            currentHolder.seekBar.setVisibility(View.GONE);
        }
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }


}
