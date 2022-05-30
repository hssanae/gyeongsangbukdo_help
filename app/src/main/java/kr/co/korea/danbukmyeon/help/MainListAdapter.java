package kr.co.korea.danbukmyeon.help;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class MainListAdapter extends BaseAdapter {
    @SuppressWarnings("unused")
    private Activity mainContext;
    private LayoutInflater inflater = null;

    private ArrayList<MainListItems> arSrc;

    private int layout;

    private MyRecyclerViewClickListener mListener;

    public interface MyRecyclerViewClickListener {
        void onItemDeleteClicked(int position);

        void onItemUpdateClicked(int position);
    }

    public void setOnClickListener(MyRecyclerViewClickListener listener) {
        mListener = listener;
    }

    public MainListAdapter(Activity context, int alayout, ArrayList<MainListItems> aarScr) {
        mainContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        arSrc = aarScr;
        layout = alayout;
    }

    @Override
    public int getCount() {
        return arSrc.size();
    }

    @Override
    public String getItem(int position) {
        return arSrc.get(position).Id;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(layout, parent, false);
        }

        TextView mainTv = (TextView) convertView.findViewById(R.id.tv_mainAdapter_main);
        TextView nameTv = (TextView) convertView.findViewById(R.id.tv_mainAdapter_name);
        TextView phoneTv = (TextView) convertView.findViewById(R.id.tv_mainAdapter_phone);
        ImageButton delBtn = (ImageButton) convertView.findViewById(R.id.btn_mainAdapter_del);
        ImageButton updateBtn = (ImageButton) convertView.findViewById(R.id.btn_mainAdapter_update);

        if (position == 0) {
            mainTv.setVisibility(View.VISIBLE);
        } else {
            mainTv.setVisibility(View.GONE);
        }

        nameTv.setText(arSrc.get(position).Name);
        phoneTv.setText(arSrc.get(position).Phone);

        if (mListener != null) {
            final int pos = position;
            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onItemUpdateClicked(pos);
                }
            });

            delBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onItemDeleteClicked(pos);
                }
            });
        }

        return convertView;
    }
}