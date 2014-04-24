package com.fimtrus.flicktalk.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.fimtrus.flicktalk.R;
import com.jhlibrary.util.Util;

public class SplashFragment extends Fragment {

	private FrameLayout mRootLayout;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootLayout = (FrameLayout) inflater.inflate(R.layout.fragment_splash,
				null);
		initialize();
		return mRootLayout;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private void initialize() {

		initializeFields();
		initializeListeners();
		initializeView();

	}

	private void initializeFields() {
	}

	private void initializeListeners() {

	}

	private void initializeView() {
	}
}
