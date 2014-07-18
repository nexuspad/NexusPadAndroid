package com.nexuspad.ui.adapters;

import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ren on 7/18/14.
 */
public class MultiAdapters extends BaseAdapter {
	/**
	 * A class that holds extra info about an adapter.
	 * </p>
	 * Most of the time you don't have to interact with it, however, a common
	 * use case is shown below, <code>
	 * <pre>
	 * public Object getItem(int position) {
	 * AdapterInfo info = getAdapterInfo(position);
	 * int positionForAdapter = getPositionForAdapter(position, info);
	 * return info.getAdapter().getItem(positionForAdapter);
	 * }
	 * </pre>
	 * </code>
	 *
	 * @author Edmond
	 */
	public static final class AdapterInfo {
		private final BaseAdapter mAdapter;

		private int mPosition;
		private int mViewTypeOffset;

		private AdapterInfo(BaseAdapter adapter) {
			if (adapter == null) {
				throw new NullPointerException("adapter can't be null");
			}
			mAdapter = adapter;
		}

		/**
		 * @return the adapter that this {@code AdapterInfo} is about
		 */
		public BaseAdapter getAdapter() {
			return mAdapter;
		}

		private boolean isEmpty() {
			return getAdapter().isEmpty();
		}

		private int getCount() {
			return getAdapter().getCount();
		}

		private int getViewTypeCount() {
			return getAdapter().getViewTypeCount();
		}

		/**
		 * @return the position of this adapter in the {@code CompoundAdapter}
		 */
		public final int getPosition() {
			return mPosition;
		}

		/**
		 * @return the internal view type offset that is used to distinguish
		 * view types
		 * of different adapters
		 * @see ListAdapter#getItemViewType(int)
		 */
		public final int getViewTypeOffset() {
			return mViewTypeOffset;
		}

		private void setPosition(int position) {
			mPosition = position;
		}

		private void setViewTypeOffset(int viewTypeOffset) {
			mViewTypeOffset = viewTypeOffset;
		}
	}

	private final List<AdapterInfo> mAdapters = new ArrayList<MultiAdapters.AdapterInfo>(2);

	/**
	 * Constructs a {@code CompoundAdapter} that contains a single adapter. You
	 * may use {@link #addAdapter(BaseAdapter)} later on.
	 *
	 * @param adapter a non-null instance of a {@code BaseAdapter}
	 */
	public MultiAdapters(BaseAdapter adapter) {
		AdapterInfo info = new AdapterInfo(adapter);
		mAdapters.add(info);

		updateOffset();
	}

	/**
	 * Constructs a {@code CompoundAdapter} that contains a 2 adapters. The
	 * parameters order determines the order of the list from top to bottom.
	 *
	 * @param adapter  the first non-null instance of a {@code BaseAdapter}
	 * @param adapter2 the second non-null instance of a {@code BaseAdapter}
	 */
	public MultiAdapters(BaseAdapter adapter, BaseAdapter adapter2) {
		AdapterInfo info = new AdapterInfo(adapter);
		AdapterInfo info2 = new AdapterInfo(adapter2);
		mAdapters.add(info);
		mAdapters.add(info2);

		updateOffset();
	}

	/**
	 * Constructs a {@code CompoundAdapter} that contains more than 2 adapters.
	 * The parameters order determines the order of the list from top to bottom.
	 *
	 * @param adapter  the first non-null instance of a {@code BaseAdapter}
	 * @param adapter2 the second non-null instance of a {@code BaseAdapter}
	 * @param adapters the other instances of {@code BaseAdapter}
	 */
	public MultiAdapters(BaseAdapter adapter, BaseAdapter adapter2, BaseAdapter... adapters) {
		AdapterInfo info = new AdapterInfo(adapter);
		AdapterInfo info2 = new AdapterInfo(adapter2);

		mAdapters.add(info);
		mAdapters.add(info2);

		for (BaseAdapter baseAdapter : adapters) {
			mAdapters.add(new AdapterInfo(baseAdapter));
		}

		updateOffset();
	}

	/**
	 * @param position the position of the {@code CompoundAdapter}
	 * @param info     usually retrieved by {@link #getAdapterInfo(int)}
	 * @return the position of the adapter contained in the {@code AdapterInfo}
	 */
	private final static int getPositionForAdapter(int position, AdapterInfo info) {
		return position - info.getPosition();
	}

	/**
	 * a shorthand for
	 * {@code getPositionForAdapter(position, getAdapterInfo(position))}
	 */
	public final int getPositionForAdapter(int position) {
		return getPositionForAdapter(position, getAdapterInfo(position));
	}

	/**
	 * @param position the position of the {@code CompoundAdapter}
	 * @param info     usually retrieved by {@link #getAdapterInfo(int)}
	 * @return the view type of the adapter contained in the {@code AdapterInfo}
	 */
	public final static int getViewTypeForAdapter(int position, AdapterInfo info) {
		int pos = getPositionForAdapter(position, info);
		return info.getAdapter().getItemViewType(pos) + info.getViewTypeOffset();
	}

