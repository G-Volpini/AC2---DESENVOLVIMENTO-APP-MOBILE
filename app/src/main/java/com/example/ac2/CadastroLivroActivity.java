package com.example.ac2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class CadastroLivroActivity extends AppCompatActivity {
    private EditText edtNome;
    private EditText edtAutor;
    private Spinner spinnerCategoria;
    private EditText edtAno;
    private Spinner spinnerStatus;
    private Switch switchFavorito;
    private Button btnCancelar;
    private Button btnSalvar;
    private FirebaseFirestore db;
    private Livro livroEditando;
    private boolean editando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro_livro);

        db = FirebaseFirestore.getInstance();

        vincularViews();
        configurarSpinners();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("livro")) {
            livroEditando = (Livro) intent.getSerializableExtra("livro");
            if (livroEditando != null) {
                editando = true;
                carregarDadosLivro();
            }
        }

        btnCancelar.setOnClickListener(v -> finish());
        btnSalvar.setOnClickListener(v -> salvarLivro());
    }

    private void vincularViews() {
        edtNome = findViewById(R.id.edtNome);
        edtAutor = findViewById(R.id.edtAutor);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        edtAno = findViewById(R.id.edtAno);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        switchFavorito = findViewById(R.id.switchFavorito);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnSalvar = findViewById(R.id.btnSalvar);
    }

    private void configurarSpinners() {
        String[] categorias = getResources().getStringArray(R.array.categorias);
        ArrayAdapter<String> adapterCategorias = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategorias);

        String[] status = getResources().getStringArray(R.array.status_leitura);
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, status);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapterStatus);
    }

    private void carregarDadosLivro() {
        edtNome.setText(livroEditando.getTitulo());
        edtAutor.setText(livroEditando.getAutor());
        edtAno.setText(String.valueOf(livroEditando.getAno()));
        switchFavorito.setChecked(livroEditando.isFavorito());
        String[] categorias = getResources().getStringArray(R.array.categorias);
        for (int i = 0; i < categorias.length; i++) {
            if (categorias[i].equals(livroEditando.getGenero())) {
                spinnerCategoria.setSelection(i);
                break;
            }
        }
        String[] statusArray = getResources().getStringArray(R.array.status_leitura);
        for (int i = 0; i < statusArray.length; i++) {
            if (statusArray[i].equals(livroEditando.getStatus())) {
                spinnerStatus.setSelection(i);
                break;
            }
        }
    }

    private void salvarLivro() {
        if (!validarCampos()) {
            return;
        }

        String titulo = edtNome.getText().toString().trim();
        String autor = edtAutor.getText().toString().trim();
        String genero = spinnerCategoria.getSelectedItem().toString();
        int ano = Integer.parseInt(edtAno.getText().toString().trim());
        String status = spinnerStatus.getSelectedItem().toString();
        boolean favorito = switchFavorito.isChecked();

        if (editando) {
            // Att livross aqui - Ok
            livroEditando.setTitulo(titulo);
            livroEditando.setAutor(autor);
            livroEditando.setGenero(genero);
            livroEditando.setAno(ano);
            livroEditando.setStatus(status);
            livroEditando.setFavorito(favorito);

            db.collection("livros").document(livroEditando.getId()).set(livroEditando).addOnSuccessListener(aVoid -> {
                Toast.makeText(CadastroLivroActivity.this, R.string.livro_atualizado, Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(CadastroLivroActivity.this, R.string.erro_salvar, Toast.LENGTH_SHORT).show();
            });
        } else {
            // Função de criar um livro - Ok
            Livro novoLivro = new Livro();
            novoLivro.setTitulo(titulo);
            novoLivro.setAutor(autor);
            novoLivro.setGenero(genero);
            novoLivro.setAno(ano);
            novoLivro.setStatus(status);
            novoLivro.setFavorito(favorito);

            db.collection("livros").add(novoLivro).addOnSuccessListener(documentReference -> {
                novoLivro.setId(documentReference.getId());
                db.collection("livros").document(documentReference.getId()).set(novoLivro);
                Toast.makeText(CadastroLivroActivity.this, R.string.livro_salvo, Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(CadastroLivroActivity.this, R.string.erro_salvar, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private boolean validarCampos() {
        String titulo = edtNome.getText().toString().trim();
        String autor = edtAutor.getText().toString().trim();
        String genero = spinnerCategoria.getSelectedItem().toString();
        String anoStr = edtAno.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

        if (titulo.isEmpty()) {
            Toast.makeText(this, R.string.erro_titulo_vazio, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (autor.isEmpty()) {
            Toast.makeText(this, R.string.erro_autor_vazio, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (genero.equals("Selecione um gênero")) {
            Toast.makeText(this, R.string.erro_genero_invalido, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (anoStr.isEmpty()) {
            Toast.makeText(this, R.string.erro_ano_vazio, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (status.equals("Selecione um status")) {
            Toast.makeText(this, R.string.erro_status_invalido, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}