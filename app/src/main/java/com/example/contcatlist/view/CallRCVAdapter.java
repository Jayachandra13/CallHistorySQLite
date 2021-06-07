package com.example.contcatlist.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contcatlist.R;
import com.example.contcatlist.model.CallRecord;

import java.util.ArrayList;

public class CallRCVAdapter extends RecyclerView.Adapter<CallRCVAdapter.CallListVH> {
    Context context;
    ArrayList<CallRecord> list;
    RecyclerView rcv;
    LinearLayoutManager linearLayoutManager;

    public CallRCVAdapter(Context context, ArrayList<CallRecord> list, RecyclerView rcv) {
        this.context = context;
        this.list = list;
        this.rcv = rcv;
        linearLayoutManager = (LinearLayoutManager) rcv.getLayoutManager();
    }

    @Override
    public CallListVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CallListVH(LayoutInflater.from(context).inflate(R.layout.call_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CallRCVAdapter.CallListVH holder, int position) {
        if (holder != null) {
            holder.tvPhoneNumber.setText(list.get(position).getPhoneNumber());
            holder.tvDuration.setText("" + list.get(position).getDuration() + " sec");
            holder.tvStartDT.setText("Call Started at : " + list.get(position).getStartTime());
            holder.tvEndDT.setText("Call Ended at: " + list.get(position).getEndTime());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CallListVH extends RecyclerView.ViewHolder {
        TextView tvPhoneNumber;
        TextView tvDuration;
        TextView tvStartDT;
        TextView tvEndDT;

        public CallListVH(View itemView) {
            super(itemView);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvStartDT = itemView.findViewById(R.id.tvStartDT);
            tvEndDT = itemView.findViewById(R.id.tvEndDT);
        }
    }
}
