package com.dvd.android.googlelaunchersicons.list;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dvd.android.googlelaunchersicons.R;
import com.dvd.android.googlelaunchersicons.iconpack.IconPack;
import com.dvd.android.googlelaunchersicons.model.App;
import com.dvd.android.googlelaunchersicons.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author dvdandroid
 */
public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private final Context context;
    private final ItemClickListener listener;
    private final App[] appList;
    private boolean small = false;

    public AppsAdapter(Context context, App[] appList, ItemClickListener listener) {
        this.context = context;
        this.appList = appList;
        this.listener = listener;
    }

    public AppsAdapter(Context context, List<IconPack> iconPacks, ItemClickListener listener) {
        this.context = context;
        this.appList = new App[iconPacks.size()];

        for (int i = 0; i < iconPacks.size(); i++) {
            App ic = iconPacks.get(i);

            this.appList[i] = ic;
        }

        this.listener = listener;
        this.small = true;
    }

    @Override
    public AppsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(small ? R.layout.app_icon_row : R.layout.app_icon_row_tall, parent, false);

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
    public void onBindViewHolder(AppsAdapter.ViewHolder holder, int position) {
        App app = appList[position];

        holder.title.setText(app.label);
        holder.summary.setText(app.packageName);
        holder.icon.setImageDrawable(Utils.get(context).byteArrayToDrawable(app.icon));
    }

    @Override
    public int getItemCount() {
        return appList.length;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(appList[position].label.charAt(0));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(android.R.id.icon) ImageView icon;
        @BindView(android.R.id.title) TextView title;
        @BindView(android.R.id.summary) TextView summary;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
