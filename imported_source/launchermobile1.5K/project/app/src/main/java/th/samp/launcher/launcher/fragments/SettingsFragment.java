package th.samp.launcher.launcher.fragments;

import static th.samp.launcher.Config.GAME_PATH;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

import es.dmoral.toasty.Toasty;
import th.samp.launcher.App;
import th.samp.launcher.R;
import th.samp.launcher.Utils;
import th.samp.launcher.launcher.Preferences;
import th.samp.launcher.launcher.activities.DownloadActivity;

public class SettingsFragment extends Fragment
{
	private EditText nickName;
	private Button repairGame, btnVK, btnYT, btnDiscord;
	private ToggleButton btnNotifications, btnFpsCounter;
	private TextView whoDeveloped;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view;
		view = inflater.inflate(R.layout.fragment_settings, container, false);
		nickName = view.findViewById(R.id.editTextTextNickName);
		repairGame = view.findViewById(R.id.repairGame);
		btnNotifications = view.findViewById(R.id.switch1);
		btnFpsCounter = view.findViewById(R.id.switch2);
		btnVK = view.findViewById(R.id.button_vk);
		btnYT = view.findViewById(R.id.button_yt);
		btnDiscord = view.findViewById(R.id.button_discord);
		whoDeveloped = view.findViewById(R.id.textView18);
		// ввод ника
		nickName.setText(Preferences.getString(getActivity(), Preferences.NICKNAME));
		nickName.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEARCH ||
					actionId == EditorInfo.IME_ACTION_DONE ||
					event.getAction() == KeyEvent.ACTION_DOWN &&
							event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
				String obj = nickName.getText().toString();
				if (obj.isEmpty()) {
					Toasty.warning(getActivity(), getResources().getString(R.string.enterNik), Toast.LENGTH_LONG).show();
				} else if (!obj.contains("_")) {
					Toasty.warning(getActivity(), getResources().getString(R.string.mustContains_), Toast.LENGTH_LONG).show();
				} else if (obj.length() < 4) {
					Toasty.warning(getActivity(), getResources().getString(R.string.minLengthNik), Toast.LENGTH_LONG).show();
				} else {
					Preferences.putString(getActivity(), Preferences.NICKNAME, obj);
					Toasty.success(getActivity(), "บันทึกชื่อเล่นของคุณแล้ว: "+obj).show();
					File settings = new File(GAME_PATH+"SAMP/settings.ini");
					if(settings.exists()) {
						try {
							Wini w = new Wini(settings);
							w.put("client", "name", obj);
							w.store();
						} catch (IOException e) {
							Utils.writeLog(getActivity(), 'e', "ข้อผิดพลาด: "+e.getMessage());
						}
					} else {
						Utils.writeLog(getActivity(), 'e', "ไม่มีไฟล์ settings.ini");
					}
				}
			}
			return false;
		});
		repairGame.setOnClickListener(view1 -> {
			delete(new File(GAME_PATH));
			App.getInstance().downloadID = App.INSTALL_TYPE_GAMEFILES;
			Intent intent = new Intent(getActivity(), DownloadActivity.class);
			intent.putExtras(getActivity().getIntent());
			startActivity(intent);
			getActivity().finish();
		});
		btnNotifications.setChecked(Preferences.getBoolean(getActivity(), Preferences.NOTIFICATION, true));
		btnNotifications.setOnCheckedChangeListener((compoundButton, b) -> Preferences.putBoolean(getActivity(), Preferences.NOTIFICATION, b));
		//
		btnFpsCounter.setChecked(false);
		File f = new File(GAME_PATH+"SAMP/settings.ini");
		if(f.exists()) {
			Wini w = null;
			try {
				w = new Wini(f);
				boolean fps = w.get("debug", "fpscounter", boolean.class);
				if(fps) {
					btnFpsCounter.setChecked(true);
				}
				w.store();
			} catch (IOException e) {
				Utils.writeLog(getActivity(), 'e', "ข้อผิดพลาด: "+e.getMessage());
			}
		}
		btnFpsCounter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				Wini w = null;
				try {
					w = new Wini(f);
					if(b) {
						w.put("debug", "fpscounter", true);
					} else w.put("debug", "fpscounter", false);
					w.store();
				} catch (IOException e) {
					Utils.writeLog(getActivity(), 'e', "ข้อผิดพลาด: "+e.getMessage());
				}
			}
		});
		//
		whoDeveloped.setOnClickListener(view15 -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/miracledeveloper?mibextid=ZbWKwL"))));
		whoDeveloped.setText(Html.fromHtml("<font color=\"#6A7BA7\">Copyright </font><font color=\"#6A7BA7\"><a href=\"https://www.facebook.com/miracledeveloper?mibextid=ZbWKwL\">Chishnucha</a></font><font color=\"#6A7BA7\"></font>"), TextView.BufferType.SPANNABLE);
		btnVK.setOnClickListener(view14 -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(App.getInstance().URL_VK))));
		btnYT.setOnClickListener(view13 -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(App.getInstance().URL_YOUTUBE))));
		btnDiscord.setOnClickListener(view12 -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(App.getInstance().URL_DISCORD))));
		return view;
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	private void delete(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File delete : file.listFiles()) {
					delete(delete);
				}
				file.delete();
				return;
			}
			file.delete();
		}
	}

	private void loadFragment(Fragment fragment) {
		FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_place, fragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}
}
