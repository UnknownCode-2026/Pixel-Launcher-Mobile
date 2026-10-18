package th.samp.launcher.launcher.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import th.samp.launcher.App;
import th.samp.launcher.R;
import th.samp.launcher.launcher.adapters.NewsAdapter;

public class NewsFragment extends Fragment {
    public NewsFragment() {}

    private RecyclerView newsRv;
    private NewsAdapter newsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_news, container, false);
        newsRv = view.findViewById(R.id.rvNews);
        newsRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        newsAdapter = new NewsAdapter(getActivity());
        newsRv.setAdapter(newsAdapter);
        newsAdapter.addItems(App.getInstance().getStories());
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
