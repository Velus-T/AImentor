package com.brightpath.science.presentation.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.brightpath.science.R;
import com.brightpath.science.util.ChemicalFormulaFormatter;
import com.brightpath.science.util.ScienceImageReader;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ScienceChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_USER = 1;
    private static final int TYPE_ASSISTANT = 2;
    private static final ExecutorService IMAGE_EXECUTOR = Executors.newFixedThreadPool(2);
    private static final LruCache<String, Bitmap> IMAGE_CACHE = new LruCache<String, Bitmap>(8 * 1024) {
                /*
 * Responsibility: Performs the `sizeOf` operation for  within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
@Override
        protected int sizeOf(@NonNull String key, @NonNull Bitmap value) {
            return Math.max(1, value.getByteCount() / 1024);
        }
    };

    public interface MessageActionListener {
                /*
 * Responsibility: Performs the `onBookmarkClicked` operation for MessageActionListener within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
void onBookmarkClicked(ScienceChatMessage message, int position);
                /*
 * Responsibility: Performs the `onCopyClicked` operation for MessageActionListener within the Science Module.
 * Purpose: Encapsulates this responsibility inside the Science Module component so the code is easier to read, test, maintain, and locate during the review.
 */
void onCopyClicked(ScienceChatMessage message);
    }

    private List<ScienceChatMessage> messages;
    private final MessageActionListener listener;
    private final ChemicalFormulaFormatter formulaFormatter = new ChemicalFormulaFormatter();

        /*
 * Responsibility: Initializes ScienceChatAdapter and receives or prepares the dependencies/state required by this RecyclerView/list adapter.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
public ScienceChatAdapter(List<ScienceChatMessage> initialMessages,
                              MessageActionListener listener) {
        this.messages = initialMessages == null ? Collections.emptyList() : initialMessages;
        this.listener = listener;
    }

        /*
 * Responsibility: Performs the adapter operation `submitMessages` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
public void submitMessages(List<ScienceChatMessage> newMessages) {
        this.messages = newMessages == null ? Collections.emptyList() : newMessages;
        notifyDataSetChanged();
    }

        /*
 * Responsibility: Retrieves the data/state required by `getItemViewType` from the appropriate RecyclerView/list adapter.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public int getItemViewType(int position) {
        return messages.get(position).getRole() == ScienceChatMessage.Role.USER
                ? TYPE_USER : TYPE_ASSISTANT;
    }

        /*
 * Responsibility: Performs the adapter operation `onCreateViewHolder` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
@NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            return new UserViewHolder(inflater.inflate(R.layout.item_science_chat_user, parent, false));
        }
        return new AssistantViewHolder(inflater.inflate(R.layout.item_science_chat_assistant, parent, false));
    }

        /*
 * Responsibility: Performs the adapter operation `onBindViewHolder` for Science list items.
 * Purpose: Transforms domain/list data into reusable UI rows without putting list-binding logic in the Activity/Fragment.
 */
@Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ScienceChatMessage message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message);
        } else if (holder instanceof AssistantViewHolder) {
            ((AssistantViewHolder) holder).bind(message);
        }
    }

        /*
 * Responsibility: Retrieves the data/state required by `getItemCount` from the appropriate RecyclerView/list adapter.
 * Purpose: Provides read access through a focused method so callers do not need to know the underlying storage/implementation details.
 */
@Override
    public int getItemCount() {
        return messages.size();
    }

    private static final class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageView;
        private final ImageView imageView;
        private final ScienceImageReader imageReader = new ScienceImageReader();

                /*
 * Responsibility: Initializes UserViewHolder and receives or prepares the dependencies/state required by this RecyclerView item holder.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
private UserViewHolder(@NonNull View itemView) {
            super(itemView);
            messageView = itemView.findViewById(R.id.tvChatUserMessage);
            imageView = itemView.findViewById(R.id.ivChatUserImage);
        }

                /*
 * Responsibility: Binds the relevant data, view, or listener in `bind`.
 * Purpose: Keeps UI binding code focused and separate from higher-level lifecycle/business logic.
 */