	/**
	 * a shorthand for
	 * {@code getViewTypeForAdapter(position, getAdapterInfo(position))}
	 */
	public final int getViewTypeForAdapter(int position) {
		return getViewTypeForAdapter(position, getAdapterInfo(position));
	}

	/**
	 * Append an adapter to the end of this {@code CompoundAdapter}.<br>
	 * This {@code CompoundAdapter} will also refresh itself.
	 *
	 * @param adapter a non-null instance of a {@code BaseAdapter}
	 * @see #notifyDataSetChanged()
	 */
	public void addAdapter(BaseAdapter adapter) {
		mAdapters.add(new AdapterInfo(adapter));
		notifyDataSetChanged();
	}

	/**
	 * Performs a linear search of the adapters in this {@code CompoundAdapter}
	 * to remove the provided adapter. If it does remove the adapter, this
	 * adapter will refresh itself.
	 *
	 * @param adapter an instance of {@code BaseAdapter}
	 * @return if it has removed the provided adapter
	 * @see #notifyDataSetChanged()
	 */
	public boolean removeAdapter(BaseAdapter adapter) {
		Iterator<AdapterInfo> it = mAdapters.iterator();
		while (it.hasNext()) {
			AdapterInfo info = it.next();
			// identity check
			if (info.getAdapter() == adapter) {
				it.remove();
				notifyDataSetChanged();
				return true;
			}
		}
		return false;
	}

	private void updateOffset() {
		int count = 0;
		int viewTypeCount = 0;

		for (AdapterInfo info : mAdapters) {
			info.setPosition(count);
			info.setViewTypeOffset(viewTypeCount);

			count += info.getCount();
			viewTypeCount += info.getViewTypeCount();
		}
	}

	/**
	 * @param position position of the {@code CompoundAdapter}
	 * @return the info about the adapter that is managing this position
	 */
	public final AdapterInfo getAdapterInfo(int position) {
		// from bottom to top
		for (int i = mAdapters.size() - 1; i >= 0; --i) {
			AdapterInfo info = mAdapters.get(i);
			if (info.getPosition() <= position) {
				return info;
			}
		}
		throw new AssertionError("the impossible bug does exist");
	}

	/**
	 * a shorthand for {@code getAdapterInfo(position).getAdapter()}
	 */
	public final BaseAdapter getAdapter(int position) {
		return getAdapterInfo(position).getAdapter();
	}

	@Override
	public boolean hasStableIds() {
		for (AdapterInfo info : mAdapters) {
			if (!info.getAdapter().hasStableIds()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		for (AdapterInfo info : mAdapters) {
			info.getAdapter().registerDataSetObserver(observer);
		}
		super.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		for (AdapterInfo info : mAdapters) {
			info.getAdapter().unregisterDataSetObserver(observer);
		}
		super.unregisterDataSetObserver(observer);
	}

	@Override
	public void notifyDataSetChanged() {
		for (AdapterInfo info : mAdapters) {
			info.getAdapter().notifyDataSetChanged();
		}
		updateOffset();
		super.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetInvalidated() {
		for (AdapterInfo info : mAdapters) {
			info.getAdapter().notifyDataSetInvalidated();
		}
		super.notifyDataSetInvalidated();
	}

	@Override
	public boolean areAllItemsEnabled() {
		for (AdapterInfo info : mAdapters) {
			if (!info.getAdapter().areAllItemsEnabled()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		AdapterInfo info = getAdapterInfo(position);
		return info.getAdapter().isEnabled(getPositionForAdapter(position, info));
	}

	@Override
	public int getItemViewType(int position) {
		return getViewTypeForAdapter(position);
	}

	@Override
	public int getViewTypeCount() {
		int count = 0;
		for (AdapterInfo info : mAdapters) {
			count += info.getAdapter().getViewTypeCount();
		}
		return count;
	}

	@Override
	public boolean isEmpty() {
		for (AdapterInfo info : mAdapters) {
			if (!info.getAdapter().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int getCount() {
		Log.i("MULTI ....", "here???????????????????????????????????????????? " + String.valueOf(getCountInternal()));
		return getCountInternal();
	}

	private int getCountInternal() {
		int count = 0;
		for (AdapterInfo info : mAdapters) {
			if (!info.isEmpty()) {
				count += info.getCount();
			}
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		AdapterInfo info = getAdapterInfo(position);
		int positionForAdapter = getPositionForAdapter(position, info);
		return info.getAdapter().getItem(positionForAdapter);
	}

	@Override
	public long getItemId(int position) {
		AdapterInfo info = getAdapterInfo(position);
		return info.getAdapter().getItemId(getPositionForAdapter(position, info));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AdapterInfo info = getAdapterInfo(position);
		int pos = getPositionForAdapter(position, info);
		return info.getAdapter().getView(pos, convertView, parent);
	}

}
