package com.example.capstone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstone.model.VoiceCommandResponse;

import java.util.List;

public class VoiceCommandAdapter extends RecyclerView.Adapter<VoiceCommandAdapter.VoiceCommandViewHolder> {

    public interface OnItemDeleteListener {
        void onDelete(int position);
    }

    private List<VoiceCommandResponse> commandList;
    private OnItemDeleteListener deleteListener;

    public VoiceCommandAdapter(List<VoiceCommandResponse> commandList, OnItemDeleteListener deleteListener) {
        this.commandList = commandList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public VoiceCommandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voice_command, parent, false);
        return new VoiceCommandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoiceCommandViewHolder holder, int position) {
        VoiceCommandResponse item = commandList.get(position);
        holder.commandText.setText("명령어: " + item.getInputText());
        holder.functionText.setText("기능: " + item.getActionName());

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commandList.size();
    }

    static class VoiceCommandViewHolder extends RecyclerView.ViewHolder {
        TextView commandText;
        TextView functionText;
        ImageButton deleteButton;

        public VoiceCommandViewHolder(@NonNull View itemView) {
            super(itemView);
            commandText = itemView.findViewById(R.id.commandText);
            functionText = itemView.findViewById(R.id.functionText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
