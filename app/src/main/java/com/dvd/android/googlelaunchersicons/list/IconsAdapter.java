/*
 * Copyright 2016 dvdandroid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
