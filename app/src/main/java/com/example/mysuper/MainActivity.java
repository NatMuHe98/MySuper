package com.example.mysuper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mysuper.adapters.ProductoAdapter;
import com.example.mysuper.models.ProductoModel;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.salir, menu);
        return true;
    }

    private static final int MY_REQUEST_CODE = 16;
    List<AuthUI.IdpConfig> providers;
    TextView textViewTotal, textViewRestante, textViewPresupuesto;
    Button btn_sign_out;
    ImageView btn_agregar_producto, btn_eliminar_lista;
    String miPresupuesto = "0";
    DatabaseReference mDatabase;

    private ArrayList<ProductoModel> list;
    private ProductoModel model;

    private ListView lv_producto;

    private String idUser ;
    private FirebaseDatabase database;
    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Mi Super");
        setContentView(R.layout.activity_main);

        //Provedores de inicio de sesión
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build()
        );
        
        IniciarSesion();

        lv_producto = findViewById(R.id.lv_producto);
        textViewTotal = findViewById(R.id.textViewTotal);
        textViewPresupuesto = findViewById(R.id.textViewPresupuesto);
        textViewRestante = findViewById(R.id.textViewRestante);
        list = new ArrayList<>();
        model = new ProductoModel();

        idUser = FirebaseAuth.getInstance().getUid();
        database = FirebaseDatabase.getInstance();
        reference =  database.getReference().child("Usuarios").child(idUser).child("Producto");


        btn_agregar_producto = (ImageView) findViewById(R.id.imageView_agregar);
        btn_agregar_producto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,NuevoProducto.class);
                startActivity(i);
            }
        });



        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    list = new ArrayList<>();
                    double total = 0;
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        model = child.getValue(ProductoModel.class);
                        list.add(model);
                        String subT = model.getTotal();
                        total = total + Double.parseDouble(subT);
                        textViewTotal.setText("$"+total);
                        double rest = 0;
                        String pres = (String) miPresupuesto;
                        rest = Double.parseDouble(pres) - total;
                        textViewRestante.setText("$"+rest);
                    }
                    lv_producto.setAdapter(new ProductoAdapter(MainActivity.this, list));
                }else{
                    textViewTotal.setText("$0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,"Error Firebase", Toast.LENGTH_SHORT).show();
            }
        });


        lv_producto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                model = (ProductoModel) adapterView.getItemAtPosition(i);
                if(!model.getId().equals("") && model.getId() != null){
                    Intent editar = new Intent(MainActivity.this,EditarProducto.class);
                    editar.putExtra("id", model.getId());
                    startActivity(editar);
                }
            }
        });

        btn_eliminar_lista = (ImageView) findViewById(R.id.imageView_eliminar);
        btn_eliminar_lista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("Eliminar Carrito de Compras")
                        .setMessage("Se eliminaran todos sus productos agregados.")
                        .setCancelable(false)
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Esto es para eliminar la lista completa
                                reference.removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(MainActivity.this, "Lista Eliminada", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("database", "no se pudo guardar");
                                            }
                                        });
                            }
                        })
                        .show();
            }
        });

    }



    public void CerrarSesion(MenuItem menuItem){
        AuthUI.getInstance()
                .signOut(MainActivity.this)
                .addOnCompleteListener(new OnCompleteListener<Void>(){
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //btn_sign_out.setEnabled(false);
                        IniciarSesion();
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, ""+e.getMessage(),  Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void AgregarPresupuesto(MenuItem menuItem){
        MaterialAlertDialogBuilder pres = new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("Agregar presupuesto para sus compras");
        final EditText presInput = new EditText(MainActivity.this);
        presInput.setInputType(InputType.TYPE_CLASS_PHONE);
        pres.setView(presInput);
        pres.setCancelable(false);

        pres.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        })
        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!presInput.getText().equals("")){
                    miPresupuesto = presInput.getText().toString();
                    textViewPresupuesto.setText("$"+miPresupuesto);
                }else{
                    Toast.makeText(MainActivity.this, "No se agregó Presupuesto", Toast.LENGTH_SHORT).show();
                }
            }
        })
        .show();
    };


    private void IniciarSesion() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.MyBackground)
                        .setLogo(R.drawable.logo)
                .build(),MY_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if(!currentUser.isEmailVerified()){
                currentUser.sendEmailVerification()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this,
                                        "Se le ha enviado un correo para validar su cuenta",
                                        Toast.LENGTH_SHORT).show();
                                Log.d("infoApp","Envío de correo exitoso");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("infoApp","error al enviar el correo");
                            }
                        });
                IniciarSesion();
            }else{

            }
            if(resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(MainActivity.this, " "+user.getDisplayName(), Toast.LENGTH_SHORT).show();
                btn_agregar_producto.setEnabled(true);
            }else{
                Toast.makeText(MainActivity.this, " "+response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}