package com.example.ac2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText edtBusca;
    private Spinner spinnerFiltroCategoria;
    private Switch switchApenasAFavoritos;
    private ListView listViewLivros;
    private Button btnCadastrar;
    private FirebaseFirestore db;
    private List<Livro> listaLivros;
    private List<Livro> listaLivrosFiltrada;
    private LivroAdapter adapter;
    private String filtroCategoria = "Todos os Gêneros";
    private boolean filtroApenasAFavoritos = false;
    private String filtroBusca = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        listaLivros = new ArrayList<>();
        listaLivrosFiltrada = new ArrayList<>();

        vincularViews();
        configurarListView();
        configurarSwitch();
        configurarSpinnerCategoria();
        configurarBuscaEditText();
        configurarBotaoCadastrar();
        carregarLivros();
    }

    private void vincularViews() {
        edtBusca = findViewById(R.id.edtBusca);
        spinnerFiltroCategoria = findViewById(R.id.spinnerFiltroCategoria);
        switchApenasAFavoritos = findViewById(R.id.switchApenasAFavoritos);
        listViewLivros = findViewById(R.id.listViewLivros);
        btnCadastrar = findViewById(R.id.btnCadastrar);
    }

    private void configurarListView() {
        listViewLivros.setOnItemClickListener((parent, view, position, id) -> {
            Livro livro = listaLivrosFiltrada.get(position);
            abrirCadastro(livro);
        });

        listViewLivros.setOnItemLongClickListener((parent, view, position, id) -> {
            Livro livro = listaLivrosFiltrada.get(position);
            confirmarExclusao(livro);
            return true;
        });
    }

    private void configurarSwitch() {
        switchApenasAFavoritos.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filtroApenasAFavoritos = isChecked;
            aplicarFiltros();
        });
    }

    private void configurarSpinnerCategoria() {
        String[] categorias = getResources().getStringArray(R.array.categorias_filtro);
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltroCategoria.setAdapter(adapterSpinner);

        spinnerFiltroCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtroCategoria = parent.getItemAtPosition(position).toString();
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void configurarBuscaEditText() {
        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtroBusca = s.toString().toLowerCase();
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void configurarBotaoCadastrar() {
        btnCadastrar.setOnClickListener(v -> abrirCadastro(null));
    }

    private void carregarLivros() {
        db.collection("livros").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(MainActivity.this, R.string.erro_carregar, Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                listaLivros.clear();
                listaLivros.addAll(value.toObjects(Livro.class));
                aplicarFiltros();
            }
        });
    }

    private void aplicarFiltros() {
        listaLivrosFiltrada.clear();
        for (Livro livro : listaLivros) {
            boolean passouFiltro = true;
            // N/A
            if (!filtroCategoria.equals("Todos os Gêneros") && !livro.getGenero().equals(filtroCategoria)) {
                passouFiltro = false;
            }
            //Ok
            if (filtroApenasAFavoritos && !livro.isFavorito()) {
                passouFiltro = false;
            }
            // Ok
            if (!filtroBusca.isEmpty() && !livro.getTitulo().toLowerCase().contains(filtroBusca)) {
                passouFiltro = false;
            }
            // Voltar testar aq
            if (passouFiltro) {
                listaLivrosFiltrada.add(livro);
            }
        }

        adapter = new LivroAdapter(this, listaLivrosFiltrada);
        listViewLivros.setAdapter(adapter);
    }

    private void abrirCadastro(Livro livro) {
        Intent intent = new Intent(this, CadastroLivroActivity.class);
        if (livro != null) {
            intent.putExtra("livro", livro);
        }
        startActivity(intent);
    }

    private void confirmarExclusao(Livro livro) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmação");
        builder.setMessage(R.string.confirmacao_exclusao);
        builder.setPositiveButton(R.string.confirmar, (dialog, which) -> excluirLivro(livro));
        builder.setNegativeButton(R.string.cancelar, null);
        builder.show();
    }

    private void excluirLivro(Livro livro) {
        db.collection("livros").document(livro.getId()).delete()
        .addOnSuccessListener(aVoid -> {
            Toast.makeText(MainActivity.this, R.string.livro_excluido, Toast.LENGTH_SHORT).show();
            carregarLivros();
        })
        .addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, R.string.erro_salvar, Toast.LENGTH_SHORT).show();
        });
    }
}

