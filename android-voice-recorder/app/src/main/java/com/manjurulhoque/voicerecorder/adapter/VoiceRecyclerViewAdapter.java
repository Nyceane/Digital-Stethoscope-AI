package com.manjurulhoque.voicerecorder.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manjurulhoque.voicerecorder.R;
import com.manjurulhoque.voicerecorder.model.Record;

import java.util.List;

public class VoiceRecyclerViewAdapter extends RecyclerView.Adapter<VoiceRecyclerViewAdapter.VoiceViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Record record, int position);
    }

    private List<Record> records;
    private Context context;
    private final OnItemClickListener listener;

    public VoiceRecyclerViewAdapter(Context context, List<Record> records, OnItemClickListener listener) {
        this.records = records;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_single_voice_record, parent, false);
        return new VoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VoiceViewHolder holder, final int position) {
        holder.bind(records.get(position), listener, position);
//        holder.mCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                holder.mCard.setCardBackgroundColor(context.getResources().getColor(R.color.relative_layout));
////                startPlayRecord(records.get(position).getMinute(), records.get(position).getName());
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class VoiceViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewRecordName;
        public TextView textViewTime;
        public CardView mCard;
        private View view;

        public VoiceViewHolder(View itemView) {
            super(itemView);

            view = itemView;

            textViewRecordName = itemView.findViewById(R.id.textViewRecordName);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            mCard = itemView.findViewById(R.id.mCard);
        }

        public void bind(final Record record, final VoiceRecyclerViewAdapter.OnItemClickListener listener, final int position) {
            textViewRecordName.setText(record.getName());
            textViewTime.setText(record.getMinute());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(record, position);
                }
            });
        }
    }

}
