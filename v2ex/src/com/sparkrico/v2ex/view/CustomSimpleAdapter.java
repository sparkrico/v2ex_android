package com.sparkrico.v2ex.view;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.widget.SectionIndexer;
import android.widget.SimpleAdapter;

public class CustomSimpleAdapter extends SimpleAdapter implements SectionIndexer{

	private List<Section> sections;
	
	public CustomSimpleAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);
	}
	
	public CustomSimpleAdapter(Context context,
			List<? extends Map<String, ?>> data, List<Section> sections, int resource, String[] from,
					int[] to) {
		super(context, data, resource, from, to);
		this.sections = sections;
	}

	@Override
	public Object[] getSections() {
		String[] a = new String[sections.size()];
		for (int j = 0; j < sections.size(); j++) {
			a[j] = sections.get(j).getTitle();
		}
		return a;
	}

	@Override
	public int getPositionForSection(int section) {
		if(section >= sections.size())
			return -1;
		return sections.get(section).getPosition();
	}

	@Override
	public int getSectionForPosition(int position) {
		int section = 0;
		for (int j = 0; j < sections.size(); j++) {
			if (position <= sections.get(j).getPosition()){
				section = j;
				break;
			}
		}
		return section;
	}
}
