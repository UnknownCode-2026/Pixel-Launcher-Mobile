package th.samp.launcher.launcher.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

import th.samp.launcher.R;
import th.samp.launcher.launcher.network.Story;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private Context context;
    private List<Story> stories = new ArrayList();

    public NewsAdapter(Context context2) { context = context2; }

    public void addItems(List<Story> list) {
        this.stories = list;
        notifyDataSetChanged();
    }

    public void deleteItem(int i) {
        this.stories.remove(i);
        notifyDataSetChanged();
    }

    public void addItem(Story story) {
        this.stories.add(story);
        notifyDataSetChanged();
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_news, viewGroup, false));
    }

    public void onBindViewHolder(ViewHolder holder, int i) {
        Story story = stories.get(i);
        holder.title.setText(story.getTitle());
        Glide.with(context).load(story.getImage()).into(holder.image);
        holder.more.setVisibility(story.getLink().isEmpty() ? View.GONE : View.VISIBLE);
        holder.more.setOnClickListener(view -> {
            view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.button_click));
            new Handler().postDelayed(() -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(story.getLink()))), 200);
        });
    }

    public int getItemCount() { return this.stories.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        View itemView;
        Button more;
        TextView title;

        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.tvNewsText);
            image = (ImageView) view.findViewById(R.id.ivNewsImage);
            more = (Button) view.findViewById(R.id.button);
            itemView = view;
        }
    }
}
