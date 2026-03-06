package com.example.capstone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone.model.ScheduleItem;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<ScheduleItem> scheduleList;
    private OnItemActionListener listener;

    // 인터페이스: 삭제 및 스위치 토글 처리
    public interface OnItemActionListener {
        void onDelete(int position);
        void onToggle(int position, boolean isOn);
    }

    // 생성자
    public ScheduleAdapter(List<ScheduleItem> scheduleList, OnItemActionListener listener) {
        this.scheduleList = scheduleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.schedule_item, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleItem item = scheduleList.get(position);
        holder.scheduleTimeText.setText(item.getDay() + " " + item.getTime() + " - " + item.getStatus());
        holder.scheduleSwitch.setChecked(item.isEnabled());

        // 스위치 ON/OFF 토글 이벤트
        holder.scheduleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setEnabled(isChecked);
            if (listener != null) listener.onToggle(position, isChecked);
        });

        // 삭제 아이콘 클릭 이벤트
        holder.deleteIcon.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(position);
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(scheduleList.size(), 4); // 최대 4개까지만 표시
    }

    // ViewHolder 클래스
    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView scheduleTimeText;
        Switch scheduleSwitch;
        ImageView deleteIcon;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            scheduleTimeText = itemView.findViewById(R.id.scheduleTimeText);
            scheduleSwitch = itemView.findViewById(R.id.scheduleSwitch);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
        }
    }
}