private void bind(ScienceChatMessage message) {
            String text = message.getText();
            messageView.setText(text.isEmpty() ? "Please analyse this image." : text);
            String uriText = message.getImageUri();
            if (uriText == null || uriText.isEmpty()) {
                clearImage();
                return;
            }

            imageView.setTag(uriText);
            imageView.setVisibility(View.VISIBLE);
            Bitmap cached = IMAGE_CACHE.get(uriText);
            if (cached != null && !cached.isRecycled()) {
                imageView.setImageBitmap(cached);
                return;
            }
            imageView.setImageDrawable(null);
            android.content.Context appContext = itemView.getContext().getApplicationContext();
            IMAGE_EXECUTOR.execute(() -> {
                try {
                    ScienceImageReader.ImageData data = imageReader.readCompressed(
                            appContext, Uri.parse(uriText));
                    Bitmap bitmap = BitmapFactory.decodeByteArray(
                            data.getBytes(), 0, data.getBytes().length);
                    if (bitmap == null) throw new IllegalArgumentException("Invalid image");
                    IMAGE_CACHE.put(uriText, bitmap);
                    imageView.post(() -> {
                        if (uriText.equals(imageView.getTag())) {
                            imageView.setVisibility(View.VISIBLE);
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception | OutOfMemoryError error) {
                    imageView.post(() -> {
                        if (uriText.equals(imageView.getTag())) clearImage();
                    });
                }
            });
        }

                /*
 * Responsibility: Removes or resets the data/state handled by `clearImage`.
 * Purpose: Prevents obsolete state from affecting the next Science operation and supports controlled CRUD behavior.
 */
private void clearImage() {
            imageView.setTag(null);
            imageView.setVisibility(View.GONE);
            imageView.setImageDrawable(null);
        }
    }

    private final class AssistantViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageView;
        private final TextView cacheView;
        private final ProgressBar progressBar;
        private final View actions;
        private final Button copyButton;
        private final Button bookmarkButton;

                /*
 * Responsibility: Initializes AssistantViewHolder and receives or prepares the dependencies/state required by this RecyclerView item holder.
 * Purpose: Keeps object creation explicit so the component is ready before its main operations are used.
 */
private AssistantViewHolder(@NonNull View itemView) {
            super(itemView);
            messageView = itemView.findViewById(R.id.tvChatAssistantMessage);
            cacheView = itemView.findViewById(R.id.tvChatCacheNotice);
            progressBar = itemView.findViewById(R.id.progressChatMessage);
            actions = itemView.findViewById(R.id.layoutChatActions);
            copyButton = itemView.findViewById(R.id.btnCopyChatAnswer);
            bookmarkButton = itemView.findViewById(R.id.btnBookmarkChatAnswer);
        }

                /*
 * Responsibility: Binds the relevant data, view, or listener in `bind`.
 * Purpose: Keeps UI binding code focused and separate from higher-level lifecycle/business logic.
 */
private void bind(ScienceChatMessage message) {
            progressBar.setVisibility(message.isLoading() ? View.VISIBLE : View.GONE);
            messageView.setText(message.isLoading()
                    ? "Science AI is thinking…"
                    : formulaFormatter.format(message.getText()));
            messageView.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    message.isError() ? R.color.science_error : R.color.science_text_primary));

            cacheView.setVisibility(message.isFromCache() ? View.VISIBLE : View.GONE);
            cacheView.setText(message.isFromCache() ? "Reused a matching saved answer" : "");

            boolean showActions = !message.isLoading() && !message.isError();
            actions.setVisibility(showActions ? View.VISIBLE : View.GONE);
            bookmarkButton.setVisibility(message.getHistoryId() > 0 ? View.VISIBLE : View.GONE);
            bookmarkButton.setText(message.isBookmarked() ? "Bookmarked" : "Bookmark");

            copyButton.setOnClickListener(view -> {
                if (listener != null) listener.onCopyClicked(message);
            });
            bookmarkButton.setOnClickListener(view -> {
                if (listener != null) {
                    int adapterPosition = getBindingAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        listener.onBookmarkClicked(message, adapterPosition);
                    }
                }
            });
        }
    }
}
