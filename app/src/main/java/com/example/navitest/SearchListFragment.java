package com.example.navitest;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SearchListFragment extends Fragment {

    View fragmentView;

    ListView searchListView;
    ArrayList<ResultItem> resultList = new ArrayList<ResultItem>();
    ResultAdapter adapter;

    ResultItem rItem;


    ItemTouchListener itemTouch;

    public SearchListFragment() {

    }

    public void setResultList(ArrayList<ResultItem> _resultList) {
        resultList = _resultList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        fragmentView = inflater.inflate(R.layout.search_fragment, container, false);
        //initList();
        adapter = new ResultAdapter(getActivity(), R.layout.search_result_item, resultList);
        searchListView = (ListView) fragmentView.findViewById(R.id.search_listview);
        searchListView.setAdapter(adapter);
        searchListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub

                itemTouch.itemClick(arg2);
            }
        });

        return fragmentView;
    }

    class ResultAdapter extends ArrayAdapter<ResultItem> {
        private int resourceId;

        public ResultAdapter(Context context, int resource,
                             List<ResultItem> objects) {
            super(context, resource, objects);
            // TODO Auto-generated constructor stub
            this.resourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ResultItem resultItem = getItem(position);
            View view;

            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            } else {
                view = convertView;
            }
            //ImageView fruitImage = (ImageView) view.findViewById(R.id.fruit_image);
            TextView titleTextView = (TextView) view.findViewById(R.id.search_result_title);
            //fruitImage.setImageResource(fruit.getImageId());
            //fruitName.setText(fruit.getName());
            titleTextView.setText(resultItem.getTitle());
            return view;
        }
    }

    public void setOnItemTouchListener(ItemTouchListener _itemTouch) {
        this.itemTouch = _itemTouch;
    }

    public interface ItemTouchListener {
        void itemClick(int arg);

    }


}
