package net.osmand.plus.mapcontextmenu.other;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.data.LatLon;
import net.osmand.plus.IconsCache;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.TargetPointsHelper;
import net.osmand.plus.TargetPointsHelper.TargetPoint;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.activities.search.SearchActivity;
import net.osmand.plus.activities.search.SearchPOIActivity;
import net.osmand.plus.pirattoplugin.PirattoPlugin;
import net.osmand.plus.pirattoplugin.core.PirattoManager;
import net.osmand.plus.poi.PoiFiltersHelper;
import net.osmand.plus.poi.PoiUIFilter;

public class DestinationReachedMenuFragment extends Fragment {
	public static final String TAG = "DestinationReachedMenuFragment";
	private DestinationReachedMenu menu;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (menu == null) {
			menu = new DestinationReachedMenu(getMapActivity());
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dest_reached_menu_fragment, container, false);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissMenu();
			}
		});

		IconsCache iconsCache = getMapActivity().getMyApplication().getIconsCache();

		ImageButton closeImageButton = (ImageButton) view.findViewById(R.id.closeImageButton);
		closeImageButton.setImageDrawable(iconsCache.getContentIcon(R.drawable.ic_action_remove_dark, menu.isLight()));
		closeImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissMenu();
			}
		});

		Button removeDestButton = (Button) view.findViewById(R.id.removeDestButton);
		removeDestButton.setCompoundDrawablesWithIntrinsicBounds(
				iconsCache.getContentIcon(R.drawable.ic_action_done, menu.isLight()), null, null, null);
		AndroidUtils.setTextPrimaryColor(view.getContext(), removeDestButton, !menu.isLight());
		removeDestButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getMapActivity().getMyApplication().getTargetPointsHelper().removeWayPoint(true, -1);
				Object contextMenuObj = getMapActivity().getContextMenu().getObject();
				if (getMapActivity().getContextMenu().isActive()
						&& contextMenuObj != null && contextMenuObj instanceof TargetPoint) {
					TargetPoint targetPoint = (TargetPoint) contextMenuObj;
					if (!targetPoint.start && !targetPoint.intermediate) {
						getMapActivity().getContextMenu().close();
					}
				}
				dismissMenu();
			}
		});

		Button recalcDestButton = (Button) view.findViewById(R.id.recalcDestButton);
		recalcDestButton.setCompoundDrawablesWithIntrinsicBounds(
				iconsCache.getContentIcon(R.drawable.ic_action_gdirections_dark, menu.isLight()), null, null, null);
		AndroidUtils.setTextPrimaryColor(view.getContext(), recalcDestButton, !menu.isLight());
		recalcDestButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TargetPointsHelper helper = getMapActivity().getMyApplication().getTargetPointsHelper();
				TargetPoint target = helper.getPointToNavigate();

				dismissMenu();

				if (target != null) {
					helper.navigateToPoint(new LatLon(target.getLatitude(), target.getLongitude()),
							true, -1, target.getOriginalPointDescription());
					getMapActivity().getMapActions().recalculateRoute(false);
					getMapActivity().getMapLayers().getMapControlsLayer().startNavigation();
				}
			}
		});


		Button findParkingButton = (Button) view.findViewById(R.id.findParkingButton);
		findParkingButton.setCompoundDrawablesWithIntrinsicBounds(
				iconsCache.getContentIcon(R.drawable.ic_action_parking_dark, menu.isLight()), null, null, null);
		AndroidUtils.setTextPrimaryColor(view.getContext(), findParkingButton, !menu.isLight());
		findParkingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PoiFiltersHelper helper = getMapActivity().getMyApplication().getPoiFilters();
				//PoiType place = getMapActivity().getMyApplication().getPoiTypes().getPoiTypeByKey("parking");
				PoiUIFilter parkingFilter = helper.getFilterById(PoiUIFilter.STD_PREFIX + "parking");
				if (parkingFilter != null) {
					final Intent newIntent = new Intent(getActivity(), SearchPOIActivity.class);
					newIntent.putExtra(SearchPOIActivity.AMENITY_FILTER, parkingFilter.getFilterId());
					newIntent.putExtra(SearchActivity.SEARCH_NEARBY, true);
					startActivityForResult(newIntent, 0);
				}
				dismissMenu();
			}
		});

		Button routeNextPoint = (Button) view.findViewById(R.id.btn_next_piratto_point);
		PirattoPlugin pirattoPlugin = OsmandPlugin.getEnabledPlugin(PirattoPlugin.class);
		if (pirattoPlugin != null) {
			try {
				final PirattoManager pirattoManager = PirattoManager.getInstance();
				if (pirattoManager.isRoutingPoint()) {
					routeNextPoint.setVisibility(View.VISIBLE);
					routeNextPoint.setCompoundDrawablesWithIntrinsicBounds(
							iconsCache.getContentIcon(R.drawable.ic_action_piratto_dark), null, null, null);
					routeNextPoint.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							pirattoManager.removeOldTargetPoint();
							pirattoManager.routeNextPoint(DestinationReachedMenuFragment.this.getActivity());
							dismissMenu();
						}
					});
				}
			} catch (Exception e) {
				Log.e("Piratto", "failed to showssss", e);
			}
		} else {
			routeNextPoint.setVisibility(View.GONE);
		}

		View mainView = view.findViewById(R.id.main_view);
		if (menu.isLandscapeLayout()) {
			AndroidUtils.setBackground(view.getContext(), mainView, !menu.isLight(),
					R.drawable.bg_left_menu_light, R.drawable.bg_left_menu_dark);
		} else {
			AndroidUtils.setBackground(view.getContext(), mainView, !menu.isLight(),
					R.drawable.bg_bottom_menu_light, R.drawable.bg_bottom_menu_dark);
		}
		TextView title = (TextView) view.findViewById(R.id.titleTextView);
		AndroidUtils.setTextPrimaryColor(view.getContext(), title, !menu.isLight());

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		getMapActivity().getContextMenu().setBaseFragmentVisibility(false);
	}

	@Override
	public void onStop() {
		super.onStop();
		getMapActivity().getContextMenu().setBaseFragmentVisibility(true);
	}


	public static void showInstance(DestinationReachedMenu menu) {
		int slideInAnim = menu.getSlideInAnimation();
		int slideOutAnim = menu.getSlideOutAnimation();

		DestinationReachedMenuFragment fragment = new DestinationReachedMenuFragment();
		fragment.menu = menu;
		menu.getMapActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(slideInAnim, slideOutAnim, slideInAnim, slideOutAnim)
				.add(R.id.fragmentContainer, fragment, TAG)
				.addToBackStack(TAG).commitAllowingStateLoss();
	}

	public void dismissMenu() {
		getMapActivity().getMapActions().stopNavigationWithoutConfirm();
		getMapActivity().getSupportFragmentManager().popBackStack();
	}

	public MapActivity getMapActivity() {
		Activity activity = getActivity();
		if (activity != null && activity instanceof MapActivity) {
			return (MapActivity) activity;
		} else {
			return null;
		}
	}
}
