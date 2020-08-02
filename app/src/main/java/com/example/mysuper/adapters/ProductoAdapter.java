package com.example.mysuper.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mysuper.R;
import com.example.mysuper.models.ProductoModel;

import java.util.ArrayList;

public class ProductoAdapter extends BaseAdapter {
    private final Context context;
    private ProductoModel model;
    private ArrayList<ProductoModel> list;

    public ProductoAdapter(Context context, ArrayList<ProductoModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View itemView = view;
        if (view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = inflater.inflate(R.layout.item_productos, viewGroup, false );
        }
        TextView textViewCant = itemView.findViewById(R.id.textViewCant);
        model = list.get(i);
        textViewCant.setText(model.getCantidad());

        TextView textViewProduct = itemView.findViewById(R.id.textViewProduct);
        model = list.get(i);
        textViewProduct.setText(model.getProducto());

        TextView textViewPrec = itemView.findViewById(R.id.textViewPrecio);
        model = list.get(i);
        textViewPrec.setText(model.getPrecio());

        TextView textViewPrecT =  itemView.findViewById(R.id.textViewPrecT);
        model = list.get(i);
        textViewPrecT.setText(model.getTotal());

        return itemView;
    }
}
