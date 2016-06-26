package com.droidsans.photo.droidphoto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.droidsans.photo.droidphoto.util.RecyclerViewLoader;
import com.droidsans.photo.droidphoto.util.transform.CircleTransform;
import com.droidsans.photo.droidphoto.util.view.FontTextView;
import com.droidsans.photo.droidphoto.util.GlobalSocket;
import com.droidsans.photo.droidphoto.util.PicturePack;
import com.droidsans.photo.droidphoto.util.adapter.ProfileFeedRecycleViewAdapter;
import com.droidsans.photo.droidphoto.util.SpacesItemDecoration;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements RecyclerViewLoader {
	public static final int EDIT_PROFILE = 1;
	public static final int UPDATE_LIKE_STATE = 2;
	public static final String DISPLAY_NAME = "displayName";
	public static final String PROFILE_DESCRIPTION = "profileDesc";
	public static final String AVATAR_URL = "avatarURL";
	public static final int TARGET_PROFILE_FRAGMENT = 12345;

	private static final int FEED_LIMIT_PER_REQUEST = 21;

	public static ProfileFragment mProfileFragment;

	private ProgressBar loadingCircle;
	private LinearLayout reloadLayout;
	private RelativeLayout mainLayout;

	private ImageView profilePic;
	private FontTextView displayNameTv, usernameTV, profileDescTV;
	private RecyclerView profileFeedPicRecyclerview;

	private FontTextView reloadText;
	private Button reloadButton;

	public static final String baseURL = "/data/avatar/";
	private String username;
	private String avatarURL;

	private Handler delayAction = new Handler();

	private Emitter.Listener onGetUserInfoRespond;
	private Emitter.Listener onGetUserFeedRespond;
	private Emitter.Listener onUpdateUserFeedRespond;
	private Emitter.Listener onDisconnect;

	//    private UserPictureGridAdapter adapter;
	public ProfileFeedRecycleViewAdapter adapter;
	private ArrayList<PicturePack> packs;
	private String skipDate = null;

	private MenuInflater menuInflater;
	private Menu menu;
	private RelativeLayout deletePicFakeSnackbar;
	private Button deletePic;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
		setHasOptionsMenu(true);
		loadingCircle = (ProgressBar) rootView.findViewById(R.id.loading_circle);
		reloadLayout = (LinearLayout) rootView.findViewById(R.id.reload_view);
		mainLayout = (RelativeLayout) rootView.findViewById(R.id.main_view);
		initialize();
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menuInflater = inflater;
		this.menu = menu;

		menu.clear();
		inflater.inflate(R.menu.menu_profile, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_edit_profile:
			//                Toast.makeText(getActivity(), "edit profile", Toast.LENGTH_SHORT).show();
			Intent editProfileIntent = new Intent(getActivity(), EditProfileActivity.class);
			editProfileIntent.putExtra(DISPLAY_NAME, displayNameTv.getText().toString());
			editProfileIntent.putExtra(PROFILE_DESCRIPTION, profileDescTV.getText().toString());
			editProfileIntent.putExtra(AVATAR_URL, avatarURL);
			startActivityForResult(editProfileIntent, EDIT_PROFILE);
			return true;
		case R.id.action_delete:
			toggleEditMode();
			return true;
		case R.id.action_cancel_delete:
			cancelEditMode();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initialize() {
		setTargetFragment(this, TARGET_PROFILE_FRAGMENT);
		findAllById();
		setupProfileFeedRecyclerView();
		setupListener();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		requestUserinfo();
		requestUserPhoto();
	}

	private void setupProfileFeedRecyclerView() {
		profileFeedPicRecyclerview.addItemDecoration(new SpacesItemDecoration(getActivity(), getResources().getInteger(
				R.integer.profile_feed_col_num), (int) getResources().getDimension(
				R.dimen.profile_recycleview_item_space), false, false, false, false));
		profileFeedPicRecyclerview.setLayoutManager(new GridLayoutManager(getActivity(), getResources().getInteger(
				R.integer.profile_feed_col_num)));
	}

	private void setupListener() {
		deletePic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleEditMode();
			}
		});

		onGetUserInfoRespond = new Emitter.Listener() {
			@Override
			public void call(final Object... args) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						JSONObject data = (JSONObject) args[0];
						if (data.optBoolean("success")) {
							loadingCircle.setVisibility(ProgressBar.GONE);
							mainLayout.setVisibility(FrameLayout.VISIBLE);

							JSONObject userObj = data.optJSONObject("userObj");

							Log.d("droidphoto", userObj.optString("username") + " | " + userObj.optString("disp_name"));
							username = userObj.optString("username");
							usernameTV.setText("@" + username);
							displayNameTv.setText(userObj.optString("disp_name"));
							avatarURL = userObj.optString("avatar_url");
							Glide.with(getActivity().getApplicationContext())
									.load(GlobalSocket.serverURL + baseURL + avatarURL)
									//                                    .load(GlobalSocket.serverURL + baseURL + "test.jpg")
									.diskCacheStrategy(DiskCacheStrategy.SOURCE)
									.placeholder(R.drawable.avatar_placeholder_300).centerCrop()
									.transform(new CircleTransform(getActivity().getApplicationContext()))
									//                                    .dontAnimate()
									.into(profilePic);
							profileDescTV.setText(userObj.optString("profile_desc"));

						} else {
							switch (data.optString("msg")) {
							case "db error":
								//                                    Toast.makeText(getActivity().getApplicationContext(), "db error, please try again", Toast.LENGTH_SHORT).show();
								Snackbar.make(mainLayout, "db error, please try again", Snackbar.LENGTH_SHORT)
								//                                            .setAction("OK", null)
										.show();
								break;
							case "token error":
								//                                    Toast.makeText(getActivity().getApplicationContext(), "what the fuck !!? how can you invalid your f*cking token ??", Toast.LENGTH_SHORT).show();
								break;
							}
							initReload();
						}
					}
				});
			}
		};
		GlobalSocket.mSocket.on("get_user_info", onGetUserInfoRespond);

		onGetUserFeedRespond = new Emitter.Listener() {
			@Override
			public void call(final Object... args) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT);
						JSONObject data = (JSONObject) args[0];
						if (data.optBoolean("success")) {
							GlobalSocket.mSocket.off("get_user_feed");

							JSONArray photoList = data.optJSONArray("photoList");
							packs = PicturePack.makePacksFromJSONArray(photoList);
							if(packs.size() > 0) {
								skipDate = packs.get(packs.size() - 1).submitDate;
							}
//							skipDate = (packs.size() - 1) + "";
							Log.d(getClass().getSimpleName(), "skipDate = " + skipDate);

							//add new footer if not the last photo
							if(photoList.length() == FEED_LIMIT_PER_REQUEST){
								PicturePack footer = new PicturePack();
								packs.add(footer);
							}

							Log.d("droidphoto", "set adapter");
							adapter = new ProfileFeedRecycleViewAdapter(getActivity(), ProfileFragment.this, packs);
							profileFeedPicRecyclerview.setAdapter(adapter);

						} else {
							Log.d("droidphoto", "User Feed error: " + data.optString("msg"));
							initReload();
						}
					}
				});
			}
		};
		if (!GlobalSocket.mSocket.hasListeners("get_user_feed")) {
			GlobalSocket.mSocket.on("get_user_feed", onGetUserFeedRespond);
		}

		onUpdateUserFeedRespond = new Emitter.Listener() {
			@Override
			public void call(final Object... args) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						GlobalSocket.mSocket.off("update_user_feed");

						//remove old footer
						packs.remove(packs.size() - 1);

						JSONObject data = (JSONObject) args[0];
						if (data.optBoolean("success")) {
							JSONArray photoList = data.optJSONArray("photoList");

							packs = PicturePack.addPacksFromJSONArray(photoList, packs);
							if(packs.size() > 0) {
								skipDate = packs.get(packs.size() - 1).submitDate;
							}
//							skipDate = (packs.size() - 1) + "";
							Log.d(getClass().getSimpleName(), "new skipDate = " + skipDate);

							//add new footer if not the last photo
							if(photoList.length() == FEED_LIMIT_PER_REQUEST){
								PicturePack footer = new PicturePack();
								packs.add(footer);
							}

							adapter.notifyDataSetChanged();

						} else {
							Snackbar.make(getView(), getString(R.string.snackbar_feed_cannot_refresh),
									Snackbar.LENGTH_LONG)
									.setAction(getString(R.string.snackbar_action_retry), new View.OnClickListener() {
										@Override
										public void onClick(View v) {
										}
									}).show();
						}
					}
				});
			}
		};
		if (!GlobalSocket.mSocket.hasListeners("update_user_feed")) {
			GlobalSocket.mSocket.on("update_user_feed", onUpdateUserFeedRespond);
		}

		onDisconnect = new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Log.e("droidphoto", "ProfileFragment: disconnected");
							GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT);
							initReload();
						}
					});
				}
			}
		};

		if (!GlobalSocket.mSocket.hasListeners("remove_pic")) {
			GlobalSocket.mSocket.on("remove_pic", new Emitter.Listener() {
				@Override
				public void call(final Object... args) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							GlobalSocket.mSocket.off("remove_pic");
							JSONObject returnData = (JSONObject) args[0];
							if (returnData.optBoolean("success")) {
								Snackbar.make(mainLayout, getString(R.string.snackbar_delete_pic_success),
										Snackbar.LENGTH_SHORT).show();
								Log.d("droidphoto", "Selected pictures are removed");
							} else {
								Snackbar.make(mainLayout, "Error: " + returnData.optString("msg"),
										Snackbar.LENGTH_SHORT).show();
								Log.d("droidphoto", "Error: " + returnData.optString("msg"));
							}
						}
					});
				}
			});
		}
	}

	private void requestUserinfo() {
		reloadButton.setClickable(false);
		mainLayout.setVisibility(View.GONE);
		reloadLayout.setVisibility(View.GONE);
		loadingCircle.setVisibility(View.VISIBLE);

		JSONObject data = new JSONObject();
		try {
			data.put("_event", "get_user_info");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (!GlobalSocket.globalEmit("user.getuserinfo", data)) {
			//retry in 2 sec
			final JSONObject finalData = data;
			delayAction.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (!GlobalSocket.globalEmit("user.getuserinfo", finalData)) {
						//reload
						initReload();
					}
				}
			}, 2000);
		}
	}

	private void requestUserPhoto() {
		Log.d(getClass().getSimpleName(), "requestUserPhoto skipDate = " + skipDate);

		JSONObject filter = new JSONObject();

		try {
			if (skipDate != null)
				filter.put("skip", skipDate);
			filter.put("limit", FEED_LIMIT_PER_REQUEST);
			filter.put("_event", "get_user_feed");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (!GlobalSocket.globalEmit("photo.getuserphoto", filter)) {
			final JSONObject delayedData = filter;
			delayAction.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (!GlobalSocket.globalEmit("photo.getuserphoto", delayedData)) {
						initReload(); //if fail twice
					} else {
						GlobalSocket.mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
					}
				}
			}, 4000);
		} else {
			//can emit: detect loss on the way
			GlobalSocket.mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case EDIT_PROFILE:

				String profileDesc = data.getStringExtra(PROFILE_DESCRIPTION);
				String displayName = data.getStringExtra(DISPLAY_NAME);
				displayNameTv.setText(displayName);
				profileDescTV.setText(profileDesc);
				Snackbar.make(getView(), "Profile information has been updated", Snackbar.LENGTH_LONG).show();
				break;

			case UPDATE_LIKE_STATE:
				boolean isLike = data.getBooleanExtra("isLike", false);
				int likeCount = data.getIntExtra("likeCount", -999);
				String photoId = data.getStringExtra("photo_id");

				int position = data.getIntExtra("position", -1);

				PicturePack pack = packs.get(position);

				if (!pack.photoId.trim().equals(photoId.trim())) {
					for (int i = 0; i < packs.size(); i++) {
						if (packs.get(i).photoId.trim().equals(photoId.trim())) {
							pack = packs.get(i);
							break;
						}
					}
					if (!pack.photoId.trim().equals(photoId.trim()))
						requestUserPhoto();
				}
				pack.isLike = isLike;
				pack.likeCount = likeCount;

				adapter.notifyDataSetChanged();

				break;
			}
		} else {
			Log.d("droidphoto", "edit profile: result canceled");
		}
	}

	private void initReload() {
		loadingCircle.setVisibility(View.GONE);
		reloadLayout.setVisibility(View.VISIBLE);
		reloadText.setText("Error loading user profile :(");
		if (!reloadButton.hasOnClickListeners()) {
			reloadButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					GlobalSocket.reconnect();
					requestUserinfo();
				}
			});
		}
		reloadButton.setClickable(true);
	}

	public void toggleEditMode() {
		if (adapter != null) {
			if (!adapter.isInEditMode) {
				adapter.isInEditMode = true;
				adapter.notifyDataSetChanged();

				menu.clear();
				menuInflater.inflate(R.menu.menu_profile_cancel_delete, menu);

				deletePicFakeSnackbar.setVisibility(View.VISIBLE);
				deletePicFakeSnackbar.setY(profileFeedPicRecyclerview.getBottom());
				deletePicFakeSnackbar
						.animate()
						.y(profileFeedPicRecyclerview.getBottom()
								- getResources().getDimension(R.dimen.snackbar_height))
						.setDuration(getResources().getInteger(R.integer.fake_snackbar_animation_speed)).start();
			} else { //exit edit mode
				int count = 0;
				final JSONArray removePicId = new JSONArray();
				for (int i = adapter.getItemCount() - 1; i >= 0; i--) {
					if (adapter.isMarkedAsRemove[i]) {
						count++;
					}
				}

				if (count > 0) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
					String pictureWord = (count > 1) ? getString(R.string.delete_pic_dialog_picture_word_pural)
							: getString(R.string.delete_pic_dialog_picture_word_single);
					dialog.setTitle(getString(R.string.delete_pic_dialog_deleting) + count + pictureWord);
					dialog.setMessage(count + " " + pictureWord + getString(R.string.delete_pic_dialog_confirmation));
					dialog.setPositiveButton(R.string.delete_pic_dialog_positive_button,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									final JSONArray removePicId = new JSONArray();
									int countInside = 0;
									for (int i = adapter.getItemCount() - 1; i >= 0; i--) {
										if (adapter.isMarkedAsRemove[i]) {
											removePicId.put(packs.get(i).photoId);
											packs.remove(i);
											countInside++;
											adapter.isMarkedAsRemove[i] = false;
										}
									}

									JSONObject removePicData = new JSONObject();
									try {
										removePicData.put("photo_count", countInside);
										removePicData.put("remove_photo", removePicId);
										removePicData.put("_event", "remove_pic");
									} catch (JSONException e) {
										e.printStackTrace();
									}

									GlobalSocket.globalEmit("photo.remove", removePicData);
									deletePicFakeSnackbar
											.animate()
											.yBy(2 * getResources().getDimension(R.dimen.snackbar_height))
											.setDuration(
													getResources().getInteger(R.integer.fake_snackbar_animation_speed))
											.start();

									menu.clear();
									menuInflater.inflate(R.menu.menu_profile, menu);
									adapter.isInEditMode = false;
									adapter.notifyDataSetChanged();
								}
							});
					dialog.setNegativeButton(R.string.delete_pic_dialog_negative_button,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
					dialog.show();
				} else if (count == 0) {
					cancelEditMode();
				}
			}
		}
	}

	public void cancelEditMode() {
		menu.clear();
		menuInflater.inflate(R.menu.menu_profile, menu);

		adapter.isInEditMode = false;
		for (int i = adapter.getItemCount() - 1; i >= 0; i--) {
			adapter.isMarkedAsRemove[i] = false;
		}
		adapter.notifyDataSetChanged();

		deletePicFakeSnackbar.animate().yBy(getResources().getDimension(R.dimen.snackbar_height))
				.setDuration(getResources().getInteger(R.integer.fake_snackbar_animation_speed)).start();
	}

	@Override
	public void onStart() {
		ProfileFeedRecycleViewAdapter.isClickOnce = false;
		ProfileFragment.mProfileFragment = this;
		super.onStart();
	}

	@Override
	public void onStop() {
		ProfileFragment.mProfileFragment = null;
		super.onStop();
	}

	@Override
	public void onDestroy() {
		GlobalSocket.mSocket.off(Socket.EVENT_DISCONNECT);
		GlobalSocket.mSocket.off("get_user_info", onGetUserInfoRespond);

		if (GlobalSocket.mSocket.hasListeners("get_user_feed")) {
			GlobalSocket.mSocket.off("get_user_feed");
		}
		if (GlobalSocket.mSocket.hasListeners("remove_pic")) {
			GlobalSocket.mSocket.off("remove_pic");
		}
		if (GlobalSocket.mSocket.hasListeners("update_user_feed")) {
			GlobalSocket.mSocket.off("update_user_feed");
		}
		super.onDestroy();
	}

	private void findAllById() {
		profilePic = (ImageView) mainLayout.findViewById(R.id.profile_image_circle);
		displayNameTv = (FontTextView) mainLayout.findViewById(R.id.display_name);
		profileDescTV = (FontTextView) mainLayout.findViewById(R.id.profile_desc);

		usernameTV = (FontTextView) mainLayout.findViewById(R.id.username);
		profileFeedPicRecyclerview = (RecyclerView) mainLayout.findViewById(R.id.recyclerview_profile_feed_picture);

		reloadText = (FontTextView) reloadLayout.findViewById(R.id.reload_text);
		reloadButton = (Button) reloadLayout.findViewById(R.id.reload_button);

		deletePicFakeSnackbar = (RelativeLayout) mainLayout.findViewById(R.id.delete_pic_fake_snackbar_layout);
		deletePic = (Button) mainLayout.findViewById(R.id.delete_pic_fake_snackbar);
	}

	@Override
	public void onUpdateRecyclerViewRequest() {
		Log.d(getClass().getSimpleName(), "onUpdateRecyclerViewRequest");

        GlobalSocket.mSocket.on("update_user_feed", onUpdateUserFeedRespond);

		JSONObject filter = new JSONObject();

		Log.d(getClass().getSimpleName(), "onUpdateRecyclerViewRequest skipDate = " + skipDate);

		try {
			if (skipDate != null)
				filter.put("skip", skipDate);
			filter.put("limit", FEED_LIMIT_PER_REQUEST);
			filter.put("_event", "update_user_feed");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (!GlobalSocket.globalEmit("photo.getuserphoto", filter)) {
			final JSONObject delayedData = filter;
			delayAction.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (!GlobalSocket.globalEmit("photo.getuserphoto", delayedData)) {
						initReload(); //if fail twice
					} else {
						GlobalSocket.mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
					}
				}
			}, 4000);
		} else {
			//can emit: detect loss on the way
			GlobalSocket.mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
		}
	}
}
