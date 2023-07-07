package jp.lopezlab.aoyagi.movesensedatacollection.activity_01_connection;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import jp.lopezlab.aoyagi.movesensedatacollection.R;

public class MovesenseAdapter extends RecyclerView.Adapter<MovesenseAdapter.ViewHolder> {

    private final String TAG = "MovesenseAdapter";

    private final ArrayList<MovesenseModel> movesenseInfoArrayList;
    private final View.OnClickListener onClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView movesenseId;

        public ViewHolder(View view) {
            super(view);
            movesenseId = view.findViewById(R.id.movesenseId);
        }

        public TextView getMovesenseId() {
            return movesenseId;
        }
    }

    public MovesenseAdapter(ArrayList<MovesenseModel> movesenseInfoArrayList, View.OnClickListener onClickListener) {
        this.movesenseInfoArrayList = movesenseInfoArrayList;
        this.onClickListener = onClickListener;
        Log.d(TAG, "MovesenseAdapter");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movesense, parent, false);
        Log.d(TAG, "onCreateViewHolder");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getMovesenseId().setText(movesenseInfoArrayList.get(position).getSerial());
        holder.itemView.setTag(movesenseInfoArrayList.get(position).getAddress());
        holder.itemView.setOnClickListener(onClickListener);
        Log.d(TAG, "onBindViewHolder");
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount");
        return movesenseInfoArrayList.size();
    }

    public void add(MovesenseModel movesense) {
        if (!movesenseInfoArrayList.contains(movesense)) {
            movesenseInfoArrayList.add(movesense);
        }
        notifyItemChanged(movesenseInfoArrayList.size());

        Log.d(TAG, "add: " + movesense.getSerial());
    }
}
