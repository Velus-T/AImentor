package com.example.modulelaptrinh.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modulelaptrinh.ProgrammingChatMessage;
import com.example.modulelaptrinh.R;

import java.util.List;

import io.noties.markwon.Markwon;

/** Hiển thị message User/AI; AI render Markdown, ảnh Base64 được decode để xem lại. */
public class ProgrammingChatAdapter extends RecyclerView.Adapter<ProgrammingChatAdapter.MessageViewHolder> {

    // Danh sách `messageList` lưu các phần tử đang được màn hình/class quản lý trong bộ nhớ.
    private List<ProgrammingChatMessage> messageList;
    // Thuộc tính `markwon` lưu đối tượng `Markwon` mà class cần dùng xuyên suốt nhiều hàm.
    private Markwon markwon;
    // Thuộc tính `imageClickListener` lưu đối tượng `OnImageClickListener` mà class cần dùng xuyên suốt nhiều hàm.
    private OnImageClickListener imageClickListener;
    // Thuộc tính `context` lưu đối tượng `Context` mà class cần dùng xuyên suốt nhiều hàm.
    private Context context;

    public interface OnImageClickListener {
        void onImageClick(String imageBase64);
    }

    // Constructor `ProgrammingChatAdapter`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
    public ProgrammingChatAdapter(List<ProgrammingChatMessage> messageList, OnImageClickListener listener) {
        // Gán giá trị tham số/đầu vào vào thuộc tính `messageList` của đối tượng hiện tại.
        this.messageList = messageList;
        // Gán giá trị tham số/đầu vào vào thuộc tính `imageClickListener` của đối tượng hiện tại.
        this.imageClickListener = listener;
    }

