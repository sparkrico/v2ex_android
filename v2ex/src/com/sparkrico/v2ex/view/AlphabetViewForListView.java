package com.sparkrico.v2ex.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AlphabetViewForListView extends View {

	private List<Section> list;

	private Paint mPaint;

	AlphabetChangeListener alphabetListener = null;

	int item_height = 0;

	int current = 0;
	
	public interface AlphabetChangeListener {
		void OnAlphabetChange(Section section);
		void isOn(boolean isOn);
	}

	public AlphabetViewForListView(Context context) {
		this(context, null, 0);
	}

	public AlphabetViewForListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AlphabetViewForListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public AlphabetChangeListener getAlphabetListener() {
		return alphabetListener;
	}

	public void setAlphabetListener(AlphabetChangeListener alphabetListener) {
		this.alphabetListener = alphabetListener;
	}

	private void init() {
		setBackgroundColor(Color.BLACK);
		setAlpha(0.5f);
		setFocusable(true);
		setClickable(true);

		list = new ArrayList<Section>();

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.WHITE);
		mPaint.setTextSize(16 * getResources().getDisplayMetrics().density);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (list.size() > 0)
			item_height = getMeasuredHeight() / list.size();

		String v = "";
		int w = getMeasuredWidth();
		for (int i = 0; i < list.size(); i++) {
			v = String.valueOf(list.get(i).getTitle().charAt(0));
			if (!TextUtils.isEmpty(v)) {
				canvas.drawText(v,
						w / 2 - mPaint.measureText(v) / 2,
						item_height * (i+1), mPaint);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(alphabetListener != null)
				alphabetListener.isOn(true);
			break;
		case MotionEvent.ACTION_MOVE:
			gotoAplhabet(y);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if(alphabetListener != null)
				alphabetListener.isOn(false);
			break;

		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	private void gotoAplhabet(float y) {

		if (item_height > 0) {

			current = (int) y / item_height;

			if (current < 0)
				current = 0;
			if (current >= list.size())
				current = list.size() - 1;
			if(alphabetListener != null)
				alphabetListener.OnAlphabetChange(list.get(current));
		}
	}

	public List<Section> getList() {
		return list;
	}

	public void setList(List<Section> list) {
		this.list = list;
	}

}
