package com.example.ac2;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
public class LivroAdapter extends ArrayAdapter<Livro> {
    private final Context context;
    private final List<Livro> livros;
    public LivroAdapter(@NonNull Context context, @NonNull List<Livro> livros) {
        super(context, 0, livros);
        this.context = context;
        this.livros = livros;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_livro, parent, false);
        }

        Livro livro = livros.get(position);
        TextView tvTitulo = convertView.findViewById(R.id.tvTituloItem);
        TextView tvAutor = convertView.findViewById(R.id.tvAutorItem);
        TextView tvGenero = convertView.findViewById(R.id.tvGeneroItem);
        TextView tvAno = convertView.findViewById(R.id.tvAnoItem);
        TextView tvStatus = convertView.findViewById(R.id.tvStatusItem);
        TextView tvFavorito = convertView.findViewById(R.id.tvFavoritoItem);
        tvTitulo.setText("Título: " + livro.getTitulo());
        tvAutor.setText("Autor: " + livro.getAutor());
        tvGenero.setText("Gênero: " + livro.getGenero());
        tvAno.setText("Ano: " + livro.getAno());
        tvStatus.setText("Status: " + livro.getStatus());
        tvFavorito.setText(livro.isFavorito() ? "⭐ Favorito" : "");
        // return - ok >:)
        return convertView;
    }
}
