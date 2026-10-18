package th.samp.launcher.launcher.fragments;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;

import th.samp.launcher.App;
import th.samp.launcher.R;
import th.samp.launcher.launcher.adapters.ServerAdapter;
import th.samp.launcher.launcher.network.Server;
import th.samp.launcher.launcher.network.ServerListener;

public class ServersFragment extends Fragment {

	private RecyclerView serversRecycler;
	private ArrayList<Server> serverList;
	private ServerAdapter serverAdapter;
	private ServerListener serverListener;

	public ServersFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view;
		view = inflater.inflate(R.layout.fragment_servers, container, false);
		serverList = new ArrayList<>();
		serversRecycler = view.findViewById(R.id.rvServers); // server
		serversRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		serverAdapter = new ServerAdapter(ServersFragment.this, App.getInstance().getServerList());
		serversRecycler.setAdapter(serverAdapter);
		serverListener = () -> serverAdapter.notifyDataSetChanged();
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
	}

	private void loadFragment(Fragment fragment) {
		FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_place, fragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}
}