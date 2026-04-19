package com.trackiq.ClientPortalPro.invoices;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trackiq.ClientPortalPro.databinding.ItemDocumentBinding;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private final List<Document> documentList;

    public DocumentAdapter(List<Document> documentList) {
        this.documentList = documentList;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDocumentBinding binding = ItemDocumentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DocumentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        Document document = documentList.get(position);
        holder.binding.tvDocTitle.setText(document.getTitle());
        holder.binding.tvDocDate.setText(document.getDate());

        holder.binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(document.getUrl()));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        ItemDocumentBinding binding;

        public DocumentViewHolder(ItemDocumentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
