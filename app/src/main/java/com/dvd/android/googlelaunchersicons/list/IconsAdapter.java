package com.dvd.android.googlelaunchersicons.list;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dvd.android.googlelaunchersicons.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author dvdandroid
 */
public class IconsAdapter extends RecyclerView.Adapter<IconsAdapter.ViewHolder> {

    private final List<Drawable> iconsList;
    private final ItemClickListener listener;

    public IconsAdapter(List<Drawable> iconsList, ItemClickListener listener) {
        this.iconsList = iconsList;
        this.listener = listener;
    }

    @Override
    public IconsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.icon, parent, false);

        final ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(viewHolder.getAdapterPosition());
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(IconsAdapter.ViewHolder holder, int position) {
        holder.icon.setImageDrawable(iconsList.get(position));
    }

    @Override
    public int getItemCount() {
        return iconsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(android.R.id.icon) ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
