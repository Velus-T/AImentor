package com.example.modulelaptrinh.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modulelaptrinh.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/** Hiển thị ảnh đang chờ gửi; chưa persist DB cho tới khi User bấm Send. */
public class PendingImageAdapter extends RecyclerView.Adapter<PendingImageAdapter.PendingImageViewHolder> {

    // Danh sách `imageUris` lưu các phần tử đang được màn hình/class quản lý trong bộ nhớ.
    private List<Uri> imageUris;
    // Thuộc tính `listener` lưu đối tượng `OnImageActionListener` mà class cần dùng xuyên suốt nhiều hàm.
    private OnImageActionListener listener;
    // Tính/đọc và lưu giá trị `MAX_IMAGES` (max images) để sử dụng ở các bước phía sau.
    private static final int MAX_IMAGES = 5;

    public interface OnImageActionListener {
        void onImageClick(int position, Uri uri);
        void onRemoveClick(int position);
    }

    // Constructor `PendingImageAdapter`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public PendingImageAdapter(OnImageActionListener listener) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `imageUris` của đối tượng hiện tại.
        this.imageUris = new ArrayList<>();
        // Gán giá trị tham số/đầu vào vào thuộc tính `listener` của đối tượng hiện tại.
        this.listener = listener;
    }

    // Hàm `canAddMore()`: Thực hiện chức năng `canAddMore` của class này.
    public boolean canAddMore() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return imageUris.size() < MAX_IMAGES;
    }

    // Hàm `getRemainingSlots()`: Trả về giá trị RemainingSlots hiện đang được đối tượng lưu giữ.
    public int getRemainingSlots() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return MAX_IMAGES - imageUris.size();
    }

    // Hàm `addImages()`: Thêm dữ liệu/phần tử mới vào trạng thái hoặc danh sách hiện tại.
    public void addImages(List<Uri> uris) {
        // Tính/đọc và lưu giá trị `remaining` (remaining) để sử dụng ở các bước phía sau.
        int remaining = getRemainingSlots();
        // Tính/đọc và lưu giá trị `countToAdd` (count to add) để sử dụng ở các bước phía sau.
        int countToAdd = Math.min(uris.size(), remaining);

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (countToAdd <= 0) return;

        // Khởi tạo giá trị `urisToAdd` (uris to add) phục vụ cho bước xử lý tiếp theo.
        List<Uri> urisToAdd = new ArrayList<>(uris.subList(0, countToAdd));
        // Tính/đọc và lưu giá trị `startPos` (start pos) để sử dụng ở các bước phía sau.
        int startPos = imageUris.size();
        // Gọi `addAll()` trên `imageUris` để thực hiện thao tác tương ứng.
        imageUris.addAll(urisToAdd);
        // Thông báo Adapter cập nhật item tương ứng mà không cần vẽ lại toàn bộ danh sách.
        notifyItemRangeInserted(startPos, urisToAdd.size());
    }

    // Hàm `removeImage()`: Thực hiện chức năng `removeImage` của class này.
    public void removeImage(int position) {
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (position >= 0 && position < imageUris.size()) {
            // Loại bỏ phần tử tương ứng khỏi `imageUris`.
            imageUris.remove(position);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (imageUris.isEmpty()) {
                // Thông báo Adapter rằng dữ liệu đã thay đổi để RecyclerView vẽ lại danh sách.
                notifyDataSetChanged();
            // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
            } else {
                // Thông báo Adapter cập nhật item tương ứng mà không cần vẽ lại toàn bộ danh sách.
                notifyItemRemoved(position);
                // Thông báo Adapter cập nhật item tương ứng mà không cần vẽ lại toàn bộ danh sách.
                notifyItemRangeChanged(position, imageUris.size() - position);
            }
        }
    }

    // Hàm `clearAll()`: Xóa dữ liệu/trạng thái tạm hiện tại để đưa đối tượng về trạng thái sạch.
    public void clearAll() {
        // Tính/đọc và lưu giá trị `size` (size) để sử dụng ở các bước phía sau.
        int size = imageUris.size();
        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (size > 0) {
            // Xóa dữ liệu cũ trong danh sách để chuẩn bị nạp lại dữ liệu mới.
            imageUris.clear();
            // Thông báo Adapter rằng dữ liệu đã thay đổi để RecyclerView vẽ lại danh sách.
            notifyDataSetChanged();
        }
    }

    // Hàm `getImageUris()`: Trả về giá trị ImageUris hiện đang được đối tượng lưu giữ.
    public List<Uri> getImageUris() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return new ArrayList<>(imageUris);
    }

    // Hàm `getImageCount()`: Trả về giá trị ImageCount hiện đang được đối tượng lưu giữ.
    public int getImageCount() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return imageUris.size();
    }

    // Hàm `isFull()`: Kiểm tra trạng thái/điều kiện và trả về true hoặc false.
    public boolean isFull() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return imageUris.size() >= MAX_IMAGES;
    }

    // Hàm `isEmpty()`: Kiểm tra trạng thái/điều kiện và trả về true hoặc false.
    public boolean isEmpty() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return imageUris.isEmpty();
    }

    @NonNull
    @Override
    // Hàm `onCreateViewHolder()`: Tạo ViewHolder mới từ layout item của RecyclerView.
    public PendingImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout XML thành cây View để Fragment/Adapter có thể thao tác.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_image, parent, false);
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return new PendingImageViewHolder(view);
    }

    @Override
    // Hàm `onBindViewHolder()`: Đưa dữ liệu của một item vào ViewHolder để RecyclerView hiển thị.
    public void onBindViewHolder(@NonNull PendingImageViewHolder holder, int position) {
        // Tính/đọc và lưu giá trị `uri` (uri) để sử dụng ở các bước phía sau.
        Uri uri = imageUris.get(position);

        Glide.with(holder.itemView.getContext())
                .load(uri)
                .centerCrop()
                .into(holder.ivImage);

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        holder.ivRemove.setOnClickListener(v -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (listener != null) {
                // Gọi `onRemoveClick()` trên `listener` để thực hiện thao tác tương ứng.
                listener.onRemoveClick(position);
            }
        });

        // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
        holder.itemView.setOnClickListener(v -> {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (listener != null) {
                // Gọi `onImageClick()` trên `listener` để thực hiện thao tác tương ứng.
                listener.onImageClick(position, uri);
            }
        });
    }

    @Override
    // Hàm `getItemCount()`: Trả về số lượng item hiện có để RecyclerView biết cần hiển thị bao nhiêu dòng.
    public int getItemCount() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return imageUris.size();
    }

    public static class PendingImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivRemove;

        // Constructor `PendingImageViewHolder`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
        public PendingImageViewHolder(@NonNull View itemView) {
            // Gọi hàm `super()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            super(itemView);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            ivImage = itemView.findViewById(R.id.iv_pending_image);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            ivRemove = itemView.findViewById(R.id.iv_remove_pending_image);
        }
    }
}
