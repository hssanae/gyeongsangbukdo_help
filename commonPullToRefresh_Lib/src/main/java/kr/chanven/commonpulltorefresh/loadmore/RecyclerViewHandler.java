package kr.chanven.commonpulltorefresh.loadmore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import kr.chanven.commonpulltorefresh.loadmore.ILoadViewMoreFactory.FootViewAdder;
import kr.chanven.commonpulltorefresh.loadmore.ILoadViewMoreFactory.ILoadMoreView;
import kr.chanven.commonpulltorefresh.recyclerview.RecyclerAdapterWithHF;

public class RecyclerViewHandler implements ViewHandler {

	@Override
	public boolean handleSetAdapter(View contentView, ILoadMoreView loadMoreView, OnClickListener onClickLoadMoreListener) {
		final RecyclerView recyclerView = (RecyclerView) contentView;
		boolean hasInit = false;
		final RecyclerAdapterWithHF adapter = (RecyclerAdapterWithHF) recyclerView.getAdapter();
		if (loadMoreView != null) {
			final Context context = recyclerView.getContext().getApplicationContext();
//			final RecyclerAdapterWithHF recyclerAdapterWithHF = new RecyclerAdapterWithHF(adapter);
			loadMoreView.init(new FootViewAdder() {

				@Override
				public View addFootView(int layoutId) {
					View view = LayoutInflater.from(context).inflate(layoutId, recyclerView, false);
					return addFootView(view);
				}

				@Override
				public View addFootView(View view) {
					adapter.addFooter(view);
					return view;
				}
			}, onClickLoadMoreListener);
			hasInit = true;
		}
		return hasInit;
	}

	@Override
	public void setOnScrollBottomListener(View contentView, OnScrollBottomListener onScrollBottomListener) {
		final RecyclerView recyclerView = (RecyclerView) contentView;
		recyclerView.addOnScrollListener(new RecyclerViewOnScrollListener(onScrollBottomListener));
	}

	/**
	 * 滑动监听
	 */
	private static class RecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {
		private OnScrollBottomListener onScrollBottomListener;

		public RecyclerViewOnScrollListener(OnScrollBottomListener onScrollBottomListener) {
			super();
			this.onScrollBottomListener = onScrollBottomListener;
		}

		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			if (newState == RecyclerView.SCROLL_STATE_IDLE && isScollBottom(recyclerView)) {
				if (onScrollBottomListener != null) {
					onScrollBottomListener.onScorllBootom();
				}
			}
		}

		private boolean isScollBottom(RecyclerView recyclerView) {
			return !isCanScollVertically(recyclerView);
		}

		private boolean isCanScollVertically(RecyclerView recyclerView) {
			if (android.os.Build.VERSION.SDK_INT < 14) {
				return ViewCompat.canScrollVertically(recyclerView, 1) || recyclerView.getScrollY() < recyclerView.getHeight();
			} else {
				return ViewCompat.canScrollVertically(recyclerView, 1);
			}
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

		}

	}

}
