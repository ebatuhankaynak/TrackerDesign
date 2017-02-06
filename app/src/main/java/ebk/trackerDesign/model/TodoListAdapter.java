package ebk.trackerDesign.model;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ebk.trackerDesign.R;

/**
 * Created by E.Batuhan Kaynak on 9.8.2016.
 */
public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.ViewHolder> {
    private String[] todo;
    private String[] estimatedTime;
    private int[] typeImageId;
    private Listener listener;

    public TodoListAdapter(String[] todo, String[] estimatedTime, int[] typeImageId) {
        this.todo = todo;
        this.estimatedTime = estimatedTime;
        this.typeImageId = typeImageId;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_todo, parent, false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        CardView cardView = holder.cardView;

        TextView todoTextView = (TextView)cardView.findViewById(R.id.todoTextView);
        todoTextView.setText(todo[position]);
        TextView estimatedTimeTextView = (TextView)cardView.findViewById(R.id.estTimeTextView);
        estimatedTimeTextView.setText("Estimated Time:" + estimatedTime[position] + "mins.");
        ImageView imageView = (ImageView)cardView.findViewById(R.id.typeImageView);
        Drawable drawable = cardView.getResources().getDrawable(typeImageId[position]);
        imageView.setImageDrawable(drawable);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(position);

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return todo.length;
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public static interface Listener {
        public void onClick(int position);
    }
}