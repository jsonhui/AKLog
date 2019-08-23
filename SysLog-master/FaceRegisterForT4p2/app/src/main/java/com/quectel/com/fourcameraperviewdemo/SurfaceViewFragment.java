package com.quectel.com.fourcameraperviewdemo;

import com.yuwei.face.register.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the {@link SurfaceViewFragment.OnFragmentInteractionListener}
 * interface to handle interaction events. Use the
 * {@link SurfaceViewFragment#newInstance} factory method to create an instance
 * of this fragment.
 */
public class SurfaceViewFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	private static final String TAG = "PreviewFragment";
	private static final String ARG_PARAM1 = "param1";
	private SurfaceView preview;
	private SurfaceHolder surfaceHolder;
	private int camera_id = 0;

	public static SurfaceViewFragment newInstance(int cameraId) {
		SurfaceViewFragment fragment = new SurfaceViewFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_PARAM1, cameraId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			camera_id = getArguments().getInt(ARG_PARAM1);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_surface_view, container, false);
//		nativeTest();
		preview = (SurfaceView) rootView.findViewById(R.id.preview);
		surfaceHolder = preview.getHolder();

		surfaceHolder.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				nativeSetVideoSurface(holder.getSurface(), camera_id);
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {

			}
		});
		return rootView;
	}


	public native void nativeTest();

	public native boolean nativeSetVideoSurface(Surface surface, int channel);

	public native void nativeShowYUV(byte[] yuvArray, int width, int height, int channel);

}