    @NonNull
    @Override
    // Hàm `onCreateViewHolder()`: Tạo ViewHolder mới từ layout item của RecyclerView.
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Cập nhật Context Android hiện tại theo trạng thái/kết quả vừa tính được.
        context = parent.getContext();
        // Inflate layout XML thành cây View để Fragment/Adapter có thể thao tác.
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        // Cập nhật giá trị `markwon` (markwon) theo trạng thái/kết quả vừa tính được.
        markwon = Markwon.create(context);
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return new MessageViewHolder(view);
    }

    @Override
    // Hàm `onBindViewHolder()`: Đưa dữ liệu của một item vào ViewHolder để RecyclerView hiển thị.
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        // Tính/đọc và lưu đối tượng/nội dung tin nhắn để sử dụng ở các bước phía sau.
        ProgrammingChatMessage message = messageList.get(position);

        // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
        holder.llUserMessage.setVisibility(View.GONE);
        // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
        holder.llAiMessage.setVisibility(View.GONE);
        // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
        holder.tvUserMessage.setVisibility(View.GONE);
        // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
        holder.rvUserImages.setVisibility(View.GONE);
        // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
        holder.tvImageCount.setVisibility(View.GONE);

        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
        if (message.isUser()) {
            // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
            holder.llUserMessage.setVisibility(View.VISIBLE);

            // Tính/đọc và lưu nội dung văn bản để sử dụng ở các bước phía sau.
            String content = message.getContent();
            // Tính/đọc và lưu giá trị `images` (images) để sử dụng ở các bước phía sau.
            List<String> images = message.getImageBase64List();

            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (content != null && !content.isEmpty()) {
                // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                holder.tvUserMessage.setVisibility(View.VISIBLE);
                // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
                holder.tvUserMessage.setText(content);
            // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
            } else {
                // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                holder.tvUserMessage.setVisibility(View.GONE);
            }

            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (images != null && !images.isEmpty()) {
                // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                holder.rvUserImages.setVisibility(View.VISIBLE);

                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (images.size() > 1) {
                    // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                    holder.tvImageCount.setVisibility(View.VISIBLE);
                    // Cập nhật nội dung hiển thị của TextView/EditText trên giao diện.
                    holder.tvImageCount.setText(holder.itemView.getContext().getString(R.string.prog_chat_image_count_format, images.size()));
                }

                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (holder.rvUserImages.getLayoutManager() == null) {
                    // Khởi tạo giá trị `layoutManager` (layout manager) phục vụ cho bước xử lý tiếp theo.
                    LinearLayoutManager layoutManager = new LinearLayoutManager(
                            context, LinearLayoutManager.HORIZONTAL, false
                    );
                    // Thiết lập cách RecyclerView sắp xếp các item trên màn hình.
                    holder.rvUserImages.setLayoutManager(layoutManager);
                }

                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (holder.imageAdapter == null) {
                    holder.imageAdapter = new ImagePreviewAdapter(imageClickListener);
                    // Gắn Adapter vào RecyclerView để biến danh sách dữ liệu thành các item giao diện.
                    holder.rvUserImages.setAdapter(holder.imageAdapter);
                }
                holder.imageAdapter.submitList(images);
            // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
            } else {
                // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
                holder.rvUserImages.setVisibility(View.GONE);
                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (holder.imageAdapter != null) {
                    holder.imageAdapter.submitList(null);
                }
            }
        // Xử lý trường hợp còn lại khi điều kiện phía trên không thỏa.
        } else {
            // Thay đổi trạng thái hiển thị/ẩn của View trên giao diện.
            holder.llAiMessage.setVisibility(View.VISIBLE);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (message.getContent() != null) {
                // Gọi `setMarkdown()` để cập nhật thuộc tính/trạng thái của `markwon`.
                markwon.setMarkdown(holder.tvAiMessage, message.getContent());
            }
        }
    }

    @Override
    // Hàm `getItemCount()`: Trả về số lượng item hiện có để RecyclerView biết cần hiển thị bao nhiêu dòng.
    public int getItemCount() {
        // Trả kết quả của hàm về cho nơi đã gọi hàm này.
        return messageList != null ? messageList.size() : 0;
    }

    private static class ImagePreviewAdapter extends ListAdapter<String, ImagePreviewAdapter.ImageViewHolder> {
        private OnImageClickListener listener;
        private int imageSize;

        ImagePreviewAdapter(OnImageClickListener listener) {
            super(new DiffUtil.ItemCallback<String>() {
                @Override
                // Hàm `areItemsTheSame()`: Thực hiện chức năng `areItemsTheSame` của class này.
                public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                    // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                    return oldItem.hashCode() == newItem.hashCode();
                }

                @Override
                // Hàm `areContentsTheSame()`: Thực hiện chức năng `areContentsTheSame` của class này.
                public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                    // Trả kết quả của hàm về cho nơi đã gọi hàm này.
                    return oldItem.equals(newItem);
                }
            });
            // Gán giá trị tham số/đầu vào vào thuộc tính `listener` của đối tượng hiện tại.
            this.listener = listener;
            // Gọi hàm `setHasStableIds()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            setHasStableIds(true);
        }

        @Override
        // Hàm `getItemId()`: Trả về giá trị ItemId hiện đang được đối tượng lưu giữ.
        public long getItemId(int position) {
            // Tính/đọc và lưu giá trị `item` (item) để sử dụng ở các bước phía sau.
            String item = getItem(position);
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return item != null ? item.hashCode() : position;
        }

        @NonNull
        @Override
        // Hàm `onCreateViewHolder()`: Tạo ViewHolder mới từ layout item của RecyclerView.
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (imageSize == 0) {
                // Cập nhật giá trị `imageSize` (image size) theo trạng thái/kết quả vừa tính được.
                imageSize = (int) (80 * parent.getContext().getResources().getDisplayMetrics().density);
            }

            // Khởi tạo giá trị `imageView` (image view) phục vụ cho bước xử lý tiếp theo.
            ImageView imageView = new ImageView(parent.getContext());
            // Khởi tạo giá trị `params` (params) phục vụ cho bước xử lý tiếp theo.
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(imageSize, imageSize);
            // Gọi `setMarginEnd()` để cập nhật thuộc tính/trạng thái của `params`.
            params.setMarginEnd((int) (8 * parent.getContext().getResources().getDisplayMetrics().density));
            // Gọi `setLayoutParams()` để cập nhật thuộc tính/trạng thái của `imageView`.
            imageView.setLayoutParams(params);
            // Gọi `setScaleType()` để cập nhật thuộc tính/trạng thái của `imageView`.
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            // Gọi `setBackgroundResource()` để cập nhật thuộc tính/trạng thái của `imageView`.
            imageView.setBackgroundResource(R.drawable.bg_chat_input_dark);
            // Gọi `setPadding()` để cập nhật thuộc tính/trạng thái của `imageView`.
            imageView.setPadding(4, 4, 4, 4);
            // Gọi `setClipToOutline()` để cập nhật thuộc tính/trạng thái của `imageView`.
            imageView.setClipToOutline(true);
            // Trả kết quả của hàm về cho nơi đã gọi hàm này.
            return new ImageViewHolder(imageView);
        }

        @Override
        // Hàm `onBindViewHolder()`: Đưa dữ liệu của một item vào ViewHolder để RecyclerView hiển thị.
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            // Tính/đọc và lưu giá trị `imageBase64` (image base64) để sử dụng ở các bước phía sau.
            String imageBase64 = getItem(position);
            // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
            if (imageBase64 == null) return;

            // Bắt đầu khối có thể phát sinh ngoại lệ để xử lý an toàn.
            try {
                // Giải mã Base64 trở lại byte[] để khôi phục dữ liệu ảnh.
                byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                // Khởi tạo danh sách các lựa chọn trả lời phục vụ cho bước xử lý tiếp theo.
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                // Chuyển byte[] của ảnh thành Bitmap để hiển thị trên ImageView.
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                if (bitmap != null) {
                    holder.imageView.setImageBitmap(bitmap);
                }

                // Đăng ký sự kiện click; đoạn lambda/callback bên trong sẽ chạy khi người dùng bấm vào View.
                holder.imageView.setOnClickListener(v -> {
                    // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                    if (listener != null) {
                        // Tính/đọc và lưu giá trị `image` (image) để sử dụng ở các bước phía sau.
                        String image = getItem(position);
                        // Kiểm tra điều kiện trước khi thực hiện nhánh xử lý tương ứng.
                        if (image != null) {
                            // Gọi `onImageClick()` trên `listener` để thực hiện thao tác tương ứng.
                            listener.onImageClick(image);
                        }
                    }
                });
            // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
            } catch (OutOfMemoryError e) {
                // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
                Log.e("ImagePreviewAdapter", "OOM", e);
            // Bắt ngoại lệ phát sinh để ứng dụng không bị dừng đột ngột và có thể xử lý lỗi.
            } catch (Exception e) {
                // Gọi `e()` trên `Log` để thực hiện thao tác tương ứng.
                Log.e("ImagePreviewAdapter", "Error", e);
            }
        }

        static class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ImageViewHolder(@NonNull ImageView itemView) {
                // Gọi hàm `super()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
                super(itemView);
                // Gán giá trị tham số/đầu vào vào thuộc tính `imageView` của đối tượng hiện tại.
                this.imageView = itemView;
            }
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llUserMessage, llAiMessage;
        TextView tvUserMessage, tvAiMessage, tvUserTime, tvAiTime, tvImageCount;
        ImageView ivUserAvatar, ivAiAvatar;
        RecyclerView rvUserImages;
        ImagePreviewAdapter imageAdapter;

        // Constructor `MessageViewHolder`: khởi tạo đối tượng và nhận các dependency/dữ liệu ban đầu cần thiết.
        public MessageViewHolder(@NonNull View itemView) {
            // Gọi hàm `super()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
            super(itemView);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            llUserMessage = itemView.findViewById(R.id.ll_user_message);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            llAiMessage = itemView.findViewById(R.id.ll_ai_message);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            tvUserMessage = itemView.findViewById(R.id.tv_user_message);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            tvAiMessage = itemView.findViewById(R.id.tv_ai_message);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            tvUserTime = itemView.findViewById(R.id.tv_user_time);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            tvAiTime = itemView.findViewById(R.id.tv_ai_time);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            ivAiAvatar = itemView.findViewById(R.id.iv_ai_avatar);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            rvUserImages = itemView.findViewById(R.id.rv_user_images);
            // Ánh xạ View trong XML sang biến Java để có thể đọc/ghi dữ liệu hoặc gắn sự kiện.
            tvImageCount = itemView.findViewById(R.id.tv_image_count);
            // Cập nhật giá trị `imageAdapter` (image adapter) theo trạng thái/kết quả vừa tính được.
            imageAdapter = null;
        }
    }
}
