package com.example.mysuper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mysuper.models.ProductoModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditarProducto extends AppCompatActivity {

    private TextView textViewDetallesCantidad, textViewDetallesProducto, textViewDetallesPrecio;
    private Button buttonEditar, buttonEliminar;
    private ProductoModel model;

    private String idUser = FirebaseAuth.getInstance().getUid();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reference = database.getReference().child("Usuarios").child(idUser).child("Producto");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Detalles Producto");
        setContentView(R.layout.activity_editar_producto);

        buttonEditar = findViewById(R.id.buttonEditar);
        buttonEliminar = findViewById(R.id.buttonEliminar);
        textViewDetallesCantidad = findViewById(R.id.textViewDetallesCantidad);
        textViewDetallesProducto = findViewById(R.id.textViewDetallesProducto);
        textViewDetallesPrecio = findViewById(R.id.textViewDetallesPrecio);
        model = new ProductoModel();

        String id = getIntent().getStringExtra("id");
        if (id != null && !id.equals("")){
            reference.child(id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    model = dataSnapshot.getValue(ProductoModel.class);
                    if(model!=null){
                        textViewDetallesCantidad.setText(model.getCantidad());
                        textViewDetallesProducto.setText(model.getProducto());
                        textViewDetallesPrecio.setText(model.getPrecio());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(EditarProducto.this,"Error Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }

        buttonEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(model.getId()!=null && !model.getId().equals("")){
                    Intent actualizar = new Intent(EditarProducto.this, ModificarProducto.class);
                    actualizar.putExtra("id", model.getId());
                    startActivity(actualizar);
                }

            }
        });

        buttonEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(model.getId()!=null && !model.getId().equals("")){
                    reference.child(model.getId()).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("database", "no se pudo guardar");
                                }
                            });
                }
            }
        });






    }
}